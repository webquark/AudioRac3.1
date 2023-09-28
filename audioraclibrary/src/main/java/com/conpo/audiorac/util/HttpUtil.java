package com.conpo.audiorac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ParseException;
import android.util.Log;

import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.model.Record;

public class HttpUtil {
	private static Context mContext;
	
//	/**
//	 * HTTP GET 요청
//	 * @param url
//	 * @return
//	 */
//	public static String sendGet(String url) {
//		HttpResponse resGet = httpGet(url);
//
//		return getResponse(resGet);
//	}
//
//	/**
//	 * HTTP GET 요청 (User Agent String이 필요한 경우에 이 버전을 사용)
//	 * @param context
//	 * @param url
//	 * @return
//	 */
//	public static String sendGet(Context context, String url) {
//		mContext = context;
//
//		HttpResponse resGet = httpGet(url);
//
//		return getResponse(resGet);
//	}
//
//	public static HttpResponse httpGet(String url) {
//		HttpResponse resGet = null;
//
//		HttpClient client = getHttpClient();
//		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
//
//
//		HttpGet reqGet = new HttpGet(url);
//
//		try {
//			resGet = client.execute(reqGet);
//		} catch (ClientProtocolException e) {
//			Log.i(Common.TAG, e.getMessage());
//		} catch (IOException e) {
//			Log.i(Common.TAG, e.getMessage());
//		}
//
//		return resGet;
//	}
//
//	private static HttpClient getHttpClient() {
//        try {
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null, null);
//
//            SSLSocketFactory sf = new CPDRMSSLSocketFactory(trustStore);
//            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//
//            HttpParams params = new BasicHttpParams();
//            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//
//            /*
//             * User Agent String 설정
//             */
//            if (mContext != null) {
//            	String originalUserAgentString = HttpProtocolParams.getUserAgent(params);
//            	String appUserAgentString = Utils.getUserAgentString(mContext);
//
//            	if (originalUserAgentString != null) {
//            		appUserAgentString = originalUserAgentString + appUserAgentString;
//            	}
//
//            	HttpProtocolParams.setUserAgent(params, appUserAgentString);
//            }
//
//            SchemeRegistry registry = new SchemeRegistry();
//            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//            registry.register(new Scheme("https", sf, 443));
//
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//
//            return new DefaultHttpClient(ccm, params);
//        } catch (Exception e) {
//            return new DefaultHttpClient();
//        }
//    }
//
//	public static String getResponse(HttpResponse result) {
//		String line = "";
//		String res = "";
//
//		try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(result.getEntity().getContent(), "Euc-kr"));
//
//			while((line = br.readLine()) != null) {
//				res += line;
//				res += "\r\n";
//			}
//		} catch (NullPointerException e) {
//
//		} catch (ParseException e) {
//
//		} catch (IOException e) {
//
//		} catch(IllegalStateException e)  {
//			Log.i(Common.TAG, "getResponse illegal");
//		}
//
//		return res;
//	}
	
	/**
	 * URL의 'http' 포함 여부를 검증하여 포함되지 않은 경우 'http://' 를 기본 프로토콜로 붙여줌 
	 * @param url
	 * @return
	 */
	public static String verifyUrl(String url) {
		if (url.indexOf("http") >= 0) {
			return url;
		}
		else {
			return "http://" + url;
		}
	}
	
	/**
	 * httpGet의 결과를 key:value 배열로 리턴함
	 * @param response
	 * @return
	 */
	public static ArrayList<Record> response2ItemList(String response) {
		ArrayList<Record> itemList = new ArrayList<Record>();

		if (response.contains("error")) {
			return itemList;
		}

		String[] arrRes = response.split("\r\n");

		if(arrRes.length > 0) {
			for(int i=0; i<arrRes.length; i++) {
				if (arrRes[i].isEmpty()) {
					continue;
				}
				
				// 도서관명 | 주소 : url | 로그인시 이름 사용여부 : Y,N | 자체 로그인 사용여부 : Y,N | 앱유형(0:플레이어,1:오디오락)
				String[] arrOrgan = arrRes[i].split("[|]");

				Record rec = new Record();
				rec.put("name", arrOrgan[0]);
				rec.put("url", arrOrgan[1]);
				
				if (arrOrgan.length > 2) {
					rec.put("use_name", arrOrgan[2]);
					rec.put("custom_login", arrOrgan[3]);
					rec.put("app_type", arrOrgan[4]);
				}
				
				itemList.add(rec);
			}
		}
		
		return itemList;
	}
}
