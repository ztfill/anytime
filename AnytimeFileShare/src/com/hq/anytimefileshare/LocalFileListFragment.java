package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.HashMap;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.FileBase;
import com.hq.anytimefileshare.model.LocalFile;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.model.dao.FileInfo;
import com.hq.anytimefileshare.model.dao.RemoteInfo;
import com.hq.anytimefileshare.ui.ActivityUI;
import com.hq.anytimefileshare.ui.ChkListAdapter;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.RemoteViews;
import android.widget.Toast;



public class LocalFileListFragment extends FragmentBase {
	String mLocalUri = null;
	static final Handler mLocalHandler = new Handler();	
    
    public static FragmentBase getNewInstance(String path) {
		return (new LocalFileListFragment()).getNewInstanceByPath(path);
	}
	
	public FragmentBase getNewInstanceByPath(String path) {
		FragmentBase lff = new LocalFileListFragment();
		Bundle b = new Bundle();		
		b.putString(Global.LOCAL_KEY_URI, path);
		lff.setArguments(b);
		
		return lff;
	}
	
	FileBase getNewFileInstance(String path) throws Exception {
		return new LocalFile(path);
	}
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}		
		
		mView = super.onCreateView(inflater, container, savedInstanceState);
		if (getArguments() != null) {
			mLocalUri = getArguments().getString(Global.LOCAL_KEY_URI);
		}
		try {
			InitFileByPath(mLocalUri);
			((TextView)getActivity().findViewById(R.id.textPath)).setText(mFile.getShowPath());
		} catch (Exception e) {
			Log.e("LocalFileListActivity", "Get local file exception:" + e.getMessage());
			MainActivity.showWarmMsg(this, e.getMessage());
			return null;
		}
		
		
		try {
			mFileList = mFile.getFileInfo();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("LocalFileListActivity", "Get local file info fail");
			MainActivity.showWarmMsg(this, "Get local file info fail");
			return null;
		}		
		if (mFileList == null) {
			Log.e("LocalFileListActivity", "Get local file list fail");
			MainActivity.showWarmMsg(this, "Get local file list fail");
			return null;
		}
		
		mAdapter = new ChkListAdapter(mView.getContext(),
				mFileList);	
		mListView.setAdapter(mAdapter);	
		
		CheckBox chkAll = (CheckBox)mView.findViewById(R.id.checkAll);
		chkAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mAdapter.setAllCheck(isChecked);
				mAdapter.notifyDataSetChanged();
			}
		});
		
		return mView;
	
	}

    private void InitLocalFile(final String uri) throws Exception {		
		mLocalUri = uri;
		if (mLocalUri == null) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.e("FileListActivity", "Don't find SDCard");
				//throw new Exception("Don't find SDCard");
				mLocalUri = "/";
			} else {
				mLocalUri = Environment.getExternalStorageDirectory().getPath();
			}
		}
		
		try {
			mFile = new LocalFile(mLocalUri);
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e("LocalFileListActivity", "Init local file exception:" + e.getMessage());
			throw e;
		}
	}
    
    String getUpPath() {
    	String path;
		try {
			path = mFile.getPath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		String[] arrayPath = path.split("/");
		if (arrayPath.length <= 1) {
			return null;
		}
		
		path = path.substring(0, path.lastIndexOf(arrayPath[arrayPath.length - 1]));
		Log.i("path1", path);
		
		return path;
    }
    
    void InitFileByPath(String path) throws Exception {
    	try {
    		InitLocalFile(path);
    	} catch (Exception e) {
    		e.printStackTrace();
    		Log.e("LocalFileListFragment.InitFileByPath", "Init file fail:" + e.getMessage());
    		throw e;
    	}
    }
}
