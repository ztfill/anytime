package com.hq.anytimefileshare;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.FileBase;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.ui.ChkListAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RemoteFileListFragment extends FragmentBase {
	Handler h = null;
	Intent mIntent;
	Bundle mBundle;
	View mView;
	
	public static FragmentBase getNewInstance(String path) {
		return (new RemoteFileListFragment()).getNewInstanceByPath(path);
	}
	
	public FragmentBase getNewInstanceByPath(String path) {
		FragmentBase rflf = new RemoteFileListFragment();
		Bundle b = new Bundle();		
		b.putString(Global.REMOTE_KEY_URI, path);
		rflf.setArguments(b);
		
		return rflf;
	}
	
	FileBase getNewFileInstance(String path) throws Exception {
		return new RemoteFile(path);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}	
		
		mView = super.onCreateView(inflater, container, savedInstanceState);
		h = new RemoteHandler();
		mView.findViewById(R.id.progressview).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.mainview).setVisibility(View.GONE);		
		
		try {
			InitRemoteFile();	
			//this.getActivity().findViewById(id)
			((TextView)getActivity().findViewById(R.id.textPath)).setText(mFile.getShowPath());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("RemoteFileListActivity", "Init remote file exception:" + e.getMessage());
			MainActivity.showWarmMsg(RemoteFileListFragment.this, e.getMessage());
			return null;
		}
		RemoteThread cThread = new RemoteThread();
		Thread t = new Thread(cThread);
		t.start();
		
		return mView;
	}
	
	
	protected void InitRemoteFile() throws Exception {	
		String localUri = null;
		try {
			if (getArguments() != null) {
				localUri = getArguments().getString(Global.REMOTE_KEY_URI);
			}
			mFile = new RemoteFile(localUri);
		} catch (Exception e) {
			Log.e("RemoteFileListActivity", "Init remote file exception:" + e.getMessage());
			throw e;
		}
	}
		
	String getUpPath() {
		String path;
		try {
			path = mFile.getPath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String[] arrayPath = path.split(Global.DIRECTORY_SPLITE_LABLE);
		
		if (arrayPath.length <= 3) {
			return null;
		}
			
		path = path.substring(0, path.lastIndexOf(arrayPath[arrayPath.length - 1]));
		Log.i("path1", path);
		
		return path;
	}
	
	void InitFileByPath(String path) throws Exception {
    	try {
    		mFile = new RemoteFile(path);
    	} catch (Exception e) {
    		e.printStackTrace();
    		Log.e("RemoteFileListFragment.InitFileByPath", "Init file fail:" + e.getMessage());
    		throw e;
    	}
    }
		
	class RemoteThread implements Runnable {
		void getRemoteFile() {
			Message msg = new Message();
			Bundle b = new Bundle();
			
			try {
				if (mFileList == null) {
					mFileList = mFile.getFileInfo();
				}
				b.putInt(Global.HANDLER_RESULT, Global.ERRNO_SUCCESS);
			} catch (Exception e) {
				Log.e("RemoteFileListActivity", "Get file exception:" + e.getMessage());
				b.putInt(Global.HANDLER_RESULT, Global.ERRNO_FAIL);
				b.putString(Global.HANDLER_MSG, e.getMessage());
			}			
			
			msg.setData(b);
			h.sendMessage(msg);
		}
		
		public void run() {
			getRemoteFile();
		}
	}
	
	class RemoteHandler extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Bundle b = msg.getData();
			int result = b.getInt(Global.HANDLER_RESULT);
			switch (result) {
			case Global.ERRNO_SUCCESS:	
				mAdapter = new ChkListAdapter(mView.getContext(),
						mFileList);
				mListView.setAdapter(mAdapter); 				
				
				mView.findViewById(R.id.progressview).setVisibility(View.GONE);
				mView.findViewById(R.id.mainview).setVisibility(View.VISIBLE);
				
				CheckBox chkAll = (CheckBox)mView.findViewById(R.id.checkAll);
				chkAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						mAdapter.setAllCheck(isChecked);
						mAdapter.notifyDataSetChanged();
					}
				});
				
				break;
			case Global.ERRNO_FAIL:
				MainActivity.showWarmMsg(RemoteFileListFragment.this, b.getString(Global.HANDLER_MSG));
				break;
			default:
				break;
			}
		}
	}

}
