package com.conpo.audiorac.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class RecordParcelable implements Parcelable {
	
	private Record mData = new Record();
	
	public RecordParcelable() {
		mData = new Record();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		final int N = mData.size();
		out.writeInt(N);
		
		out.writeInt(mData.id);
		
		if (N > 0) {
			for (Map.Entry<String, String> entry : mData.entrySet()) {
				out.writeString(entry.getKey());
				out.writeString(entry.getValue());
			}
		}
	}
	
	public static final Creator<RecordParcelable> CREATOR
		= new Creator<RecordParcelable>() {
		
		public RecordParcelable createFromParcel(Parcel in) {
			return new RecordParcelable(in);
		}
		
		public RecordParcelable[] newArray(int size) {
			return new RecordParcelable[size];
		}
	};
	
	private RecordParcelable(Parcel in) {
		final int N = in.readInt();
		
		mData.id = in.readInt();
		
		for (int i = 0; i < N; i++) {
			String key = in.readString();
			String value = in.readString();
			
			mData.put(key, value);
		}
	}
	
	public void setData(Record rec) {
		mData = rec;
	}
	
	public Record getData() {
		return mData;
	}
}
