#!/usr/bin/env sh
#

cd "$(dirname "$0")"

./gradlew :server:assemble || exit

cd server

docker build -t alonalbert/enphase-monitor-server . || exit

docker-compose down || exit
docker-compose up -d --remove-orphans
