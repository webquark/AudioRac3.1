package com.conpo.audiorac.model;

import java.util.HashMap;


@SuppressWarnings("serial")
public class Record extends HashMap<String, String> {
	
	public static final Result RESULT_OK = Result.OK;
	public static final Result RESULT_FAIL = Result.FAIL;
	public static final Result RESULT_UNDEF = Result.UNDEF;
	
	public int id = 0;
	
	public enum Result {
		OK, FAIL, UNDEF;
		
		@Override
		public String toString() {
			String s = super.toString();
			return s;
		}
		
		public static Result parseRes(String s) {
			if (s == null) {
				return UNDEF;
			}
			else if (s.equals("OK") || s.equals("0")) {
				return Result.OK;
			}
			else if (s.equals("FAIL")) {
				return Result.FAIL;
			}
			else {
				return UNDEF;	// undefined
			}
		}
	}
	
	public Record setResultOK() {
		return this.setResult(Result.OK, "");
	}
	
	public Record setResult(Result res, String message) {
		this.put("code", res.toString());
    	this.put("message", message);
    	
    	return this;
	}
	
	public boolean isComplete() {
		return getResult() == Result.OK;
	}
	
	public boolean isFail() {
		return getResult() == Result.FAIL;
	}
	
	public Result getResult() {
		
		return Result.parseRes(this.get("code"));
	}
	
	public String getMessage() {
		if (this.containsKey("message")) {
			return this.get("message");
		}
		else {
			return "";
		}
	}
	
	public void setMessage(String msg) {
		this.put("message", msg);
	}
	
	public int getInt(String key) {
		if (this.containsKey(key)) {
			return Integer.parseInt(this.get(key).replace(",", ""));
		}
		else {
			return 0;
		}
	}

	/**
	 * 전달된 키가 포함되어 있는지 확인한 후, 키가 호함된 경우 키값 리턴
	 * @param key
	 * @return
	 */
	public String safeGet(Object key) {
		if (this.containsKey(key)) {
			String value = super.get(key);
			
			if (value == null || value.equals("null"))
				return "";
			else
				return value;
		}
		else {
			return "";
		}
	}
	
	
}
