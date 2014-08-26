package org.universAAL.android.activities;

import java.io.IOException;

import org.universAAL.android.R;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.IntentConstants;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HandlerActivity extends Activity {
	ProgressReceiver mReceiver=null;
	private boolean mHandlerLayoutSet=false;
	public static final String PROPERTY_REG_ID = "registration_id";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "HandlerActivity";
	private GoogleCloudMessaging gcm;
	private String regid;
	private Context context;
	/**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SERVER_ID = "1036878524725";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
		context = getApplicationContext();
		// Check device for Play Services APK.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if (regid.isEmpty()) {
                registerInBackground();
            }
		}else{
			Toast.makeText(getApplicationContext(),
					"Google Play Services is not available", Toast.LENGTH_LONG)
					.show();// TODO manage text
			// Do not block the app from running if Play Services is not available
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
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device does not support Google Play Services");
//	            finish(); // Do not close app if it does not have Play Services
	        }
	        return false;
	    }
	    return true;
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Google Play Services GCM Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed. GCM Registration must be renewed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
	    new AsyncTask() {
	        @Override
	        protected String doInBackground(Object... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SERVER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend(regid);

	                // Persist the regID - no need to register again.
	                storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // TODO If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }
	    }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 * @param regid 
	 */
	private void sendRegistrationIdToBackend(String regid) {
	    // TODO Your implementation here.
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    Log.v(TAG, "%%%%%%%%%%%%%%%%%% REGISTRATION ID IS : " + regId+"  %%%%%%%%%%%%%%%%%%%%%%%%");
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}

}