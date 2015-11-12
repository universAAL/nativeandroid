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
import android.util.Log;

public class SettingsReceiver extends BroadcastReceiver {

	private static final String TAG="SettingsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received Config Intent");
		Bundle extras = intent.getExtras();
		if(extras!=null && !extras.isEmpty()){
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			if(extras.containsKey(AppConstants.Keys.CONNUSR)){
				    editor.putString(AppConstants.Keys.CONNUSR, extras.getString(AppConstants.Keys.CONNUSR));
			}
			if(extras.containsKey(AppConstants.Keys.CONNPWD)){
				    editor.putString(AppConstants.Keys.CONNPWD, extras.getString(AppConstants.Keys.CONNPWD));
			}
			if(extras.containsKey(AppConstants.Keys.ISCOORD)){
				editor.putBoolean(AppConstants.Keys.ISCOORD, extras.getBoolean(AppConstants.Keys.ISCOORD));
			}
			if(extras.containsKey(AppConstants.Keys.UIHANDLER)){
				editor.putBoolean(AppConstants.Keys.UIHANDLER, extras.getBoolean(AppConstants.Keys.UIHANDLER));
			}
			if(extras.containsKey(AppConstants.Keys.CONNWIFI)){
				editor.putString(AppConstants.Keys.CONNWIFI, extras.getString(AppConstants.Keys.CONNWIFI));
			}
			if(extras.containsKey(AppConstants.Keys.CFOLDER)){
				editor.putString(AppConstants.Keys.CFOLDER, extras.getString(AppConstants.Keys.CFOLDER));
			}
			if(extras.containsKey(AppConstants.Keys.OFOLDER)){
				editor.putString(AppConstants.Keys.OFOLDER, extras.getString(AppConstants.Keys.OFOLDER));
			}
			if(extras.containsKey(AppConstants.Keys.IFOLDER)){
				editor.putString(AppConstants.Keys.IFOLDER, extras.getString(AppConstants.Keys.IFOLDER));
			}
			if(extras.containsKey(AppConstants.Keys.USER)){
				editor.putString(AppConstants.Keys.USER, extras.getString(AppConstants.Keys.USER));
			}
			if(extras.containsKey(AppConstants.Keys.TYPE)){
				editor.putString(AppConstants.Keys.TYPE, Integer.toString(extras.getInt(AppConstants.Keys.TYPE)));
			}
			if(extras.containsKey(AppConstants.Keys.CONNMODE)){
				editor.putString(AppConstants.Keys.CONNMODE, Integer.toString(extras.getInt(AppConstants.Keys.CONNMODE)));
			}
			if(extras.containsKey(AppConstants.Keys.CONNTYPE)){
				editor.putString(AppConstants.Keys.CONNTYPE, Integer.toString(extras.getInt(AppConstants.Keys.CONNTYPE)));
			}
			if(extras.containsKey(AppConstants.Keys.CONNURL)){
				editor.putString(AppConstants.Keys.CONNURL, extras.getString(AppConstants.Keys.CONNURL));
			}
			if(extras.containsKey(AppConstants.Keys.CONNGCM)){
				editor.putString(AppConstants.Keys.CONNGCM, extras.getString(AppConstants.Keys.CONNGCM));
			}
			// permission with "signatureOrSystem"?
			editor.commit();
			// When a human user changes settings, SettingsActivity restarts the
			// MW when closed, but here settings are changed without going through SettingsActivity
			Config.load(context); // Sync Preferences in Config util now that they are changed
//			Intent stopServiceIntent = new Intent(context, MiddlewareService.class);
			// Notify whoever listens that we are configured. Do it before messing with the service
			Log.d(TAG, "Notify caller");
			Intent notifConfig = new Intent(AppConstants.ACTION_NOTIF_CONFIG);
			context.sendBroadcast(notifConfig);
//			boolean stopped = context.stopService(stopServiceIntent);
//			if (stopped) {// The service was running, restart it
//				Intent startServiceIntent = new Intent(context, MiddlewareService.class);
//				context.startService(startServiceIntent);
//			}
		}
	}

}
