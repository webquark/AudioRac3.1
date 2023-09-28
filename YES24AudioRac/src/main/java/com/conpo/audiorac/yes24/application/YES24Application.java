package com.conpo.audiorac.yes24.application;

import com.conpo.audiorac.application.AudioRacApplication;

public class YES24Application extends AudioRacApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        CFG_AUDIORAC_SERVER_URL  = "http://www.audiorac.kr";
        CFG_AUDIORAC_SITE_PREFIX = "yes24_";
        CFG_DRM_FOLDER			 = "/Music/Yes24Audiorac/";
    }

}
