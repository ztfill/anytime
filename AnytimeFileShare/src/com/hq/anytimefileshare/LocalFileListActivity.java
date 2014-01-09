package com.hq.anytimefileshare;

import java.util.ArrayList;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.LocalFile;
import com.hq.anytimefileshare.model.ModelProcess;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.dao.FileInfo;
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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RemoteViews;



public class LocalFileListActivity extends UiBaseActivity {
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
    
    final Runnable mUpdateUI = new Runnable() {
    	public void run() {
    		//updateListView(mFileNameList.get(i));
    		mAdapter.notifyDataSetChanged();
    	}
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist_main);	
		
		Activity ac = ActivityUI.getPrevActivity();
		if (ac instanceof LocalFileListActivity) {
			((LocalFileListActivity)ac).finish();
		}
		
		findViewById(R.id.checkAll).setVisibility(View.INVISIBLE);
		findViewById(R.id.textChoiceAll).setVisibility(View.INVISIBLE);
		Button leftBtn = (Button)findViewById(R.id.btnCopy);
		leftBtn.setText(R.string.paste);
		
		mIntent = getIntent();
		mBundle = mIntent.getExtras();
		if (mBundle != null) {
			mLocalUri = mBundle.getString(Global.LOCAL_KEY_URI);	
		}
				
		try {
			InitLocalFile(mLocalUri);
		} catch (Exception e) {
			Log.e("LocalFileListActivity", "Get local file exception:" + e.getMessage());
			showWarmMsg(e.getMessage());
			return;
		}
		
		((TextView)findViewById(R.id.textPath)).setText(mLocalFile.getPath());
		mFileList = mLocalFile.getLocalFileInfo();		
		if (mFileList == null) {
			showWarmMsg("Get local file list fail");
			return;
		}
		
		mAdapter = new ChkListAdapter(getApplicationContext(),
				mFileList);
		mAdapter.setCheckedHide();
		ListView listView = (ListView)findViewById(R.id.fileListView);	
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				FileInfo fileInfo = (FileInfo)mAdapter.getItem(arg2);
				if (!fileInfo.isDirectory()) {
					return;
				}
				
