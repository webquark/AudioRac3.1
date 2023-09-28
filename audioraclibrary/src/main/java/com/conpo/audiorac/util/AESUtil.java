package com.conpo.audiorac.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
	   
	public static String key = "cyberdesiclib0123456789012345678";	// 기본키 (application에서 별도설정 필요) 

	public static void setKey(String key) {
		AESUtil.key = key;
	}
	
	/*
	 * 암호화 - 암호화 후 Base64 인코딩 
	 */
	public static String encrypt(String message) throws Exception {
	    SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
	    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	    cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
	    
	    byte[] encrypted = cipher.doFinal(message.getBytes());
	    
	    String strReturn = new String(Base64Coder.encode(encrypted));
	    
	    return strReturn;        
	}
	
	/* 
	 * 복호화 - Base64 디코딩 후 복호화
	 */
	public static String decrypt(String encrypted) throws Exception {
	    SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
	    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	    cipher.init(Cipher.DECRYPT_MODE, sKeySpec);

	    byte[] original = cipher.doFinal(Base64Coder.decode(encrypted));
	    
	    String originalString = new String(original);
	    
	    return originalString;
	}
}
