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
package org.universAAL.middleware.android.buses.servicebus.service;

import org.universAAL.middleware.android.buses.servicebus.messages.ServiceBusMessageFactory;
import org.universAAL.middleware.android.buses.servicebus.messages.handlers.ServiceBusHandlerFactory;
import org.universAAL.middleware.android.common.buses.service.AbstractAndroidBusService;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;

import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public class AndroidServiceBusService extends AbstractAndroidBusService {

    private final static String TAG = AndroidServiceBusService.class.getCanonicalName();

    @Override
    protected void privateOnStart(Intent intent, int startId) {
	// Analyze the intent - create the message
	IMessage message = ServiceBusMessageFactory.createMessage(this, intent);

	// Create the handler
	IMessageHandler handler = ServiceBusHandlerFactory.createHandler(intent.getAction());

	if (null == handler || null == message) {
	    Log.w(TAG, "Action is null!");
	    return;
	}

	// Handle the message
	message.handle(handler);
    }
}
