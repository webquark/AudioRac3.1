package com.conpo.audiorac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.DrmFile;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.conpo.audiorac.model.ModelBase;

/**
 * 파일 리스트 뷰 - 파일 리스트 adapter
 * @author hansolo
 *
 */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

	private Context mContext;
	private ArrayList<DrmFile> mItems;

	private int mNameColor = 0;
    private int mRemainColor = 0;
    private int mExpiredNameColor = 0;
    private int mExpiredRemainColor = 0;

	private OnItemClickListener mOnItemClickListener;

	public interface OnItemClickListener {
		void onItemClick(ModelBase item);
		void onFolderMenu(ModelBase item);
		void onFileMenu(ModelBase item);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewGroup contentFrame;
		public TextView tvFilename;
		public TextView tvExpireDate;
		public TextView tvDuration;
		public ImageView ivIcon;

		public ViewHolder(View view) {
			super(view);

			contentFrame = view.findViewById(R.id.content_frame);
			tvFilename = view.findViewById(R.id.tv_filename);
			tvExpireDate = view.findViewById(R.id.tv_expire);
			tvDuration = view.findViewById(R.id.tv_duration);
			ivIcon = view.findViewById(R.id.iv_icon);
		}
	}

	public FileListAdapter(Context context, ArrayList<DrmFile> items) {
		mContext = context;
		mItems = items;

		mNameColor = mContext.getColor(R.color.item_file_name);
		mRemainColor = mContext.getColor(R.color.item_file_remain);
		mExpiredNameColor = mContext.getColor(R.color.item_expired_file_name);
		mExpiredRemainColor = mContext.getColor(R.color.item_expired_file_remain);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_file, parent, false);

		return new ViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		DrmFile drmFile = mItems.get(position);

		int nameColor = mNameColor;
		int remainColor = mRemainColor;

		holder.tvFilename.setText( drmFile.name );

		if (drmFile.type.equals("folder")) {
			/*
			 * 폴더 아이템 뷰 설정
			 */
			if (drmFile.name.equals("..")) {
				holder.tvExpireDate.setVisibility(View.INVISIBLE);

			} else {
				int fileCnt = drmFile.fileCnt;
				int expiredCnt = drmFile.expiredCnt;
				String remainDesc = mContext.getString(R.string.drm_remain_folder_2,fileCnt);

				if (expiredCnt > 0) {
					remainDesc = mContext.getString(R.string.drm_remain_folder_1, fileCnt, expiredCnt);
					remainColor = mExpiredRemainColor;
				}

				holder.tvExpireDate.setText( remainDesc );
				holder.tvExpireDate.setTextColor(remainColor);
			}

			holder.tvDuration.setVisibility(View.INVISIBLE);
			Glide.with(mContext).load(R.drawable.ico_folder).into(holder.ivIcon);

		} else {
			/*
			 * 파일 아이템 뷰 설정
			 */
			if (drmFile.isDrmFile) {    // Conpo DRM 파일인 경우 - 남은 이용시간 표시
				String remainStr;
				long remain = drmFile.remain;

				if (remain == -1) {
					remainStr = mContext.getString(R.string.drm_remain_not_used);

				} else if (remain == -2) {
					remainStr = mContext.getString(R.string.drm_remain_no_limit);
				} else {
					long day = TimeUnit.MILLISECONDS.toDays(remain);
					long hour = TimeUnit.MILLISECONDS.toHours(remain);
					long min = TimeUnit.MILLISECONDS.toMinutes(remain);
					long sec = TimeUnit.MILLISECONDS.toSeconds(remain);

					if (day > 0) {
						hour -= day * 24;
						remainStr = mContext.getString(R.string.drm_remain_file_1, day, hour);

					} else if (hour > 0) {
						min -= hour * 60;
						remainStr = mContext.getString(R.string.drm_remain_file_2, hour, min);

					} else if (min > 0) {
						sec -= min * 60;
						remainStr = mContext.getString(R.string.drm_remain_file_3, min, sec);

					} else {
						remainStr = mContext.getString(R.string.drm_remain_expired);
						nameColor = mExpiredNameColor;
						remainColor = mExpiredRemainColor;
					}
				}

				holder.tvFilename.setTextColor(nameColor);

				holder.tvExpireDate.setVisibility(View.VISIBLE);
				holder.tvExpireDate.setText(remainStr);
				holder.tvExpireDate.setTextColor(remainColor);
			} else {
				holder.tvExpireDate.setVisibility(View.GONE);
			}

			holder.tvDuration.setText(drmFile.duration);
			holder.tvDuration.setVisibility(View.VISIBLE);
			Glide.with(mContext).load(R.drawable.ico_audio).into(holder.ivIcon);
		}

		holder.contentFrame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(drmFile);
				}
			}
		});

		View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mOnItemClickListener != null) {
					if (drmFile.type.equals("folder")) {
						mOnItemClickListener.onFolderMenu(drmFile);
					} else {
						mOnItemClickListener.onFileMenu(drmFile);
					}
				}
				return true;
			}
		};

		if (!drmFile.name.equals("..")) {
			holder.contentFrame.setOnLongClickListener(longClickListener);
		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}
}
