package com.conpo.audiorac.util;

import java.text.NumberFormat;

public class StringUtil {

	public static String padSpace(int length) {
		return pad("", length);
	}
	
	public static String pad(String value, int length) {
	    return pad(value, length, " ");
	}

	public static String pad(String value, int length, String with) {
		if (length < 0)
			return "";
		
	    StringBuilder result = new StringBuilder(length);
	    result.append(value);

	    while (result.length() < length) {
	        result.insert(0, with);
	    }

	    return result.toString();
	}
	
	public static String padL(String value, int length) {
		return padL(value, length, " ");
	}
	
	public static String padC(String value, int length) {
		return padC(value, length, " ");
	}
	
	public static String padR(String value, int length) {
		return padR(value, length, " ");
	}
	
	public static String padL(String value, int length, String with) {
		if (length < 0)
			return "";
		
	    StringBuilder result = new StringBuilder(length);
	    
	    while ( result.length() < (length - value.length()) ) {
	        result.insert(0, with);
	    }

	    result.append(value);
	    
	    return result.toString();
	}
	
	public static String padC(String value, int length, String with) {
		if (length < 0)
			return "";
		
	    StringBuilder result = new StringBuilder(length);
	    
	    int half = (int)((length - value.length()) / 2);
	    
	    for (int i = 0; i < half; i++) {
	        result.insert(0, with);
	    }

	    result.append(value);
	    
	    for (int i = 0; i < (length - value.length() - half); i++) {
	        result.insert(0, with);
	    }
	    
	    return result.toString();
	}
	
	public static String padR(String value, int length, String with) {
		return pad(value, length, with);
	}
	
	public static String left(String text, int length)
    {
          return text.substring(0, length);
    }

    public static String right(String text, int length)
    {
          return text.substring(text.length() - length, length);
    } 
    
    
    public static String getAmount(String amount) {
		long lAmount = Long.parseLong( amount );
		
		return NumberFormat.getInstance().format(lAmount);
	}
    
    public static boolean isEmptyOrNull(String str) {
    	return ( str == null || str.equals("") || str.equals("null") ); 
    }
}
