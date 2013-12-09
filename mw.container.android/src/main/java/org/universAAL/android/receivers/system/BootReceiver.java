package org.universAAL.android.receivers.system;

import org.universAAL.android.services.MiddlewareService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Device has booted. Launch the MW service forever (scan packages is called from MWService)
		Log.v("BootReceiver", "Received Broadcast: "+intent.getAction());
		Intent start = new Intent(context, MiddlewareService.class);
		context.startService(start);
	}

}
