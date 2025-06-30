#!/bin/bash

email=$1
password="$2"
envoy_serial=$3
login_response=$(curl -L -X POST http://enlighten.enphaseenergy.com/login/login.json? -F "user[email]=$email" -F "user[password]=$password")

#echo $login_response

session_id=$(echo "$login_response" | jq -r ".session_id")

web_token=$(curl -L -X POST https://entrez.enphaseenergy.com/tokens -H "Content-Type: application/json" -d "{\"session_id\": \"$session_id\", \"serial_num\": \"$envoy_serial\", \"username\": \"$email\"}")

echo $web_token

