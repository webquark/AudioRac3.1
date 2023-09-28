package com.conpo.audiorac.model;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * Retrofit HTTP API APIResponse Template
 */
public class APIResponse {
    private static final String LOG_TAG = "API";

    @SerializedName("code")
    @Expose
    public String code = "-1";

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("data")
    @Expose
    public APIData data;

    @SerializedName("cnt")
    @Expose
    public int cnt;

    @SerializedName("sql")
    @Expose
    public String sql;

    @SerializedName("api")
    @Expose
    public String api;

    /**
     * API 호출결과의 성공여부
     * @return 성공여부
     */
    public boolean success() {
        return (code != null && code.equals("0") );
    }

    /**
     * API 호출결과가 실패한 경우 오류 메시지를 되돌린다.
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * API를 통해 요청한 데이터 또는 데이터 목록
     * @return
     */
    public APIData getData() {
        return data;
    }

    public int getCount() {
        return cnt;
    }

    /**
     * API data로 부터 지정한 형식의  Onject를 리턴함
     * @param className Object 클래스명
     * @param <T>
     * @return Object
     */
    public <T> T getObject(Class<?> className) {
        Gson gson = new Gson();

        return (T)gson.fromJson(this.data.toString(), className);
    }

    /**
     * data HashMap에서 지정한 이름의 엘리먼트 value를 ArrayList JSON 스트링으로 리턴
     * @param name 엘리먼트 이름
     * @return
     */
    public String getListJson(String name) {
        String json = "";

        try {
            ArrayList<LinkedTreeMap<String, Object>> tree = (ArrayList<LinkedTreeMap<String, Object>>)this.data.get(name);
            json = new Gson().toJson(tree);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * JSON string 으로부터 ProxyRequest 또는 그의 상속 클래스의 객체 인스턴스로 변환
     * @param jsonString
     * @param className
     * @param <T>
     * @return
     */
    public <T> T fromJson(@Nullable String jsonString, @Nullable Class<?> className) {
        Gson gson = new Gson();

        if (jsonString == null) {
            jsonString = this.data.toString();
        }

        if (className == null) {
            return (T)gson.fromJson(jsonString, APIResponse.class);
        } else {
            return (T)gson.fromJson(jsonString, className);
        }
    }

    /**
     * 네트워크 오류로 인해 API 요청이 실패한 경우 메시지 지정
     * @param msg
     */
    public void setNetworkError(String msg) {
        code = "99";
        message = "네트워크 오류: " + msg;

        Log.d("API CALL", "RES:" + message);
    }


}

