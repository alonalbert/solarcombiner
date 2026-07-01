#!/usr/bin/env python3
"""
mqtt_example.py - Real-time Enphase Solar/Battery telemetry stream viewer.

This script logs into Enlighten Cloud, discovers sites/gateways, retrieves AWS IoT
custom-authorizer credentials, and connects to the real-time MQTT-over-WebSockets
stream to print live telemetry.
"""

import json
import os
import ssl
import sys
import getpass
import urllib.request
import urllib.parse
import urllib.error
from datetime import datetime

# Verify paho-mqtt dependency
try:
    import paho.mqtt.client as mqtt
except ImportError:
    print("Error: The 'paho-mqtt' library is required to run this script.")
    print("Please install it in your environment using:")
    print("  pip install paho-mqtt")
    sys.exit(1)

# Check for paho-mqtt v2.0+ API versioning compatibility
try:
    from paho.mqtt.enums import CallbackAPIVersion
    PAHO_V2 = True
except ImportError:
    PAHO_V2 = False

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
USERINFO_FILE = os.path.join(ROOT_DIR, "local.userinfo")
CACHE_FILE = os.path.join(ROOT_DIR, "mqtt.info.cache")

_original_build_opener = urllib.request.build_opener

def _build_opener_with_logging(*handlers):
    opener = _original_build_opener(*handlers)
    _original_open = opener.open
    
    def _logged_open(req, data=None, timeout=None):
        if isinstance(req, str):
            req = urllib.request.Request(req)
        
        print("\n" + "="*80)
        print(f"HTTP REQUEST: {req.get_method()} {req.get_full_url()}")
        print("Headers:")
        all_hdrs = {}
        all_hdrs.update(req.unredirected_hdrs)
        all_hdrs.update(req.headers)
        for k, v in all_hdrs.items():
            print(f"  {k}: {v}")
        if req.data:
            print("Body:")
            try:
                print(req.data.decode("utf-8"))
            except Exception:
                print(req.data)
        print("="*80)

        try:
            resp = _original_open(req, data=data, timeout=timeout)
            print("\n" + "-"*80)
            status_code = resp.status if hasattr(resp, "status") else resp.code
            reason_phrase = resp.reason if hasattr(resp, "reason") else resp.msg
            print(f"HTTP RESPONSE: {status_code} {reason_phrase}")
            print("Headers:")
            print(resp.info())
            
            body = resp.read()
            print("Body:")
            try:
                print(body.decode("utf-8"))
            except Exception:
                print(body)
            print("-"*80 + "\n")
            
            resp.read = lambda: body
            return resp
        except urllib.error.HTTPError as err:
            print("\n" + "!"*80)
            print(f"HTTP ERROR RESPONSE: {err.code} {err.reason}")
            print("Headers:")
            print(err.headers)
            try:
                err_body = err.read()
                print("Body:")
                print(err_body.decode("utf-8"))
                err.read = lambda: err_body
            except Exception:
                pass
            print("!"*80 + "\n")
            raise
        except Exception as err:
            print(f"NETWORK ERROR: {err}")
            raise
            
    opener.open = _logged_open
    return opener

urllib.request.build_opener = _build_opener_with_logging


