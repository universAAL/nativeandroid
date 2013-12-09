package org.universAAL.android.receivers.system;

//import org.universAAL.android.services.ScanService;
import org.universAAL.android.services.ScanService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// A change in packages happened. Relay to ScanService who will know what to do.
		Log.v("PackageReceiver", "Received Broadcast: "+intent.getAction());
		Intent start = new Intent(intent);
		start.setClass(context, ScanService.class);
		context.startService(start);
	}

}
