package org.universAAL.middleware.android.modules;

import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.connectors.util.ChannelMessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ModulesCommWrapper implements ServiceConnection {
	private final static String TAG = "ModulesCommWrapper";
	private Context context;
	private ModulesBinder binder = null;

	// No need for binder lock here, because we never unbind (?)

	public ModulesCommWrapper(Context context) {
		this.context = context;
	}

	public synchronized void messageReceived(ChannelMessage channelMessage) {
		Intent intent = ModulesIntentFactory
				.createCommModReceived(channelMessage);
		context.sendBroadcast(intent);
	}

	public synchronized AndroidContextBusImpl createContextBus() {
		waitForBInder();
		if (binder != null) {
			return binder.createContextBus();
		} else {
			Log.e(TAG, "no binder received for createContextBus");
			return null;
		}
	}

	public synchronized AndroidServiceBusImpl createServiceBus() {
		waitForBInder();
		if (binder != null) {
			return binder.createServiceBus();
		} else {
			Log.e(TAG, "no binder received for createContextBus");
			return null;
		}
	}

	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (ModulesBinder) service;
	}

	public void onServiceDisconnected(ComponentName name) {
		binder = null;
	}

	private void waitForBInder() {
		Intent intent = new Intent(context, ModulesService.class);
		// TODO Deal with the strategy. If only bind auto create is used, the
		// service gets destroyed when unbound and connector stops working. Use
		// startService to make sure the service keeps running. In this case it
		// is yet to be seen if it´s possible to have the buses wake on demand.
		context.startService(intent);
		boolean res = context.bindService(intent, this, 0);
		// boolean res=context.bindService(intent, this,
		// Context.BIND_AUTO_CREATE);
		Log.d(TAG, "result of bind:" + res);
		// Wait for binding
		int i = 0;
		while (binder == null && i < 20) {
			synchronized (this) {
				if (binder == null) {
					try {
						i++;
						wait(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// Is it necessary to unbind? It works without it. Because Modules is always
	// on.
}
