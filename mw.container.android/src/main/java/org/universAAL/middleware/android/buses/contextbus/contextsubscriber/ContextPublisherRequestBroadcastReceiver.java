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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber;

import org.universAAL.middleware.android.buses.contextbus.ContextBusService;
import org.universAAL.middleware.android.common.StringConstants;
//import org.universAAL.middleware.android.common.IAndroidSodaPop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class ContextPublisherRequestBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = ContextPublisherRequestBroadcastReceiver.class
			.getCanonicalName();

	private String contextPublisherID;
	private String androidAction;

	public ContextPublisherRequestBroadcastReceiver(String contextPublisherID,
			String androidAction) {
		this.contextPublisherID = contextPublisherID;
		this.androidAction = androidAction;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,
				"ContextSubscriberRequestBroadcastReceiver received Intent ["
						+ intent.toString() + "]; ContextPublisherID ["
						+ contextPublisherID + "]");

		// Check if need to unregister
		if (needToUnregister(intent)) {
			context.unregisterReceiver(this);
			Log.d(TAG, "Broadcast receiver has been unregistered!");
			return;
		}

		// Clone the intent
		Intent androidContextBusIntent = new Intent(intent);

		// Set the class to be the ContextBus
		androidContextBusIntent.setClass(context, ContextBusService.class);

		// Change the action
		androidContextBusIntent
				.setAction(StringConstants.ACTION_CONTEXT_PUBLISHER_REQUEST);

		// Store the context publisher ID
		androidContextBusIntent.putExtra(
				StringConstants.EXTRAS_KEY_CONTEXT_PUBLISHER_ID,
				contextPublisherID);

		// Store the android action (the action that is related to the caller)
		androidContextBusIntent.putExtra(
				StringConstants.EXTRAS_KEY_ACTION_NAME, androidAction);

		// Add protocol
		// androidContextBusIntent.putExtra(StringConstants.EXTRAS_KEY_PROTOCOL,
		// StringConstants.PROTOCOL_UPNP);

		// Start the service
		context.startService(androidContextBusIntent);
	}

	private boolean needToUnregister(Intent intent) {
		boolean unreigster = intent
				.getBooleanExtra(
						StringConstants.EXTRAS_KEY_UNREGISTER_BROADCAST_RECEIVER,
						false);

		return unreigster;
	}

	public String getContextPublisherID() {
		return contextPublisherID;
	}

	public String getAndroidAction() {
		return androidAction;
	}
}
