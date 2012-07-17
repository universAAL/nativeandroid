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
package org.universAAL.middleware.android.buses.servicebus.servicecaller;

import org.universAAL.middleware.android.buses.common.UnregisterBroadcastReceivers;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ServiceRequestGroundingXmlObj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class ServiceRequestsMngr {

    private final static String TAG = ServiceRequestsMngr.class.getCanonicalName();

    public static void reigsterToServiceRequestGroundingActions(String serviceCallerID,
	    ServiceRequestGroundingXmlObj serviceRequestGroundingXmlObj, Context context) {

	for (ActionXmlObj action : serviceRequestGroundingXmlObj.getActions()) {
	    // Register
	    registerReceiver(serviceCallerID, action, context);
	}
    }

    public static void unreigsterToServiceRequestGroundingActions(
	    ServiceRequestGroundingXmlObj serviceRequestGroundingXmlObj, Context context) {

	for (ActionXmlObj action : serviceRequestGroundingXmlObj.getActions()) {
	    // unregister
	    UnregisterBroadcastReceivers.unregisterReceiver(action.getAndroidAction(),
		    action.getAndroidCategory(), context);
	}
    }

    private static BroadcastReceiver registerReceiver(String serviceCallerID, ActionXmlObj action,
	    Context context) {
	// Initiate the receiver
	BroadcastReceiver receiver = new ServiceCallerRequestBroadcastReceiver(serviceCallerID,
		action.getAndroidAction());

	// Intent filter
	IntentFilter filter = new IntentFilter(action.getAndroidAction());
	filter.addCategory(action.getAndroidCategory());

	// Register the receiver
	context.registerReceiver(receiver, filter);

	Log.d(TAG, "A registration to receiver for service request has been performed: Action ["
		+ action.getAndroidAction() + "]; Category [" + action.getAndroidCategory() + "]");

	return receiver;
    }
}
