package com.conpo.audiorac.server;

import android.provider.SyncStateContract;

import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.model.APIResponse;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

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



    public static void requestUserLogin(String url, String userId, String userPwd, String appVer,
                                        Callback<APIResponse> callback) {
        getRetrofitService(url).requestUserLogin(userId, userPwd, appVer).enqueue(callback);
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

