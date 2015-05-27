package org.universAAL.android.receivers;

import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.Config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if(extras!=null && !extras.isEmpty()){
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			if(extras.containsKey("setting_connusr_key")){
				    editor.putString("setting_connusr_key", extras.getString("setting_connusr_key"));
			}
			if(extras.containsKey("setting_connpwd_key")){
				    editor.putString("setting_connpwd_key", extras.getString("setting_connpwd_key"));
			}
			if(extras.containsKey("setting_iscoord_key")){
				editor.putBoolean("setting_iscoord_key", extras.getBoolean("setting_iscoord_key"));
			}
			if(extras.containsKey("setting_uihandler_key")){
				editor.putBoolean("setting_uihandler_key", extras.getBoolean("setting_uihandler_key"));
			}
			if(extras.containsKey("setting_connwifi_key")){
				editor.putString("setting_connwifi_key", extras.getString("setting_connwifi_key"));
			}
			if(extras.containsKey("setting_cfolder_key")){
				editor.putString("setting_cfolder_key", extras.getString("setting_cfolder_key"));
			}
			if(extras.containsKey("setting_ofolder_key")){
				editor.putString("setting_ofolder_key", extras.getString("setting_ofolder_key"));
			}
			if(extras.containsKey("setting_ifolder_key")){
				editor.putString("setting_ifolder_key", extras.getString("setting_ifolder_key"));
			}
			if(extras.containsKey("setting_user_key")){
				editor.putString("setting_user_key", extras.getString("setting_user_key"));
			}
			if(extras.containsKey("setting_type_key")){
				editor.putString("setting_type_key", Integer.toString(extras.getInt("setting_type_key")));
			}
			if(extras.containsKey("setting_connmode_key")){
				editor.putString("setting_connmode_key", Integer.toString(extras.getInt("setting_connmode_key")));
			}
			if(extras.containsKey("setting_conntype_key")){
				editor.putString("setting_conntype_key", Integer.toString(extras.getInt("setting_conntype_key")));
			}
			if(extras.containsKey("setting_connurl_key")){
				editor.putString("setting_connurl_key", extras.getString("setting_connurl_key"));
			}
			if(extras.containsKey("setting_conngcm_key")){
				editor.putString("setting_conngcm_key", extras.getString("setting_conngcm_key"));
			}
			// permission with "signatureOrSystem"?
			editor.commit();
			// When a human user changes settings, SettingsActivity restarts the
			// MW when closed, but here settings are changed without going through SettingsActivity
			Config.load(context); // Sync Preferences in Config util now that they are changed
			Intent stopServiceIntent = new Intent(context, MiddlewareService.class);
			// Notify whoever listens that we are configured. Do it before messing with the service
			Intent notifConfig = new Intent(AppConstants.ACTION_NOTIF_CONFIG);
			context.sendBroadcast(notifConfig);
			boolean stopped = context.stopService(stopServiceIntent);
			if (stopped) {// The service was running, restart it
				Intent startServiceIntent = new Intent(context, MiddlewareService.class);
				context.startService(startServiceIntent);
			}
		}
	}

}
