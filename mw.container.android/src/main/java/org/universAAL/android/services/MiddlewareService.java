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
package org.universAAL.android.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.container.AndroidRegistry;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.wrappers.CommunicationConnectorWrapper;
import org.universAAL.android.wrappers.DiscoveryConnectorWrapper;
import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.communication.jgroups.JGroupsCommunicationConnector;
import org.universAAL.middleware.connectors.discovery.slp.SLPDiscoveryConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.managers.aalspace.AALSpaceManagerImpl;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.aalspace.AALSpaceModuleImpl;
import org.universAAL.middleware.modules.communication.CommunicationModuleImpl;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.impl.BusMemberRegistryImpl;
import org.universAAL.middleware.ui.IUIBus;
import org.universAAL.middleware.ui.impl.UIBusImpl;
import org.universAAL.middleware.util.Constants;
import org.universAAL.ontology.profile.User;
import org.universAAL.ri.gateway.communicator.service.impl.CommunicatorStarter;
import org.universAAL.ri.gateway.communicator.service.impl.GatewayAddress;
import org.universAAL.ri.gateway.communicator.service.impl.GatewayCommunicatorImpl;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;
import org.universAAL.ri.gateway.eimanager.impl.EIOperationManager;
import org.universAAL.ri.gateway.eimanager.impl.ExportManagerImpl;
import org.universAAL.ri.gateway.eimanager.impl.ImportManagerImpl;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.impl.AdvertiserImpl;
import ch.ethz.iks.slp.impl.LocatorImpl;
import ch.ethz.iks.slp.impl.SLPCore;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Central class and service of the application. It takes care of starting and
 * stopping the middleware, while maintaining in memory, as local variables,
 * each module of the middleware. It handles the different messages, sent as
 * intents, that are received to identify the different operations and stages of
 * the middleware.
 * 
 * @author alfiva
 * 
 */
public class MiddlewareService extends Service implements AALSpaceListener{

	private static final String TAG = "MiddlewareService";
	private static final String CREATE_THREAD_TAG = "MW Service Create";
	private static final String START_THREAD_TAG = "MW Service Start";
	private static final String UI_THREAD_TAG = "UI Handler Create";
	private static final int ONGOING_NOTIFICATION = 3948234; // TODO Random one?
	private static final String MY_WIFI = "home_wifi";
	private static final String NO_WIFI = "uAALGhostWifi";
	private static final int WIFI_HOME = 0;
	private static final int WIFI_NOT_SET = 1;
	private static final int WIFI_STRANGER = 2;
	private static final int WIFI_NOT_ON = 3;
	public static final String uAAL_CONF_ROOT_DIR = "/data/felix/configurations/etc/"; // this is just the default

	private MulticastLock mLock;
	// MW modules stay in memory in this service class (Container holds only WeakRefs)
	private Advertiser mJSLPadvertiser;
	private Locator mJSLPlocator;
	private CommunicationConnector mModJGROUPS;
	private DiscoveryConnector mModJSLP;
	private CommunicationModuleImpl mModCOMMUNICATION;
	private AALSpaceManagerImpl mModSPACEMANAGER;
	private AALSpaceModuleImpl mModSPACEMODULE;
	private ControlBroker mModCONTROLBROKER;
	private TurtleSerializer mModSERIALIZER;
	private BusMemberRegistryImpl mModTRACKER;
	private GatewayCommunicatorImpl mModGATEWAY;
	private ExportManagerImpl mModEXPORT;
	private ImportManagerImpl mModIMPORT;
	private AndroidHandler mModHANDLER;
	
