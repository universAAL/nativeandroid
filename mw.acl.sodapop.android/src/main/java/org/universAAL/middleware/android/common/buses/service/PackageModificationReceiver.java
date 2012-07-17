/*
	OCO Source Materials
            © Copyright IBM Corp. 2011

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
package org.universAAL.middleware.android.common.buses.service;

import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis
 *         Dalinkevicius</a> <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class PackageModificationReceiver extends BroadcastReceiver {

    private final static String TAG = PackageModificationReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d(TAG, "PackageModificationReceiver received Intent [" + intent.toString() + "]");

	// Get the sodapop action
	String sodaPopAction = toSodaPopAction(intent.getAction());
	if (StringUtils.isEmpty(sodaPopAction)) {
	    Log.w(TAG, "Action [" + intent.getAction() + "] is not recognized!");
	    return;
	}

	// Populate the "pure" package name
	String packageName = intent.getDataString();
	packageName = packageName.replaceFirst("package:", "");

	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(sodaPopAction);

	// Set the package name - this will be used by the service
	broadcastIntent
		.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, IAndroidSodaPop.PROTOCOL_UPNP);
	broadcastIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PACKAGE_NAME, packageName);

	// Send a broadcast message - so every bus can listen to this message
	context.sendBroadcast(broadcastIntent);
    }

    private static String toSodaPopAction(String androidAction) {
	String sodaPopAction = "";

	if (Intent.ACTION_PACKAGE_ADDED.equals(androidAction)
		|| Intent.ACTION_PACKAGE_INSTALL.equals(androidAction)
		|| Intent.ACTION_PACKAGE_REPLACED.equals(androidAction)) {
	    sodaPopAction = IAndroidSodaPop.ACTION_REGISTER;
	} else if (Intent.ACTION_PACKAGE_REMOVED.equals(androidAction)) {
	    sodaPopAction = IAndroidSodaPop.ACTION_UNREGISTER;
	}

	return sodaPopAction;
    }
}