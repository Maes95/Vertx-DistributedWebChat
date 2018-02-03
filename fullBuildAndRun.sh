#!/bin/bash
if [ "$#" -eq  "0" ]
 then
   echo "Use: ./buildAndRun PORT"
else
  mvn install
  docker build -t maes95/vertx-chat .
  docker run -t -i -p $1:$1 -e PORT=$1 maes95/vertx-chat
fi
