package org.universAAL.android.receivers.system;

import org.universAAL.android.services.MiddlewareService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// A change in Wifi happened. Whether its ON or OFF send the right command to MWService, Do not start/stop from here.
		Log.v("WifiReceiver", "Received Broadcast: "+intent.getAction());
		Intent start = new Intent(intent);
		start.setClass(context, MiddlewareService.class);
		context.startService(start);
	}

}
