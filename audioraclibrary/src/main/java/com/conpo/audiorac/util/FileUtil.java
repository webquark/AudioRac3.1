package com.conpo.audiorac.util;

import android.util.Log;

import java.io.File;
import java.util.Arrays;

/**
 * 파일관련 유틸리티
 */
public class FileUtil {

    private static final String LOG_TAG = "FileUtil";

    public static void makeFolder(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            try {
                boolean res = file.mkdirs();
                if (!res)
                    Log.d(LOG_TAG, "Make folder failed " + folderPath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean createFile(File file) {
        try {
            return file.createNewFile();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 지정한 폴더의 모든 하위 폴더 및 파일을 리스팅 한다
     * @param folderPath 폴더의 경로
     * @return
     */
    public static File[] listFiles(String folderPath) {
        File file = new File(folderPath);
        File[] files = file.listFiles();

        return files;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    public static boolean deleteFolder(String path) {
        File dir = new File(path);

        try {
            if (dir.isDirectory()) {
                for (File child : dir.listFiles()) {
                    deleteFolder(child.getPath());
                }
            }

            dir.delete();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 지정한 경로의 부모 폴더 경로를 리턴
     * @param path full path
     * @return 부모 폴더의 full path
     */
    public static String getParentFolderPath(String path) {
        String folderList[] = path.split("/");

        folderList = Arrays.copyOf(folderList, folderList.length-1);

        String result = "";

        for (String folder : folderList) {
            result += folder + "/";
        }

        return result;
    }

    /**
     * 경로명 검증
     * @return
     */
    public static String validatePath(String path) {
        if (path.length() == 0) {
            path = "/";

        } else if (!path.endsWith("/")) {
            path += "/";
        }

        return path;
    }
}
