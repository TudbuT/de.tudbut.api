package de.tudbut.api;

import java.io.IOException;
import java.util.UUID;

import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.net.http.HTTPUtils;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.parsing.JSON.JSONFormatException;

public class TudbuTAPIAdminClient {

    String service;
    String password;
    String host;
    int port;
    public TCN serviceData;

    public TudbuTAPIAdminClient(String service, String password, String host, int port) {
        this.service = service;
        this.password = password;
        this.host = host;
        this.port = port;
        // ignore the results of these. the constructor might be called with no internet connection
        getService();
    }

    public RequestResult<?> setAllowChat(boolean allowChat) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/allowChat", "application/x-www-urlencoded", 
                "servicePass=" + HTTPUtils.encodeUTF8(password) + "&allow=" + allowChat
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("success")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> setPremium(UUID uuid, int premiumStatus) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/setPremium", "application/x-www-urlencoded", 
                "servicePass=" + HTTPUtils.encodeUTF8(password) + "&uuid=" + uuid+ "&status=" + premiumStatus
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("success")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> setData(UUID uuid, TCN data) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/data/set", "application/x-www-urlencoded", 
                "servicePass=" + HTTPUtils.encodeUTF8(password) + "&uuid=" + uuid+ "&data=" + HTTPUtils.encodeUTF8(JSON.write(data))
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("success")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> sendData(TCN data) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/data/sendAll", "application/x-www-urlencoded", 
                "servicePass=" + HTTPUtils.encodeUTF8(password) + "&data=" + HTTPUtils.encodeUTF8(JSON.write(data))
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("success")) {
                return RequestResult.SUCCESS(jsonResponse);
            }
            else {
                return RequestResult.FAIL(jsonResponse);
            }
        } catch (JSONFormatException | IOException e) {
            return RequestResult.FAIL(e);
        }
    }

    public RequestResult<?> sendData(UUID uuid, TCN data) {
        HTTPRequest request = new HTTPRequest(
                HTTPRequestType.POST, host, port, "/api/service/" + service + "/data/send", "application/x-www-urlencoded", 
                "servicePass=" + HTTPUtils.encodeUTF8(password) + "&uuid=" + uuid+ "&data=" + HTTPUtils.encodeUTF8(JSON.write(data))
        );
        try {
            TCN jsonResponse = JSON.read(request.send().parse().getBody());
            if(jsonResponse.getBoolean("success")) {
                return RequestResult.SUCCESS(jsonResponse);
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
            if(jsonResponse.getBoolean("success")) {
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

}
