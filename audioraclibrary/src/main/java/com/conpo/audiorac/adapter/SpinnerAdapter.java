package com.conpo.audiorac.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.Record;

/**
 * 
 * @author hansolo
 *
 */
public class SpinnerAdapter extends AdapterBase {
	public SpinnerAdapter(Context context, ArrayList<Record> arSrc) {
		super(context, arSrc);
	}
	
	class ViewHolder {
		public TextView tvItemName;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if (view == null) {
			view = mInflater.inflate(R.layout.spinner_view, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.tvItemName = (TextView)view.findViewById(R.id.tv_name);
			
			view.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		Record rec = arItems.get(position);
		
		viewHolder.tvItemName.setText( Html.fromHtml(rec.safeGet("name")).toString(), BufferType.SPANNABLE );
		
		return view;
	}



	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		
		if (view == null) {
			view = mInflater.inflate(R.layout.spinner_dropdown_view, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.tvItemName = (TextView)view.findViewById(R.id.tv_name);
			
			view.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		Record rec = arItems.get(position);
		
		viewHolder.tvItemName.setText( Html.fromHtml(rec.safeGet("name")).toString(), BufferType.SPANNABLE );
		
		return view;
	}
	
	
}
