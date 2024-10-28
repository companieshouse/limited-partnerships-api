#!/bin/bash
#
# Start script for limited-partnerships-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "limited-partnerships-api.jar"