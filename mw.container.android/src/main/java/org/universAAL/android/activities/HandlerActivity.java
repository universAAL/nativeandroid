package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.IntentConstants;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

public class HandlerActivity extends Activity {
	ProgressReceiver mReceiver=null;
	private boolean mHandlerLayoutSet=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidHandler.setActivity(this);
		AndroidHandler handler = (AndroidHandler) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { AndroidHandler.class.getName() });
		if (handler != null) {
			setContentView(R.layout.handler);
			handler.render();
		}else{
			setContentView(R.layout.progress);
			setPercentage();
			if(mReceiver==null){
				mReceiver=new ProgressReceiver();
			}
			IntentFilter filter=new IntentFilter(IntentConstants.ACTION_UI_PROGRESS);
			registerReceiver(mReceiver, filter);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mReceiver!=null){
			unregisterReceiver(mReceiver);
			mReceiver=null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		AndroidHandler.setActivity(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.xml.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_start:
			Intent startServiceIntent = new Intent(this, MiddlewareService.class);
			this.startService(startServiceIntent);
			return true;
		case R.id.action_stop:
			Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
			this.stopService(stopServiceIntent);
			return true;
		case R.id.action_settings:
			Intent startSettingsIntent = new Intent(this, SettingsActivity.class);
			startSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			this.startActivity(startSettingsIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public class ProgressReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			setPercentage();
		}
	}
	
	private void setPercentage() {
		ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar1);
		if (bar != null) {
			if (MiddlewareService.percentage >= 100) {
				bar.setIndeterminate(true);
			} else {
				bar.setIndeterminate(false);
				bar.setProgress(MiddlewareService.percentage);
			}
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		if(layoutResID==R.layout.handler){
			if(!mHandlerLayoutSet){
				mHandlerLayoutSet=true;
				super.setContentView(layoutResID);
			}
		}else{
			super.setContentView(layoutResID);
		}
	}

}