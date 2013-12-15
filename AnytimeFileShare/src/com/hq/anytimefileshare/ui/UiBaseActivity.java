package com.hq.anytimefileshare.ui;

import java.util.ArrayList;

import com.hq.anytimefileshare.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class UiBaseActivity extends Activity {
	Dialog mAboutDlg;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityUI.addActivity(this);
	}
	

	public void finish() {
		ActivityUI.delActivity(this);
		super.finish();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch (item.getItemId()) {
		case R.id.menu_back:
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
	

	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {    
        	finish();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

	
	
	public void dealNextStep() {
	}
	
	public void showWarmMsg(String content) {
		AlertDialog dlg = new AlertDialog.Builder(this)
			.setTitle(getString(R.string.warn)).setMessage(content).setPositiveButton(getString(R.string.ok), new DialogInterface
					.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dealNextStep();
						}
					}).create();
		dlg.show();
	}
	
	public void onClickRightBtn(View view) {
		finish();
	}
}
