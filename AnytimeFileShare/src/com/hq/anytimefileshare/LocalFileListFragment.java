package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.HashMap;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.LocalFile;
import com.hq.anytimefileshare.model.ModelProcess;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.model.dao.FileInfo;
import com.hq.anytimefileshare.model.dao.RemoteInfo;
import com.hq.anytimefileshare.ui.ActivityUI;
import com.hq.anytimefileshare.ui.ChkListAdapter;
import com.hq.anytimefileshare.ui.UiBaseActivity;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.RemoteViews;



public class LocalFileListFragment extends Fragment {
	ChkListAdapter mAdapter = null;
	ArrayList<FileInfo> mFileList = null;
	ProgressDialog mProDlg = null;
	LocalFile mLocalFile = null;
	String mLocalUri = null;
	Intent mIntent = null;
	Bundle mBundle = null;	
	static final Handler mLocalHandler = new Handler();
	NotificationManager manager;  
    Notification notif;	
    static int mIndex = 0;
    ListView mListView = null;
    
    final Runnable mUpdateUI = new Runnable() {
    	public void run() {
    		//updateListView(mFileNameList.get(i));
    		mAdapter.notifyDataSetChanged();
    	}
    };
    
    public static LocalFileListFragment getNewInstance(String path) {
    	LocalFileListFragment lff = new LocalFileListFragment();
		Bundle b = new Bundle();		
		b.putString(Global.LOCAL_KEY_URI, path);
		lff.setArguments(b);
		
		return lff;
    }
    
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null)
			return null;
		
		
	
		View v = inflater.inflate(R.layout.filelist_main, container, false); 
		
		v.findViewById(R.id.checkAll).setVisibility(View.INVISIBLE);
		v.findViewById(R.id.textChoiceAll).setVisibility(View.INVISIBLE);
		Button leftBtn = (Button)v.findViewById(R.id.btnCopy);
		leftBtn.setText(R.string.paste);
		/*
		mIntent = v.getIntent();
		mBundle = mIntent.getExtras();
		if (mBundle != null) {
			mLocalUri = mBundle.getString(Global.LOCAL_KEY_URI);	
		}
		*/
		if (getArguments() != null) {
			mLocalUri = getArguments().getString(Global.LOCAL_KEY_URI);
		}
		try {
			InitLocalFile(mLocalUri);
		} catch (Exception e) {
			Log.e("LocalFileListActivity", "Get local file exception:" + e.getMessage());
			MainActivity.showWarmMsg(this, e.getMessage());
			return null;
		}
		
		((TextView)v.findViewById(R.id.textPath)).setText(mLocalFile.getPath());
		if (mListView == null) {
			mFileList = mLocalFile.getLocalFileInfo();		
			if (mFileList == null) {
				Log.e("LocalFileListActivity", "Get local file list fail");
				MainActivity.showWarmMsg(this, "Get local file list fail");
				return null;
			}
		}
		
		mAdapter = new ChkListAdapter(v.getContext(),
				mFileList);
		mAdapter.setCheckedHide();
		mListView = (ListView)v.findViewById(R.id.fileListView);	
		mListView.setAdapter(mAdapter);
	
		ImageButton btnUp = (ImageButton)v.findViewById(R.id.imageBtnUp);
		btnUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickUp(v);
			}
		});
		
		return v;
	
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
			mLocalFile = new LocalFile(mLocalUri);
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e("LocalFileListActivity", "Init local file exception:" + e.getMessage());
			throw e;
		}
	}
    
    private void onClickUp(View view) {
		String path = mLocalFile.getPath();
		String[] arrayPath = path.split("/");
		if (arrayPath.length <= 1) {
			return;
		}
		
		path = path.substring(0, path.lastIndexOf(arrayPath[arrayPath.length - 1]));
		Log.i("path1", path);
		
		//gotoNextLocalActivity(path);
		LocalFileListFragment lff = getNewInstance(path);
		MainActivity.changeFragment(lff, true);
	}

}
