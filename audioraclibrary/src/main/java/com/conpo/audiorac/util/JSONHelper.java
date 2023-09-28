package com.conpo.audiorac.util;

import android.util.Log;

import com.conpo.audiorac.model.JSONResult;
import com.conpo.audiorac.model.Parameters;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSON 요청 및 응답 처리 Helper
 */
public class JSONHelper {

    @SuppressWarnings("unchecked")
    public static <T> T readValue(String content, Class<T> valueType)
            throws IOException, JsonParseException, JsonMappingException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return (T) mapper.readValue(content, valueType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T readValue(String content, TypeReference valueTypeRef)
            throws IOException, JsonParseException, JsonMappingException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return (T) mapper.readValue(content, valueTypeRef);
    }

    public static JSONResult jsonGET(String url) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.GET, url);
    }

    public static JSONResult jsonPOST(String url, HashMap<String, Object> params) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.POST, url, params);
    }

    public static JSONResult jsonPOST(String url, ArrayList<Parameters> paramArray) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.POST, url, paramArray);
    }

    public static JSONResult jsonPUT(String url) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.PUT, url);
    }

    public static JSONResult jsonPUT(String url, HashMap<String, Object> params) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.PUT, url, params);
    }

    /**
     *
     * @param url
     * @param paramArray
     * @return
     */
    public static JSONResult jsonPUT(String url, ArrayList<Parameters> paramArray) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.PUT, url, paramArray);
    }

    public static JSONResult jsonDELETE(String url) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.DELETE, url);
    }

    public static JSONResult jsonDELETE(String url, HashMap<String, Object> params) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.DELETE, url, params);
    }

    public static JSONResult jsonDELETE(String url, ArrayList<Parameters> paramArray) {
        return jsonHttpRequest(JSONHttpRequest.RequestMethod.DELETE, url, paramArray);
    }

    /**
     * Body 없는 Http Request를 보내고 JSON 응답 받기
     * @param method
     * @param url
     * @return
     */
    public static JSONResult jsonHttpRequest(JSONHttpRequest.RequestMethod method, String url) {
        return jsonHttpRequest(method, url, "");
    }

    /**
     * HashMap을 받아 JSON으로 변환하여 Http Request를 보내고 JSON 응답 받기
     * @param method
     * @param url
     * @param params
     * @return
     */
    public static JSONResult jsonHttpRequest(JSONHttpRequest.RequestMethod method, String url, HashMap<String, Object> params) {
        JSONResult result = new JSONResult();
        String jsonBody = "";

        if (params != null) {
            try {
                JSONStringer stringer = new JSONStringer();
                jsonBody = Map2JSON(params, stringer);
            } catch (JSONException e) {
                return result.setResult("99", "" + e.getMessage());
            }
        }

        return jsonHttpRequest(method, url, jsonBody);
    }

    public static JSONResult jsonHttpRequest(JSONHttpRequest.RequestMethod method, String url, ArrayList<Parameters> paramArray) {
        JSONResult result = new JSONResult();
        String jsonBody = "";

        if (paramArray != null) {
            try {
                jsonBody = Array2JSON(paramArray);
            } catch (JSONException e) {
                return result.setResult("99", "" + e.getMessage());
            }
        }

        return jsonHttpRequest(method, url, jsonBody);
    }

    /**
     * JSON Body 추가하여 Http Request를 보내고 JSON 응답 받기
     * @param method
     * @param url
     * @return
     */
    public static JSONResult jsonHttpRequest(JSONHttpRequest.RequestMethod method, String url, String jsonBody) {
        JSONResult result = new JSONResult();
        String jsonResponse;

        try {
            // prevent for "method PUT must have a request body" error
            if (method == JSONHttpRequest.RequestMethod.PUT
                    && (jsonBody == null || jsonBody.equals(""))) {
                jsonBody = "{}";
            }

            result.setRequestUrl(url, jsonBody)
                    .setRequestMethod(method.toString());

            JSONHttpRequest req = new JSONHttpRequest();

            Log.d("REQ", url);
            jsonResponse = req.sendHttpRequest(method, url, jsonBody);
            Log.d("RES", jsonResponse);
        }
        catch (Exception e) {
            return result.setResult("99", e.getMessage());
        }

        if (jsonResponse == null || jsonResponse.equals("")) {
            return result.setResult("98", "Empty response, check network status.");
        }

        try {
            result.setResult(new JSONObject(jsonResponse));
        }
        catch (Exception e) {
            return result.setResult("97", e.getMessage());
        }

        return result;
    }

    /**
     * 단순 GET request
     * @param url
     * @return JSON string
     */
    public static String get(String url) {
        String jsonResponse;

        try {
            JSONHttpRequest req = new JSONHttpRequest();
            jsonResponse = req.sendHttpRequest(JSONHttpRequest.RequestMethod.GET, url, "");
        }
        catch (Exception e) {
            return "{'error': 'Server not connected'}";
        }

        if (jsonResponse == null || jsonResponse.equals("")) {
            return "{'error': 'Server not connected'}";
        }
        else {
            return jsonResponse;
        }
    }

    /**
     * Map을 JSON 문자열로 변환함
     * @param map
     * @return
     * @throws JSONException
     */
    public static String Map2JSON(Map<?, ?> map, JSONStringer stringer) throws JSONException {
        stringer.object();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            stringer.key(String.valueOf(entry.getKey()));
            toJSONValue(entry.getValue(), stringer);
        }

        stringer.endObject();

        return stringer.toString();
    }

    /**
     * ArrayList를 JSON 문자열로 변환함
     * @param arr 변환할 ArrayList
     * @return
     */
    public static String Array2JSON(ArrayList<Parameters> arr) throws JSONException {
        JSONStringer stringer = new JSONStringer();

        stringer.array();

        for (Parameters param : arr){
            Map2JSON(param, stringer);
        }

        stringer.endArray();

        return stringer.toString();
    }

    /**
     * JSON 문자열에서 스트링 배열 구하기
     * ex)   ["매핑쇼가 13시부터 시작합니다","무비 이벤트 (자세한 사항은 App.에서 확인)"]
     * @param content
     * @return
     */
    public static List<String> readValue(String content) throws JSONException {
        List<String> arrStr = new ArrayList<String>();

        JSONArray jsonArray = new JSONArray(content);

        for (int i = 0, count = jsonArray.length(); i < count; i++) {
            arrStr.add( jsonArray.get(i).toString() );
        }

        return arrStr;
    }

    /**
     * Object를 JSON 스트링으로 변환
     * @param value
     * @param stringer
     * @throws JSONException
     */
    public static void toJSONValue(Object value, JSONStringer stringer) throws JSONException {
        if (value == null) {
            stringer.value(null);

        }
        else if (value instanceof Map) {
            Map2JSON((Map<?, ?>) value, stringer);
        }
        else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().isPrimitive()) {
                stringer.array();

                if (value instanceof byte[]) {
                    for (byte b : (byte[]) value) {
                        stringer.value(b);
                    }
                }
                else if (value instanceof short[]) {
                    for (short s : (short[]) value) {
                        stringer.value(s);
                    }
                }
                else if (value instanceof int[]) {
                    for (int i : (int[]) value) {
                        stringer.value(i);
                    }
                }
                else if (value instanceof float[]) {
                    for (float f : (float[]) value) {
                        stringer.value(f);
                    }
                }
                else if (value instanceof double[]) {
                    for (double d : (double[]) value) {
                        stringer.value(d);
                    }
                }
                else if (value instanceof char[]) {
                    for (char c : (char[]) value) {
                        stringer.value(c);
                    }
                }
                else if (value instanceof boolean[]) {
                    for (boolean b : (boolean[]) value) {
                        stringer.value(b);
                    }
                }

                stringer.endArray();
            }
        }
        else {
            stringer.value(value);
        }
    }
}
