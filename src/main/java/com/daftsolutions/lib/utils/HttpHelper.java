/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.daftsolutions.lib.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author colin
 */
public class HttpHelper {

    /**
     * Sena a HTMP Post to a URL and return the response data
     * TODO handle response error codes etc.
     * @param postUrl
     * @param encoding
     * @param requestHeaders 
     * @param params
     * @return
     */
    public static String post(String postUrl, String encoding, HashMap<String, String> requestHeaders, Object postContent) {
        String result = null;
        try {
            URL url;
            URLConnection urlConn;
            DataOutputStream printout;
            BufferedReader input;

            url = new URL(postUrl);
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            if (!requestHeaders.containsKey("Content-Type")) {
                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                urlConn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            // Send POST output.
            printout = new DataOutputStream(urlConn.getOutputStream());
            String content = "";
            if (postContent instanceof String) {
                content = (String) postContent;
            } else if (postContent instanceof HashMap) {
                HashMap<String, String> params = (HashMap<String, String>) postContent;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    content += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), encoding);
                }
            }
            printout.writeBytes(content);
            printout.flush();
            printout.close();

            // Get response data.
            input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String str;
            while (null != ((str = input.readLine()))) {
                result += str + "\n";
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Send a HTML Get to URL and return the response data
     * TODO handle response error codes etc.
     * @param getUrl
     * @param encoding
     * @param params
     * @return
     */
    public static String get(String getUrl) {
        String result = null;
        try {
            URL url;
            URLConnection urlConn;
            BufferedReader input;

            url = new URL(getUrl);
            urlConn = url.openConnection();
            urlConn.setDoInput(false);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Get response data.
            input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String str;
            while (null != ((str = input.readLine()))) {
                result += str + "\n";
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Sent a HTML Put to URL and return the response data
     * TODO handle response error codes etc.
     * @param putUrl
     * @param encoding
     * @param params
     * @return
     */
    public static String put(String putUrl, String encoding, HashMap<String, String> requestHeaders) {
        String result = null;
        try {
            throw new Exception("Not yet implemented.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Sent a HTML Delete to URL and return the response data
     * TODO handle response error codes etc.
     * @param deleteUrl
     * @param encoding
     * @param params
     * @return
     */
    public static String delete(String deleteUrl, String encoding, HashMap<String, String> requestHeaders) {
        String result = null;
        try {
            throw new Exception("Not yet implemented.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
