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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;


public class RemoteFileListActivity extends UiBaseActivity {
	static final int ITEM_COPY = 0;
	
	ChkListAdapter mAdapter = null;
	ListView mListView;
	ArrayList<FileInfo> mFileList;
	RemoteFile mRemoteFile = null;
	Handler h = null;
	Intent mIntent;
	Bundle mBundle;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist_main);
		
		h = new RemoteHandler();
		findViewById(R.id.progressview).setVisibility(View.VISIBLE);
		findViewById(R.id.mainview).setVisibility(View.GONE);
		
		//Spinner spinTopPath = (Spinner)findViewById(R.id.spinnerTopPath);
		//ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.arrayTopPath, android.R.layout.simple_spinner_item); 
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//spinTopPath.setAdapter(adapter);
		
		//((TextView)findViewById(R.id.textPath)).setText(mRemoteUri);
		
		mListView = (ListView)findViewById(R.id.fileListView);
		registerForContextMenu(mListView);	
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				FileInfo fileInfo = (FileInfo)mAdapter.getItem(arg2);
				if (!fileInfo.isDirectory()) {
					return;
				}
				
				gotoNextRemoteActivity(mRemoteFile.getPath() + fileInfo.getFileName() + Global.DIRECTORY_SPLITE_LABLE);
			}
		});			
		
		RemoteThread cThread = new RemoteThread();
		Thread t = new Thread(cThread);
		t.start();
			
	}
	

	/* 创建长按menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo mi) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)mi;
		FileInfo fileInfo = (FileInfo)mAdapter.getItem(info.position);

		Log.i("RemoteFileListActivity", "Long time click file is:" + fileInfo.getFileName());		
		menu.setHeaderTitle(fileInfo.getFileName());
		menu.add(0, ITEM_COPY, 1, R.string.copy);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void dealNextStep() {
		finish();
	}
	
	protected void InitRemoteFile() throws MalformedURLException {	
		try {
			mRemoteFile = new RemoteFile(Global.getRemoteUri());
		} catch (MalformedURLException e) {
			Log.e("RemoteFileListActivity", "Init remote file exception:" + e.getMessage());
			throw e;
		}
	}
	
	void startNextActivity(Class<?> cls) {
		mIntent = new Intent(RemoteFileListActivity.this, /*RemoteFileListActivity.class*/cls);
		startActivity(mIntent);		
	}
	
	void gotoNextActivity(ArrayList<String> fileNameList) {
		Global.setFileNameList(fileNameList);
		startNextActivity(LocalFileListActivity.class);
	}
	
	void gotoNextRemoteActivity(String remoteUri) {
		Global.setRemoteUri(remoteUri);
		startNextActivity(RemoteFileListActivity.class);
		this.finish();
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
		
		gotoNextActivity(fileNameList);
		
		return false;
	}
	
	protected void onClickCopy() {		
		ArrayList<String> fileNameList = new ArrayList<String>();
		ArrayList<Integer> list = mAdapter.getCheckedListIndex();
		
		if (list.size() == 0) {
			Toast.makeText(RemoteFileListActivity.this, R.string.prompt_choicefile, Global.PROMPT_TIME).show();
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
		
		gotoNextActivity(fileNameList);
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
		
		gotoNextRemoteActivity(path);
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
				InitRemoteFile();
				((TextView)findViewById(R.id.textPath)).setText(getRemotePath());
				
				mFileList = mRemoteFile.getFileInfo();
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
				mAdapter = new ChkListAdapter(RemoteFileListActivity.this,
						mFileList);
				mListView.setAdapter(mAdapter); 				
				
				findViewById(R.id.progressview).setVisibility(View.GONE);
				findViewById(R.id.mainview).setVisibility(View.VISIBLE);
				
				CheckBox chkAll = (CheckBox)findViewById(R.id.checkAll);
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
				showWarmMsg(b.getString(Global.HANDLER_MSG));
				break;
			default:
				break;
			}
		}
	}

}
