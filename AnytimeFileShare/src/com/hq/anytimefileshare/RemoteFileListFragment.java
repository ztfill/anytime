package com.hq.anytimefileshare;


import java.net.MalformedURLException;
import java.util.ArrayList;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.dao.FileInfo;
import com.hq.anytimefileshare.ui.ChkListAdapter;
import com.hq.anytimefileshare.ui.UiBaseActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;


public class RemoteFileListFragment extends Fragment {
	static final int ITEM_COPY = 0;
	
	ChkListAdapter mAdapter = null;
	ArrayAdapter<String> mAdapterSp = null;
	ListView mListView;
	ArrayList<FileInfo> mFileList;
	RemoteFile mRemoteFile = null;
	Handler h = null;
	Spinner mSpPath;
	Intent mIntent;
	Bundle mBundle;
	View mView;
	
	public static RemoteFileListFragment getNewInstance(String path) {
		RemoteFileListFragment rflf = new RemoteFileListFragment();
		Bundle b = new Bundle();		
		b.putString(Global.REMOTE_KEY_URI, path);
		rflf.setArguments(b);
		
		return rflf;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}	
	
		mView = inflater.inflate(R.layout.filelist_main, container, false); 
		h = new RemoteHandler();
		mView.findViewById(R.id.progressview).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.mainview).setVisibility(View.GONE);
		mListView = (ListView)mView.findViewById(R.id.fileListView);
		registerForContextMenu(mListView);	
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				FileInfo fileInfo = (FileInfo)mAdapter.getItem(arg2);
				if (!fileInfo.isDirectory()) {
					return;
				}
				
				gotoNextRemoteFragment(mRemoteFile.getPath() + fileInfo.getFileName() + Global.DIRECTORY_SPLITE_LABLE);
			}
		});	
		
		Button btnCopy = (Button)mView.findViewById(R.id.btnCopy);
		btnCopy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickCopy();
			}
		});
		
		
		RemoteThread cThread = new RemoteThread();
		Thread t = new Thread(cThread);
		t.start();
		
		return mView;
	}
	
	/* 创建长按menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo mi) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)mi;
		FileInfo fileInfo = (FileInfo)mAdapter.getItem(info.position);

		Log.i("RemoteFileListActivity", "Long time click file is:" + fileInfo.getFileName());		
		menu.setHeaderTitle(fileInfo.getFileName());
		menu.add(0, ITEM_COPY, 1, R.string.copy);
	}
	
	protected void InitRemoteFile() throws MalformedURLException {	
		String localUri = null;
		try {
			if (getArguments() != null) {
				localUri = getArguments().getString(Global.REMOTE_KEY_URI);
			}
			mRemoteFile = new RemoteFile(localUri);
		} catch (MalformedURLException e) {
			Log.e("RemoteFileListActivity", "Init remote file exception:" + e.getMessage());
			throw e;
		}
	}
	
	void startNextActivity(Class<?> cls) {
		//mIntent = new Intent(RemoteFileListFragment.this, /*RemoteFileListActivity.class*/cls);
		startActivity(mIntent);		
	}
	
	void setClipboard(ArrayList<String> fileNameList) {
		Global.setgClipboardPath(getArguments().getString(Global.REMOTE_KEY_URI));
		Global.setFileNameList(fileNameList);
		
		Toast.makeText(this.getActivity(), R.string.prompt_setclipboard, Global.PROMPT_TIME).show();
	}
	
	void gotoNextRemoteFragment(String remoteUri) {
		//Global.setRemoteUri(remoteUri);
		//startNextActivity(RemoteFileListFragment.class);
		//this.finish();
		RemoteFileListFragment rflf = getNewInstance(remoteUri);
		MainActivity.changeFragment(rflf, true);
	}
	

	/* 响应长按menu的点击事件 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo(); 
		
		FileInfo f = (FileInfo)mAdapter.getItem(info.position);
		String fileName = f.getFileName();
		if (f.isDirectory()) {
			fileName += Global.DIRECTORY_SPLITE_LABLE;
		} 			
		ArrayList<String> fileNameList = new ArrayList<String>();		
		fileNameList.add(fileName);		
		
		setClipboard(fileNameList);
		
		return false;
	}
	
	private void onClickCopy() {	
		
		ArrayList<String> fileNameList = new ArrayList<String>();
		ArrayList<Integer> list = mAdapter.getCheckedListIndex();
		
		if (list.size() == 0) {
			Toast.makeText(this.getActivity(), R.string.prompt_choicefile, Global.PROMPT_TIME).show();
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			int index = list.get(i);
			FileInfo f = mFileList.get(index);
			String fileName = f.getFileName();
			if (f.isDirectory()) {
				fileName += Global.DIRECTORY_SPLITE_LABLE;
			} 
			fileNameList.add(fileName);			
		}
		
		setClipboard(fileNameList);
		
		
	}	

	
	public void onClickLeftBtn(View view) {
		onClickCopy();
	}
	
	public void onClickUp(View view) {
		String path = mRemoteFile.getPath();
		String[] arrayPath = path.split(Global.DIRECTORY_SPLITE_LABLE);
		
		if (arrayPath.length <= 3) {
			return;
		}
			
		path = path.substring(0, path.lastIndexOf(arrayPath[arrayPath.length - 1]));
		Log.i("path1", path);
		
		gotoNextRemoteFragment(path);
	}
	
	public String getRemotePath() {
		String str = null;			
		
		str = mRemoteFile.getPath();		
		String[] arrayStr = str.split("@");
		if (arrayStr.length > 1) {
			str = Global.DIRECTORY_SPLITE_LABLE + arrayStr[1];
		} else {
			str = str.substring(Global.REMOTE_URI_LABEL.length());
			str = Global.DIRECTORY_SPLITE_LABLE + str;
		}
		
		return str;
	}
	
	class RemoteThread implements Runnable {
		void getRemoteFile() {
			Message msg = new Message();
			Bundle b = new Bundle();
			
			try {
				if (mFileList == null) {
					InitRemoteFile();
					((TextView)mView.findViewById(R.id.textPath)).setText(getRemotePath());
				
					mFileList = mRemoteFile.getFileInfo();
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
