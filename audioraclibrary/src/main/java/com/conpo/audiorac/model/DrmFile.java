package com.conpo.audiorac.model;

public class DrmFile extends ModelBase {
    public String name = "";
    public String album = "";

    public String url = "";
    public String path = "";
    public String albumPath = "";

    public int fileCnt = 0;
    public int expiredCnt = 0;
    public String type = "file";
    public boolean isDrmFile = false;
    public String duration = "0";
    /**
     * 잔여권한 정보
     *   -2: 무제한, -1:최초재생 전, 0:만료, 0보다 크면 플레이 가능
     */
    public long remain = 0;

    public String courseId = null;
    public String chapterId = null;
}