				gotoNextLocalActivity(mLocalFile.getPath() + Global.DIRECTORY_SPLITE_LABLE 
									+ fileInfo.getFileName() + Global.DIRECTORY_SPLITE_LABLE);
			}
		});			
	}	
	
	
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void InitLocalFile(final String uri) throws Exception {		
		mLocalUri = uri;
		if (mLocalUri == null) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.e("FileListActivity", "Don't find SDCard");
				throw new Exception("Don't find SDCard");
			}
			mLocalUri = Environment.getExternalStorageDirectory().getPath();
		}
		
		try {
			mLocalFile = new LocalFile(mLocalUri);
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e("LocalFileListActivity", "Init local file exception:" + e.getMessage());
			throw e;
		}
	}
	 
	
	void startNextLocalActivity(Bundle bundle) {
		mIntent = new Intent(LocalFileListActivity.this, LocalFileListActivity.class);
		mIntent.putExtras(bundle);
		startActivity(mIntent);
		this.finish();
	}
	
	void gotoNextActivity() {
		mIntent = new Intent(LocalFileListActivity.this, RemoteFileListActivity.class);
		startActivity(mIntent);
		finish();
	}
	
	void gotoNextLocalActivity(String path) {
		mBundle = new Bundle();
		mBundle.putString(Global.LOCAL_KEY_URI,path);
		startNextLocalActivity(mBundle);
	}
	

	
	public void onClickLeftBtn(View view) {
		Intent intent = new Intent(LocalFileListActivity.this, LocalFileListActivity.class);  
        
		Bundle b = new Bundle();
		b.putString(Global.LOCAL_KEY_URI, mLocalFile.getPath());
		intent.putExtras(b);
        PendingIntent pIntent = PendingIntent.getActivity(LocalFileListActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);  
        notif = new Notification();  
        notif.icon = R.drawable.notify;
        notif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR; 
              
        //通知栏显示所用到的布局文件   
        notif.contentView = new RemoteViews(getPackageName(), R.layout.notify_copyview);  
        notif.contentIntent = pIntent; 
        notif.tickerText = getString(R.string.new_task);	
        CopyThread cThread = new CopyThread(Global.getFileNameList(),
        		mLocalFile.getPath(), Global.getRemoteUri(), manager, notif);
        if (!cThread.setNotifyIndex()) {
        	showWarmMsg(getResources().getString(R.string.prompt_error_max_copynum));
        	return;
        }
        
		Thread t = new Thread(cThread);
        t.start();
	}
	
	public void onClickUp(View view) {
		String path = mLocalFile.getPath();
		String[] arrayPath = path.split("/");
		if (arrayPath.length <= 1) {
			return;
		}
		
		path = path.substring(0, path.lastIndexOf(arrayPath[arrayPath.length - 1]));
		Log.i("path1", path);
		
		gotoNextLocalActivity(path);
	}
	
	 void updateListView(final String uri) throws Exception {
		mFileList.clear();
		try {
			InitLocalFile(uri);
			mLocalFile.getLocalFileInfo(mFileList);	
		} catch (Exception e) {
			Log.e("updateListView", "Update list view is fail.");
			throw e;
		}
	}
	
	String getLocalPath() {
		return mLocalUri;
	}
	 
	public class CopyThread implements Runnable {
		NotificationManager mManager;  
	    Notification mNotif;	
	    ArrayList<String> mFileNameList;
	    String mLocalUriThread;
	    String mRemoteUri;
	    int mNotifyIndex = NOTIFY_INDEX_INVALID;	    
	    static final int NOTIFY_INDEX_INVALID = -1;
	    static final int NOTIFY_INDEX_0 = 0x1;
	    static final int NOTIFY_INDEX_1 = 0x2;
	    static final int NOTIFY_INDEX_2 = 0x4;
	    
	    CopyThread( ArrayList<String> list, String localUri, String remoteUri,
	    		NotificationManager manager, Notification notif) {
	    	mFileNameList = list;
	    	mLocalUriThread =localUri;
	    	mRemoteUri = remoteUri;
	    	mManager = manager;
	    	mNotif = notif;
	    	
	    }
	    
	    synchronized boolean setNotifyIndex() {
	    	boolean ret = true;
	    	
	    	Log.d("CopyThread", "mIndex:" + mIndex);
	    	if ((mIndex & NOTIFY_INDEX_0) != NOTIFY_INDEX_0) {
	    		mNotifyIndex = 0;
	    		mIndex = mIndex | NOTIFY_INDEX_0;
	    	} else if ((mIndex & NOTIFY_INDEX_1) != NOTIFY_INDEX_1) {
	    		mNotifyIndex = 1;
	    		mIndex = mIndex | NOTIFY_INDEX_1;
	    	} else if ((mIndex & NOTIFY_INDEX_2) != NOTIFY_INDEX_2) {
	    		mNotifyIndex = 2;
	    		mIndex = mIndex | NOTIFY_INDEX_2;
	    	} else {
	    		ret = false;
	    	}    	
	    	
	    	return ret;
	    }
	    
	    synchronized boolean resumeNotifyIndex(int notifyIndex) {
	    	boolean ret = true;
	    	
	    	switch (notifyIndex) {
	    	case 0:
	    		mIndex &= ~NOTIFY_INDEX_0;
	    		break;
	    	case 1:
	    		mIndex &= ~NOTIFY_INDEX_1;
	    		break;
	    	case 2:
	    		mIndex &= ~NOTIFY_INDEX_2;
	    		break;
	    	default:
	    		ret = false;
	    		break;
	    	}
	    	return ret;
	    }
		
		void copyFile() {			
			Log.i("FileListActivity", "Copy remote files to local path:" + mLocalUriThread);		
			int step = Global.PROGRESS_MAX / mFileNameList.size();
			String tip1 = "复制失败", tip2 = "";
			ModelProcess mp = new CopyProcess();
			try {					
				for (int  i = 0; i < mFileNameList.size(); i++) {
					//mNotif.tickerText = "正在复制：" + mFileNameList.get(i);
					notif.contentView.setTextViewText(R.id.textCopyFile, mFileNameList.get(i)); 
					mManager.notify(mNotifyIndex, mNotif); 
					if (i > 0) {
						tip2 += ",";
					}
					tip2 += mFileNameList.get(i);
					RemoteFile from = new RemoteFile(mRemoteUri + mFileNameList.get(i));
					from.copyToLocal(mLocalUriThread, mp, step);
					
					Activity ac = ActivityUI.getCurrentActivity();
					if (ac instanceof LocalFileListActivity) {
						LocalFileListActivity localAc = (LocalFileListActivity)ac;
						if (localAc.getLocalPath() == mLocalUriThread) {
							localAc.updateListView(mLocalUriThread);
							mLocalHandler.post(mUpdateUI);
						}
					}					
				}
				
				mNotif.contentView.setProgressBar(R.id.content_view_progress, Global.PROGRESS_MAX, Global.PROGRESS_MAX, false);  
				mManager.notify(mNotifyIndex, mNotif); 
				
				tip1 = "复制成功";
			} catch (Exception e) {
				Log.e("FileListActivity.CopyThread", "Copy to local exception:" + e.getMessage());
				tip1 = "复制失败:" + e.getMessage();
			} finally {
				mManager.cancelAll();
				PendingIntent pIntent = mNotif.contentIntent;
				mNotif = new Notification(mNotif.icon, tip1, System.currentTimeMillis());
				mNotif.tickerText = tip1;
				mNotif.flags |= Notification.FLAG_AUTO_CANCEL;
				
				mNotif.setLatestEventInfo(LocalFileListActivity.this, tip2, tip1, pIntent);
				mManager.notify(mNotifyIndex, mNotif); 
				
				resumeNotifyIndex(mNotifyIndex);				
			}
				
		}
		
		public void run() {
			mManager.notify(mNotifyIndex, mNotif);
			copyFile();
		}
		
		class CopyProcess implements ModelProcess {
			long mCurrentTime = System.currentTimeMillis();
			int mStep = 0;
			int mLimitStep = 0;
			

			
			public void copyToLocalIncProcess(int step) {
				if (mStep > Global.PROGRESS_MAX) {
					return;
				} 
				mStep += step;
				if (mStep > Global.PROGRESS_MAX) {
					mStep = Global.PROGRESS_MAX;
				} 
				
				if ((mCurrentTime + 1000) > System.currentTimeMillis()) {
					return;
				}
				
				mCurrentTime = System.currentTimeMillis();
				mNotif.contentView.setProgressBar(R.id.content_view_progress, Global.PROGRESS_MAX, mStep, false);  
	            mManager.notify(mNotifyIndex, mNotif); 
	            
			}
		}
	}
}
