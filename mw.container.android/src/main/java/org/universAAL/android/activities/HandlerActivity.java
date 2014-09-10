package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.IntentConstants;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HandlerActivity extends Activity {
	ProgressReceiver mReceiver=null;
	private boolean mHandlerLayoutSet=false;
    private static final String TAG = "HandlerActivity";
	private Context mContext;
	/**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SERVER_ID = "1036878524725";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
		mContext = getApplicationContext();
		// Check device for Play Services APK. ONLY IF R-API MODE
		Integer remoteType = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"setting_conntype_key", "0"));
		if(remoteType==IntentConstants.REMOTE_TYPE_RAPI){
			if (checkPlayServices()) {
				String mRegID = RAPIManager.getRegistrationId(mContext);
				if (mRegID.isEmpty()) {
					RAPIManager.registerInThread(getApplicationContext());
				}
			}else{
				Toast.makeText(getApplicationContext(), R.string.warning_gplay, Toast.LENGTH_LONG).show();// TODO manage text
				// Do not block the app from running if Play Services is not available
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//checkPlayServices();//Because Play Services should not block the app if not present, no need to check every time
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
			if (MiddlewareService.mPercentage >= 100) {
				bar.setIndeterminate(true);
			} else {
				bar.setIndeterminate(false);
				bar.setProgress(MiddlewareService.mPercentage);
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
	
	//The following is from http://developer.android.com/google/gcm/client.html
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	            		IntentConstants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device does not support Google Play Services");
//	            finish(); // Do not close app if it does not have Play Services
	        }
	        return false;
	    }
	    return true;
	}
}