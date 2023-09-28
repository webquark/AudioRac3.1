package com.conpo.audiorac.adapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.Record;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 파일 리스트 뷰 - 파일 리스트 adapter
 * @author hansolo
 *
 */
public class FileAdapter extends AdapterBase {

	private int mNameColor = 0;
    private int mRemainColor = 0;
    private int mExpiredNameColor = 0;
    private int mExpiredRemainColor = 0;
	
	public FileAdapter(Context context, ArrayList<Record> arSrc) {
		super(context, arSrc);
		
		mNameColor = mContext.getResources().getColor(R.color.item_file_name);
	    mRemainColor = mContext.getResources().getColor(R.color.item_file_remain);
	    mExpiredNameColor = mContext.getResources().getColor(R.color.item_expired_file_name);
	    mExpiredRemainColor = mContext.getResources().getColor(R.color.item_expired_file_remain);
	}
	
	class ViewHolder {
		public TextView tvFilename;
		public TextView tvExpireDate;
		public TextView tvDuration;
		public ImageView ivIcon;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if (view == null) {
			view = mInflater.inflate(R.layout.list_item_file, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.tvFilename = (TextView)view.findViewById(R.id.tv_filename);
			viewHolder.tvExpireDate = (TextView)view.findViewById(R.id.tv_expire);
			viewHolder.tvDuration = (TextView)view.findViewById(R.id.tv_duration);
			viewHolder.ivIcon = (ImageView)view.findViewById(R.id.iv_icon);
			
			view.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		Record rec = arItems.get(position);
		
		int nameColor = mNameColor;
	    int remainColor = mRemainColor;
		
		viewHolder.tvFilename.setText( rec.get("name") );
		
		if (rec.safeGet("type").equals("folder")) {
			/*
			 * 폴더 아이템 뷰 설정
			 */
			if (rec.get("name").equals("..")) {
				viewHolder.tvExpireDate.setVisibility(View.GONE);

			} else {
				int fileCnt = Integer.parseInt( rec.get("file_cnt") );
				int expiredCnt = Integer.parseInt( rec.get("expired_cnt") );
				String remainDesc = "";
				
				if (expiredCnt > 0) {
					remainDesc = fileCnt + "개의 파일 중 " + expiredCnt + "개 이용기간 만료";
					remainColor = mExpiredRemainColor;

				} else {
					remainDesc = fileCnt + "개의 파일";
				}
				
				viewHolder.tvExpireDate.setText( remainDesc );
				viewHolder.tvExpireDate.setTextColor(remainColor);
			}
			
			viewHolder.tvDuration.setVisibility(View.GONE);
			viewHolder.ivIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_directory));

		} else {
			/*
			 * 파일 아이템 뷰 설정
			 */
			if (rec.safeGet("is_drm").equals("true")) {	// Conpo DRM 파일인 경우 - 남은 이용시간 표시
				String remainStr;
				long remain = Long.parseLong(rec.safeGet("remain"));
				
				if (remain == -1) {
					remainStr = "최초재생 전";

				} else if (remain == -2) {
					remainStr = "이용기간 제한없음";
				} else {
				    long day = TimeUnit.MILLISECONDS.toDays(remain);
				    long hour = TimeUnit.MILLISECONDS.toHours(remain);
				    long min = TimeUnit.MILLISECONDS.toMinutes(remain);
				    long sec = TimeUnit.MILLISECONDS.toSeconds(remain);
					
				    if (day > 0) {
				    	hour -= day*24;
				    	remainStr = "약 " + day + "일 " + hour + "시간 남음";

				    } else if (hour > 0) {
				    	min -= hour *60;
				    	remainStr = "약 " + hour + "시간 " + min + "분 남음";

				    } else if (min > 0) {
				    	sec -= min * 60;
				    	remainStr = "약 " + min + "분 " + sec + "초 남음";

				    } else {
				    	remainStr = "이용기간 만료";
				    	nameColor = mExpiredNameColor;
					    remainColor = mExpiredRemainColor;
				    }
				}
				
			    viewHolder.tvFilename.setTextColor(nameColor);
			    
				viewHolder.tvExpireDate.setVisibility(View.VISIBLE);
				viewHolder.tvExpireDate.setText(remainStr);
				viewHolder.tvExpireDate.setTextColor(remainColor);
			} else {
				viewHolder.tvExpireDate.setVisibility(View.GONE);
			}

			viewHolder.tvDuration.setText(rec.safeGet("duration"));
			
			viewHolder.tvDuration.setVisibility(View.VISIBLE);
			viewHolder.ivIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_mp3));
		}
		
		return view;
	}
}
