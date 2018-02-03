###
# vert.x docker example using a Java verticle
# To build:
#  docker build -t maes95/vertx-chat .
# To run:
#   docker run -t -i -p 8080:8080 -e PORT=8080 michel/vertx-web-chat
###

# Extend vert.x image
FROM vertx/vertx3

ENV VERTICLE_NAME com.globex.app.ChatManager
ENV VERTICLE_FILE target/WebChatVertxMaven-0.1.0.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Copy your verticle to the container
COPY $VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/* -cluster -cluster-host 192.168.112.10"]

# vertx run com.globex.app.ChatManager -cp target/* -cluster -cluster-host 127.0.0.1
