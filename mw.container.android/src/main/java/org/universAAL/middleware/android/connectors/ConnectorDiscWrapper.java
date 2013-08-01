package org.universAAL.middleware.android.connectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.android.modules.impl.AndroidAALSpaceModuleImpl;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ConnectorDiscWrapper implements ServiceConnection {
	private final static String TAG = "ConnectorDiscWrapper";
	private Context context;
	private/* static */ConnectorBinder binder = null;
	// private Object binderLock = new Object();
	private static int reserve = 0;

	public ConnectorDiscWrapper(Context context) {
		this.context = context;
	}

	public synchronized Collection<? extends AALSpaceCard> findAALSpace() {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of findAALSpace (it is a bind)");
		waitForBInder();
		List<AALSpaceCard> list = new ArrayList<AALSpaceCard>();
		if (binder != null) {
			list = binder.findAALSpace();
		} else {
			Log.e(TAG, "no binder received for findAALSpace");
		}
		assureUnbind();
		return list;
		// }
	}

	public synchronized Collection<? extends AALSpaceCard> findAALSpace(
			Dictionary<String, String> filters) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of findAALSpace filter (it is a bind)");
		waitForBInder();
		List<AALSpaceCard> list = new ArrayList<AALSpaceCard>();
		if (binder != null) {
			list = binder.findAALSpace(filters);
		} else {
			Log.e(TAG, "no binder received for findAALSpace");
		}
		assureUnbind();
		return list;
		// }
	}

	public synchronized void announceAALSpace(AALSpaceCard aalSpaceCard) {
		Log.d(TAG, "attempting wrap of announceAALSpace (it is a broadcast)");
		Intent intent = ConnectorIntentFactory
				.createConnDiscAnnounceBusses(aalSpaceCard);
		context.sendBroadcast(intent);
	}

	public synchronized void deregisterAALSpace(AALSpaceCard spaceCard) {
		Log.d(TAG, "attempting wrap of deregisterAALSpace (it is a broadcast)");
		Intent intent = ConnectorIntentFactory
				.createConnDiscDeregister(spaceCard);
		context.sendBroadcast(intent);
	}

	public synchronized void addAALSpaceListener(
			AndroidAALSpaceModuleImpl listener) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of addAALSpaceListener (it is a bind)");
		waitForBInder();
		if (binder != null) {
			binder.addAALSpaceListener(listener);
		} else {
			Log.e(TAG, "no binder received for addAALSpaceListener");
		}
		assureUnbind();
		// }
	}

	public synchronized void removeAALSpaceListener(
			AndroidAALSpaceModuleImpl androidAALSpaceModuleImpl) {
		// synchronized (binderLock) {
		Log.d(TAG, "attempting wrap of removeAALSpaceListener (it is a bind)");
		waitForBInder();
		if (binder != null) {
			binder.removeAALSpaceListener(androidAALSpaceModuleImpl);
		} else {
			Log.e(TAG, "no binder received for removeAALSpaceListener");
		}
		assureUnbind();
		// }
	}

	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		Log.d(TAG, "on bound service: trying " + arg1);
		binder = (ConnectorBinder) arg1;
		Log.d(TAG, "on bound service: result " + (binder != null));
	}

	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "on unbound service: trying");
		binder = null;
		Log.d(TAG, "on unbound service: result " + (binder != null));
	}

	private void waitForBInder() {
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
