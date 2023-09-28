package com.conpo.audiorac.model;

import android.content.Context;
import android.util.Log;

import com.conpo.audiorac.activity.ActivityBase;
import com.conpo.audiorac.util.JSONHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


/**
 * JSON 응답 기본 클래스
 */
public class JSONResult extends JSONObject {

    private boolean JSON_DEBUG_MODE = true;

    public String message = "";
    public String code = "0";
    public String content = "";

    private JSONObject mJSONObject;

    private String requestUrl;
    private String requestBody;
    private String requestMethod;

    public JSONResult() {

    }

    public JSONResult(String c, String m) {
        setResult(c, m);
    }

    public JSONResult(String c, String m, JSONObject obj) {
        setResult(c, m);
        mJSONObject = obj;
    }

    public JSONResult setResult(JSONObject obj) {
        mJSONObject = obj;

        String data = "{}";

        try {
            data = obj.getString("content");
        } catch (Exception e) { }

        try {
            setResult(obj.getString("code"), obj.getString("message"), data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * JSONObject의 추가 속성 받아오기
     * @param name code/message/content 외의 추가로 return된 속성의 이름
     * @return 추가 속성 Object 값
     */
    public Object get(String name) {
        Object obj = null;

        try {
            obj = mJSONObject.get(name);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public JSONResult setResult(String c, String m) {
        code = c;
        message = m;

        if (JSON_DEBUG_MODE == true) {
            debugLog();
        }

        return this;
    }

    public JSONResult setResult(String c, String m, String cnt) {
        code = c;
        message = m;
        content = cnt;

        if (JSON_DEBUG_MODE == true) {
            debugLog();
        }

        return this;
    }

    public JSONResult setRequestUrl(String url, String json) {
        this.requestUrl = url;
        this.requestBody = json;

        return this;
    }

    public JSONResult setRequestMethod(String method) {
        this.requestMethod = method;

        return this;
    }

    public boolean isSuccess() {
        return code.equals("0");
    }

    public boolean recordNotFound() {
        return code.equals("101");
    }

    public boolean isJSONError() {
        return (code.compareTo("97") >= 0 && code.compareTo("99") <= 0);
    }

    public String getMessage() {
        return message;
    }

    private void debugLog() {
        Log.d("JSON Request", this.requestMethod + ": " + this.requestUrl);

        if (this.requestBody != null && !this.requestBody.equals("")) {
            Log.d("JSON Request", "BODY: " + this.requestBody);
        }

        if (mJSONObject != null) {
            Log.d("JSON Response", mJSONObject.toString());
        }
        else {
            Log.d("JSON Response", "code=" + code + ", message=" + message + ", \ncontent=" + content);
        }
    }

    public void reportError(Context context) {

        if (isJSONError()) {
            reportError(context, "JSON Error: code=" + code + ", " + this.getMessage());
        }
        else {
            reportError(context, "처리오류: code=" + code + ", " + this.getMessage());
        }
    }

    public void reportError(Context context, Exception e) {
        reportError(context, "에러발생: code=" + code + ", " + e.getMessage());
    }

    public void reportError(Context context, String message) {
        debugLog();

        Log.d("JSON Error", message);

        if (isJSONError()) {
            ((ActivityBase)context).reportServerError(message);
        }
        else if (recordNotFound()) {
            //
        }
        else {
            ((ActivityBase)context).reportServerError(message);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readValue(Class<T> valueType)
            throws IOException, JsonParseException, JsonMappingException
    {
        return JSONHelper.readValue(this.content, valueType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T readValue(TypeReference valueTypeRef)
            throws IOException, JsonParseException, JsonMappingException
    {
        return (T) JSONHelper.readValue(this.content, valueTypeRef);
    }

    public List<String> readValue() throws JSONException {
        return JSONHelper.readValue(this.content);
    }
}
