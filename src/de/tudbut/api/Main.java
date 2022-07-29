package de.tudbut.api;

import java.util.ArrayList;
import java.util.UUID;

import tudbut.parsing.JSON;
import tudbut.parsing.TCN;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // Demo for de.tudbut.api-v3 running on localhost. Change "localhost" and 8080 to correct host and port.
        TudbuTAPIClient client = new TudbuTAPIClient("ttc", UUID.fromString("d8d5a923-7b20-43d8-883b-1150148d6955"), "localhost", 8080);
        client.login("v0.1.0a").success().ensureGet();
        while(true) {
            client.use().success(TCN.class).consume(tcn -> System.out.println(JSON.write(tcn)));
            if(client.hasNewMessages()) {
                ArrayList<TCN> messages = client.getMessages().success(ArrayList.class).ensureGet();
                for(int i = 0; i < messages.size(); i++) {
                    System.out.println(messages.get(i).toString());
                }
            }
            client.sendMessage(UUID.fromString("d8d5a923-7b20-43d8-883b-1150148d6955"), "Hello there!");
            Thread.sleep(500);
        }
    }
}
