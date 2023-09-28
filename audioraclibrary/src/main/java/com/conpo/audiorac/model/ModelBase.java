package com.conpo.audiorac.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Data Model 기본 클래스
 */
public class ModelBase {

    public String no = "";
    public String name = "";

    /**
     * JSON 문자열로부터 전달된 클래스 형식의 오브젝트를 맵핑한다
     * @param jsonString JSON 문자열
     * @param valueType 오브젝트의 클래스 형식
     * @return 맵핑한 클래스 오브젝트
     */
    public static <T> T parse(String jsonString, Class<T> valueType) {
        ObjectMapper mapper = new ObjectMapper();
        T res;

        try {
            res = (T) mapper.readValue(jsonString, valueType);

        } catch (IOException e) {
            e.printStackTrace();
            res = null;
        }

        return res;
    }

    public String toString() {
        return "";
    }

    public String getValue() {
        return "";
    }
}

