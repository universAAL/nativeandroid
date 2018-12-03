package org.universAAL.android.receivers;

import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.AppConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Handles requests from other apps to start/stop the MW service.
 * It also controls the state machine based on the MW service status.
 *
 * States of the MW service state machine:
 * STOPPED: Service does not exist
 * STARTING: Service is being created, or is already created and started, but the MW hasnt finished registering all apps
 * STARTED: Service is running, and MW is running, and all apps are registered in buses. Apps can now use uAAL.
 * STOPPING: Service is being destroyed, and MW is in closing procedure
 *
 * Signals to the MW service state machine:
 * SYS_START: Request the MW to start
 * SYS_STOP: Request the MW to stop
 *
 * Signals from the MW or this receiver to whoever called this
 * NOTIF_STARTED: When the MW reaches status STARTED (or was already there and asked to SYS_START)
 * NOTIF_STOPPED: When the MW reaches status STOPPED (or was already there and asked to SYS_STOP)
 *
 * Appropriate state transfers:
 * [STOPPED] >SYS_START> [STARTING] >NOTIF_STARTED> [STARTED] >SYS_STOP> [STOPPING] >NOTIF_STOPPED> [STOPPED]
 *
 * In all other cases incoming signals are ignored or redundant and have no effect on the MW service.
 * In case of ignoring an incoming signal, the out-notifying signal includes the boolean extra "ignored" with value "true".
 */
public class RestartReceiver extends BroadcastReceiver {
	private static final String TAG="RestartReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"Received Restart Intent");
		if (intent != null) {
			String action = intent.getAction();
			Intent serviceIntent = new Intent(context, MiddlewareService.class);
			if (action.equals(AppConstants.ACTION_SYS_STOP)) {
				Intent notifStopped;
				switch (MiddlewareService.mStatus){
					case AppConstants.STATUS_STOPPED:
						Log.d(TAG,"STOP WHILE STOPPED - do nothing but tell the caller it is stopped");
						//Already stopped, do nothing but tell the caller it is stopped
						notifStopped = new Intent(AppConstants.ACTION_NOTIF_STOPPED);
						context.sendBroadcast(notifStopped);
						break;
					case AppConstants.STATUS_STARTING:
						Log.d(TAG,"STOP WHILE STARTING - Tell the caller it was ignored");
						//Do nothing, to avoid conflict. Tell the caller it was ignored
						notifStopped = new Intent(AppConstants.ACTION_NOTIF_STOPPED);
						notifStopped.putExtra(AppConstants.ACTION_NOTIF_KEY_IGNORE,true);
						context.sendBroadcast(notifStopped);
						break;
					case AppConstants.STATUS_STARTED:
						Log.d(TAG,"STOP WHILE STARTED - The caller will be notified when MW stops");
						//Do the stop. The caller will be notified when MW stops
						if (!context.stopService(serviceIntent)) {
							// The service was asked to stop and it will notify, but if for some reason was already sopped:
							MiddlewareService.mStatus = AppConstants.STATUS_STOPPED;//Probably already set, but just in case
							notifStopped = new Intent(AppConstants.ACTION_NOTIF_STOPPED);
							context.sendBroadcast(notifStopped);
						}
						break;
					case AppConstants.STATUS_STOPPING:
						Log.d(TAG,"STOP WHILE STOPPING - The caller will be notified in the end by MW");
						//Do nothing, in fact it was already ignored. The caller will be notified in the end by MW.
						break;
				}
			} else if (action.equals(AppConstants.ACTION_SYS_START)) {
				Intent notifStarted;
				switch (MiddlewareService.mStatus){
					case AppConstants.STATUS_STOPPED:
						Log.d(TAG,"START WHILE STOPPED - The caller will be notified when MW is started");
						//Do the start. The caller will be notified when MW is started
						context.startService(serviceIntent);
						break;
					case AppConstants.STATUS_STARTING:
						Log.d(TAG,"START WHILE STARTING - The caller will be notified in the end by MW");
						//Do nothing, in fact it was already ignored. The caller will be notified in the end by MW.
						break;
					case AppConstants.STATUS_STARTED:
						Log.d(TAG,"START WHILE STARTED - do nothing but tell the caller it is started");
						//Already started, do nothing but tell the caller it is started
						notifStarted = new Intent(AppConstants.ACTION_NOTIF_STARTED);
						context.sendBroadcast(notifStarted);
						break;
					case AppConstants.STATUS_STOPPING:
						Log.d(TAG,"START WHILE STOPPING - Tell the caller it was ignored");
						//Do nothing, to avoid conflict. Tell the caller it was ignored
						notifStarted = new Intent(AppConstants.ACTION_NOTIF_STARTED);
						notifStarted.putExtra(AppConstants.ACTION_NOTIF_KEY_IGNORE,true);
						context.sendBroadcast(notifStarted);
						break;
				}
			}
		}
	}

}
