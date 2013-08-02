/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.android.connectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.connectors.messages.handlers.ConnectorHandlerFactory;
import org.universAAL.middleware.android.connectors.impl.AndroidJGroupsCommunicationConnector;
import org.universAAL.middleware.android.connectors.impl.AndroidSLPDiscoveryConnector;
import org.universAAL.middleware.android.connectors.messages.ConnectorMessageFactory;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class ConnectorService extends Service {

	private static final String TAG = "ConnectorService";
	private static final int ONGOING_NOTIFICATION = 11072013;
	protected ConnectorBinder binder = new ConnectorBinder(this);
	private AndroidJGroupsCommunicationConnector jgroupsCommunicationConnector;
	private AndroidSLPDiscoveryConnector slpDiscoveryConnector;
	private boolean started = false;
	private MulticastLock m_lock;

	@Override
	public void onCreate() {
		super.onCreate();

		// These properties must be set here, not from file
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Stack", "false");
		System.setProperty("java.net.preferIPv4Addresses", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		System.setProperty("jgroups.use.jdk_logger ", "true");
		System.setProperty("net.slp.port", "5555");

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		m_lock = wifi.createMulticastLock("felix_multicast");
		m_lock.acquire();

		// TODO Check! Supposedly, this starts as foreground but without
		// notification
		Notification notif = new Notification(0, null,
				System.currentTimeMillis());
		notif.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(ONGOING_NOTIFICATION, notif);
		Log.d(TAG, "Creating the Service");
		new Thread(new Runnable() {
			public void run() {
				// TODO CHECK WIFI. IF NOT ON, DONT START
				// Start JGroups
				jgroupsCommunicationConnector = new AndroidJGroupsCommunicationConnector(
						new ModulesCommWrapper(ConnectorService.this));
				Properties jGroupDCOnnector = getProperties("mw.connectors.communication.jgroups.core");
				if (jGroupDCOnnector != null) {
					jgroupsCommunicationConnector
							.loadConfigurations(jGroupDCOnnector);
				}

				// Start JSLP
				slpDiscoveryConnector = new AndroidSLPDiscoveryConnector();
				Properties slpDConnectorProperties = getProperties("mw.connectors.discovery.slp.core");
				if (slpDConnectorProperties != null) {
					slpDiscoveryConnector
							.loadConfigurations(slpDConnectorProperties);
					slpDiscoveryConnector.init();
				}
				started = true;
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		started = false;
		Log.d(TAG, "Destroying the Service");
		if (jgroupsCommunicationConnector != null) {
			jgroupsCommunicationConnector.dispose();
		}
		if (slpDiscoveryConnector != null) {
			slpDiscoveryConnector.dispose();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "Binding the Service: " + (binder != null));
		return binder;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		Log.d(TAG,
				"onStartCommand has been called with action ["
						+ intent.getAction() + "]");
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

	private void handleCommand(Intent intent) {
		Log.d(TAG,
				"Is about to handle command with action [" + intent.getAction()
						+ "]");
		// Analyze the intent - create the message
		IMessage message = ConnectorMessageFactory.createMessage(this, intent);
		// Create the handler
		IMessageHandler handler = ConnectorHandlerFactory.createHandler(
				intent.getAction(), this);
		// Handle the message
		message.handle(handler);
	}

	public AndroidJGroupsCommunicationConnector getJgroupsCommunicationConnector() {
		Log.d(TAG, "Waiting for JGroups to be initialized");
		waitForStart();
		if (jgroupsCommunicationConnector == null || started == false) {
			Log.w(TAG, "JGroups not initialized in time!!!!!");
		}
		return jgroupsCommunicationConnector;
	}

	public AndroidSLPDiscoveryConnector getSlpDiscoveryConnector() {
		Log.d(TAG, "Waiting for SLP to be initialized");
		waitForStart();
		if (slpDiscoveryConnector == null || started == false) {
			Log.w(TAG, "SLP not initialized in time!!!!!");
		}
		return slpDiscoveryConnector;
	}

	// TODO handle this with sync and wait and wake and thread ...
	private void waitForStart() {
		int i = 0;
		while (started == false && i < 20) {
			synchronized (this) {
				if (started == false) {
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

	// TODO move to utils class. And change folder to own!!
	private Properties getProperties(String file) {
		Properties prop = new Properties();
		try {
			File conf = new File(Environment.getExternalStorageDirectory()
					.getPath(), "/data/felix/configurations/etc/" + file
					+ ".properties");
			InputStream in = new FileInputStream(conf);
			prop.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			Log.w(TAG, "Properties file does not exist: " + file);
		} catch (IOException e) {
			Log.w(TAG, "Error reading props file: " + file);
		}
		return prop;
	}

}