def load_userinfo() -> tuple[str, str]:
    """Load Enlighten credentials from local.userinfo if present."""
    email, password = "", ""
    if os.path.exists(USERINFO_FILE):
        try:
            with open(USERINFO_FILE, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith("#"):
                        continue
                    if "=" in line:
                        k, v = line.split("=", 1)
                        k = k.strip().upper()
                        v = v.strip()
                        if (v.startswith('"') and v.endswith('"')) or (
                            v.startswith("'") and v.endswith("'")
                        ):
                            v = v[1:-1].strip()
                        if k == "EMAIL":
                            email = v
                        elif k == "PASSWORD":
                            password = v
        except Exception as err:
            print(f"Warning: Failed to read local.userinfo: {err}")
    return email, password


def get_enlighten_session() -> tuple[urllib.request.OpenerDirector, str]:
    """Perform Enlighten cloud login and return cookie-supporting opener + session_id."""
    print("--- Enlighten Cloud Authentication ---")
    email, password = load_userinfo()

    if not email:
        email = input("Enlighten Email: ").strip()
    else:
        print(f"Using email from local.userinfo: {email}")

    if not password:
        password = getpass.getpass("Enlighten Password: ").strip()

    if not email or not password:
        raise ValueError("Email and password are required.")

    print("Logging into Enlighten...")
    login_url = "https://enlighten.enphaseenergy.com/login/login.json"
    login_data = {"user[email]": email, "user[password]": password}

    cookie_handler = urllib.request.HTTPCookieProcessor()
    opener = urllib.request.build_opener(cookie_handler)

    req_data = urllib.parse.urlencode(login_data).encode("utf-8")
    req = urllib.request.Request(
        login_url,
        data=req_data,
        headers={"Accept": "application/json, text/plain, */*"},
        method="POST"
    )

    try:
        with opener.open(req, timeout=15) as resp:
            resp_data = json.loads(resp.read().decode("utf-8"))
    except Exception as err:
        print(f"Login failed: {err}")
        raise

    if not resp_data.get("session_id"):
        if resp_data.get("requires_mfa"):
            print("MFA is required on this account. Please authenticate in a browser first.")
            raise RuntimeError("MFA required.")
        raise RuntimeError(f"Login rejected: {resp_data.get('message', 'Unknown error')}")

    session_id = resp_data["session_id"]
    print("Success! Logged in successfully.")
    return opener, session_id


def discover_site_and_gateway(opener: urllib.request.OpenerDirector) -> tuple[str, str]:
    """Find site ID and Envoy gateway serial number."""
    print("\n--- Site & Gateway Discovery ---")
    site_search_url = "https://enlighten.enphaseenergy.com/app-api/search_sites.json?searchText=&favourite=false"
    site_req = urllib.request.Request(site_search_url, headers={"Accept": "application/json"})

    try:
        with opener.open(site_req, timeout=15) as resp:
            sites_payload = json.loads(resp.read().decode("utf-8"))
    except Exception as err:
        print(f"Site discovery failed: {err}")
        raise

    sites = sites_payload.get("sites", [])
    if not sites:
        raise RuntimeError("No sites found on this account.")

    print(f"Found {len(sites)} site(s):")
    for idx, s in enumerate(sites):
        print(f"  [{idx + 1}] Site ID: {s.get('id')}, Name: {s.get('title')}")

    if len(sites) == 1:
        site_choice = 0
    else:
        choice = input(f"Select site (1-{len(sites)}) [1]: ").strip()
        site_choice = int(choice) - 1 if choice.isdigit() else 0

    selected_site = sites[site_choice]
    site_id = str(selected_site["id"])

    # Gateway discovery
    print(f"Fetching devices for site '{selected_site.get('title')}'...")
    devices_url = f"https://enlighten.enphaseenergy.com/app-api/{site_id}/devices.json"
    devices_req = urllib.request.Request(devices_url, headers={"Accept": "application/json"})

    try:
        with opener.open(devices_req, timeout=15) as resp:
            devices_payload = json.loads(resp.read().decode("utf-8"))
    except Exception as err:
        print(f"Failed to fetch devices: {err}")
        raise

    envoys = []
    devices_list = []
    if isinstance(devices_payload, dict):
        if "result" in devices_payload and isinstance(devices_payload["result"], list):
            for block in devices_payload["result"]:
                if isinstance(block, dict) and block.get("type") == "envoy":
                    devices_list = block.get("devices", [])
                    break
        else:
            devices_list = devices_payload.get("envoys", [])
            if not devices_list:
                for k, v in devices_payload.items():
                    if isinstance(v, list) and k in ("envoys", "devices", "items"):
                        devices_list = v
                        break
    elif isinstance(devices_payload, list):
        devices_list = devices_payload

    for dev in devices_list:
        if isinstance(dev, dict):
            s_num = dev.get("serial_num") or dev.get("serial_number")
            if s_num:
                normalized_dev = dev.copy()
                normalized_dev["serial_num"] = s_num
                if "name" in dev and "model" not in dev:
                    normalized_dev["model"] = dev["name"]
                envoys.append(normalized_dev)

    if not envoys:
        envoy_serial = input("No Envoy gateways discovered. Enter Envoy Serial Number manually: ").strip()
        if not envoy_serial:
            raise ValueError("Serial number is required.")
    else:
        print(f"Found {len(envoys)} Envoy gateway(s):")
        for idx, env in enumerate(envoys):
            print(f"  [{idx + 1}] Serial: {env.get('serial_num')}, Model: {env.get('model', 'Unknown')}")

        if len(envoys) == 1:
            env_choice = 0
        else:
            choice = input(f"Select gateway (1-{len(envoys)}) [1]: ").strip()
            env_choice = int(choice) - 1 if choice.isdigit() else 0
        envoy_serial = envoys[env_choice]["serial_num"]

    return site_id, envoy_serial


def get_mqtt_authorizer_info(
    opener: urllib.request.OpenerDirector, session_id: str, serial_num: str, live_debug: bool
) -> dict:
    """Retrieve AWS SigV4 signed authorizer info and topics."""
    print("\nRequesting AWS IoT MQTT configuration...")
    base_url = "https://enlighten.enphaseenergy.com/pv/aws_sigv4/livestream.json"
    params = {"serial_num": serial_num}
    if live_debug:
        params["live_debug"] = "true"

    url = f"{base_url}?{urllib.parse.urlencode(params)}"
    headers = {
        "Accept": "*/*",
        "e-auth-token": session_id,
        "X-Requested-With": "XMLHttpRequest",
    }
    req = urllib.request.Request(url, headers=headers)

    try:
        with opener.open(req, timeout=15) as resp:
            auth_data = json.loads(resp.read().decode("utf-8"))
            return auth_data
    except Exception as err:
        raise


def run_mqtt_client(auth_data: dict, site_id: str, live_debug: bool):
    """Establish WebSocket MQTT connection and print messages."""
    endpoint = auth_data["aws_iot_endpoint"]
    authorizer = auth_data["aws_authorizer"]
    token_key = auth_data["aws_token_key"]
    token_val = auth_data["aws_token_value"]
    aws_digest = auth_data["aws_digest"]

    # Retrieve topic based on stream type
    if live_debug:
        topic = auth_data["live_debug_topic"]
        stream_duration = auth_data.get("live_debug_duration", 900)
        stream_name = "Live Vitals (JSON)"
    else:
        topic = auth_data["live_stream_topic"]
        stream_duration = auth_data.get("live_stream_duration", 900)
        stream_name = "Live Status (Protobuf)"

    print(f"\n--- Connecting to Enphase MQTT WS ---")
    print(f"Stream Type: {stream_name}")
    print(f"AWS Endpoint: {endpoint}")
    print(f"Target Topic: {topic}")
    print(f"Session Duration: {stream_duration} seconds (15 minutes max)")

    # Construct the query string username needed by AWS custom authorizer
    username = (
        f"?x-amz-customauthorizer-name={authorizer}"
        f"&{token_key}={token_val}"
        f"&site-id={site_id}"
        f"&x-amz-customauthorizer-signature={urllib.parse.quote(aws_digest)}"
        f"&env=production"
    )

    # Instantiate MQTT client over WebSockets
    if PAHO_V2:
        client = mqtt.Client(CallbackAPIVersion.VERSION1, transport="websockets")
    else:
        client = mqtt.Client(transport="websockets")

    # Set WebSocket parameters: path /mqtt and origin header
    client.ws_set_options(path="/mqtt", headers={"Origin": "https://enlighten.enphaseenergy.com"})
    
    # Secure connection setup
    client.tls_set(cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLSv1_2)
    client.username_pw_set(username=username, password=None)

    # Callbacks
    def on_connect(client_instance, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker!")
            print(f"Subscribing to topic: {topic}...")
            client_instance.subscribe(topic)
            print("Successfully subscribed! Waiting for messages...")
        else:
            print(f"Connection failed with code {rc}")

    def on_message(client_instance, userdata, msg):
        now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"\n[{now_str}] Received message on {msg.topic} ({len(msg.payload)} bytes):")
        
        if live_debug:
            # Parse and pretty print the JSON Live Vitals
            try:
                data = json.loads(msg.payload.decode("utf-8"))
                # Anonymize serial numbers for safety before printing
                anonymized_json = anonymize_vitals(data)
                print(json.dumps(anonymized_json, indent=2))
            except Exception as err:
                print(f"Error parsing JSON payload: {err}")
                print(msg.payload[:500])
        else:
            # Decode the binary Protobuf-like payload using DataMsg.proto schema
            try:
                raw_decoded = decode_protobuf(msg.payload)
                # Translate raw tags to names using DataMsg schema
                decoded = map_to_schema(raw_decoded, "DataMsg")
                
                print("\nDecoded Protobuf Payload (DataMsg Schema):")
                print(json.dumps(anonymize_vitals(decoded), indent=2))
                
                print("\nInterpreted Live Status Summary:")
                print("-" * 75)
                
                meters = decoded.get("meters", {})
                pv = meters.get("pv", {})
                storage = meters.get("storage", {})
                grid = meters.get("grid", {})
                load = meters.get("load", {})
                generator = meters.get("generator", {})
                
                # Helper to print power values (stored in milliwatts, scaled by 1,000,000 to kW)
                def fmt_power(channel):
                    val = channel.get("agg_p_mw")
                    if val is None:
                        return "N/A"
                    kw = val / 1_000_000.0
                    return f"{kw:+.3f} kW (raw={val:+} mW)"

                print(f"Solar Production:  {fmt_power(pv)}")
                print(f"Grid Net Power:    {fmt_power(grid)}  [Positive = Import, Negative = Export]")
                print(f"Battery Power:     {fmt_power(storage)}  [Positive = Discharging, Negative = Charging]")
                print(f"Household Load:    {fmt_power(load)}")
                if generator.get("agg_p_mw") is not None:
                    print(f"Generator Power:   {fmt_power(generator)}")
                
                soc = meters.get("soc")
                if soc is not None:
                    print(f"Battery SoC:       {soc}%")
                backup_soc = decoded.get("backup_soc")
                if backup_soc is not None:
                    print(f"Backup Reserve:    {backup_soc}%")
                batt_mode = decoded.get("batt_mode")
                if batt_mode is not None:
                    print(f"Battery Mode:      {batt_mode}")
                grid_relay = meters.get("grid_relay")
                if grid_relay is not None:
                    print(f"Grid Relay State:  {grid_relay}")
                gen_relay = meters.get("gen_relay")
                if gen_relay is not None:
                    print(f"Gen Relay State:   {gen_relay}")
                    
                print("-" * 75)
            except Exception as err:
                print(f"Error decoding raw Protobuf payload: {err}")
                print(f"Raw hex: {msg.payload.hex()[:200]}...")

    client.on_connect = on_connect
    client.on_message = on_message

    try:
        client.connect(endpoint, 443, keepalive=60)
        client.loop_forever()
    except KeyboardInterrupt:
        print("\nDisconnecting client...")
        client.disconnect()
        print("Done.")


def decode_varint(data: bytes, pos: int) -> tuple[int, int]:
    """Decode a varint from the byte array starting at pos."""
    result = 0
    shift = 0
    while True:
        if pos >= len(data):
            raise IndexError("Truncated varint")
        b = data[pos]
        pos += 1
        result |= (b & 0x7F) << shift
        if not (b & 0x80):
            break
        shift += 7
    return result, pos


def decode_zigzag(n: int) -> int:
    """Decode a ZigZag-encoded signed integer."""
    return (n >> 1) ^ -(n & 1)


def to_signed_int(n: int, bits: int = 64) -> int:
    """Convert an unsigned integer to a signed integer (two's complement)."""
    if n >= (1 << (bits - 1)):
        n -= (1 << bits)
    return n


def decode_protobuf(data: bytes, pos: int = 0, end: int = None) -> dict:
    """Recursively decode raw Protobuf bytes to a dictionary of field numbers to values."""
    if end is None:
        end = len(data)
        
    result = {}
    while pos < end:
        try:
            key, pos = decode_varint(data, pos)
        except IndexError:
            break
            
        field_num = key >> 3
        wire_type = key & 0x07
        
        if wire_type == 0:  # Varint
            val, pos = decode_varint(data, pos)
            result[field_num] = val
        elif wire_type == 1:  # 64-bit
            if pos + 8 > end:
                break
            val = data[pos:pos+8]
            pos += 8
            result[field_num] = val
        elif wire_type == 2:  # Length-delimited
            length, pos = decode_varint(data, pos)
            if pos + length > end:
                break
            val_bytes = data[pos:pos+length]
            pos += length
            
            # Attempt recursive sub-message decoding
            try:
                sub_msg = decode_protobuf(val_bytes)
                # Ensure we got a valid dictionary back before keeping it
                if sub_msg and isinstance(sub_msg, dict):
                    result[field_num] = sub_msg
                else:
                    result[field_num] = val_bytes
            except Exception:
                result[field_num] = val_bytes
        elif wire_type == 5:  # 32-bit
            if pos + 4 > end:
                break
            val = data[pos:pos+4]
            pos += 4
            result[field_num] = val
        else:
            # Unsupported wire type, stop decoding this branch
            break
            
    return result


def get_path(d: dict, path: list) -> any:
    """Helper to traverse nested dict by list of keys."""
    current = d
    for key in path:
        if isinstance(current, dict):
            current = current.get(key)
        else:
            return None
    return current


def parse_power_value(val: any) -> tuple[float | None, str]:
    """Interpret raw varint value as both two's complement and zigzag, returning scaled kW options."""
    if val is None or not isinstance(val, (int, float)):
        return None, "N/A"
    
    # Zigzag signed
    zz_val = decode_zigzag(int(val))
    # Two's complement signed 64-bit and 32-bit
    tc_val_64 = to_signed_int(int(val), bits=64)
    tc_val_32 = to_signed_int(int(val), bits=32)
    
    # Scale by 1_000_000 to get kW
    zz_kw = zz_val / 1_000_000.0
    tc_kw_64 = tc_val_64 / 1_000_000.0
    tc_kw_32 = tc_val_32 / 1_000_000.0
    
    return tc_kw_64, f"raw={val} | tc_64={tc_kw_64:+.3f} kW | tc_32={tc_kw_32:+.3f} kW | zigzag={zz_kw:+.3f} kW"


def map_to_schema(decoded: any, schema_type: str) -> any:
    """Translate raw dict tags to names and types according to proto schema."""
    if not isinstance(decoded, dict):
        return decoded
        
    result = {}
    
    # Define fields configuration
    if schema_type == "DataMsg":
        fields = {
            1: ("protocol_ver", "int"),
            2: ("timestamp", "int"),
            3: ("meters", "MeterSummaryData"),
            4: ("batt_mode", "BattMode"),
            5: ("backup_soc", "int"),
            6: ("dry_contact_relay_status", "repeated_DryContactStatus"),
            7: ("dry_contact_relay_name", "repeated_DryContactName"),
            8: ("load_status", "repeated_LoadStatus"),
            9: ("power_match_status", "PowerMatchStatus"),
        }
    elif schema_type == "MeterSummaryData":
        fields = {
            1: ("pv", "MeterChannel"),
            2: ("storage", "MeterChannel"),
            3: ("grid", "MeterChannel"),
            4: ("load", "MeterChannel"),
            5: ("grid_relay", "MeterSumGridState"),
            6: ("soc", "int"),
            7: ("generator", "MeterChannel"),
            8: ("gen_relay", "MeterSumGridState"),
            9: ("phase_count", "int"),
            10: ("is_split_phase", "bool"),
            14: ("meter_channel", "repeated_AggMeterChannel"),
        }
    elif schema_type in ("MeterChannel", "AggMeterChannel"):
        fields = {
            1: ("agg_p_mw", "int"),
            2: ("agg_s_mva", "int"),
            3: ("agg_p_ph_mw", "repeated_int"),
            4: ("agg_s_ph_mva", "repeated_int"),
            5: ("device_sn", "string") if schema_type == "MeterChannel" else ("type", "MeterType"),
            6: ("channels", "repeated_MeterChannel"),
        }
    elif schema_type == "DryContactStatus":
        fields = {
            1: ("id", "DryContactId"),
            2: ("state", "DryContactRelayState"),
        }
    elif schema_type == "DryContactName":
        fields = {
            1: ("id", "DryContactId"),
            2: ("load_name", "string"),
        }
    elif schema_type == "LoadStatus":
        fields = {
            1: ("id", "string"),
            2: ("relay_status", "string"),
            3: ("power", "float"),
        }
    elif schema_type == "PowerMatchStatus":
        fields = {
            1: ("status", "bool"),
            2: ("totalPCUCount", "int"),
            3: ("runningPCUCount", "int"),
            4: ("isSupported", "bool"),
        }
    else:
        return decoded

    for tag, val in decoded.items():
        if tag in fields:
            name, ftype = fields[tag]
            
            # Map based on type
            if ftype == "int":
                result[name] = to_signed_int(val) if isinstance(val, int) else val
            elif ftype == "bool":
                result[name] = bool(val)
            elif ftype == "string":
                if isinstance(val, bytes):
                    try:
                        result[name] = val.decode("utf-8")
                    except Exception:
                        result[name] = val.hex()
                else:
                    result[name] = str(val)
            elif ftype == "float":
                if isinstance(val, bytes) and len(val) == 4:
                    import struct
                    result[name] = struct.unpack("<f", val)[0]
                else:
                    result[name] = val
            elif ftype == "repeated_int":
                if isinstance(val, bytes):
                    pos = 0
                    ints = []
                    while pos < len(val):
                        try:
                            v, pos = decode_varint(val, pos)
                            ints.append(to_signed_int(v))
                        except Exception:
                            break
                    result[name] = ints
                elif isinstance(val, list):
                    result[name] = [to_signed_int(x) if isinstance(x, int) else x for x in val]
                else:
                    result[name] = [to_signed_int(val)] if isinstance(val, int) else [val]
            elif ftype.startswith("repeated_"):
                subtype = ftype.split("_", 1)[1]
                if isinstance(val, list):
                    result[name] = [map_to_schema(x, subtype) for x in val]
                elif isinstance(val, dict):
                    result[name] = [map_to_schema(val, subtype)]
                else:
                    result[name] = val
            elif ftype == "BattMode":
                result[name] = map_enum(val, {
                    0: "BATT_MODE_FULL_BACKUP",
                    1: "BATT_MODE_SELF_CONS",
                    2: "BATT_MODE_SAVINGS",
                    -1: "BATT_MODE_UNKNOWN",
                    4294967295: "BATT_MODE_UNKNOWN"
                })
            elif ftype == "MeterSumGridState":
                result[name] = map_enum(val, {
                    0: "OPER_RELAY_UNKNOWN",
                    1: "OPER_RELAY_OPEN",
                    2: "OPER_RELAY_CLOSED",
                    3: "OPER_RELAY_OFFGRID_AC_GRID_PRESENT",
                    4: "OPER_RELAY_OFFGRID_READY_FOR_RESYNC_CMD",
                    5: "OPER_RELAY_WAITING_TO_INITIALIZE_ON_GRID",
                    6: "OPER_RELAY_GEN_OPEN",
                    7: "OPER_RELAY_GEN_CLOSED",
                    8: "OPER_RELAY_GEN_STARTUP",
                    9: "OPER_RELAY_GEN_SYNC_READY",
                    10: "OPER_RELAY_GEN_AC_STABLE",
                    11: "OPER_RELAY_GEN_AC_UNSTABLE",
                })
            elif ftype == "DryContactId":
                result[name] = map_enum(val, {0: "NC1", 1: "NC2", 2: "NO1", 3: "NO2"})
            elif ftype == "DryContactRelayState":
                result[name] = map_enum(val, {0: "DC_RELAY_STATE_INVALID", 1: "DC_RELAY_OFF", 2: "DC_RELAY_ON"})
            elif ftype == "MeterType":
                result[name] = map_enum(val, {0: "METER_TYPE_NONE", 1: "METER_TYPE_PV", 2: "METER_TYPE_STORAGE"})
            else:
                if isinstance(val, dict):
                    result[name] = map_to_schema(val, ftype)
                elif isinstance(val, list):
                    result[name] = [map_to_schema(x, ftype) for x in val]
                else:
                    result[name] = val
        else:
            result[tag] = val
            
    return result


def map_enum(val: any, enum_dict: dict) -> str:
    """Resolve raw enum integer values to proto-defined string names."""
    if isinstance(val, int):
        return enum_dict.get(val, f"UNKNOWN_ENUM_VALUE_{val}")
    return str(val)


def anonymize_vitals(data: any) -> any:
    """Recursively anonymize sensitive serial numbers or IDs from printout."""
    if isinstance(data, dict):
        new_dict = {}
        for k, v in data.items():
            # Check key name (could be int from protobuf or str from json)
            k_str = str(k).lower()
            if any(term in k_str for term in ("serial", "mac", "ip", "username", "email", "site")):
                new_dict[k] = "<REDACTED>"
            else:
                new_dict[k] = anonymize_vitals(v)
        return new_dict
    elif isinstance(data, list):
        return [anonymize_vitals(item) for item in data]
    elif isinstance(data, bytes):
        return data.hex()
    return data


def load_cache() -> dict:
    """Load cached details from mqtt.info.cache if present."""
    if os.path.exists(CACHE_FILE):
        try:
            with open(CACHE_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            pass
    return {}


def save_cache(data: dict):
    """Save details to mqtt.info.cache."""
    try:
        with open(CACHE_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
    except Exception as err:
        print(f"Warning: Failed to save cache: {err}")


def main():
    try:
        cache = load_cache()
        session_id = cache.get("session_id")
        site_id = cache.get("site_id")
        serial_num = cache.get("serial_num")
        live_debug = cache.get("live_debug", False)
        
        use_cache = False
        if session_id and site_id and serial_num:
            print(f"\nFound cached session & gateway details:")
            print(f"  Site ID: {site_id}")
            print(f"  Gateway Serial: {serial_num}")
            print(f"  Preferred Stream: {'Live Vitals (JSON)' if live_debug else 'Live Status (Protobuf)'}")
            ans = input("Do you want to reuse these cached details? (y/n) [y]: ").strip().lower()
            if ans != 'n':
                use_cache = True
                
        # Create standard cookie-supporting opener
        cookie_handler = urllib.request.HTTPCookieProcessor()
        opener = urllib.request.build_opener(cookie_handler)
        
        if use_cache:
            print("\nReusing cached session and gateway details...")
        else:
            opener, session_id = get_enlighten_session()
            site_id, serial_num = discover_site_and_gateway(opener)
            
            print("\nChoose stream type:")
            print(" [1] Live Status Stream (Binary/Protobuf, updates every 1 sec)")
            print(" [2] Live Vitals Stream (JSON, updates every 5 sec)")
            choice = input("Enter choice [1]: ").strip()
            live_debug = (choice == "2")

        # Request MQTT authorization configuration
        try:
            auth_data = get_mqtt_authorizer_info(opener, session_id, serial_num, live_debug)
        except Exception as err:
            if use_cache:
                print(f"\nCached session/credentials failed ({err}).")
                print("Falling back to full login and discovery...")
                opener, session_id = get_enlighten_session()
                site_id, serial_num = discover_site_and_gateway(opener)
                
                print("\nChoose stream type:")
                print(" [1] Live Status Stream (Binary/Protobuf, updates every 1 sec)")
                print(" [2] Live Vitals Stream (JSON, updates every 5 sec)")
                choice = input("Enter choice [1]: ").strip()
                live_debug = (choice == "2")
                
                auth_data = get_mqtt_authorizer_info(opener, session_id, serial_num, live_debug)
            else:
                raise

        # Save successful settings to cache
        save_cache({
            "session_id": session_id,
            "site_id": site_id,
            "serial_num": serial_num,
            "live_debug": live_debug
        })

        run_mqtt_client(auth_data, site_id, live_debug)

    except Exception as err:
        print(f"\nAn error occurred: {err}")
        sys.exit(1)


if __name__ == "__main__":
    main()
