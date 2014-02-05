package com.hq.anytimefileshare;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.FileBase;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.dao.FileInfo;
import com.hq.anytimefileshare.ui.ChkListAdapter;

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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;


public abstract class FragmentBase extends Fragment {
	static final int ITEM_COPY = 0;
	
	ChkListAdapter mAdapter = null;
	ListView mListView = null;
	ArrayList<FileInfo> mFileList = null;
	FileBase mFile = null;
	View mView;
	
	public abstract FragmentBase getNewInstanceByPath(String path);
	abstract FileBase getNewFileInstance(String path) throws Exception;
	abstract String getUpPath();
	
	void gotoNextFragment(String remoteUri) {
		if (remoteUri == null) {
			return;
		}
		FragmentBase rflf = getNewInstanceByPath(remoteUri);
		MainActivity.changeFragment(rflf, true);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}	
	
		mView = inflater.inflate(R.layout.filelist_main, container, false);
				
		mListView = (ListView)mView.findViewById(R.id.fileListView);
		registerForContextMenu(mListView);	
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				FileInfo fileInfo = (FileInfo)mAdapter.getItem(arg2);
				if (!fileInfo.isDirectory()) {
					return;
				}
				
				try {
					gotoNextFragment(mFile.getPath() + fileInfo.getFileName() + RemoteFile.FILE_DIRECTORY_SPLITE_LABLE);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});	
		
		Button btnCopy = (Button)mView.findViewById(R.id.btnCopy);
		btnCopy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickCopy();
			}
		});
		
		Button btnPaste = (Button)mView.findViewById(R.id.btnPaste);
		btnPaste.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickPaste();
			}
		});
		
		Button btnCancel = (Button)mView.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		ImageButton btnUp = (ImageButton)mView.findViewById(R.id.imageBtnUp);
		btnUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickUp();
			}
		});
		
		
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
	
	/* 响应长按menu的点击事件 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo(); 
		
		FileInfo f = (FileInfo)mAdapter.getItem(info.position);
		try {
			String fileName = mFile.getPath() + RemoteFile.FILE_DIRECTORY_SPLITE_LABLE + f.getFileName();
			if (f.isDirectory()) {
				fileName += Global.DIRECTORY_SPLITE_LABLE;
			} 				
			
			FileBase r = getNewFileInstance(fileName);
			Global.removeAllClipboardFile();
			Global.addFileToClipboardFileList(r);
			
			Common.setClipboard(this);	
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("onContextItemSelected", "Long press copy fail:" + e.getMessage());
		}
		
		return false;
	}
	
	private void onClickCopy() {
		ArrayList<Integer> list = mAdapter.getCheckedListIndex();
		
		if (list.isEmpty()) {
			Toast.makeText(this.getActivity(), R.string.prompt_choicefile, Global.PROMPT_TIME).show();
			return;
		}
		
		Global.removeAllClipboardFile();		
		try {
			for (int i = 0; i < list.size(); i++) {
				int index = list.get(i);
				FileInfo f = mFileList.get(index);
				String fileName = mFile.getPath() + RemoteFile.FILE_DIRECTORY_SPLITE_LABLE + f.getFileName();
				if (f.isDirectory()) {
					fileName += RemoteFile.FILE_DIRECTORY_SPLITE_LABLE;
				} 

				FileBase r = getNewFileInstance(fileName);
				Global.addFileToClipboardFileList(r);											
			}			
		} catch (Exception e) {
			Global.removeAllClipboardFile();
			e.printStackTrace();
			Log.e("RemoteFileListFragment", "onClickCopy fail.");
		}
		
		Common.setClipboard(this);		
	}	
	
	private void onClickPaste() {
		 ArrayList<FileBase> list = Global.getClipboardFileList();
		 if (list.isEmpty()) {
			 Toast.makeText(this.getActivity(), R.string.prompt_choicecopy, Global.PROMPT_TIME).show();
			 return;
		 }
		
		new FileStream().copyFile(getActivity(), mFile);
	}
	
	private void onClickUp() {
		/*String path = getUpPath();
		if (path == null) {
			return;
		}
		*/
	
		gotoNextFragment(mFile.getParent());
	}		
	
	
}
