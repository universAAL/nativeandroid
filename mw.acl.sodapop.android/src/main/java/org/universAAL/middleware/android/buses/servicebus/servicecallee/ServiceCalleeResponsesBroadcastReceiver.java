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
package org.universAAL.middleware.android.buses.servicebus.servicecallee;

import org.universAAL.middleware.android.buses.servicebus.service.AndroidServiceBusService;
import org.universAAL.middleware.android.common.IAndroidSodaPop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 7, 2012
 * 
 */
public class ServiceCalleeResponsesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ServiceCalleeResponsesBroadcastReceiver.class
	    .getCanonicalName();

    private String serviceCalleeID;
    private String operationNameToRespondTo;
    private String replyTo;

    public ServiceCalleeResponsesBroadcastReceiver(String serviceCalleeID,
	    String operationNameToRespondTo, String replyTo) {
	this.serviceCalleeID = serviceCalleeID;
	this.operationNameToRespondTo = operationNameToRespondTo;
	this.replyTo = replyTo;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	Log.d(TAG, "ServiceCalleResponsesBroadcastReceiver received Intent [" + intent.toString()
		+ "]; in reply to [" + replyTo + "]");

	// Unregister this
	context.unregisterReceiver(this);

	// Clone the given intent
	Intent androidServiceBusIntent = new Intent(intent);

	// Set the class to be the ServiceBus
	androidServiceBusIntent.setClass(context, AndroidServiceBusService.class);

	// The action is the message ID, store it in the extras
	String messageIDInReplyTo = intent.getAction();

	// Override the action
	androidServiceBusIntent.setAction(IAndroidSodaPop.ACTION_PROCESS_BUS_MESSAGE_RSP);

	// Add the serviceCalleeID + replyTo to the extras section
	androidServiceBusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_SERVICE_CALLEE_ID,
		serviceCalleeID);
	androidServiceBusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_OPERATION_NAME,
		operationNameToRespondTo);
	androidServiceBusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_REPLY_TO, replyTo);
	androidServiceBusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_MESSAGE_ID_IN_REPLY,
		messageIDInReplyTo);

	// Add protocol // TODO: this should be handled in more generic way
	androidServiceBusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL,
		IAndroidSodaPop.PROTOCOL_UPNP);

	// Start the service
	context.startService(androidServiceBusIntent);
    }
}
