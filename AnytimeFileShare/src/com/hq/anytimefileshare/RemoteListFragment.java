package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.HashMap;


import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.model.dao.RemoteInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;


public class  RemoteListFragment extends Fragment {
	static final int ITEM_DELETE = 1;
	ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
	ListView mListView;
	SimpleAdapter mAdapter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}		
		
		
	
			View v = inflater.inflate(R.layout.remotelist_main, container, false); 
		if (mListView == null) {	// 用于判断返回键按钮，如果是返回键，不进入下面的语句 
			RemoteManger rm = new RemoteManger(v.getContext());
			ArrayList<RemoteInfo> dataList = null;
			try {
				dataList = rm.getAll();
			} catch (Exception e) {			
				e.printStackTrace();
				Log.e("RemoteListFragment", "Create View error:" + e.getMessage());
				MainActivity.showWarmMsg(this, "Create View error:" + e.getMessage());
				return null;
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
		} 
		
		mListView = (ListView)v.findViewById(R.id.listRemote);
		registerForContextMenu(mListView);
		mAdapter = new SimpleAdapter(v.getContext(), mList,
				R.layout.remotelistitem, new String[] {"ItemTitle", "ItemText"},
				new int[] {R.id.ItemTitle, R.id.ItemText});	
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,   
                    long arg3) {  
				HashMap<String, String> map = mList.get(arg2);
				
				String connectUri = getRemoteUri(map.get("ItemTitle"), map.get("ItemDomain"),
								map.get("ItemUserName"), map.get("ItemUserPwd"), map.get("ItemText"));
				Log.i("RemoteListFragment", "Connect uri is:" + connectUri);
				RemoteFileListFragment rflf = RemoteFileListFragment.getNewInstance(connectUri);
				
				MainActivity.changeFragment(rflf, true);
			}
		});	
		
		
		Button btnNewConnect = (Button)v.findViewById(R.id.btnNewConnect);
		btnNewConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickNewConnect(v);
			}
		});
		
		return v;
	}
	
	private void onClickNewConnect(View view) {
		//Intent intent = new Intent(RemoteListActivity.this, ConnectActivity.class);
		//startActivity(intent);
		//finish();
		
		MainActivity.changeFragment(new ConnectFragment(), true);
	}
	
	private String getRemoteUri(String remoteAddr, String domain, String username, String pwd, String path) {
		String uri = null;
		
		uri = Global.REMOTE_URI_LABEL;
		if (domain != null) {
			uri += domain.trim() + ":";
		}
		
		if (username != null) {
			uri += username.trim();
			if (pwd != null) {
				uri += ":" + pwd.trim();
			}
			uri += "@";			
		}
		uri += remoteAddr.trim();		
		if (path != null) {
			uri += path.trim() + Global.DIRECTORY_SPLITE_LABLE;
		} else {
			uri += Global.DIRECTORY_SPLITE_LABLE;
		}
		
		
		return uri;
	}
	
}
