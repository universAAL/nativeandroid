package org.universAAL.middleware.android.connectors;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.ChannelDescriptor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ConnectorCommWrapper implements ServiceConnection {
	private final static String TAG = "ConnectorCommWrapper";
	private Context context;
	private/* static */ConnectorBinder binder = null;
	// private Object binderLock = new Object();
	// With this lock I make sure no one messes with the binder while I wait for
	// it to be not null
	// Except onServiceDis/Connected. Those two can.
	private static int reserve = 0;

	public ConnectorCommWrapper(Context context) {
		this.context = context;
	}

	public void dispose(List<ChannelDescriptor> channels) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of dispose (it is a bind)"
				+ Thread.currentThread().getName());
		waitForBinder();
		if (binder != null) {
			binder.dispose(channels);
		} else {
			Log.e(TAG, "no binder received for dispose");
		}
		assureUnbind();
		// }
	}

	public synchronized void configureConnector(
			List<ChannelDescriptor> channels, String peerName) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of configureConnector (it is a bind)"
				+ Thread.currentThread().getName());
		waitForBinder();
		if (binder != null) {
			binder.configureConnector(channels, peerName);
		} else {
			Log.e(TAG, "no binder received for configureConnector");
		}
		assureUnbind();
		// }
	}

	public synchronized List<String> getGroupMembers(String group) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of getGroupMembers (it is a bind)"
				+ Thread.currentThread().getName());
		System.out.println("##### WAIT");
		waitForBinder();
		List<String> list = new ArrayList<String>();
		if (binder != null) {
			System.out.println("##### SUCCESS");
			list = binder.getGroupMembers(group);
		} else {
			System.out.println("##### FAIL !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			Log.e(TAG, "no binder received for getGroupMembers");
		}
		assureUnbind();
		return list;
		// }
	}

	public synchronized boolean hasChannel(String channelName) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of hasChannel (it is a bind)"
				+ Thread.currentThread().getName());
		waitForBinder();
		boolean result = false;
		if (binder != null) {
			result = binder.hasChannel(channelName);
		} else {
			Log.e(TAG, "no binder received for hasChannel");
		}
		assureUnbind();
		return result;
		// }
	}

	public synchronized void unicast(ChannelMessage message, String peerID) {
		Log.d(TAG, "attempting wrap of unicast (it is a broadcast)");
		Intent intent = ConnectorIntentFactory.createConnCommUnicast(message,
				peerID);
		context.sendBroadcast(intent);
	}

	public synchronized void multicast(ChannelMessage message) {
		Log.d(TAG, "attempting wrap of multicast (it is a broadcast)");
		Intent intent = ConnectorIntentFactory.createConnCommMulticast(message);
		context.sendBroadcast(intent);
	}

	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		Log.d(TAG, "on bound service: trying " + arg1);
		binder = (ConnectorBinder) arg1;
		Log.d(TAG, "on bound service: result" + (binder != null));
	}

	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "on unbound service: trying");
		binder = null;
		Log.d(TAG, "on unbound service: result " + (binder != null));
	}

	private void waitForBinder() {
		reserve++;
		System.out.println(reserve + " <<< UP TO");
		Intent intent = new Intent(context, ConnectorService.class);
		// TODO Deal with the strategy. If only bind auto create is used, the
		// service gets destroyed when unbound and connector stops working. Use
		// startService to make sure the service keeps running, but this is
		// against my goal of having connector on only when WIFI is on. But that
		// is yet to be done...
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

	private void assureUnbind() {
		reserve--;
		System.out.println(reserve + " <<< DOWN TO");
		try {
			context.unbindService(this);
		} catch (Exception ex) {
			Log.w(TAG,
					"Error attempting to unbind. Probably the service terminated before it could unbind",
					ex);
		} finally {
			// Dont set to null if I´m waiting for it not to be!
			if (reserve < 1) {
				binder = null;
				reserve = 0;// just in case
				System.out.println("0 <<< SET");
			}

			// Always set to null, because if unbind fails its because it is
			// unbound already (?)
		}
	}

}
