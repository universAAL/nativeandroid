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
package org.universAAL.middleware.android.localsodapop.messages.handlers;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.contextbus.messages.RegisterContextsMessage;
import org.universAAL.middleware.android.buses.contextbus.messages.handlers.RegisterContextsHandler;
import org.universAAL.middleware.android.buses.servicebus.messages.RegisterServicesMessage;
import org.universAAL.middleware.android.buses.servicebus.messages.handlers.RegisterServicesHandler;
import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.InitializeMessage;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.android.localsodapop.intents.LocalSodaPopIntentFactory;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class InitializeLocalSodaPopHandler extends AbstractMessagePersistableHandler {

    private static final String TAG = InitializeLocalSodaPopHandler.class.getCanonicalName();

    private static final long TIME_TO_WAIT_FOR_SCANNING_APPLICATIONS_IN_MILLI_SECONDS = 10 * 1000;

    @Override
    protected void privateHandleMessage(IMessage message, AbstractSodaPopAndroidImpl sodaPop) {

	// Cast it to initialize message
	InitializeMessage initializeMessage = (InitializeMessage) message;

	// Empty the DB
	sodaPopPeersSQLiteMngr.resetDB();

	// Persist the local peer info
	sodaPop.addLocalPeer();

	// Send notice new local SodaPop intent
	Intent noticeNewLocalSodaPopIntent = LocalSodaPopIntentFactory
		.createNoticeJoiningLocalSodaPopPeer(sodaPop.getID(), IAndroidSodaPop.PROTOCOL_UPNP);
	Log.d(TAG,
		"Is about to send intent with action [" + noticeNewLocalSodaPopIntent.getAction()
			+ "] and category ["
			+ noticeNewLocalSodaPopIntent.getCategories().iterator().next());
	initializeMessage.getContext().sendBroadcast(noticeNewLocalSodaPopIntent);

	// NScan all packages to figure out if there are some packages that are
	// related to service registrations
	scanForInstalledUniversaalApplicationsAndRegister(initializeMessage.getContext());
    }

    private void scanForInstalledUniversaalApplicationsAndRegister(Context context) {
	Log.d(TAG, "Is about to wait before scanning uAAL packages");
	try {
	    Thread.sleep(TIME_TO_WAIT_FOR_SCANNING_APPLICATIONS_IN_MILLI_SECONDS);
	} catch (InterruptedException e) {
	    // Do nothing... just log a warning message
	    Log.w(TAG, "Unable to sleep for ["
		    + TIME_TO_WAIT_FOR_SCANNING_APPLICATIONS_IN_MILLI_SECONDS + "] mili seconds");
	}
	Log.d(TAG, "Is about to scan uAAL packages");
	PackageManager pm = context.getPackageManager();
	List<String> packageNames = new ArrayList<String>();
	List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES
		| PackageManager.GET_SERVICES);
	for (PackageInfo curPackage : packages) {
	    packageNames.add(curPackage.packageName);
	}

	// For each package create a register message + handle it
	for (String packageName : packageNames) {
	    Intent intent = new Intent();
	    intent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, IAndroidSodaPop.PROTOCOL_UPNP);
	    intent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PACKAGE_NAME, packageName);

	    // Register services
	    RegisterServicesMessage servicesMessage = new RegisterServicesMessage(context, intent);
	    RegisterServicesHandler servicesHandler = new RegisterServicesHandler();
	    servicesMessage.handle(servicesHandler);

	    // Register contexts
	    RegisterContextsMessage contextsMessage = new RegisterContextsMessage(context,
		    new Intent(intent));
	    RegisterContextsHandler contextsHandler = new RegisterContextsHandler();
	    contextsMessage.handle(contextsHandler);
	}
    }
}
