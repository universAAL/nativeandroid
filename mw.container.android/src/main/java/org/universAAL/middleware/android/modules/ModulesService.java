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
package org.universAAL.middleware.android.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.connectors.ConnectorCommWrapper;
import org.universAAL.middleware.android.connectors.ConnectorDiscWrapper;
import org.universAAL.middleware.android.container.BundleConfigHome;
import org.universAAL.middleware.android.container.ContextEmulator;
import org.universAAL.middleware.android.container.uAALBundleContainer;
import org.universAAL.middleware.android.modules.impl.AndroidAALSpaceManagerImpl;
import org.universAAL.middleware.android.modules.impl.AndroidAALSpaceModuleImpl;
import org.universAAL.middleware.android.modules.impl.AndroidCommunicationModuleImpl;
import org.universAAL.middleware.android.modules.impl.AndroidControlBroker;
import org.universAAL.middleware.android.modules.messages.ModulesMessageFactory;
import org.universAAL.middleware.android.modules.messages.handlers.ModulesHandlerFactory;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.impl.ServiceBusImpl;

import ae.com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import ae.com.sun.xml.bind.v2.model.annotation.XmlSchemaMine;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class ModulesService extends Service {

	private static final String TAG = "ModulesService";
	protected ModulesBinder binder = new ModulesBinder(this);
	private static final int ONGOING_NOTIFICATION = 16072013;
	private static final int WAIT_TICKS = 200;
	private static final long WAIT_PERIOD_MS = 500;

	public static ContextEmulator context;

	private AndroidCommunicationModuleImpl communicationModule;
	private AndroidAALSpaceModuleImpl aalSpaceModule;
	private AndroidControlBroker controlBroker;
	private AndroidAALSpaceManagerImpl spaceManager;
	private AndroidContextBusImpl contextBus;
	private AndroidServiceBusImpl serviceBus;
	// private DeployManagerImpl deployManager;
	private boolean started = false;

	@Override
	public void onCreate() {
		super.onCreate();

		RuntimeInlineAnnotationReader
				.cachePackageAnnotation(
						org.universAAL.middleware.connectors.deploy.karaf.model.ObjectFactory.class
								.getPackage(),
						new XmlSchemaMine(
								"http://karaf.apache.org/xmlns/features/v1.0.0"));
		RuntimeInlineAnnotationReader
				.cachePackageAnnotation(
						org.universAAL.middleware.deploymanager.uapp.model.ObjectFactory.class
								.getPackage(), new XmlSchemaMine(
								"http://www.universaal.org/aal-uapp/v1.0.0"));
		RuntimeInlineAnnotationReader
				.cachePackageAnnotation(
						org.universAAL.middleware.interfaces.aalspace.model.ObjectFactory.class
								.getPackage(),
						new XmlSchemaMine(
								"http://universaal.org/aalspace-channel/v1.0.0"));

		// TODO Check! Supposedly, this starts as foreground but without
		// notification
		Notification notif = new Notification(0, null,
				System.currentTimeMillis());
		notif.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(ONGOING_NOTIFICATION, notif);
		Log.d(TAG, "Creating the Service");
		new Thread(new Runnable() {
			public void run() {
				// OSGi Bundle Context emulation: tracks instances to share
				context = ContextEmulator.createContextEmulator();

				// //////////////////COMMUNICATION MODULE///////////////////
				ModuleContext modContext3 = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.modules.communication.osgi" });
				communicationModule = new AndroidCommunicationModuleImpl(
						new ConnectorCommWrapper(ModulesService.this),
						ModulesService.this);
				communicationModule.init();
				uAALBundleContainer.THE_CONTAINER.shareObject(modContext3,
						communicationModule,
						new Object[] { CommunicationModule.class.getName() });
				Log.d(TAG, "Started the CommunicationModule...");

				// ///////////////////AALSPACE MODULE//////////////////////
				ModuleContext modContext4 = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.modules.aalspace.osgi" });
				aalSpaceModule = new AndroidAALSpaceModuleImpl(
						new ConnectorDiscWrapper(ModulesService.this),
						modContext4);
				Dictionary aalSpaceModuleProp = getProperties("mw.modules.aalspace.core");
				if (aalSpaceModuleProp != null) {
					aalSpaceModule.loadConfigurations(aalSpaceModuleProp);
				} else
					aalSpaceModule
							.loadConfigurations(new Hashtable<String, String>());
				aalSpaceModule.init();
				uAALBundleContainer.THE_CONTAINER.shareObject(modContext4,
						aalSpaceModule,
						new Object[] { AALSpaceModule.class.getName() });
				Log.d(TAG, "AALSpaceModule registered");

				// ///////////////////CONTROL BROKER///////////////////////
				ModuleContext modContext5 = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.brokers.control.osgi" });
				controlBroker = new AndroidControlBroker(modContext5);
				uAALBundleContainer.THE_CONTAINER.shareObject(modContext5,
						controlBroker,
						new Object[] { AndroidControlBroker.class.getName() });
				Log.d(TAG, "Started ControlBroker!");

				// //////////////////AALSPACE MANAGER//////////////////////
				ModuleContext modContext6 = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.managers.aalspace.osgi" });
				BundleConfigHome confHome = new BundleConfigHome(modContext6
						.getID());// TODO !!!!!!!!!!!
				spaceManager = new AndroidAALSpaceManagerImpl(modContext6,
						confHome.getAbsolutePath());
				Dictionary aalSpaceManagerProps = getProperties("mw.managers.aalspace.core");
				if (aalSpaceManagerProps == null) {
					aalSpaceManagerProps = new Hashtable<String, String>();
				} else {
					spaceManager.loadConfigurations(aalSpaceManagerProps);
				}
				spaceManager.init();
				uAALBundleContainer.THE_CONTAINER.shareObject(modContext6,
						spaceManager,
						new String[] { AALSpaceManager.class.getName(),
								AALSpaceEventHandler.class.getName() });
				Log.d(TAG, "Registered");

				/*
				 * ////////////////////DEPLOY
				 * MANAGER////////////////////////////// ModuleContext
				 * modContext7 = uAALBundleContainer.THE_CONTAINER
				 * .registerModule(new Object[] {
				 * context,"mw.managers.deploy.osgi" }); deployManager = new
				 * DeployManagerImpl(modContext7, confHome); Dictionary
				 * deployManagerProps =
				 * getProperties("mw.managers.deploy.core"); if
				 * (deployManagerProps == null) { deployManagerProps = new
				 * Hashtable<String, String>(); } else {
				 * deployManager.loadConfigurations(deployManagerProps); }
				 * deployManager.init();
				 * uAALBundleContainer.THE_CONTAINER.shareObject( modContext7,
				 * deployManager, new Object[] { DeployManager.class.getName()
				 * });
				 */

				// ///////////////////DATA REPRESENTATION/////////////////
				SharedResources.moduleContext = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.data.representation.osgi" });
				try {
					SharedResources.loadReasoningEngine();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SharedResources.setDefaults();
				
				// ///////////////////DATA SERIALIZATION//////////////////
				TurtleUtil.moduleContext = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.data.serialization.osgi" });
				uAALBundleContainer.THE_CONTAINER
						.shareObject(TurtleUtil.moduleContext,
								new TurtleSerializer(),
								new Object[] { MessageContentSerializer.class
										.getName() });
				uAALBundleContainer.THE_CONTAINER.shareObject(
						TurtleUtil.moduleContext, new TurtleSerializer(),
						new Object[] { MessageContentSerializerEx.class
								.getName() });

				// //////////////////BUS MODEL///////////////////////////
				ModuleContext modContext8 = uAALBundleContainer.THE_CONTAINER
						.registerModule(new Object[] { context,
								"mw.bus.model.osgi" });
				AbstractBus.initBrokerage(modContext8, spaceManager,
						communicationModule);
				BusMessage.setThisPeer(spaceManager.getMyPeerCard());
				BusMessage
						.setMessageContentSerializer((MessageContentSerializer) uAALBundleContainer.THE_CONTAINER
								.fetchSharedObject(
										modContext8,
										new Object[] { MessageContentSerializer.class
												.getName() }));

				started = true;
				Log.d(TAG, "All the Modules have been STARTED");
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying the Service");
		started = false;
		if (communicationModule != null) {
			communicationModule.dispose();
		}
		if (aalSpaceModule != null) {
			aalSpaceModule.dispose();
		}
		if (controlBroker != null) {
			controlBroker.dispose();
		}
		if (spaceManager != null) {
			spaceManager.dispose();
		}
		context = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		if (intent != null) {
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
		}
		return START_STICKY;
	}

	private void handleCommand(Intent intent) {
		Log.d(TAG,
				"Is about to handle command with action [" + intent.getAction()
						+ "]");
		// Analyze the intent - create the message
		IMessage message = ModulesMessageFactory.createMessage(this, intent);
		// Create the handler
		IMessageHandler handler = ModulesHandlerFactory.createHandler(
				intent.getAction(), this);
		// Handle the message
		message.handle(handler);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "Binding the Service: " + (binder != null));
		return binder;
	}

	public AndroidCommunicationModuleImpl getCommunicationModule() {
		Log.d(TAG, "Waiting for communicationModule to be initialized");
		waitForStart();
		if (communicationModule == null || started == false) {
			Log.w(TAG, "communicationModule not initialized in time!!!!!");
		}
		return communicationModule;
	}

	public AndroidAALSpaceModuleImpl getAalSpaceModule() {
		Log.d(TAG, "Waiting for aalSpaceModule to be initialized");
		waitForStart();
		if (aalSpaceModule == null || started == false) {
			Log.w(TAG, "aalSpaceModule not initialized in time!!!!!");
		}
		return aalSpaceModule;
	}

	public AndroidControlBroker getControlBroker() {
		Log.d(TAG, "Waiting for controlBroker to be initialized");
		waitForStart();
		if (controlBroker == null || started == false) {
			Log.w(TAG, "controlBroker not initialized in time!!!!!");
		}
		return controlBroker;
	}

	public AndroidAALSpaceManagerImpl getSpaceManager() {
		Log.d(TAG, "Waiting for spaceManager to be initialized");
		waitForStart();
		if (spaceManager == null || started == false) {
			Log.w(TAG, "spaceManager not initialized in time!!!!!");
		}
		return spaceManager;
	}

	public AndroidContextBusImpl createContextBus() {
		Log.d(TAG, "Waiting for Context Bus to be created");
		waitForStart();
		Log.d(TAG, "Waited for :" + WAIT_PERIOD_MS * WAIT_TICKS);
		if (!started) {
			Log.w(TAG, "Modules not initialized in time!!!!!");
			return null;
		} else if (contextBus == null) {
			Object[] busFetchParams = new Object[] { ContextBus.class.getName() };
			ModuleContext modContext = uAALBundleContainer.THE_CONTAINER
					.registerModule(new Object[] { context,
							"mw.bus.context.osgi" });
			// PATCH I need to do this because Csub/pubs call
			// ContextBusImpl.busFetchParams and I have no other way to set
			// those fetch params, and cannot change the classes calling them
			ContextBusImpl.startModule(uAALBundleContainer.THE_CONTAINER,
					modContext, null, busFetchParams);
			ContextBusImpl.stopModule();
			// In theory that bus is no more, but the static fetch params remain
			contextBus = AndroidContextBusImpl.startModule(
					uAALBundleContainer.THE_CONTAINER, modContext,
					busFetchParams, busFetchParams, this);
		}
		return contextBus;
	}

	public AndroidServiceBusImpl createServiceBus() {
		Log.d(TAG, "Waiting for Service Bus to be created");
		waitForStart();
		Log.d(TAG, "Waited for :" + WAIT_PERIOD_MS * WAIT_TICKS);
		if (!started) {
			Log.w(TAG, "Modules not initialized in time!!!!!");
			return null;
		} else if (serviceBus == null) {
			Object[] busFetchParams = new Object[] { ServiceBus.class.getName() };
			ModuleContext modContext = uAALBundleContainer.THE_CONTAINER
					.registerModule(new Object[] { context,
							"mw.bus.service.osgi" });
			// PATCH I need to do this because Cee/Cer call
			// ServiceBusImpl.busFetchParams and I have no other way to set
			// those fetch params, and cannot change the classes calling them
			ServiceBusImpl.startModule(uAALBundleContainer.THE_CONTAINER,
					modContext, null, busFetchParams);
			ServiceBusImpl.stopModule();
			// In theory that bus is no more, but the static fetch params remain
			serviceBus = AndroidServiceBusImpl.startModule(
					uAALBundleContainer.THE_CONTAINER, modContext,
					busFetchParams, busFetchParams, this);
		}
		return serviceBus;
	}

	// TODO handle this with sync and wait and wake and thread ...
	private void waitForStart() {
		int i = 0;
		while (started == false && i < WAIT_TICKS) {
			synchronized (this) {
				if (started == false) {
					try {
						wait(WAIT_PERIOD_MS);
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
