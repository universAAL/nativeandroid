/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.android.upnp.service;

import java.io.InputStream;

import org.teleal.cling.UpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.sodapop.R;
import org.universAAL.middleware.android.upnp.intents.UPnPIntentFactory;
import org.universAAL.middleware.android.upnp.messages.UPnPMessageFactory;
import org.universAAL.middleware.android.upnp.messages.handlers.UPnPSodaPopHandlerFactory;
import org.universAAL.middleware.android.upnp.plainjava.importer.ImportingSodaPopPeerProxy;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class UPnPSodaPopPeersService extends AndroidUpnpServiceImpl implements
	IRemotePeerStateUpdateListener {

    private static final String TAG = UPnPSodaPopPeersService.class.getCanonicalName();
    protected UPnPAndroidServiceBinder binder = new UPnPAndroidServiceBinder();
    private InputStream deviceDescription;

    private ImportingSodaPopPeerProxy importer;

    @Override
    public void onCreate() {
	super.onCreate();

	Log.d(TAG, "onCreate has been called");

	// Show some notification so the user will be aware that the service is
	// running
	startInForeground();

	if (upnpService != null) { // checking upnp service initialization
				   // result (it might fail)
	    binder.setService(upnpService); // on success setting new fully
					    // created upnp service to the
					    // previously returned binder
	} else {
	    Log.e(TAG, "Starting UPnP service failed! Can not continue!");
	}

	deviceDescription = getResources().openRawResource(R.raw.device_description); // Getting
										      // the
										      // input
										      // stream
										      // to
										      // read
										      // for
										      // UPnP
										      // device
										      // description

	// Initiated the RemoteServices container + add a listener
	RemoteServicesContainer.getInstance().addListener(this);

	// Import remote devices
	importer = new ImportingSodaPopPeerProxy();
	importer.importDevices(upnpService);
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return binder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
	Log.d(TAG, "onStartCommand has been called with action [" + intent.getAction() + "]");

	if (null != intent.getAction()) {
	    Thread commandThread = new Thread() {

		@Override
		public void run() {
		    handleCommand(intent);
		}

	    };
	    commandThread.start();
	}

	return START_STICKY;
    }

    public UpnpService getUPnPService() {
	return upnpService;
    }

    public InputStream getDeviceDescription() {
	return deviceDescription;
    }

    private void handleCommand(Intent intent) {

	Log.d(TAG, "Is about to handle command with action [" + intent.getAction() + "]");

	// Analyze the intent - create the message
	IMessage message = UPnPMessageFactory.createMessage(this, intent);

	// Create the handler
	IMessageHandler handler = UPnPSodaPopHandlerFactory.createHandler(intent.getAction());

	// Handle the message
	message.handle(handler);
    }

    private void startInForeground() {
	int icon = R.drawable.uaal;
	String serviceTitle = getResources().getString(R.string.UPnPServiceTitle);
	String serviceDescription = getResources().getString(R.string.UPnPServiceDescription);
	long when = System.currentTimeMillis();

	ComponentName comp = new ComponentName(this.getPackageName(), getClass().getName());
	Intent intent = new Intent().setComponent(comp);
	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
		Intent.FLAG_ACTIVITY_NEW_TASK);

	Notification notification = new Notification(icon, serviceDescription, when);
	notification.setLatestEventInfo(this, serviceTitle, serviceDescription, pendingIntent);

	startForeground(1, notification);
    }

    public void remotePeerAdded(String peerID) {
	// Create the intent
	Intent peerAddedIntent = UPnPIntentFactory.createNoticeJoiningRemoteSodaPopPeer(peerID,
		IAndroidSodaPop.PROTOCOL_UPNP);

	// Send broadcast message
	sendBroadcast(peerAddedIntent);
    }

    public void remotePeerRemoved(String peerID) {
	// Create the intent
	Intent peerRemovedIntent = UPnPIntentFactory.createNoticeLeavingRemoteSodaPopPeer(peerID,
		IAndroidSodaPop.PROTOCOL_UPNP);

	// Send broadcast message
	sendBroadcast(peerRemovedIntent);
    }
}
