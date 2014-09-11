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
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.RAPIManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		// Manage the availability of "remove current wifi" option
		Preference wifiPref = (Preference) findPreference("setting_resetwifi_key");
		String wifiVal = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(IntentConstants.MY_WIFI, IntentConstants.NO_WIFI);
		if (wifiVal.equals(IntentConstants.NO_WIFI)) {
			wifiPref.setSummary(R.string.setting_resetwifi_text_none);
			wifiPref.setEnabled(false);
		} else {
			wifiPref.setSummary(R.string.setting_resetwifi_text);
			wifiPref.setEnabled(true);
			wifiPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					PreferenceManager
							.getDefaultSharedPreferences(SettingsActivity.this)
							.edit().remove(IntentConstants.MY_WIFI).commit();
					preference.setEnabled(false);
					return true;
				}
			});
		}
		// Manage the registration on Google Play Services GCM when selecting R-API conn type
		ListPreference connType = (ListPreference) findPreference("setting_conntype_key");
		connType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (newValue.equals("1")) {
					// Set to R-API -> Check Play Services
					if (checkPlayServices()) {
						String mRegID = RAPIManager
								.getRegistrationId(getApplicationContext());
						if (mRegID.isEmpty()) {
							RAPIManager
									.registerInThread(getApplicationContext());
						}
						return true;
					} else {
						Toast.makeText(getApplicationContext(),
								R.string.warning_gplay, Toast.LENGTH_LONG)
								.show();
						// Do not block the app from running if Play Services is not available
						return false;// just dont allow change
					}
				} else {
					return true;// Allow the change
				}
			}

		});
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
						IntentConstants.PLAY_SERVICES_RESOLUTION_REQUEST)
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
