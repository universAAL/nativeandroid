package org.universAAL.android.activities;

import org.universAAL.android.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	//TODO These have to be the same values as in MiddlewareService. Make them public, or in Constants? 
	private static final String MY_WIFI = "home_wifi";
	private static final String NO_WIFI = "uAALGhostWifi";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		Preference wifiPref = (Preference) findPreference("setting_resetwifi_key");
		String wifiVal=PreferenceManager.getDefaultSharedPreferences(this).getString(MY_WIFI, NO_WIFI);
		if(wifiVal.equals(NO_WIFI)){
			wifiPref.setSummary(R.string.setting_resetwifi_text_none);
			wifiPref.setEnabled(false);
		}else{
			wifiPref.setSummary(R.string.setting_resetwifi_text);
			wifiPref.setEnabled(true);
			wifiPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().remove(MY_WIFI).commit();
					preference.setEnabled(false);
					return true;
				}
			});
		}
	}

}
