package com.conpo.audiorac.server;

import com.conpo.audiorac.model.APIResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface RetrofitService {

    /**
     * 버전체크
     * @param os 앱 OS [Android|iOS]
     * @return 지정한 OS의 AudioRac 앱 최신버전
     */
    @GET("/api/mobile/getAppVersion.php")
    Call<APIResponse> getAppVersion(@Query("os") String os);

    /**
     * 서버에서 오디오락 도서관(library) 또는 대학교(school) 목록을 읽어dhrl
     * @param sitePrefix 오디오락 사이트 프리픽스 (Yes24)
     * @param appVer 앱 버전
     * @return
     */
    @GET("/api/mobile/getLibraryList.php")
    Call<APIResponse> getAudioRacSiteList(@Query("prefix") String sitePrefix,
                                          @Query("appVer") String appVer);

    /**
     * 사용자 로그인
     * @return
     */
    @GET("/api/mobile/userLogin.php")
    Call<APIResponse> requestUserLogin(@Query("use_name") String useName,
                                       @Query("usid") String userId,
                                       @Query("usname") String userPwd,
                                       @Query("appVer") String appVer);

    @GET("/api/mobile/streamLog.php")
    Call<APIResponse> saveStreamLog(@Query("userId") String userId,
                                    @Query("volumeId") String volumeId);

    @GET("/api/mobile/getAlbumArtUrl.php")
    Call<APIResponse> getAlbumArtUrl(@Query("volumeId") String volumeId);
}
