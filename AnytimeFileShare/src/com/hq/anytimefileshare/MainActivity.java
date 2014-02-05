package com.hq.anytimefileshare;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class MainActivity extends FragmentActivity {
	private static FragmentManager mFm;
	private Dialog mAboutDlg;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mFm = getSupportFragmentManager();
		//changeFragment(/*LocalFileListFragment.getNewInstance(null)*/ new RemoteListFragment(), false);
		
		Spinner spinner = (Spinner)findViewById(R.id.topPathSpinner);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){  
            public void onItemSelected(AdapterView<?> parent, View view,  
                    int position, long id) {               
                if (position == 0) {
                	changeFragment(new RemoteListFragment(), false);
                } else {
                	changeFragment(LocalFileListFragment.getNewInstance(null), false);
                }
            }  
  
  
            public void onNothingSelected(AdapterView<?> parent) {                  
            }     
        });  

		
		//mAdapter.add("xx"); 

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
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch (item.getItemId()) {
		case R.id.menu_exit:
			finish();			
			break;
		case R.id.menu_about:
			mAboutDlg = new Dialog(this);
			mAboutDlg.setContentView(R.layout.about);
			mAboutDlg.setTitle(R.string.about);
			mAboutDlg.findViewById(R.id.btnAboutOK).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mAboutDlg.dismiss();
				}
			});
			
			mAboutDlg.show();
			break;
		default:
			break;		
		}
        return super.onOptionsItemSelected(item);
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
		Context c = context.getActivity();
		if (c == null) {
			Log.i("MainActivity", "(showWarmMsg)Not find fragment's activity");
			return;
		}

		AlertDialog dlg = new AlertDialog.Builder(c)
			.setTitle(context.getString(R.string.warn)).setMessage(content).setPositiveButton(context.getString(R.string.ok), new DialogInterface
					.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							context.getFragmentManager().popBackStack();
						}
					}).create();
		dlg.show();
	}
}
