package com.conpo.audiorac.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.conpo.audiorac.model.ModelBase;
import com.conpo.audiorac.model.Record;
import com.google.gson.internal.LinkedTreeMap;

public class AdapterBase extends BaseAdapter {
	protected Context mContext;
	protected LayoutInflater mInflater;
	protected ArrayList<Record> arItems = new ArrayList<>();

	public AdapterBase(Context context, ArrayList<Record> arSrc) {
		this.mContext = context;
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (arSrc != null) {
			this.arItems = arSrc;
		}
	}
	
	@Override
	public int getCount() {
		return arItems.size();
	}

	@Override
	public Record getItem(int position) {
		return arItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
}
