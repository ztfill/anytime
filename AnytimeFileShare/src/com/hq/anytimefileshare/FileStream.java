package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.RemoteViews;

import com.hq.anytimefileshare.model.FileBase;
import com.hq.anytimefileshare.model.FileProgress;

public class FileStream {
	NotificationManager mManager;  
	Notification mNotif;	
	static int mIndex = 0;
	FileBase mTo;
	Activity mActivity = null;
	
	
	public boolean copyFile(Activity activity, FileBase to) {
		mActivity = activity;
		Intent intent = new Intent(mActivity, activity.getClass());  
        
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		Bundle b = new Bundle();
		//todo b.putString(Global.LOCAL_KEY_URI, mLocalFile.getPath());
		intent.putExtras(b);
		
		PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		mManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);  
		mNotif = new Notification();  
		mNotif.icon = R.drawable.notify;
		mNotif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR; 
              
        //通知栏显示所用到的布局文件   
		mNotif.contentView = new RemoteViews(activity.getPackageName(), R.layout.notify_copyview);  
		mNotif.contentIntent = pIntent; 
		mNotif.tickerText = activity.getString(R.string.new_task);	
        CopyThread cThread = new CopyThread();
        if (!cThread.setNotifyIndex()) {
        	Log.w("FileStream.copyFile", "Only support five files copy at the same time");
        	//MainActivity.showWarmMsg(activity.getString(R.string.prompt_error_max_copynum));
        	return false;
        }
             
        mTo = to;
        Thread t = new Thread(cThread);
        t.start();
		
		return true;
	}
	
	private class CopyThread implements Runnable {
	    
	    int mNotifyIndex = NOTIFY_INDEX_INVALID;	    
	    static final int NOTIFY_INDEX_INVALID = -1;
	    static final int NOTIFY_INDEX_0 = 0x1;
	    static final int NOTIFY_INDEX_1 = 0x2;
	    static final int NOTIFY_INDEX_2 = 0x4;
	    	    
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
	    
	    ArrayList<FileBase> cloneClipboard(ArrayList<FileBase> l) {
	    	ArrayList<FileBase> list = new ArrayList<FileBase>();
	    	Iterator<FileBase> it = l.iterator();
	    	
	    	while (it.hasNext()) {
	    		list.add(it.next());
	    	}
	    	
	    	return list;
	    }
		
		void copyFile() {			
			String tip1 = mActivity.getString(R.string.copy_fail), tip2 = "";			
			
			ArrayList<FileBase> list = cloneClipboard(Global.getClipboardFileList());
			Iterator<FileBase> it = list.iterator();
			FileProgress fp = new CopyProcess();
			int step = Global.PROGRESS_MAX / list.size();
			String copyUrl = null;
			try {
				copyUrl = mTo.getPath();
				Log.i("FileStream.copyFile", "Copy remote files to local path:" + copyUrl);	 
				while (it.hasNext()) {
					FileBase from = it.next();	
					mNotif.contentView.setTextViewText(R.id.textCopyFile, from.getFileName()); 
					mManager.notify(mNotifyIndex, mNotif);
					tip2 += from.getFileName() + " ";
					FileBase rf = mTo.getNewFileInstance(mTo.getPath() + from.getFileName());
					rf.write(from, fp, step);
				}
				
				mNotif.contentView.setProgressBar(R.id.content_view_progress, Global.PROGRESS_MAX, Global.PROGRESS_MAX, false);  
				mManager.notify(mNotifyIndex, mNotif); 
				
				tip1 = mActivity.getString(R.string.copy_sucess);
			} catch (Exception e) {
				Log.e("FileStream.CopyThread", "Copy to local exception:" + e.getMessage());
				tip1 += ":" + e.getMessage();
			} finally {
				mManager.cancelAll();
				PendingIntent pIntent = mNotif.contentIntent;
				mNotif = new Notification(mNotif.icon, tip1, System.currentTimeMillis());
				mNotif.tickerText = tip1;
				mNotif.flags |= Notification.FLAG_AUTO_CANCEL;
				
				mNotif.setLatestEventInfo(mActivity, tip2, tip1, pIntent);
				mManager.notify(mNotifyIndex, mNotif); 
				
				resumeNotifyIndex(mNotifyIndex);	
				
				/* 更新文件列表 */
				Fragment uiFrag = MainActivity.getCurrentFragment();
				if (uiFrag != null) {
					if (uiFrag instanceof FragmentBase) {
						try {
							FragmentBase fragb = (FragmentBase) uiFrag;							
							if (fragb.getPath().equals(copyUrl)) {
								fragb.updateListView();
							}
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("FileStream.copyFile", "update list view fail:" + e.getMessage());
						}
						
					}
				} 
			}		
		}
		
		public void run() {
			mManager.notify(mNotifyIndex, mNotif);
			copyFile();
		}
		
		class CopyProcess implements FileProgress {
			private final static int mill = 1000;
			private final static int KILO = 1024;
			private long mLastTime = System.currentTimeMillis();
			private int mStep = 0;
			private long lastBytes = 0, curBytes = 0;
			
			CopyProcess() {	
				mNotif.contentView.setTextViewText(R.id.textProgress, "0");
				mNotif.contentView.setTextViewText(R.id.textSpeed, "0");
				mManager.notify(mNotifyIndex, mNotif);
			}
			
			public void writeProgress(int step, long incBytes) {
				if (mStep > Global.PROGRESS_MAX) {
					return;
				} 
				mStep += step;
				if (mStep > Global.PROGRESS_MAX) {
					mStep = Global.PROGRESS_MAX;
				} 
				
				curBytes += incBytes;
				
				if ((mLastTime + mill) > System.currentTimeMillis()) {
					return;
				}
				
				long currentTime = System.currentTimeMillis();
				long interval = Math.abs(currentTime - mLastTime) / mill;
				mLastTime = currentTime;
				
				long speed = (curBytes - lastBytes) / KILO / interval;
				lastBytes = curBytes;
				mNotif.contentView.setTextViewText(R.id.textProgress, String.valueOf(mStep));
				mNotif.contentView.setTextViewText(R.id.textSpeed, String.valueOf(speed));
				mNotif.contentView.setProgressBar(R.id.content_view_progress, Global.PROGRESS_MAX, mStep, false);  
				mManager.notify(mNotifyIndex, mNotif); 	            
			}
		}
	}
}
