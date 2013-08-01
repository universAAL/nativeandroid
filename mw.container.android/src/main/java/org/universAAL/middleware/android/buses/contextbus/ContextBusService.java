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
package org.universAAL.middleware.android.buses.contextbus;

import org.universAAL.middleware.android.buses.contextbus.messages.ContextBusMessageFactory;
import org.universAAL.middleware.android.buses.contextbus.messages.handlers.ContextBusHandlerFactory;
import org.universAAL.middleware.android.common.AbstractAndroidBusService;
//import org.universAAL.middleware.android.common.buses.service.AbstractAndroidBusService;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.container.ContextEmulator;
import org.universAAL.middleware.android.container.uAALBundleContainer;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;

import android.app.Notification;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public class ContextBusService extends AbstractAndroidBusService {

	private static final String TAG = "AndroidContextBusService";
//	private static final int ONGOING_NOTIFICATION = 22072013;
//	public static ContextEmulator context;
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		// TODO Check! Supposedly, this starts as foreground but without
//		// notification
//		Notification notif = new Notification(0, null,
//				System.currentTimeMillis());
//		notif.flags |= Notification.FLAG_NO_CLEAR;
//		startForeground(ONGOING_NOTIFICATION, notif);
//		Log.d(TAG, "Creating the Service");
//		new Thread(new Runnable() {
//			public void run() {
//				// OSGi Bundle Context emulation: tracks instances to share
//				context = ContextEmulator.createContextEmulator();
//				Object[] busFetchParams = new Object[] { ContextBus.class.getName() };
//				ModuleContext modContext10 = uAALBundleContainer.THE_CONTAINER
//						.registerModule(new Object[] { context,"mw.bus.context.osgi" });
//				ContextBusImpl.startModule(
//						uAALBundleContainer.THE_CONTAINER, modContext10,
//						busFetchParams, busFetchParams);
//			}
//		});
//	}
//
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//	}

	@Override
	protected void privateOnStart(Intent intent, int startId) {
		// Analyze the intent - create the message
		IMessage message = ContextBusMessageFactory.createMessage(this, intent);

		// Create the handler
		IMessageHandler handler = ContextBusHandlerFactory.createHandler(intent
				.getAction(), new ModulesCommWrapper(this));

		if (null == handler || null == message) {
			Log.w(TAG, "Action is null!");
			return;
		}

		// Handle the message
		message.handle(handler);
	}
}
