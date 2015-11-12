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
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.gcm.RegistrationService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Framework-handled activity for setting up configuration options in menu >
 * settings
 * 
 * @author alfiva
 * 
 */
public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = "SettingsActivity";
	private static boolean changed=false;
	private static OnSharedPreferenceChangeListener listener=new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			changed=true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Config.load(getApplicationContext()); //Sync Preferences in Config util
		addPreferencesFromResource(R.xml.settings);
		// Be notified if there is any change
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
		// Manage the availability of "remove current wifi" option
		Preference wifiPref = (Preference) findPreference("setting_resetwifi_key");
		String wifiVal = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(AppConstants.MY_WIFI, AppConstants.NO_WIFI);
		if (wifiVal.equals(AppConstants.NO_WIFI)) {
			wifiPref.setSummary(R.string.setting_resetwifi_text_none);
			wifiPref.setEnabled(false);
		} else {
			wifiPref.setSummary(R.string.setting_resetwifi_text);
			wifiPref.setEnabled(true);
			wifiPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					PreferenceManager
							.getDefaultSharedPreferences(SettingsActivity.this)
							.edit().remove(AppConstants.MY_WIFI).commit();
					preference.setEnabled(false);
					return true;
				}
			});
		}
		
		//_______MANAGE START AND STOP___________
		Preference start = (Preference) findPreference("setting_start_key");
		start.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent startServiceIntent = new Intent(SettingsActivity.this, MiddlewareService.class);
				SettingsActivity.this.startService(startServiceIntent);
				return true;
			}
		});
		Preference stop = (Preference) findPreference("setting_stop_key");
		stop.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent stopServiceIntent = new Intent(SettingsActivity.this, MiddlewareService.class);
				SettingsActivity.this.stopService(stopServiceIntent);
				return true;
			}
		});
			
		// Manage the registration on Google Play Services GCM when selecting R-API conn type
		ListPreference connType = (ListPreference) findPreference(AppConstants.Keys.CONNTYPE);
		setRAPIOptionsEnabled(connType.getValue()!=null && connType.getValue().equals("1"));
		connType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (newValue.equals("1")) {
					// Set to R-API -> Check Play Services
					if (RAPIManager.checkPlayServices(getApplicationContext())) {
//						String mRegID = RAPIManager
//								.getRegistrationId(getApplicationContext());
//						if (mRegID.isEmpty()) {
//							RAPIManager
//									.registerInThread(getApplicationContext(), null);
//						}
						//Intent intent = new Intent(getApplicationContext(), RegistrationService.class);
						//startService(intent);
						RAPIManager.performRegistrationInThread(getApplicationContext(),null);
						setRAPIOptionsEnabled(true);//Enable RAPI options only if RAPI selected
						return true;
					} else {
						Toast.makeText(getApplicationContext(),
								R.string.warning_gplay, Toast.LENGTH_LONG)
								.show();
						// Do not block the app from running if Play Services is not available
						setRAPIOptionsEnabled(false);
						return false;// just dont allow change
					}
				} else {
					setRAPIOptionsEnabled(false);
					return true;// Allow the change
				}
			}
		});
		// Manage the registration on Google Play Services GCM when changing project API key
		EditTextPreference connKey = (EditTextPreference) findPreference(AppConstants.Keys.CONNGCM);
		connKey.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (RAPIManager.checkPlayServices(getApplicationContext())) {
					//Lets assume newValue has changed
//					RAPIManager.registerInThread(getApplicationContext(), (String) newValue);
					//Intent intent = new Intent(getApplicationContext(), RegistrationService.class);
					//startService(intent);
					RAPIManager.performRegistrationInThread(getApplicationContext(),(String) newValue);
					return true;
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.warning_gplay, Toast.LENGTH_LONG).show();
					// Do not block the app from running if Play Services is not
					// available
					return false;// just dont allow change
				}
			}
		});
	}
	
	private void setRAPIOptionsEnabled(boolean enable){
		Preference pref = (Preference) findPreference(AppConstants.Keys.CONNURL);
		pref.setEnabled(enable);
		pref = (Preference) findPreference(AppConstants.Keys.CONNGCM);
		pref.setEnabled(enable);
		pref = (Preference) findPreference(AppConstants.Keys.CONNUSR);
		pref.setEnabled(enable);
		pref = (Preference) findPreference(AppConstants.Keys.CONNPWD);
		pref.setEnabled(enable);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (changed) {// There was a change in settings, restart MW service
			changed=false; // for the next time
			Config.load(getApplicationContext()); // Sync Preferences in Config util now that they are changed
//			Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
//			boolean stopped = stopService(stopServiceIntent);
//			if (stopped) {// The service was running, restart it
//				Intent startServiceIntent = new Intent(this, MiddlewareService.class);
//				this.startService(startServiceIntent);
//			}
		}
	}

	// The following is from http://developer.android.com/google/gcm/client.html

//	/**
//	 * Check the device to make sure it has the Google Play Services APK. If it
//	 * doesn't, display a dialog that allows users to download the APK from the
//	 * Google Play Store or enable it in the device's system settings.
//	 */
//	private boolean checkPlayServices() {
//		int resultCode = GooglePlayServicesUtil
//				.isGooglePlayServicesAvailable(this);
//		if (resultCode != ConnectionResult.SUCCESS) {
//			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//						AppConstants.PLAY_SERVICES_RESOLUTION_REQUEST)
//						.show();
//			} else {
//				Log.i(TAG, "This device does not support Google Play Services");
//				// finish(); // Do not close app if it does not have Play Services
//			}
//			return false;
//		}
//		return true;
//	}
}
