package com.hq.anytimefileshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainActivity extends FragmentActivity {
	static FragmentManager mFm;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mFm = getSupportFragmentManager();
		changeFragment(/*LocalFileListFragment.getNewInstance(null)*/new RemoteListFragment(), false);
		/*
		RemoteListFragment newFragment = new RemoteListFragment();

	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// Replace whatever is in thefragment_container view with this fragment,
		// and add the transaction to the backstack
		transaction.replace(R.id.mainContent,newFragment);
	
		//提交修改
		transaction.commit();	
		*/		
	}
	
	static void changeFragment(Fragment f, boolean isBack) {
		FragmentTransaction transaction = mFm.beginTransaction();
		// Replace whatever is in thefragment_container view with this fragment,
		// and add the transaction to the backstack
		transaction.replace(R.id.mainContent, f);		
		if (isBack) {
			transaction.addToBackStack(null);
		}
		//提交修改
		transaction.commit();
	}
	
	public static void showWarmMsg(final Fragment context, String content) {
		AlertDialog dlg = new AlertDialog.Builder(context.getActivity())
			.setTitle(context.getString(R.string.warn)).setMessage(content).setPositiveButton(context.getString(R.string.ok), new DialogInterface
					.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							/*
							 * if (preFrag == null) {
							 
								context.getActivity().finish();
							} else {
								changeFragment(preFrag, false);
							}
							*/
							context.getFragmentManager().popBackStack();
							//FragmentTransaction transaction = mFm.beginTransaction();
							//transaction.detach(context);
							//transaction.commit();
						}
					}).create();
		dlg.show();
	}
	
	
	static void getFragmentCount() {
		int i = mFm.getBackStackEntryCount();
		Log.i("=====", "AAAAAA:" + i);
	}
}
