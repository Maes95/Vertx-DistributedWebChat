mvn install
#java -jar target/WebChatVertxMaven-0.1.0-jar-with-dependencies.jar -cluster 5000
vertx run com.globex.app.ChatManager -cp target/* --cluster
