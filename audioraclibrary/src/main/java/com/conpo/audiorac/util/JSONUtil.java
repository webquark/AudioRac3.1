package com.conpo.audiorac.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.util.Log;

import com.conpo.audiorac.model.Record;

/**
 * JSON 관련 Wrapper
 * @author Hansolo
 *
 */
public class JSONUtil {
	
	private static final String LOG_TAG = "JSONUtil";

	private static boolean mWriteLog = true;
	
	/**
	 * Record를 JSON 문자열로 변환함
	 * @param rec 변환할 Record 객체
	 * @return
	 */
	public static String toJSON(Record rec) {
		JSONStringer stringer = new JSONStringer();
		
	    try {
	    	stringer.object();
	    	
	    	for (Map.Entry<?, ?> entry : rec.entrySet()) {
		        stringer.key(String.valueOf(entry.getKey()));
		        toJSONValue(entry.getValue(), stringer);
		    }
	    	
	    	stringer.endObject();
	    }
	    catch (JSONException e) {
	    	e.printStackTrace();
	    }
		 
	    return stringer.toString();
	}
	
	/**
	 * ArrayList를 JSON 문자열로 변환함
	 * @param arr 변환할 ArrayList
	 * @return
	 */
	public static <T> String toJSON(ArrayList<T> arr) {
		JSONStringer stringer = new JSONStringer();
		
	    try {
			stringer.array();
		    
		    for (T item : arr) {
		    	stringer.object();
		    	
		    	for (Map.Entry<?, ?> entry : ((Record)item).entrySet()) {
			        stringer.key(String.valueOf(entry.getKey()));
			        toJSONValue(entry.getValue(), stringer);
			    }
		    	
		    	stringer.endObject();
		    }
		    
		    stringer.endArray();
	    }
	    catch (JSONException e) {
	    	e.printStackTrace();
	    }
		 
	    return stringer.toString();
	}
	
	/**
	 * Map을 JSON 문자열로 변환함
	 * @param map
	 * @param stringer
	 * @return
	 * @throws JSONException
	 */
	public static String toJSON(Map<?, ?> map, JSONStringer stringer) throws JSONException {
		stringer.object(); 
	    
	    for (Map.Entry<?, ?> entry : map.entrySet()) {
	        stringer.key(String.valueOf(entry.getKey()));
	        toJSONValue(entry.getValue(), stringer);
	    }
	    
	    stringer.endObject(); 
	    
	    return stringer.toString();
	}
	
	public static void toJSONValue(Object value, JSONStringer stringer) throws JSONException {
	    if (value == null) {
	        stringer.value(null);

	    } 
	    else if (value instanceof Map) {
	        toJSON((Map<?, ?>) value, stringer);
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
	
	
	public static Record toRecord(JSONObject jsonObj) throws JSONException {
		Record rec = new Record();
		
		if (jsonObj.has("id")) {
			rec.id   = jsonObj.getInt("id");
		}
		
		Iterator keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			rec.put(key, jsonObj.getString(key));
		}
		
		return rec;
	}
	
	public static ArrayList<Record> toRecordList(JSONArray jsonArr) throws JSONException {
		ArrayList<Record> arr = new ArrayList<Record>();
		
		if (jsonArr != null) {
        	for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = (JSONObject)jsonArr.get(i);
                
                Record record = toRecord(jsonObj);
                
                arr.add(record);
           }
        }
		
		return arr;
	}
	
	public static void setWriteLog(boolean writeLog) {
		mWriteLog = writeLog;
	}
	
}
