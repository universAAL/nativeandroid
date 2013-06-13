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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber;

import org.universAAL.middleware.android.buses.common.UnregisterBroadcastReceivers;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class ContextRequestsMngr {

    private final static String TAG = ContextRequestsMngr.class.getCanonicalName();

    public static void registerToContextPublisherRequestGroundingActions(String publisherID,
	    ContextPublisherGroundingXmlObj contextPublisherGrounding, Context context) {

	for (ActionXmlObj action : contextPublisherGrounding.getActions()) {
	    // Register
	    registerReceiver(publisherID, action, context);
	}
    }

    public static void unreigsterToContextPublisherGroundingActions(
	    ContextPublisherGroundingXmlObj contextPublisherGroundingXmlObj, Context context) {

	for (ActionXmlObj action : contextPublisherGroundingXmlObj.getActions()) {
	    // unregister
	    UnregisterBroadcastReceivers.unregisterReceiver(action.getAndroidAction(),
		    action.getAndroidCategory(), context);
	}
    }

    private static BroadcastReceiver registerReceiver(String publisherID, ActionXmlObj action,
	    Context context) {

	// Initiate the receiver
	BroadcastReceiver receiver = new ContextPublisherRequestBroadcastReceiver(publisherID,
		action.getAndroidAction());

	// Intent filter
	IntentFilter filter = new IntentFilter(action.getAndroidAction());
	filter.addCategory(action.getAndroidCategory());

	// Register the receiver
	context.registerReceiver(receiver, filter);

	Log.d(TAG, "A registration to receiver for ContextPublisher has been performed: Action ["
		+ action.getAndroidAction() + "]; Category [" + action.getAndroidCategory() + "]");

	return receiver;
    }
}
