#!/bin/bash

# https://stackoverflow.com/questions/41451159/how-to-execute-a-script-when-i-terminate-a-docker-container

JAVA_MAIN="de.micromata.borgbutler.server.BorgButlerApplication"
APP_NAME="BorgButler"

#Define cleanup procedure
cleanup() {
  echo "Container stopped, performing cleanup..."

  pid=$(pgrep -f $JAVA_MAIN)
  if [[ -z $pid ]]; then
    echo "${APP_NAME} process not found."
    exit 0
  else
    echo "Sending shutdown signal to $APP_NAME..."
    kill $pid
  fi

  echo "waiting 5 sec for termination of pid $pid..."
  sleep 5

  pid=$(pgrep -f $JAVA_MAIN)
  if [[ -z $pid ]]; then
    echo "${APP_NAME} stopped"
    exit 0
  else
    echo "${APP_NAME} not stopped, now sending sigkill"
    kill -9 $pid
  fi

  sleep 0.5

  pid=$(pgrep -f $JAVA_MAIN)
  if [[ -z $pid ]]; then
    echo "${APP_NAME} killed"
    exit 0
  else
    echo "${APP_NAME} could not be killed"
    exit 1
  fi
}

echo "Starting ${APP_NAME}..."

#Trap SIGTERM
trap cleanup INT SIGTERM

echo "Starting java ${JAVA_OPTS} -cp app/web/*:app/lib/* -DBorgButlerHome=/BorgButler/ -DapplicationHome=/app -DbindAddress=0.0.0.0 -DallowedClientIps=172.17. ${JAVA_MAIN} -q ${JAVA_ARGS}"

java $JAVA_OPTS -cp app/web/*:app/lib/* -DborgbutlerHome=/BorgButler/ -DapplicationHome=/app -Dserver.address=0.0.0.0 -DallowedClientIps=172.17. $JAVA_MAIN -q $JAVA_ARGS &

CHILD=$!
wait $CHILD

echo "$APP_NAME terminated."
#wait $!

#Cleanup
#cleanup Not needed, Java process already terminated.
