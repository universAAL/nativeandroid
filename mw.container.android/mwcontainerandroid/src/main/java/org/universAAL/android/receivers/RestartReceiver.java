package org.universAAL.android.receivers;

import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.AppConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			Intent serviceIntent = new Intent(context, MiddlewareService.class);
			if (action.equals(AppConstants.ACTION_SYS_STOP)) {
				if(!context.stopService(serviceIntent)){
				    // If already stopped, send notif anyway
				    Intent notifStopped = new Intent(AppConstants.ACTION_NOTIF_STOPPED);
				    context.sendBroadcast(notifStopped);
				}
			} else if (action.equals(AppConstants.ACTION_SYS_START)) {
				context.startService(serviceIntent);
			} /*else if (action.equals(AppConstants.ACTION_SYS_RESTART)) {
				context.stopService(serviceIntent);
				context.startService(serviceIntent);
			}*/
		}
	}

}
