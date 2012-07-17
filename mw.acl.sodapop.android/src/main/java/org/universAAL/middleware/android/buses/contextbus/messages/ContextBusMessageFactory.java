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
package org.universAAL.middleware.android.buses.contextbus.messages;

import org.universAAL.middleware.android.buses.common.messages.ProcessBusMessage;
import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.InitializeMessage;
import org.universAAL.middleware.android.common.messages.UnregisterAppsMessage;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class ContextBusMessageFactory {

    private static final String TAG = ContextBusMessageFactory.class.getCanonicalName();

    public static IMessage createMessage(Context context, Intent intent) {
	IMessage message = null;

	String actionAsString = intent.getAction();
	if (null == actionAsString)
	    return null;

	switch (Action.getAction(actionAsString)) {
	case INITIALIZE:
	    message = new InitializeMessage(context, intent);
	    break;
	case REGISTER:
	    message = new RegisterContextsMessage(context, intent);
	    break;
	case UNREGISTER:
	    message = new UnregisterAppsMessage(context, intent);
	    break;
	case CONTEXT_PUBLISHER_REQUEST:
	    message = new ContextPublisherRequestMessage(context, intent);
	    break;
	case PROCESS_BUS_MESSAGE:
	    message = new ProcessBusMessage(context, intent);
	    break;
	}

	if (null == message) {
	    String errMsg = "Unkown action for message [" + message + "]";
	    Log.e(TAG, errMsg);
	    throw new IllegalArgumentException(errMsg);
	}

	return message;
    }
}
