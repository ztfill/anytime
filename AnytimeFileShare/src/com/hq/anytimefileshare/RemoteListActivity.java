package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.model.dao.RemoteInfo;
import com.hq.anytimefileshare.ui.UiBaseActivity;

public class RemoteListActivity extends UiBaseActivity {
	static final int ITEM_DELETE = 1;
	ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
	ListView mListView;
	SimpleAdapter mAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remotelist_main);
		
		RemoteManger rm = new RemoteManger(this);
		ArrayList<RemoteInfo> dataList = null;
		try {
			dataList = rm.getAll();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}		
		
		for (int i = 0; i < dataList.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemId", (Integer.valueOf(dataList.get(i).getId())).toString());
			map.put("ItemTitle", dataList.get(i).getAddr());
			map.put("ItemText", dataList.get(i).getPath());
			map.put("ItemDomain", dataList.get(i).getDomain());
			map.put("ItemUserName", dataList.get(i).getUserName());
			map.put("ItemUserPwd", dataList.get(i).getUserPwd());
		
			mList.add(map);
		}
		
		mAdapter = new SimpleAdapter(this, mList,
				R.layout.remotelistitem, new String[] {"ItemTitle", "ItemText"},
				new int[] {R.id.ItemTitle, R.id.ItemText});
		mListView = (ListView)findViewById(R.id.listRemote);
		registerForContextMenu(mListView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				HashMap<String, String> map = mList.get(arg2);
				
				Common.gotoRemoteActivityBySetting(RemoteListActivity.this, RemoteFileListActivity.class,
							map.get("ItemTitle"), map.get("ItemDomain"), 
							map.get("ItemUserName"), map.get("ItemUserPwd"), map.get("ItemText"));
			}
		});	
	}
	

	
	public boolean onKeyDown(int keyCode, KeyEvent event) {   
		if (keyCode == KeyEvent.KEYCODE_BACK) {   
			finish();
			System.exit(0);
			return true;
		}   
		
		return super.onKeyDown(keyCode, event);  
	} 
	
	public void onClickNewConnect(View view) {
		Intent intent = new Intent(RemoteListActivity.this, ConnectActivity.class);
		startActivity(intent);
		finish();
	}
	
	/* 创建长按menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo mi) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)mi;
		HashMap<String, String> map = mList.get(info.position);

		Log.i("RemoteFileListActivity", "Long time click remote is:" + map.get("ItemTitle"));
		menu.setHeaderTitle(map.get("ItemTitle"));
		menu.add(0, ITEM_DELETE, 1, R.string.delete);
	}
	
	/* 响应长按menu的点击事件 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo(); 
		HashMap<String, String> map = mList.get(info.position);
		RemoteManger rm = new RemoteManger(this);
		try {
			rm.delete(Integer.valueOf(map.get("ItemId")));
		} catch (Exception e) {		
			Log.e("RemoteListActivity", "Delte remote fail:" + e.getMessage());
			Common.showWarmMsg(RemoteListActivity.this, e.getMessage());
			return false;
		} 
		
		mList.remove(info.position);
		mAdapter.notifyDataSetChanged();
		Toast.makeText(RemoteListActivity.this, R.string.prompt_delete_success, Global.PROMPT_TIME).show();
		return false;
	}
}
