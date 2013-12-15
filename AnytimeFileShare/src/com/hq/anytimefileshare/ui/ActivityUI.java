package com.hq.anytimefileshare.ui;

import java.util.ArrayList;

import android.app.Activity;

public class ActivityUI {
	private static ArrayList<Activity> gActivityList = new ArrayList<Activity>();
	
	public static Activity getCurrentActivity() {
		if (gActivityList.size() > 0) {
			return gActivityList.get(gActivityList.size() - 1);
		} else {
			return null;
		}
	}
	
	public static Activity getPrevActivity() {
		if (gActivityList.size() > 1) {
			return gActivityList.get(gActivityList.size() - 2);
		} else {
			return null;
		}
	}
	
	public static ArrayList<Activity> getAllActivity() {
		return gActivityList;
	}
	
	public static void addActivity(Activity activity) {
		ActivityUI.gActivityList.add(activity);
	}
	
	public static boolean delActivity(Activity activity) {
		boolean ret = false;
		int i = gActivityList.size() - 1;
		
		for ( ; i >= 0; i--) {
			if (gActivityList.get(i) == activity) {
				gActivityList.remove(i);
				ret = true;
				break;
			}
		}
		
		return ret;
	}
}
