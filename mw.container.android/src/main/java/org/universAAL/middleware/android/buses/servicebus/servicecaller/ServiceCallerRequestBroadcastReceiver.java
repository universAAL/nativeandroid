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
package org.universAAL.middleware.android.buses.servicebus.servicecaller;

import org.universAAL.middleware.android.buses.servicebus.ServiceBusService;
import org.universAAL.middleware.android.common.StringConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class ServiceCallerRequestBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = ServiceCallerRequestBroadcastReceiver.class
			.getCanonicalName();

	private String serviceCallerID;
	private String androidAction;

	public ServiceCallerRequestBroadcastReceiver(String serviceCallerID,
			String androidAction) {
		this.serviceCallerID = serviceCallerID;
		this.androidAction = androidAction;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "ServiceCallerRequestBroadcastReceiver received Intent ["
				+ intent.toString() + "]; ServiceCallerID [" + serviceCallerID
				+ "]");

		// Check if need to unregister
		if (needToUnregister(intent)) {
			context.unregisterReceiver(this);
			Log.d(TAG, "Broadcast receiver has been unregistered!");
			return;
		}

		// Clone the intent
		Intent androidServiceBusIntent = new Intent(intent);

		// Set the class to be the ServiceBus
		androidServiceBusIntent.setClass(context, ServiceBusService.class);

		// Change the action
		androidServiceBusIntent
				.setAction(StringConstants.ACTION_SERVICE_CALLER_REQUEST);

		// Store the service caller ID
		androidServiceBusIntent.putExtra(
				StringConstants.EXTRAS_KEY_SERVICE_CALLER_ID, serviceCallerID);

		// Store the android action (the action that is related to the caller)
		androidServiceBusIntent.putExtra(
				StringConstants.EXTRAS_KEY_ACTION_NAME, androidAction);

		// Start the service
		context.startService(androidServiceBusIntent);
	}

	private boolean needToUnregister(Intent intent) {
		boolean unreigster = intent
				.getBooleanExtra(
						StringConstants.EXTRAS_KEY_UNREGISTER_BROADCAST_RECEIVER,
						false);

		return unreigster;
	}

	public String getServiceCallerID() {
		return serviceCallerID;
	}

	public String getAndroidAction() {
		return androidAction;
	}
}
