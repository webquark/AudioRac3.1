package com.conpo.audiorac.util;

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * JSON http request helper
 */
public class JSONHttpRequest {

    private OkHttpClient client = new OkHttpClient();

    private static String mCharsetName = "utf-8";   // "euc-kr";

    public static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public enum RequestMethod {
        GET, POST, PUT, DELETE
    }


    public String sendHttpRequest(RequestMethod method, String url, String json) {

        Request.Builder builder = new Request.Builder();

        builder.url(url);
//                .addHeader("Accept", "application/json")
//                .addHeader("Host", Utils.getHostHeader(url));

        RequestBody body = null;

        if (json != null && !json.equals("")) {
            body = RequestBody.create(JSON, json);
        }

        switch (method) {
            case POST :
                builder.post(body);
                break;
            case PUT :
                builder.put(body);
                break;
            case DELETE :
                builder.delete(body);
                break;
            case GET :
            default:
                break;
        }

        Request request = builder.build();

        String line = "";
        String res = "";
        try {
            Response response = client.newCall(request).execute();
            //res = response.body().string();

            BufferedReader br = new BufferedReader(new InputStreamReader(response.body().byteStream(), mCharsetName));

            while((line = br.readLine()) != null) {
                res += line;
                res += "\r\n";
            }

            Log.d("RES", res);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
