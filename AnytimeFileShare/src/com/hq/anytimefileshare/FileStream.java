package com.hq.anytimefileshare;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.RemoteViews;

import com.hq.anytimefileshare.LocalFileListActivity.CopyThread.CopyProcess;
import com.hq.anytimefileshare.model.ModelProcess;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.ui.ActivityUI;

public class FileStream {
	NotificationManager mManager;  
	Notification notif;	
	static int mIndex = 0;
	
	public boolean copyFile(Activity activity, String src, String dst) {
		Intent intent = new Intent(activity, activity.getClass());  
        
		Bundle b = new Bundle();
		//todo b.putString(Global.LOCAL_KEY_URI, mLocalFile.getPath());
		intent.putExtras(b);
		
		PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		mManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);  
        notif = new Notification();  
        notif.icon = R.drawable.notify;
        notif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR; 
              
        //通知栏显示所用到的布局文件   
        notif.contentView = new RemoteViews(activity.getPackageName(), R.layout.notify_copyview);  
        notif.contentIntent = pIntent; 
        notif.tickerText = activity.getString(R.string.new_task);	
		
		return true;
	}
	
	public class CopyThread implements Runnable {
		Fragment mFrag;
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
							// todo mLocalHandler.post(mUpdateUI);
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
				
				mNotif.setLatestEventInfo(mFrag.getActivity(), tip2, tip1, pIntent);
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
