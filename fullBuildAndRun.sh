#!/bin/bash
if [ "$#" -eq  "0" ]
 then
   echo "Use: ./buildAndRun PORT"
else
  mvn install
  docker build -t michel/vertx-web-chat .
  docker run -t -i -p $1:$1 -e PORT=$1 michel/vertx-web-chat
fi
