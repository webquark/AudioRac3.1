package com.conpo.audiorac.player;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Media Player wrapper
 * @author tigerfive
 *
 */
public class CPDRMPlayer {

	private Context mContext;
	private MediaPlayer mMediaPlayer;
	
	private int mPlayState = 0; // 0: 중지, 1: 재생, 2: 일시정지
	
	public CPDRMPlayer(Context context) {
		mContext = context;
		mMediaPlayer = new MediaPlayer();
	}
	
	public void release() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}
	
	public int getPlayState() {
		return mPlayState;
	}
	
	public void setPlayState(int state) {
		mPlayState = state;
	}
	
	public int getCurrentPosition() {
		int pos = 0;
		if(mMediaPlayer != null)
			pos = mMediaPlayer.getCurrentPosition();
		return pos;
	}
	
	public int getDuration() {
		int dur = 0;
		if(mMediaPlayer != null)
			dur = mMediaPlayer.getDuration();
		return dur;
	}
	
	public boolean isPlaying() {
		boolean isPlaying = false;
		
		if(mMediaPlayer != null)
			isPlaying = mMediaPlayer.isPlaying();
		
		return isPlaying;
	}
	
	public void seekTo(int playPositionInMillisecconds) {
		if(mMediaPlayer != null)
			mMediaPlayer.seekTo(playPositionInMillisecconds);
	}
	
	public int start() {
		if(mMediaPlayer != null) {
			mMediaPlayer.start();
		}
		
		return getDuration();
	}
	
	public void pause() {
		if(mMediaPlayer != null)
			mMediaPlayer.pause();
	}
	
	public void stop() {
		pause();		
		seekTo(0);
	}

	public void reset() {
		if(mMediaPlayer != null)
			mMediaPlayer.reset();
	}
	
	public void setDataSource(FileDescriptor fd) throws IllegalArgumentException, IllegalStateException, IOException {
		if(mMediaPlayer != null)
			mMediaPlayer.setDataSource(fd);
	}
	
	public void prepare() throws IllegalStateException, IOException {
		if(mMediaPlayer != null)
			mMediaPlayer.prepare();
	}

}