	@Override
	public void onCreate() {
		// This is where MW is created as it is called once when service is instantiated. Make sure it runs forever.
		super.onCreate();
		Log.v(TAG, "Create");

		// TODO Check! Supposedly, this starts as foreground but without notification. Not on 4.0 anymore
		Notification notif = new Notification(0, null,
				System.currentTimeMillis());
		notif.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(ONGOING_NOTIFICATION, notif);
		
		// These properties must be set here, not from file
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Stack", "false");
		System.setProperty("java.net.preferIPv4Addresses", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");
		System.setProperty("jgroups.use.jdk_logger ", "true");
		System.setProperty("net.slp.port", "5555");
		
		// This is for setting IP manually, just in case (set it everytime you
		// get a new IP). If not set, it seems it also works, but SLP keeps
		// working on multicast for a while after Wifi is turned off.
		// System.setProperty("net.slp.interfaces", "192.168.0.126");
		
		// This is for not blocking the sockets when reconnecting (check its
		// javadoc). Common sense would suggest to set it false, but it still
		// works without doing so.
		// System.setProperty("http.keepAlive", "false");
		
		// Create (dont use yet) the multicast lock. Make it not counted: this app only uses one lock
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mLock = wifi.createMulticastLock("uaal_multicast");
		mLock.setReferenceCounted(false);

		// Set the static platform of SLP (for logging and stuff)
		SLPCore.platform=AndroidContext.THE_CONTEXT;
		// Start the MW! Put all the above inside thread too? Cmon, its fast! Also, notification must be there
		new Thread(new Runnable() {
			public void run() {
				int wifiStatus=checkWifi();
				// 1. Stop-start the connector modules. Use jSLP only IF WIFI==WIFI_HOME or WIFI==WIFI_NOT_SET
				if(wifiStatus==WIFI_HOME || wifiStatus==WIFI_NOT_SET){
					restartConnector(true);
				}else{
					restartConnector(false);
				}
				// 2. Start the MW modules
				startMiddleware();
				// 3. Register the ontologies
				//TODO Place call to ont better? Race cond: Currently in OntService it is not a thread and will block, as it should, but is there any other way?
				Intent ont = new Intent(IntentConstants.ACTION_ONT_REG_ALL);
				ont.setClass(MiddlewareService.this, OntologyService.class);
				startService(ont);
				// 4. Start GW IF WIFI==NOT_ON or WIFI==STRANGER
				if(wifiStatus==WIFI_NOT_ON || wifiStatus==WIFI_STRANGER){
					startGateway();
				}
				// 5. Register the apps
				Intent scan = new Intent(IntentConstants.ACTION_PCK_REG_ALL);
				scan.setClass(MiddlewareService.this, ScanService.class);
				startService(scan);
			}
		},CREATE_THREAD_TAG).start();

		Log.v(TAG, "Created");
	}

	@Override
	public void onDestroy() {
		// This would only be called when low on memory: stop and release everything instantiated in onCreate an die. unshare
		super.onDestroy();
		Log.v(TAG, "Destroy");
		//TODO Call to unreg onts? really? Place call to scan better?
		Intent scan = new Intent(IntentConstants.ACTION_PCK_UNREG_ALL);
		scan.setClass(this, ScanService.class);
		startService(scan);
		stopGateway();
		stopMiddleware();
		stopConnector();
		Log.v(TAG, "Destroyed");
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		// This will be called each time someone (scan/boot/wifi) sends an intent to this service. Analyze and react accordingly.
		Log.v(TAG, "Start command: ");
		new Thread(new Runnable() {
			public void run() {
				if (intent != null) {
					String action = intent.getAction();
					Log.v(TAG, "Intent: " + action);
					if (action != null) {
						if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
							// Do we really have to do anything here? The MW is running by now...
							Log.v(TAG, "Action is BOOT");
						} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
							// Change in Wifi TODO Can we get ON after ON?
							Log.v(TAG, "Action is WIFI");
							int wifiStatus=checkWifi();
							switch (wifiStatus) {
							case WIFI_HOME://We are at home: Stop GW and connect with jSLP
								stopGateway();
								restartConnector(true);
								break;
							case WIFI_NOT_SET://We still don´t have home: Dont use GW and try to connect with jSLP
								stopGateway();
								restartConnector(true);
								break;
							case WIFI_STRANGER://We are outside home: Start GW and dont try to connect with jSLP
								stopGateway();
								restartConnector(false);
								startGateway();
								break;
							case WIFI_NOT_ON://No wifi: Start GW and dont try to connect with jSLP
								stopGateway();
								restartConnector(false);
								startGateway();
								break;
							default:
								break;
							}// I know, this switch can be refactored better, but this way it is easier to read and tweak
						} else if (action.equals(IntentConstants.ACTION_PCK_REG)) {
							// REGISTER message from scan service
							Log.v(TAG, "Action is REGISTER");
							AndroidRegistry.register(
											intent.getStringExtra(IntentConstants.ACTION_PCK_REG_X_ID),
											(GroundingParcel) intent.getParcelableExtra(IntentConstants.ACTION_PCK_REG_X_PARCEL),
											intent.getIntExtra(IntentConstants.ACTION_PCK_REG_X_TYPE, 0),
											MiddlewareService.this);
						} else if (action.equals(IntentConstants.ACTION_PCK_UNREG)) {
							// UNREGISTER message from scan service
							Log.v(TAG, "Action is UNREGISTER");
							AndroidRegistry.unregister(
											intent.getStringExtra(IntentConstants.ACTION_PCK_UNREG_X_ID),
											intent.getIntExtra(IntentConstants.ACTION_PCK_UNREG_X_TYPE,0));
						} else {
							// Not the right action yet (TODO check if from receivers)
							Log.v(TAG, "Action is... Not the right action yet");
						}
					} else {
						// If (action=null) who?
						Log.v(TAG, "Action is none");
					}
				}
			}
		}, START_THREAD_TAG).start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// This will be called each time someone asks for our binder to perform operations. Give it. - NO! no more binders...
		return null;
	}
	
	/**
	 * This makes the connector modules start (And also restarts the AAL Space
	 * Manager). Depending on the parameter passed, it can either start the real
	 * jSLP+jGroups connectors or the fake ones.
	 * 
	 * @param connect
	 *            Set to true if there is an actual WiFi connection where th
	 *            real connectors should be started. Set to false to start the
	 *            fake connectors.
	 */
	private synchronized void restartConnector(boolean connect) {
		boolean aalspaceflag=false;
		// First stop everything related to connector: JSLP, JGroups, Advertiser and Locator
		// I could use stopConnector() but it is sync, I dont want to change it
		// TODO Check presence in Container rather than not null?
		// Stop AALSpaceManager (and GW) to later trigger AALSpace creation, only if it was already started!
		if (mModSPACEMANAGER != null) {
			mModSPACEMANAGER.dispose();
			AndroidContainer.THE_CONTAINER.removeSharedObjectListener(mModSPACEMANAGER);
			AndroidContainer.THE_CONTAINER.unshareObject(AALSpaceManager.class.getName(), mModSPACEMANAGER);
			mModSPACEMANAGER = null;
			aalspaceflag=true;
		}
		if (mModJSLP != null) {
			try{
				mModJSLP.dispose(); // -> Exception if wifi off, but must be done
			}catch(Exception e){
				Log.e(TAG,"Could not dispose correctly the JSLP");
			}
			AndroidContainer.THE_CONTAINER.unshareObject(DiscoveryConnector.class.getName(), mModJSLP);
			mModJSLP = null;
		}
		if (mModJGROUPS != null) {
			try{
				mModJGROUPS.dispose(); // -> Exception if wifi off, but must be done
			}catch(Exception e){
				Log.e(TAG,"Could not dispose correctly the JGROUPS");
			}
			AndroidContainer.THE_CONTAINER.unshareObject(CommunicationConnector.class.getName(),mModJGROUPS);
			mModJGROUPS = null;
		}
		if (mJSLPlocator != null) {
			AndroidContainer.THE_CONTAINER.unshareObject(Locator.class.getName(), mJSLPlocator);
			mJSLPlocator = null;
		}
		if (mJSLPadvertiser != null) {
			AndroidContainer.THE_CONTAINER.unshareObject(Advertiser.class.getName(), mJSLPadvertiser);
			mJSLPadvertiser = null;
			SLPCore.destroyMulticastSocket();// TODO place this better?
			SLPCore.stop();
		}
		// Then depending on WIFI start the real or the fake connectors
		if (connect) {
			// If wifiON start the real connectors
			Log.v(TAG, "Wifi is ON");
			try {
				// First acquire multicast lock
				mLock.acquire();
				// Then start JSLP library
				SLPCore.init();
				SLPCore.initMulticastSocket();
				mJSLPadvertiser=new AdvertiserImpl();
				mJSLPlocator=new LocatorImpl();
				AndroidContainer.THE_CONTAINER.shareObject(
						AndroidContext.THE_CONTEXT, mJSLPadvertiser,
						new Object[] { Advertiser.class.getName() });
				AndroidContainer.THE_CONTAINER.shareObject(
						AndroidContext.THE_CONTEXT, mJSLPlocator,
						new Object[] { Locator.class.getName() });
				Log.d(TAG, "Started libraries...");
				// _____________________JGROUPS______________________
				mModJGROUPS = new JGroupsCommunicationConnector(
						AndroidContext.THE_CONTEXT);
				Dictionary jGroupDCOnnector = getProperties("mw.connectors.communication.jgroups.core");
				if (jGroupDCOnnector != null) {
					mModJGROUPS
							.loadConfigurations(jGroupDCOnnector);
				}
				AndroidContainer.THE_CONTAINER
						.shareObject(AndroidContext.THE_CONTEXT,
								mModJGROUPS,
								new Object[] { CommunicationConnector.class
										.getName() });
				Log.d(TAG, "Started the JGroupCommunicationConnector...");
				// _____________________JSLP_________________________
				mModJSLP = new SLPDiscoveryConnector(
						AndroidContext.THE_CONTEXT);
				Dictionary slpDConnectorProperties = getProperties("mw.connectors.discovery.slp.core");
				if (slpDConnectorProperties != null) {
					mModJSLP
							.loadConfigurations(slpDConnectorProperties);
					mModJSLP.init();
				}
				AndroidContainer.THE_CONTAINER.shareObject(
						AndroidContext.THE_CONTEXT, mModJSLP,
						new Object[] { DiscoveryConnector.class.getName() });
				Log.d(TAG, "Starting the SLPDiscoveryConnector...");
			} catch (Exception e) {
				Log.e(TAG, "Error while initializing connector", e);
			}
		} else {
			// If wifi OFF start the fake connectors
			Log.v(TAG, "Wifi is OFF");
			// Release multicast
			mLock.release();
			// _____________________JGROUPS_________________________
			mModJGROUPS = new CommunicationConnectorWrapper();
			AndroidContainer.THE_CONTAINER.shareObject(
					AndroidContext.THE_CONTEXT, mModJGROUPS,
					new Object[] { CommunicationConnector.class.getName() });
			Log.d(TAG, "Starting the FAKE JGroupCommunicationConnector...");
			// _____________________JSLP_________________________
			mModJSLP = new DiscoveryConnectorWrapper();
			AndroidContainer.THE_CONTAINER.shareObject(
					AndroidContext.THE_CONTEXT, mModJSLP,
					new Object[] { DiscoveryConnector.class.getName() });
			Log.d(TAG, "Starting the FAKE SLPDiscoveryConnector...");
		}
		// Restart AALSpaceManager to trigger AALSpace creation, only if it was already started!
		if (aalspaceflag) {
			// _________________AALSPACE MANAGER_____________________
			AndroidContext tempCtxt = new AndroidContext("mw.managers.aalspace.osgi");
//			mModSPACEMANAGER = new AALSpaceManagerImpl(tempCtxt, new ModuleConfigHome(Environment.getExternalStorageDirectory()
//					.getPath()+uAAL_CONF_ROOT_DIR, "mw.managers.aalspace.osgi"));//TODO check if works
			mModSPACEMANAGER = new AALSpaceManagerImpl(tempCtxt, Environment.getExternalStorageDirectory().getPath()+getConfDir()+"/mw.managers.aalspace.osgi");
			Dictionary aalSpaceManagerProps = getProperties("mw.managers.aalspace.core");
			if (aalSpaceManagerProps == null) {
				aalSpaceManagerProps = new Hashtable<String, String>();
			} else {
				mModSPACEMANAGER.loadConfigurations(aalSpaceManagerProps);
			}
			mModSPACEMANAGER.init();
			AndroidContainer.THE_CONTAINER.shareObject(tempCtxt, mModSPACEMANAGER,
					new String[] { AALSpaceManager.class.getName(),
							AALSpaceEventHandler.class.getName() });
			Log.d(TAG, "Started AALSPACE MANAGER again");
		}
	}
	
	/**
	 * Stops the communication connectors, either real or fake.
	 */
	private synchronized void stopConnector() {
		// _____________________JSLP_________________________
		if (mModJSLP != null) {
			try{
				mModJSLP.dispose();// -> Exception if wifi off, but must be done
			}catch(Exception e){
				Log.e(TAG,"Could not dispose correctly the JSLP");
			}
			AndroidContainer.THE_CONTAINER.unshareObject(DiscoveryConnector.class.getName(), mModJSLP);
			mModJSLP = null;
		}
		// _____________________JGROUPS_______________________
		if (mModJGROUPS != null) {
			try{
				mModJGROUPS.dispose();// -> Exception if wifi off, but must be done
			}catch(Exception e){
				Log.e(TAG,"Could not dispose correctly the JGROUPS");
			}
			AndroidContainer.THE_CONTAINER.unshareObject(CommunicationConnector.class.getName(),mModJGROUPS);
			mModJGROUPS = null;
		}
		// Then stop JSLP library
		if (mJSLPlocator != null) {
			AndroidContainer.THE_CONTAINER.unshareObject(Locator.class.getName(), mJSLPlocator);
			mJSLPlocator = null;
		}
		if (mJSLPadvertiser != null) {
			AndroidContainer.THE_CONTAINER.unshareObject(Advertiser.class.getName(), mJSLPadvertiser);
			mJSLPadvertiser = null;
			SLPCore.destroyMulticastSocket();//TODO place this better?
			SLPCore.stop();
		}
		// And release multicast lock
		mLock.release();
	}
	
	/**
	 * Starts the middleware modules in the right order with the appropriate
	 * sequence for each. Does not include the connectors.
	 */
	private synchronized void startMiddleware(){
		try {
			// _________________COMMUNICATION MODULE_________________
			AndroidContext c1 = new AndroidContext("mw.modules.communication.osgi");
			mModCOMMUNICATION = new CommunicationModuleImpl(c1);
			mModCOMMUNICATION.init();
			AndroidContainer.THE_CONTAINER.shareObject(c1, mModCOMMUNICATION,
					new Object[] { CommunicationModule.class.getName() });
			Log.d(TAG, "Started COMMUNICATION MODULE");
			// _________________AALSPACE MODULE_____________________
			AndroidContext c2=new AndroidContext("mw.modules.aalspace.osgi");
			mModSPACEMODULE = new AALSpaceModuleImpl(
					c2);
			Dictionary aalSpaceModuleProp = getProperties("mw.modules.aalspace.core");
			if (aalSpaceModuleProp != null) {
				mModSPACEMODULE.loadConfigurations(aalSpaceModuleProp);
			} else {
				mModSPACEMODULE.loadConfigurations(new Hashtable<String, String>());
			}
			mModSPACEMODULE.init();
			AndroidContainer.THE_CONTAINER.shareObject(c2, mModSPACEMODULE,
					new Object[] { AALSpaceModule.class.getName() });
			Log.d(TAG, "Started AALSPACE MODULE");
			// _________________CONTROL BROKER_____________________
			AndroidContext c3=new AndroidContext("mw.brokers.control.osgi");
			mModCONTROLBROKER = new ControlBroker(c3);
			AndroidContainer.THE_CONTAINER.shareObject(c3, mModCONTROLBROKER,
					new Object[] { ControlBroker.class.getName() });
			Log.d(TAG, "Started CONTROL BROKER");
			// _________________AALSPACE MANAGER_____________________
			AndroidContext c4=new AndroidContext("mw.managers.aalspace.osgi");
//			mModSPACEMANAGER = new AALSpaceManagerImpl(c4, new ModuleConfigHome(Environment.getExternalStorageDirectory()
//					.getPath()+uAAL_CONF_ROOT_DIR, "mw.managers.aalspace.osgi"));//TODO check if works
			mModSPACEMANAGER = new AALSpaceManagerImpl(c4, Environment.getExternalStorageDirectory().getPath()+getConfDir()+"mw.managers.aalspace.osgi");
			Dictionary aalSpaceManagerProps = getProperties("mw.managers.aalspace.core");
			if (aalSpaceManagerProps == null) {
				aalSpaceManagerProps = new Hashtable<String, String>();
			} else {
				mModSPACEMANAGER.loadConfigurations(aalSpaceManagerProps);
			}
			mModSPACEMANAGER.init();
			AndroidContainer.THE_CONTAINER.shareObject(c4, mModSPACEMANAGER,
					new String[] { AALSpaceManager.class.getName(),
							AALSpaceEventHandler.class.getName() });
			mModSPACEMANAGER.addAALSpaceListener(this);// For listening to AAL space changes
			Log.d(TAG, "Started AALSPACE MANAGER");
			// _________________DEPLOY MANAGER_______________________
			// TODO DEPLOY MANAGER
			// _________________DATA REPRESENTATION________________
			SharedResources.moduleContext = AndroidContext.THE_CONTEXT;
			SharedResources.loadReasoningEngine();
			SharedResources.setDefaults();
			Log.d(TAG, "Started DATA REP");
			// _________________DATA SERIALIZATION_________________
			TurtleUtil.moduleContext = AndroidContext.THE_CONTEXT;
			mModSERIALIZER=new TurtleSerializer();
			AndroidContainer.THE_CONTAINER.shareObject(
					TurtleUtil.moduleContext, mModSERIALIZER,
					new Object[] { MessageContentSerializer.class
							.getName() });
			AndroidContainer.THE_CONTAINER.shareObject(
					TurtleUtil.moduleContext, mModSERIALIZER,
					new Object[] { MessageContentSerializerEx.class
							.getName() });
			Log.d(TAG, "Started DATA SER");
			// _________________BUS MODEL_________________________
			AndroidContext c5=new AndroidContext("mw.bus.model.osgi");
			AbstractBus.initBrokerage(c5,mModSPACEMANAGER, mModCOMMUNICATION);
			BusMessage.setThisPeer(mModSPACEMANAGER.getMyPeerCard());
			BusMessage.setMessageContentSerializer((MessageContentSerializer) mModSERIALIZER);
			Log.d(TAG, "Started BUS MODEL");
			// _________________SERVICE BUS_________________________
			AndroidContext c6=new AndroidContext("mw.bus.service.osgi");
			Object[] busFetchParams = new Object[] { ServiceBusImpl.class
					.getName() };
			ServiceBusImpl.startModule(AndroidContainer.THE_CONTAINER,
					c6, busFetchParams,
					busFetchParams);
			Log.d(TAG, "Started SERVICE BUS");
			// _________________CONTEXT BUS_________________________
			AndroidContext c7=new AndroidContext("mw.bus.context.osgi");
			busFetchParams = new Object[] { ContextBus.class.getName() };
			ContextBusImpl.startModule(AndroidContainer.THE_CONTAINER,
					c7, busFetchParams,
					busFetchParams);
			Log.d(TAG, "Started CONTEXT BUS");
			// _________________UI BUS_________________________
			AndroidContext c8 = new AndroidContext("mw.bus.ui.osgi");
			busFetchParams = new Object[] { IUIBus.class.getName() };
			UIBusImpl.startModule(AndroidContainer.THE_CONTAINER, c8,
					busFetchParams, busFetchParams);
			Log.d(TAG, "Started UI BUS");
			// _________________UI HANDLER_________________________
			new Thread(new Runnable() {
				public void run() {
					mModHANDLER = new AndroidHandler(AndroidContext.THE_CONTEXT,
							Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
									+ getUser());
					AndroidContainer.THE_CONTAINER.shareObject(
							AndroidContext.THE_CONTEXT, mModHANDLER, new String[] {
									AndroidHandler.class.getName(),
									AndroidHandler.class.getName() });
					mModHANDLER.render();
				}
			}, UI_THREAD_TAG).start();
			Log.d(TAG, "Started UI HANDLER");
		} catch (Exception e) {
			Log.e(TAG, "Error while initializing MW", e);
		}
	}
	
	/**
	 * Stops the middleware modules in order, with the appropriate sequence for
	 * each. Does not include the connectors.
	 */
	private synchronized void stopMiddleware(){		
		// _________________UI HANDLER_________________________
		if (mModHANDLER != null) {
			mModHANDLER.close();// TODO Close better?
			AndroidContainer.THE_CONTAINER.unshareObject(AndroidHandler.class.getName(), mModHANDLER);
			mModHANDLER = null;
		}
		// _________________UI BUS_________________________
		UIBusImpl.stopModule();
		// _________________CONTEXT BUS_________________________
		ContextBusImpl.stopModule();
		// _________________SERVICE BUS_________________________
		ServiceBusImpl.stopModule();
		// _________________BUS MODEL_________________________
		//BusModel -> Nothing
		// _________________/DATA SERIALIZATION_________________
		//DataSerialization -> Nothing TODO check if dont need to unshare
		if (mModSERIALIZER != null) {
			AndroidContainer.THE_CONTAINER.unshareObject(MessageContentSerializerEx.class.getName(), mModSERIALIZER);
			AndroidContainer.THE_CONTAINER.unshareObject(MessageContentSerializer.class.getName(), mModSERIALIZER);
			mModSERIALIZER = null;
		}
		// _________________DATA REPRESENTATION________________
		SharedResources.unloadReasoningEngine();
		// _________________DEPLOY MANAGER_____________________
		// TODO DEPLOY MANAGER
		// _________________AALSPACE MANAGER_____________________
		if (mModSPACEMANAGER != null) {
			mModSPACEMANAGER.removeAALSpaceListener(this);
			mModSPACEMANAGER.dispose();
			AndroidContainer.THE_CONTAINER.removeSharedObjectListener(mModSPACEMANAGER);
			AndroidContainer.THE_CONTAINER.unshareObject(AALSpaceManager.class.getName(), mModSPACEMANAGER);
			mModSPACEMANAGER = null;
		}
		// _________________CONTROL BROKER_____________________
		if (mModCONTROLBROKER != null) {
			mModCONTROLBROKER.dispose();
			AndroidContainer.THE_CONTAINER.removeSharedObjectListener(mModCONTROLBROKER);
			AndroidContainer.THE_CONTAINER.unshareObject(AALSpaceManager.class.getName(), mModCONTROLBROKER);
			mModCONTROLBROKER = null;
		}
		// _________________AALSPACE MODULE_____________________
		if (mModSPACEMODULE != null) {
			mModSPACEMODULE.dispose();
			AndroidContainer.THE_CONTAINER.removeSharedObjectListener(mModSPACEMODULE);
			AndroidContainer.THE_CONTAINER.unshareObject(AALSpaceManager.class.getName(), mModSPACEMODULE);
			mModSPACEMODULE = null;
		}
		// _________________COMMUNICATION MODULE_________________
		if(mModCOMMUNICATION!=null){
			mModCOMMUNICATION.dispose();
			AndroidContainer.THE_CONTAINER.unshareObject(CommunicationModule.class.getName(),mModCOMMUNICATION);
		}
	}
	
	/**
	 * Start the modules needed for the RI AAL Space Gateway to start.
	 */
	private synchronized void startGateway(){
		// _________________BUS TRACKER_________________ TODO Allow ON/OFF
		mModTRACKER = new BusMemberRegistryImpl(AndroidContext.THE_CONTEXT);
		AndroidContainer.THE_CONTAINER.shareObject(
				AndroidContext.THE_CONTEXT, mModTRACKER,
				new Object[] { IBusMemberRegistry.class.getName() });
		Log.d(TAG, "Started BUS TRACKER");
		// _________________GATEWAY_________________________ TODO Allow ON/OFF
		ModuleContext c8 = new AndroidContext("ri.gateway.communicator");
		Dictionary gatewayProps = getProperties("ri.gateway.communicator.core");
		try {
			CommunicatorStarter.properties=(Properties) gatewayProps;
			CommunicatorStarter.mc=AndroidContext.THE_CONTEXT;
			Serializer.contentSerializer=mModSERIALIZER;
			mModGATEWAY = new GatewayCommunicatorImpl();
			final List<GatewayAddress> remoteAddresses = CommunicatorStarter.extractRemoteGateways();
			mModGATEWAY.addRemoteGateways(remoteAddresses);
			mModEXPORT = new ExportManagerImpl(mModGATEWAY);
			mModIMPORT = new ImportManagerImpl(mModGATEWAY);
			mModGATEWAY.setManagers(mModIMPORT, mModEXPORT);
			mModGATEWAY.start();
			mModTRACKER.addBusRegistryListener(mModEXPORT, true);
			mModTRACKER.addBusRegistryListener(mModIMPORT, true);
			AndroidContainer.THE_CONTAINER.fetchSharedObject(c8, new Object[] {
					ImportOperationInterceptor.class.getName(),
					ExportOperationInterceptor.class.getName() },
					EIOperationManager.Instance);
			AndroidContainer.THE_CONTAINER
					.shareObject(c8, mModGATEWAY,
							new Object[] { ImportOperationInterceptor.class
									.getName() });
			AndroidContainer.THE_CONTAINER
					.shareObject(c8, mModGATEWAY,
							new Object[] { ExportOperationInterceptor.class
									.getName() });
			Log.d(TAG, "Started GATEWAY");
		} catch (Exception e) {
			Log.e(TAG, "Error while initializing Gateway", e);
		}
	}
	
	/**
	 * Stops the modules needed for the RI AAL Space Gateway to stop.
	 */
	private synchronized void stopGateway(){	
		// _________________GATEWAY_________________________
		if (mModTRACKER!= null){
			mModTRACKER.removeBusRegistryListener(mModEXPORT);
			mModTRACKER.removeBusRegistryListener(mModIMPORT);
		}
		if (mModEXPORT != null){
			mModEXPORT.shutdown();
		}
		if (mModIMPORT != null){
			mModIMPORT.shutdown();
		}
		AndroidContainer.THE_CONTAINER.removeSharedObjectListener(EIOperationManager.Instance);
		if (mModGATEWAY != null) {
			mModGATEWAY.stop();
			AndroidContainer.THE_CONTAINER.unshareObject(ExportOperationInterceptor.class.getName(), mModGATEWAY);
			AndroidContainer.THE_CONTAINER.unshareObject(ImportOperationInterceptor.class.getName(), mModGATEWAY);
			CommunicatorStarter.properties=null;
			CommunicatorStarter.mc=null;
			mModGATEWAY = null;
			Serializer.contentSerializer=null;
		}
		// _________________BUS TRACKER_________________
		if (mModTRACKER != null) {
			mModTRACKER.removeRegistryListeners();
			AndroidContainer.THE_CONTAINER.unshareObject(IBusMemberRegistry.class.getName(), mModTRACKER);
			mModTRACKER = null;
		}
		Log.d(TAG, "Stopped GATEWAY");
	}

	/**
	 * Gets the properties of a property file located in the config folder.
	 * 
	 * @param file
	 *            The name of the file (without path nor extension).
	 * @return The Properties
	 */
	private Dictionary getProperties(String file) {
		Properties prop = new Properties();
		try {
			File conf = new File(Environment.getExternalStorageDirectory()
					.getPath(), getConfDir() + file + ".properties");
			InputStream in = new FileInputStream(conf);
			prop.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			Log.w("startBrokerClient", "Properties file does not exist: "
					+ file);
		} catch (IOException e) {
			Log.w("startBrokerClient", "Error reading props file: " + file);
		}
		return prop;
	}
	
	/**
	 * Get the config dir
	 * 
	 * @return the location of config dir as of setting_cfolder_key pref
	 */
	private String getConfDir(){
		return PreferenceManager.getDefaultSharedPreferences(this).getString("setting_cfolder_key", uAAL_CONF_ROOT_DIR);
	}
	
	/**
	 * Get the user id
	 * 
	 * @return the user id as of setting_user_key pref
	 */
	private String getUser(){
		return PreferenceManager.getDefaultSharedPreferences(MiddlewareService.this).getString("setting_user_key", "saied");
	}
	
	/**
	 * Helper method to determine if there is a WiFi data connection.
	 * 
	 * @return True if there is one.
	 */
	private boolean isWifiOn(){
		ConnectivityManager connectivityManager = (ConnectivityManager) MiddlewareService.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		return (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}
	
	//All this "isOurWifi" thing is for knowing when to start the GW or connector.
	
	/**
	 * Sets the current active WiFi connection as the network where "our" AAL
	 * Space is located.
	 */
	private void thisIsOurWifi() {
		if (isWifiOn()) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String networkId = wifiInfo.getSSID();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(MY_WIFI, networkId).commit();
			Log.i(TAG, "Setting home space Wifi: "+networkId);
		}
	}
	
	/**
	 * Check whether we are now connected to "our" AAL Space WiFi Network.
	 * 
	 * @return Constant representing the status.
	 */
	private int checkWifi(){
		if (isWifiOn()) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String networkId = wifiInfo.getSSID();
			String home=PreferenceManager.getDefaultSharedPreferences(this).getString(MY_WIFI, NO_WIFI);
			Log.i(TAG, "WIFI CHECK: Evaluating: "+networkId);
			if(home.equals(NO_WIFI)){
				Log.i(TAG, "WIFI CHECK: We still do not have a home wifi, maybe this one will be?");
				return WIFI_NOT_SET;
			}else if(networkId.equals(home)){
				Log.i(TAG, "WIFI CHECK: We have a home wifi, and we are in it!");
				return WIFI_HOME;
			}else{
				Log.i(TAG, "WIFI CHECK: We have a home wifi, but it is not this one. If you want it to be, clear app data");
				return WIFI_STRANGER;
			}
		}
		Log.i(TAG, "WIFI CHECK: Wifi is not on");
		return WIFI_NOT_ON;
	}

	public void aalSpaceJoined(AALSpaceDescriptor spaceDescriptor) {
		String home=PreferenceManager.getDefaultSharedPreferences(this).getString(MY_WIFI, NO_WIFI);
		if(home.equals(NO_WIFI)){ //This is the first time we connect to a space
			thisIsOurWifi();
		}
		//Always stop the GW because we are in a space (probably already stopped)
		stopGateway();
	}

	public void aalSpaceLost(AALSpaceDescriptor spaceDescriptor) {
		// TODO Auto-generated method stub
	}

	public void newPeerJoined(PeerCard peer) {
		String home=PreferenceManager.getDefaultSharedPreferences(this).getString(MY_WIFI, NO_WIFI);
		if(home.equals(NO_WIFI)){ //This is the first time we connect to a space
			//This is the first time
			thisIsOurWifi();
		}
		//Always stop the GW because we are in a space (probably already stopped)
		stopGateway();
	}

	public void peerLost(PeerCard peer) {
		// TODO Auto-generated method stub
	}

	public void aalSpaceStatusChanged(AALSpaceStatus status) {
		// TODO Auto-generated method stub	
	}

}
