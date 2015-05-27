/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.AppConstants;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * This is the main Activity (screen) of the app. It has 2 purposes: showing the
 * progress bar of the MiddlewareService initialization, and when UI framework
 * is available in the network, act as the face of the android UI handler.
 * 
 * @author alfiva
 * 
 */
public class HandlerActivity extends Activity {
	private static final String TAG = "HandlerActivity";
	private ProgressReceiver mReceiver = null;
	private boolean mHandlerLayoutSet = false;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
		mContext = getApplicationContext();
		Config.load(mContext); //Sync Preferences in Config util
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(AppConstants.FIRST, true)){
			// first time we run the app (or app data has been cleared)
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(AppConstants.FIRST, false).commit();
			Config.createFiles(mContext);
		}
		// Check device for Play Services APK. ONLY IF R-API MODE
		Integer remoteType = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"setting_conntype_key", "0"));
		if (remoteType == AppConstants.REMOTE_TYPE_RAPI) {
			if (checkPlayServices()) {
				String mRegID = RAPIManager.getRegistrationId(mContext);
				if (mRegID.isEmpty()) {
					RAPIManager.registerInThread(getApplicationContext());
				}
			} else {
				Toast.makeText(getApplicationContext(), R.string.warning_gplay,
						Toast.LENGTH_LONG).show();// TODO manage text
				// Do not block the app from running if Play Services is not available
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// checkPlayServices();//Because Play Services should not block the app if not present, no need to check every time
		AndroidHandler.setActivity(this);
		AndroidHandler handler = (AndroidHandler) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { AndroidHandler.class.getName() });
		if (handler != null) {
			setContentView(R.layout.handler);
			handler.render();
		} else {
			setContentView(R.layout.progress);
			setPercentage();
			if (mReceiver == null) {
				mReceiver = new ProgressReceiver();
			}
			IntentFilter filter = new IntentFilter(
					AppConstants.ACTION_UI_PROGRESS);
			registerReceiver(mReceiver, filter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
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
//		switch (item.getItemId()) {
//		case R.id.action_start:
//			Intent startServiceIntent = new Intent(this,
//					MiddlewareService.class);
//			this.startService(startServiceIntent);
//			return true;
//		case R.id.action_stop:
//			Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
//			this.stopService(stopServiceIntent);
//			return true;
//		case R.id.action_settings:
		// To be used as a library, it cannot use switch{} with R.id
		if(R.id.action_settings==item.getItemId()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.pin, null);
			final EditText pin = (EditText) view.findViewById(R.id.editPin);
			builder.setView(view).setTitle(R.string.pinTitle)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String editable=pin.getText().toString();
									if (editable.equals("8225")) {
										Intent startSettingsIntent = new Intent(
												HandlerActivity.this,
												SettingsActivity.class);
										startSettingsIntent
												.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
										HandlerActivity.this
												.startActivity(startSettingsIntent);
									} else {
										Toast.makeText(HandlerActivity.this,
												R.string.pinError,
												Toast.LENGTH_LONG).show();
									}
								}
							}).create().show();
			return true;}
//		default:
			return super.onOptionsItemSelected(item);
//		}
	}

	/**
	 * Broadcast receiver to get notified about progress in the progress bar by
	 * the other services
	 * 
	 * @author alfiva
	 * 
	 */
	public class ProgressReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setPercentage();
		}
	}

	/**
	 * Notify the progress to update its status to that set by the
	 * MiddlewareService
	 */
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
		if (layoutResID == R.layout.handler) {
			if (!mHandlerLayoutSet) {
				mHandlerLayoutSet = true;
				super.setContentView(layoutResID);
			}
		} else {
			super.setContentView(layoutResID);
		}
	}

	// The following is from http://developer.android.com/google/gcm/client.html

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						AppConstants.PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Log.i(TAG, "This device does not support Google Play Services");
				// finish(); // Do not close app if it does not have Play Services
			}
			return false;
		}
		return true;
	}
}