package com.hq.anytimefileshare;

import com.hq.anytimefileshare.model.RemoteManger;
import com.hq.anytimefileshare.ui.UiBaseActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class ConnectActivity extends UiBaseActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	private void gotoRemoteListActivity() {
		Common.gotoNextActivity(ConnectActivity.this, RemoteListActivity.class);
		finish();
	}
	
	public void onClickSave(View view) {
		EditText edit = (EditText)findViewById(R.id.editRemote);	
		if (edit.length() <= 0) {
			edit.requestFocus();
			edit.setError(getResources().getString(R.string.warm_input_remote));
			return;
		}
		
		String remoteAddr = edit.getText().toString();
		Log.d("ConnectActivity", "Remote address is:" + remoteAddr);
		String userName = null;
		edit = (EditText)findViewById(R.id.editUsername);		
		if (edit.length() > 0) {
			userName = edit.getText().toString();
			Log.i("ConnectActivity", "Username is:" + userName);
		}
		String userPwd = null;
		edit = (EditText)findViewById(R.id.editPwd);		
		if (edit.length() > 0) {
			if (userName == null) {
				((EditText)findViewById(R.id.editUsername)).requestFocus();
				((EditText)findViewById(R.id.editUsername)).setError(getResources().getString(R.string.warm_input_username));
				return;
			}
			userPwd = edit.getText().toString();
		}
		String domain = null;
		edit = (EditText)findViewById(R.id.editDomain);
		if (edit.length() > 0) {
			if (userName == null) {
				((EditText)findViewById(R.id.editUsername)).requestFocus();
				((EditText)findViewById(R.id.editUsername)).setError(getResources().getString(R.string.warm_input_username));
				return;
			}
			domain = edit.getText().toString();
			Log.i("ConnectActivity", "Domain is:" + domain);
		}
		
		String path = null;
		edit = (EditText)findViewById(R.id.editRemotePath);
		if (edit.length() > 0) {
			path = edit.getText().toString();
			Log.i("ConnectActivity", "Path is:" + path);
		}
		
		RemoteManger rm = new RemoteManger(this);
		try {
			rm.insert(remoteAddr, domain, path, userName, userPwd);
		} catch (Exception e) {
			String s = "Save remote fail:" + e.getMessage();
			Log.e("onClickConnect", s);
			Common.showWarmMsg(ConnectActivity.this, s);
			return;
		}
	
		
		gotoRemoteListActivity();
	}
	
	public void onClickBack(View view) {
		gotoRemoteListActivity();
	}
}
