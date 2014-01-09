package com.hq.anytimefileshare.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.dao.FileInfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ChkListAdapter extends BaseAdapter {
	Context context;
	boolean isChkInvisible;
	ArrayList<FileInfo> mList;
	HashMap<Integer, View> map = new HashMap<Integer, View>();
	HashMap<Integer, Boolean> mState = new HashMap<Integer, Boolean>();
	LayoutInflater mInflater;
	
	public ChkListAdapter(Context c, ArrayList<FileInfo> list) {
		super();
		
		isChkInvisible = false;
		context = c;
		mInflater = LayoutInflater.from(context);
		this.mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<Integer> getCheckedListIndex() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for (int i = 0; i < mList.size(); i++) {
			if (mState.get(i) == null) {
				continue;
			}
			list.add(i);
		}
		return list;
	}
	
	public void setCheckedHide() {
		isChkInvisible = true;
	}
	
	public void setCheckVisible() {
		isChkInvisible = false;
	}
	
	public void setAllCheck(boolean b) {
		for (int i = 0; i < mList.size(); i++) {
			//isChkedList.set(i, b);
			if (b) {
				mState.put(i, b);
			} else {
				mState.remove(i);
			}
		}
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		convertView = null;		
		if (convertView == null) {
			Log.i("ChkListAdapter", "Add view, listview position:" + position);
			 
			convertView = mInflater.inflate(R.layout.filelistitem, null);
			
			holder = new ViewHolder();
			holder.cb = (CheckBox)convertView.findViewById(R.id.checkFile);
			if (isChkInvisible) {
				holder.cb.setVisibility(View.INVISIBLE);
			}
			holder.fileName = (TextView)convertView.findViewById(R.id.fileName);
			holder.fileImg = (ImageView)convertView.findViewById(R.id.imageFile);
			convertView.setTag(holder);
			map.put(position, convertView);
			
			holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					//isChkedList.set(p, isChecked);	
					if (isChecked) {
						mState.put(position, isChecked);
					} else {
						mState.remove(position);
					}
				}
			});
			
		} else {
			Log.i("ChkListAdapter", "Get view listview position:" + position);
			holder = (ViewHolder)convertView.getTag();
		}
		
		FileInfo fileInfo = mList.get(position);
		holder.fileName.setText(fileInfo.getFileName());
		holder.cb.setChecked(mState.get(position) == null ? false:true);
		holder.isDirectory = fileInfo.isDirectory();
		if (holder.isDirectory) {
			holder.fileImg.setImageResource(R.drawable.folder_img);
		} else {
			holder.fileImg.setImageResource(R.drawable.file_img);
		}
		
		return convertView;
	}
	
	
	public class ViewHolder {
		TextView fileName;
		boolean isDirectory;
		CheckBox cb;
		ImageView fileImg;
	
		public String getFileName() {
			return fileName.getText().toString();
		}
		
		public boolean isDirectory() {
			return isDirectory;
		}
	}

}
