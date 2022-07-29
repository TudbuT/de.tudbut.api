package de.tudbut.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.net.http.HTTPUtils;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.parsing.TCNArray;
import tudbut.parsing.JSON.JSONFormatException;
import tudbut.tools.encryption.RawKey;

public class TudbuTAPIClient {

    String service;
    UUID uuid;
    String host;
    int port;
    TCN user;
    TCN serviceData;
    RawKey authKey;
    String authToken;

    public TudbuTAPIClient(String service, UUID uuid, String host, int port) {
        this.service = service;
        this.uuid = uuid;
        this.host = host;
        this.port = port;
        // ignore the results of these. the constructor might be called with no internet connection
        getService();
        getUser();
    }

    public RequestResult<?> use() {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/service/" + service + "/use", "application/x-www-urlencoded", "uuid=" + uuid);
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("updated")) {
                user = jsonResponse.getSub("user");
                serviceData = jsonResponse.getSub("service");
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> getUser() {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/user/" + uuid);
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                this.user = jsonResponse.getSub("user");
                return RequestResult.SUCCESS(jsonResponse.getSub("user"));
            }
            else {
                return RequestResult.FAIL();
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> login(String version) {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/service/" + service + "/login", "application/x-www-urlencoded", "uuid=" + uuid + "&version=" + HTTPUtils.encodeUTF8(version));
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                this.authKey = new RawKey(jsonResponse.getString("key"));
                this.authToken = jsonResponse.getString("token");
                this.user = jsonResponse.getSub("user");
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> sendMessage(UUID uuidOther, String message) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/message", "application/x-www-urlencoded", 
                "uuid=" + uuid + "&uuidOther=" + uuidOther + "&token=" + authToken + "&message=" + HTTPUtils.encodeUTF8(message)
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> sendMessage(String nameOther, String message) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/message", "application/x-www-urlencoded", 
                "uuid=" + uuid + "&nameOther=" + nameOther + "&token=" + authToken + "&message=" + HTTPUtils.encodeUTF8(message)
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> getMessages() {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/message/read", "application/x-www-urlencoded", 
                "uuid=" + uuid + "&token=" + authToken
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("accessGranted")) {
                return RequestResult.SUCCESS(decryptMessages(jsonResponse.getArray("messages")));
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> getService() {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/service/" + service);
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                this.serviceData = jsonResponse.getSub("service");
                return RequestResult.SUCCESS(jsonResponse.getSub("service"));
            }
            else {
                return RequestResult.FAIL();
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> setPassword(String oldPass, String newPass) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/user/" + uuid + "/password", "application/x-www-urlencoded",
                "password=" + HTTPUtils.encodeUTF8(oldPass) + "&new=" + HTTPUtils.encodeUTF8(newPass)
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("set")) {
                this.user = jsonResponse.getSub("user");
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> getUsersOnline() {
        HTTPRequest request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/service/" + service + "/online");
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                this.serviceData = jsonResponse.getSub("service");
                return RequestResult.SUCCESS(jsonResponse.getInteger("usersOnline"));
            }
            else {
                return RequestResult.FAIL();
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public boolean hasNewMessages() {
        return user.getSub("services").getSub(service).getArray("messages").size() != 0;
    }

    public long userOnlineTime() {
        return user.getLong("onlineTime");
    }

    public long serviceUseTime() {
        return serviceData.getLong("useTime");
    }

    public long userServiceUseTime() {
        return user.getSub("services").getSub(service).getLong("useTime");
    }

    public ArrayList<TCN> decryptMessages() {
        return decryptMessages(user.getSub("services").getSub(service).getArray("messages"));
    }

    private ArrayList<TCN> decryptMessages(TCNArray array) {
        ArrayList<TCN> messages = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            try {
                messages.add(JSON.read(authKey.decryptString(new String(Base64.getDecoder().decode(array.getString(i))))));
            } catch(Exception e) {
                messages.add(null);
            }
        }
        return messages;
    }
}

