/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.container.android.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import org.universAAL.middleware.android.container.BundleConfigHome;
import org.universAAL.middleware.android.container.ContextEmulator;
import org.universAAL.middleware.android.container.uAALBundleContainer;
//import org.universAAL.middleware.android.modules.impl.AndroidControlBroker;
import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.communication.jgroups.JGroupsCommunicationConnector;
import org.universAAL.middleware.connectors.discovery.slp.SLPDiscoveryConnector;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.managers.aalspace.AALSpaceManagerImpl;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.deploy.DeployManagerImpl;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.aalspace.AALSpaceModuleImpl;
import org.universAAL.middleware.modules.communication.CommunicationModuleImpl;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;
import org.universAAL.middleware.service.impl.ServiceBusImpl;

import ch.ethz.iks.slp.Advertiser;
import ch.ethz.iks.slp.Locator;
import ch.ethz.iks.slp.ServiceLocationManager;

import ae.com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import ae.com.sun.xml.bind.v2.model.annotation.XmlSchemaMine;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public class AndroidContainerService extends Service {

	private static final String TAG = AndroidContainerService.class
			.getCanonicalName();
	private ContextEmulator context;
	private JGroupsCommunicationConnector jgroupsCommunicationConnector;
	private SLPDiscoveryConnector slpDiscoveryConnector;
	private CommunicationModuleImpl communicationModule;
	private AALSpaceModuleImpl aalSpaceModule;
	private ControlBroker controlBroker;
	private AALSpaceManagerImpl spaceManager;
	private DeployManagerImpl deployManager;
	private MulticastLock m_lock;

	// /**
	// * Class for clients to access. Because we know this service always
	// * runs in the same process as its clients, we don't need to deal with
	// * IPC.
	// */
	// public class LocalBinder extends Binder {
	// LocalSodaPopPeerService getService() {
	// return LocalSodaPopPeerService.this;
	// }
	// }

	@Override
	public IBinder onBind(Intent intent) {
		return null; // Don't allow binding...
	}

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

		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Stack", "false");
		System.setProperty("java.net.preferIPv4Addresses", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");

		System.setProperty("jgroups.use.jdk_logger ", "true");

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		m_lock = wifi.createMulticastLock("felix_multicast");
		m_lock.acquire();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Log.d(TAG, "Is about to handle intent");

		Thread commandThread = new Thread() {

			@Override
			public void run() {
				context = new ContextEmulator();

				try {
					System.setProperty("net.slp.port", "5555");

					ModuleContext defaultContext = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context });

					// register the service factories so each consumer gets its
					// own Locator/Advert instance (but we only have 1 consumer
					// for these)
					Advertiser advertiser = ServiceLocationManager
							.getAdvertiser(new Locale("en"));
					Locator locator = ServiceLocationManager
							.getLocator(new Locale("en"));
					uAALBundleContainer.THE_CONTAINER.shareObject(
							defaultContext, advertiser,
							new Object[] { Advertiser.class.getName() });
					uAALBundleContainer.THE_CONTAINER.shareObject(
							defaultContext, locator,
							new Object[] { Locator.class.getName() });
					// ///////////////////////////////////////////////////////////////////

					ModuleContext modContext1 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.connectors.communication.jgroups.osgi" });
					jgroupsCommunicationConnector = new JGroupsCommunicationConnector(
							modContext1);
					Dictionary jGroupDCOnnector = getProperties("mw.connectors.communication.jgroups.core");
					if (jGroupDCOnnector != null) {
						jgroupsCommunicationConnector
								.loadConfigurations(jGroupDCOnnector);
					}
					uAALBundleContainer.THE_CONTAINER.shareObject(modContext1,
							jgroupsCommunicationConnector,
							new Object[] { CommunicationConnector.class
									.getName() });
					Log.d("startBrokerClient",
							"Started the JGroupCommunicationConnector...");
					// ////////////////////////////////////////////////////////////////

					ModuleContext modContext2 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.connectors.discovery.slp.osgi" });
					slpDiscoveryConnector = new SLPDiscoveryConnector(
							modContext2);
					Dictionary slpDConnectorProperties = getProperties("mw.connectors.discovery.slp.core");
					if (slpDConnectorProperties != null) {
						slpDiscoveryConnector
								.loadConfigurations(slpDConnectorProperties);
						slpDiscoveryConnector.init();
					}
					uAALBundleContainer.THE_CONTAINER
							.shareObject(modContext2, slpDiscoveryConnector,
									new Object[] { DiscoveryConnector.class
											.getName() });
					Log.d("startBrokerClient",
							"Starting the SLPDiscoveryConnector...");
					// ///////////////////////////////////////////////////////////////////

					ModuleContext modContext3 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.modules.communication.osgi" });
					communicationModule = new CommunicationModuleImpl(
							modContext3);
					communicationModule.init();
					uAALBundleContainer.THE_CONTAINER
							.shareObject(modContext3, communicationModule,
									new Object[] { CommunicationModule.class
											.getName() });
					Log.d("startBrokerClient",
							"Started the CommunicationModule...");
					// ///////////////////////////////////////////////////////////////////

					ModuleContext modContext4 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.modules.aalspace.osgi" });
					aalSpaceModule = new AALSpaceModuleImpl(modContext4);
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
					Log.d("startBrokerClient", "AALSpaceModule registered");
					// /////////////////////////////////////////////////////////////////////

					ModuleContext modContext5 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.brokers.control.osgi" });
					controlBroker = new ControlBroker(modContext5);
					uAALBundleContainer.THE_CONTAINER.shareObject(modContext5,
							controlBroker,
							new Object[] { ControlBroker.class.getName() });
					Log.d("startBrokerClient", "Started ControlBroker!");
					// /////////////////////////////////////////////////////////////////////

					ModuleContext modContext6 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.managers.aalspace.osgi" });
					BundleConfigHome confHome = new BundleConfigHome(
							modContext6.getID());// TODO !!!!!!!!!!!
					spaceManager = new AALSpaceManagerImpl(modContext6,
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
					Log.d("Activator", "Registered");
					// //////////////////////////////////////////////////////////////////ç

					ModuleContext modContext7 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.managers.deploy.osgi" });
					deployManager = new DeployManagerImpl(modContext7, confHome);
					Dictionary deployManagerProps = getProperties("mw.managers.deploy.core");
					if (deployManagerProps == null) {
						deployManagerProps = new Hashtable<String, String>();
					} else {
						deployManager.loadConfigurations(deployManagerProps);
					}
					deployManager.init();
					uAALBundleContainer.THE_CONTAINER.shareObject(modContext7,
							deployManager,
							new Object[] { DeployManager.class.getName() });
					// ////////////////////////////////////////////////////////////////////

					SharedResources.moduleContext = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.data.representation.osgi" });
					SharedResources.loadReasoningEngine();
					SharedResources.setDefaults();
					// //////////////////////////////////////////////////////////////////////

					TurtleUtil.moduleContext = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.data.serialization.osgi" });
					uAALBundleContainer.THE_CONTAINER.shareObject(
							TurtleUtil.moduleContext, new TurtleSerializer(),
							new Object[] { MessageContentSerializer.class
									.getName() });
					uAALBundleContainer.THE_CONTAINER.shareObject(
							TurtleUtil.moduleContext, new TurtleSerializer(),
							new Object[] { MessageContentSerializerEx.class
									.getName() });
					// ///////////////////////////////////////////////////////////////////

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
					// /////////////////////////////////////////////////////////////////////

					Object[] busFetchParams = new Object[] { ServiceBusImpl.class
							.getName() };
					ModuleContext modContext9 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.bus.service.osgi" });
					ServiceBusImpl.startModule(
							uAALBundleContainer.THE_CONTAINER, modContext9,
							busFetchParams, busFetchParams);
					// //////////////////////////////////////////////////////////////////

					busFetchParams = new Object[] { ContextBus.class.getName() };
					ModuleContext modContext10 = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"mw.bus.context.osgi" });
					ContextBusImpl.startModule(
							uAALBundleContainer.THE_CONTAINER, modContext10,
							busFetchParams, busFetchParams);
					// /////////////////////////////////////////////////////////////////

					// TODO FAKE SUBSCRIBER FOR TEST!!! REMOVE THIS
					ModuleContext subsContext = uAALBundleContainer.THE_CONTAINER
							.registerModule(new Object[] { context,
									"tester.subscriber" });
					new Sub(subsContext);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};
		commandThread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		m_lock.release();
	}

	private Dictionary getProperties(String file) {
		Properties prop = new Properties();
		try {
			File conf = new File(Environment.getExternalStorageDirectory()
					.getPath(), BundleConfigHome.uAAL_CONF_ROOT_DIR + file
					+ ".properties");
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

	protected class Sub extends ContextSubscriber {

		protected Sub(ModuleContext connectingModule) {
			super(connectingModule,
					new ContextEventPattern[] { new ContextEventPattern() });
		}

		@Override
		public void communicationChannelBroken() {
			// TODO Auto-generated method stub
			Log.d("Subscriber", "Broken");
		}

		@Override
		public void handleContextEvent(ContextEvent event) {
			Log.d("Subscriber",
					"______________ Event of : " + event.getSubjectURI());
		}

	}

}
