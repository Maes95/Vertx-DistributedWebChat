package com.globex.app;

// Vertx libraries
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author michel
 */

public class ChatManager extends AbstractVerticle {

    private final static String DEFAULT_IP = "0.0.0.0";

    private final static String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"Ya existe un usuario con ese nombre\"}";

    //Add because the most easy way to obtain the deploymentID is when I deploy the verticle
    private final Map<String, String> users = new HashMap<>();

    private int port;

    public ChatManager() {
      this.port = 8080;
    }

    @Override
    public void start() throws Exception {

        vertx.createHttpServer().websocketHandler( (ServerWebSocket ws) -> {
            if (ws.path().equals("/chat")) {
                ws.handler((Buffer data) -> {

                    JsonObject message = data.toJsonObject();

                    if (!message.containsKey("message")){
                        // Is a connect message
                        if (!users.containsKey(message.getString("name"))){
                            // Create new user
                            newUser(message, ws);
                        }else{
                            // User already exist
                            ws.writeFinalTextFrame(DUPLICATE_MSG);
                            ws.close();
                        }

                    }else{
                        // Broadcast the message to all Users
                    	System.out.println(message);
                        vertx.eventBus().publish(message.getString("chat"), message);
                    }
                });
            }else{
               ws.reject();
            }
        })
        .requestHandler((HttpServerRequest req) -> {
            if (req.uri().equals("/")) req.response().sendFile("webroot/index.html"); // Serve the html
        }).listen(this.port);

        // Listen for disconected users event
        vertx.eventBus().consumer("delete.user", data -> {
            this.deleteUser(data.body().toString());
        });

    }

    private void newUser(JsonObject message, ServerWebSocket ws){
        String name = message.getString("name");
        String chat = message.getString("chat");
        User user = new User(name, chat, ws);
        vertx.deployVerticle(user, res -> {
            if (res.succeeded()) {
                //Save the deploymentID to later remove the verticle
                users.put(name, res.result());
            } else {
                System.err.println("Error at deploy User");
            }
        });
    }

    //Remove the verticle and unregister the handler
    private void deleteUser (String user_name){
          vertx.undeploy(users.get(user_name), res -> {
              if (res.succeeded()) {
                  System.out.println("Undeployed ok");
              } else {
                  System.err.println("Undeploy failed!");
              }
          });
          users.remove(user_name);
    }

}
