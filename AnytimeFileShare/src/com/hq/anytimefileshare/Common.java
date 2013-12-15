package com.hq.anytimefileshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

public class Common {
	static String getRemoteUri(String remoteAddr, String domain, String username, String pwd, String path) {
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
	
	static void gotoNextActivity(Context context, Class<?> cls) {
		Intent intent = new Intent(context, cls);
		context.startActivity(intent);
	}
	
	
	public static void gotoRemoteActivityBySetting(Context context, Class<?> cls,
			String remoteAddr, String domain, String userName, String userPwd, String path) {
		String connectUri = getRemoteUri(remoteAddr, domain, userName, userPwd, path);
		Global.setRemoteUri(connectUri);
		Log.i("Common", "Connect uri is:" + connectUri);
		gotoNextActivity(context, cls);
	}
	
	public static void showWarmMsg(Context context, String content) {
		AlertDialog dlg = new AlertDialog.Builder(context)
			.setTitle("¾¯¸æ").setMessage(content).setPositiveButton("È·¶¨", new DialogInterface
					.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();
		dlg.show();
	}
}
