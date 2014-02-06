package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.Iterator;

import com.hq.anytimefileshare.R;
import com.hq.anytimefileshare.model.FileBase;
import com.hq.anytimefileshare.model.RemoteFile;
import com.hq.anytimefileshare.model.dao.FileInfo;
import com.hq.anytimefileshare.ui.ChkListAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public abstract class FragmentBase extends Fragment {
	static final int ITEM_COPY = 0;
	
	ChkListAdapter mAdapter = null;
	ListView mListView = null;
	ArrayList<FileInfo> mFileList = null;
	FileBase mFile = null;
	View mView;
	final Handler mFragHandler = new Handler();
	
	final Runnable mUpdateUI = new Runnable() {
    	public void run() {
    		mAdapter.notifyDataSetChanged();
    	}
    };
	
	public abstract FragmentBase getNewInstanceByPath(String path);
	abstract FileBase getNewFileInstance(String path) throws Exception;
	abstract String getUpPath();
	abstract void InitFileByPath(String path) throws Exception;
	
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
	
	private void fileToClipboard(FileInfo f) throws Exception {
		try {
			String fileName = mFile.getPath() + RemoteFile.FILE_DIRECTORY_SPLITE_LABLE + f.getFileName();
			if (f.isDirectory()) {
				fileName += Global.DIRECTORY_SPLITE_LABLE;
			} 				
			
			FileBase r = getNewFileInstance(fileName);			
			Global.addFileToClipboardFileList(r);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("fileToClipboard", "File to clipboard fail:" + e.getMessage());
			throw e;
		}
	}
	
	/* 响应长按menu的点击事件 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo(); 
		
		Global.removeAllClipboardFile();
		FileInfo f = (FileInfo)mAdapter.getItem(info.position);
		try {
			fileToClipboard(f);			
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
				fileToClipboard(f);												
			}			
		} catch (Exception e) {
			Global.removeAllClipboardFile();
			e.printStackTrace();
			Log.e("RemoteFileListFragment", "onClickCopy fail.");
			return;
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
	
	String getPath() {
		String ret = null;
		if (mFile == null) {
			return null;
		}
		
		try {
			ret = mFile.getPath();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("FragmentBase.getPath", "get path fail:" + e.getMessage());
		}
		
		return ret;
	}
	
	void updateListView() throws Exception {
		mFileList.clear();
		try {
			InitFileByPath(mFile.getPath());
			ArrayList<FileInfo> list = mFile.getFileInfo();	
			Iterator<FileInfo> it = list.iterator();
			while (it.hasNext()) {
				mFileList.add(it.next());
			}
			mFragHandler.post(mUpdateUI);
		} catch (Exception e) {
			Log.e("updateListView", "Update list view is fail.");
			throw e;
		}
	}
}
