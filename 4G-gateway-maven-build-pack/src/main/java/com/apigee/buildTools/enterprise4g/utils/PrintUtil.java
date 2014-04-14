package com.apigee.buildTools.enterprise4g.utils;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Set;


public class PrintUtil {

    public static String formatRequest(HttpRequest request) {

        String prettyRequest = "\n\n\nRequest prepared for the server \n **************************\n";

        // Print all headers except auth

        prettyRequest = prettyRequest + request.getMethod() + "  " + request.getUrl();

        HttpHeaders headers = request.getHeaders();

        Set<String> tempheadersmap = headers.keySet();

        for (Iterator<String> iter = tempheadersmap.iterator(); iter.hasNext(); ) {

            try {
                String headerkey = iter.next();
                if (!headerkey.trim().equalsIgnoreCase("Authorization")) {
                    String headervalue = ""+headers.get(headerkey);
                    prettyRequest = prettyRequest + "\n" + headerkey + ": " + headervalue;
                }else {
                    prettyRequest = prettyRequest + "\n" + "authorization" + ": " + "Basic [Not shown in log]";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
        if (request.getMethod().compareTo(HttpMethod.POST) == 0  ){

            if (request.getContent()!=null && request.getContent().getType() !=null)
            {
                prettyRequest = prettyRequest + "\n" + "content-type" + ": " + request.getContent().getType();

                if (!request.getContent().getType().contains("octet"))
                {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                request.getContent().writeTo(out);
                prettyRequest = prettyRequest + "\n [Request body]\n" + out.toString();
                } else {
                    prettyRequest = prettyRequest + "\n [Request body contains data, not shown] \n";
                }
            }

        }
        }catch (Exception e){
            e.printStackTrace();
        }

        return  prettyRequest;
    }


    public static String formatResponse(HttpResponse response, String body) {

        String prettyString = "\n\n\nResponse returned by the server \n **************************\n";

        // Print all headers except auth

        prettyString = prettyString + response.getStatusCode() + "  " + response.getStatusMessage();

        HttpHeaders headers = response.getHeaders();

        Set<String> tempheadersmap = headers.keySet();

        for (Iterator<String> iter = tempheadersmap.iterator(); iter.hasNext(); ) {

            try {
                String headerkey = iter.next();
                if (!headerkey.trim().equalsIgnoreCase("Authorization")) {
                    String headervalue = ""+headers.get(headerkey);
                    prettyString = prettyString + "\n" + headerkey + ": " + headervalue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // print response body
        prettyString = prettyString + "\n" + body;

        return  prettyString;
    }
}
