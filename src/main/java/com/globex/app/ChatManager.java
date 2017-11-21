package com.globex.app;

// Vertx libraries
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author michel
 */

public class ChatManager extends AbstractVerticle {

    private final static int DEFAULT_PORT = 9000;

    private final static String DEFAULT_IP = "0.0.0.0";

    private final static String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"Ya existe un usuario con ese nombre\"}";

    //Add because the most easy way to obtain the deploymentID is when I deploy the verticle
    private final Map<String, String> users = new HashMap<>();

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
    	int _port = DEFAULT_PORT;
    	if(args[0].length() > 0) {
    		try {
            	_port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
               System.out.println("Failed trying to parse a non-numeric argument, " + args[0]);
            }
    	}else {
    		System.out.println("No port declared, using default port: "+ DEFAULT_PORT);
    	}

        Vertx.vertx().deployVerticle(new ChatManager(_port));
    }

    private int port;

    public ChatManager(int _port) {
    	this.port = _port;
    }

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);
        final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();

        router.get("/").handler(ctx -> {
          ctx.put("ip", DEFAULT_IP);
          ctx.put("port", this.port);
          engine.render(ctx, "webroot/index.html", res -> {
            if (res.succeeded()) {
              ctx.response().end(res.result());
            } else {
              ctx.fail(res.cause());
            }
          });
        });

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
                        vertx.eventBus().publish(message.getString("chat"), message);
                    }
                });
            }else{
               ws.reject();
            }
        })
        .requestHandler(router::accept).listen(this.port);

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
