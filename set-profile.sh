#!/bin/bash

email=$1
password="$2"
battery_reserve="$3"

login_response=$(curl -sL -X POST http://enlighten.enphaseenergy.com/login/login.json? -F "user[email]=$email" -F "user[password]=$password")

session_id=$(echo "$login_response" | jq -r ".session_id")
system_id=$(echo "$login_response" | jq -r ".system_id")


response=$(curl -s "https://enlighten.enphaseenergy.com/service/batteryConfig/api/v1/profile/${system_id}" \
  -X 'PUT' \
  -H 'content-type: application/json' \
  -b "_enlighten_4_session=${session_id};" \
  --data-raw "{\"profile\":\"self-consumption\",\"batteryBackupPercentage\":${battery_reserve}}")

echo "${response}" | jq -r .message


