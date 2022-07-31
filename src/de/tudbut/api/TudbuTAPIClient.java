package de.tudbut.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

import de.tudbut.tools.Hasher;
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
    String authToken;     // this is for accessing TudbuTAPI service messages
    String fullAuthToken; // this is for changing the api password, obtained through game authentication

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

    public RequestResult<?> sendMessage(String message) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/messageAll", "application/x-www-urlencoded", 
                "uuid=" + uuid + "&token=" + authToken + "&message=" + HTTPUtils.encodeUTF8(message)
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("accessGranted")) {
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

    public RequestResult<?> getDataMessages() {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/data/read", "application/x-www-urlencoded", 
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
        HTTPRequest request = new HTTPRequest(HTTPRequestType.GET, host, port, "/api/service/" + service);
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
                HTTPRequestType.GET, host, port, "/api/user/" + uuid + "/password?" +
                "old=" + HTTPUtils.encodeUTF8(oldPass) + "&new=" + HTTPUtils.encodeUTF8(newPass)
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

    public RequestResult<?> authorizeWithGameAuth(String mojangToken) { // The token is never sent to my API. It is only sent to mojang, 
                                                                        // in the same way that when you join a minecraft server, that server 
                                                                        // does not get your token.
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/auth/game/start", "application/x-www-urlencoded",
                "uuid=" + uuid + "&name=" + user.getString("name")
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("found")) {
                TCN json = new TCN();
                json.set("accessToken", mojangToken); // Here, the token is put into the data to send to mojang.
                json.set("selectedProfile", uuid.toString().replace("-", "")); // Here, the player's uuid is put into the request to mojang.
                json.set("serverId", jsonResponse.getString("serverToJoin")); // Here, we get the server ID the api told us to send to mojang.
                // Now, we send the data to mojang
                request = new HTTPRequest(HTTPRequestType.POST, "https://sessionserver.mojang.com", 443, "/session/minecraft/join", "application/json", JSON.write(json));
                // Now, we check if the request was successful. If so, we tell the api to check if the "server" was joined.
                if(request.send().parse().getStatusCode() == 204) {
                    // success! now ask api to check.
                    request = new HTTPRequest(HTTPRequestType.POST, host, port, "/api/auth/game/check", "application/x-www-urlencoded", "uuid=" + uuid);
                    jsonResponse = JSON.read(request.send().parse().getBody());
                    if(jsonResponse.getBoolean("success")) {
                        fullAuthToken = jsonResponse.getString("token"); // This is the TudbuTAPI token, not the minecraft token.
                        return RequestResult.SUCCESS();
                    }
                }
            }
        } catch (JSONFormatException | IOException e) {}
        return RequestResult.FAIL();
    }

    public RequestResult<?> unauthorize() {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/auth/delete", "application/x-www-urlencoded",
                "uuid=" + uuid + "&token=" + fullAuthToken
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("set")) {
                return RequestResult.SUCCESS();
            }
        } catch (JSONFormatException | IOException e) {}
        return RequestResult.FAIL();
    }

    public RequestResult<?> setPassword(String newPassword) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/user/" + uuid + "/password", "application/x-www-urlencoded",
                "token=" + fullAuthToken + "&new=" + HTTPUtils.encodeUTF8(newPassword)
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

    public boolean hasNewMessages() {
        return user.getSub("services").getSub(service).getArray("messages").size() != 0;
    }

    public boolean hasNewDataMessages() {
        return user.getSub("services").getSub(service).getArray("dataMessages").size() != 0;
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

    public int premiumStatus() {
        return user.getSub("services").getSub(service).getInteger("premiumStatus");
    }

    public String getPasswordHash() {
        return user.getString("passwordHash");
    }

    public boolean checkPassword(String pass) {
        return Hasher.sha512hex(Hasher.sha512hex(pass)).equals(getPasswordHash());
    }

    public TCN serviceData() {
        return user.getSub("services").getSub(service).getSub("data");
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

