package com.conpo.audiorac.player;

import java.util.Calendar;

import com.conpo.audiorac.application.Common;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


public class PlayerCloseEvent {

	private static PlayerCloseEvent _instance;

	private static Handler g_handler_backkey;
	private static boolean g_IsBackKeyPressed = false; 
	private static long g_lCurTime = 0;
	
	static {
		_instance = new PlayerCloseEvent();
	}
	
	public static PlayerCloseEvent getInstance() {
		return _instance;
	}
	
	public static boolean Handler(Context context) {
		
		// Back key press Handler
		g_handler_backkey = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Common.MSG_TIMER_EXPIRED: 
					g_IsBackKeyPressed = false;
					break;
				}
			}
		};
		
		if(g_IsBackKeyPressed == false) {
			g_IsBackKeyPressed = true;
			g_lCurTime = Calendar.getInstance().getTimeInMillis();
			Toast.makeText(context, "플레이어를 종료하려면 뒤로가기 버튼을 한번 더 누르세요.", Toast.LENGTH_SHORT).show();
			
			g_handler_backkey.sendEmptyMessageDelayed(Common.MSG_TIMER_EXPIRED, Common.BACKKEY_TIMEOUT * Common.MILLIS_IN_SEC);
		}
		else {
			g_IsBackKeyPressed = false;
			
			if(Calendar.getInstance().getTimeInMillis() <= (g_lCurTime + (Common.BACKKEY_TIMEOUT * Common.MILLIS_IN_SEC))) {
				return true;
			}
		}
		
		return false;
	}
}
