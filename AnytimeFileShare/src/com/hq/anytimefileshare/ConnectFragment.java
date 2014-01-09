package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.HashMap;

import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.model.dao.RemoteInfo;
import com.hq.anytimefileshare.ui.UiBaseActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ConnectFragment extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}	
	
		View v = inflater.inflate(R.layout.connect, container, false); 		
		Button btnSave = (Button)v.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickSave(v);
			}
		});
		
		Button btnBack = (Button)v.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//todo
			}
		});
		
		return v;
	}
	
	String getRemoteUri(String remoteAddr, String domain, String username, String pwd, String path) {
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
	
	private void gotoRemoteListFragment() {
		//Common.gotoNextActivity(ConnectFragment.this, RemoteListActivity.class);
		//finish();
		MainActivity.changeFragment(new RemoteListFragment(), true);
	}
	
	public void onClickSave(View view) {
		EditText edit = (EditText)getView().findViewById(R.id.editRemote);	
		if (edit.length() <= 0) {
			edit.requestFocus();
			edit.setError(getResources().getString(R.string.warm_input_remote));
			return;
		}
		
		String remoteAddr = edit.getText().toString();
		Log.d("ConnectActivity", "Remote address is:" + remoteAddr);
		String userName = null;
		edit = (EditText)getView().findViewById(R.id.editUsername);		
		if (edit.length() > 0) {
			userName = edit.getText().toString();
			Log.i("ConnectActivity", "Username is:" + userName);
		}
		String userPwd = null;
		edit = (EditText)getView().findViewById(R.id.editPwd);		
		if (edit.length() > 0) {
			if (userName == null) {
				((EditText)getView().findViewById(R.id.editUsername)).requestFocus();
				((EditText)getView().findViewById(R.id.editUsername)).setError(getResources().getString(R.string.warm_input_username));
				return;
			}
			userPwd = edit.getText().toString();
		}
		String domain = null;
		edit = (EditText)getView().findViewById(R.id.editDomain);
		if (edit.length() > 0) {
			if (userName == null) {
				((EditText)getView().findViewById(R.id.editUsername)).requestFocus();
				((EditText)getView().findViewById(R.id.editUsername)).setError(getResources().getString(R.string.warm_input_username));
				return;
			}
			domain = edit.getText().toString();
			Log.i("ConnectActivity", "Domain is:" + domain);
		}
		
		String path = null;
		edit = (EditText)getView().findViewById(R.id.editRemotePath);
		if (edit.length() > 0) {
			path = edit.getText().toString();
			Log.i("ConnectActivity", "Path is:" + path);
		}
		
		RemoteManger rm = new RemoteManger(getView().getContext());
		try {
			rm.insert(remoteAddr, domain, path, userName, userPwd);
		} catch (Exception e) {
			String s = "Save remote fail:" + e.getMessage();
			Log.e("onClickConnect", s);
			MainActivity.showWarmMsg(this, s);
			return;
		}	
		
		gotoRemoteListFragment();
	}
	
	public void onClickBack(View view) {
		//gotoRemoteListActivity();
	}
}
