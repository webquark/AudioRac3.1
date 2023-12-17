package com.conpo.audiorac.application;

import com.conpo.audiorac.model.DrmFile;
import com.conpo.audiorac.model.Record;

import java.util.ArrayList;

/**
 * 미디어 플레이어의 플레이리스트 관리
 * @author hansolo
 *
 */
public class PlayList {

	/**
	 * 현재 플레이 중인 파일의 인덱스
	 */
	private static int mPlayIndex = -1;

	/**
	 * 플레이 리스트
	 */
	private static ArrayList<DrmFile> mPlayList = new ArrayList<>();
	
	public static void setPlayIndex(int index) {
		mPlayIndex = index;
	}
	
	public static int getPlayIndex() {
		return mPlayIndex;
	}
	
	public static void resetPlayList() {
		mPlayList.clear();
		mPlayIndex = 0;
	}
	
	public static int size() {
		return mPlayList.size();
	}
	
	/**
	 * 전체 플레이리스트 리턴
	 * @return
	 */
	public static ArrayList<DrmFile> getPlayList() {
		return mPlayList;
	}
	
	public static DrmFile getFile(int index) {
		return mPlayList.get(index);
	}
	
	public static boolean isNextAvailable() {
		return (size() > (mPlayIndex+1));
	}
	
	public static int moveNext() {
		if (isNextAvailable()) {
			return ++mPlayIndex;

		} else {
			return -1;
		}
	}
	
	public static DrmFile getCurrentFile() {
		if (mPlayIndex < 0) {
			return null;
		} else {
			return mPlayList.get(mPlayIndex);
		}
	}
	
	/**
	 * 플레이 리스트에 파일 추가
	 * @param drmFile
	 */
	public static void addFile(DrmFile drmFile) {
		mPlayList.add(drmFile);
	}

	/**
	 * 플레이 리스트에 여러 파일 추가
	 * @param drmFileList
	 */
	public static void addFiles(ArrayList<DrmFile> drmFileList) {
		for (DrmFile drmFile : drmFileList) {
			if (drmFile.type.equals("folder")) {
				if (drmFile.name.equals(".."))
					mPlayIndex--;

				continue;
			}

			mPlayList.add(drmFile);
		}
	}
}
