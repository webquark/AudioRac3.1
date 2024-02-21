package com.conpo.audiorac.server;

import android.provider.SyncStateContract;

import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.model.APIResponse;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * AudioRac API 클라이언트
 */
public class RetrofitClient {

    private static Retrofit retrofit = null;

    /**
     * API 클라이언트 생성 - AudioRac 서버 또는 도서관 서버의 Url에 따라 동적 생성
     * @param baseUrl API 서버 Url
     * @return
     */
    public static Retrofit getClient(String baseUrl) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static RetrofitService getRetrofitService() {
        return RetrofitClient.getClient(AudioRacApplication.CFG_AUDIORAC_SERVER_URL).create(RetrofitService.class);
    }

    public static RetrofitService getRetrofitService(String url) {
        return RetrofitClient.getClient( HttpUtil.verifyUrl(url) )
                             .create(RetrofitService.class);
    }

    public static void getAppVersion(Callback<APIResponse> callback) {
        getRetrofitService().getAppVersion("Android").enqueue(callback);
    }

    public static void getAudioRacSiteList(String sitePrefix, String appVer,
                                           Callback<APIResponse> callback) {
        getRetrofitService().getAudioRacSiteList(sitePrefix, appVer).enqueue(callback);
    }



    public static void requestUserLogin(String url, String useName, String userId, String userPwd,  String appVer,
                                        Callback<APIResponse> callback) {
        getRetrofitService(url).requestUserLogin(useName, userId, userPwd, appVer).enqueue(callback);
    }

    public static void saveStreamLog(String url, String userId, String volumeId,
                                        Callback<APIResponse> callback) {
        getRetrofitService(url).saveStreamLog(userId, volumeId).enqueue(callback);
    }

    public static void getAlbumArtUrl(String url, String volumeId,
                                     Callback<APIResponse> callback) {
        getRetrofitService(url).getAlbumArtUrl(volumeId).enqueue(callback);
    }
}

