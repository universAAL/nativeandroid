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
package org.universAAL.middleware.android.connectors.messages;

import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.IMessage;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 18, 2012
 * 
 */
public class ConnectorMessageFactory {

	private static final String TAG = "ConnectorMessageFactory";

	public static IMessage createMessage(Context context, Intent intent) {
		IMessage message = null;

		String actionAsString = intent.getAction();
		if (null == actionAsString)
			return null;

		switch (Action.getAction(actionAsString)) {
		case CONNECTOR_COMM_MULTICASTSOME:
			message = new ConnCommMulticastSomeMessage(context, intent);
			break;
		case CONNECTOR_COMM_MULTICAST:
			message = new ConnCommMulticastMessage(context, intent);
			break;
		case CONNECTOR_COMM_UNICAST:
			message = new ConnCommUnicastMessage(context, intent);
			break;
		case CONNECTOR_DISC_ANNOUNCE:
			message = new ConnDiscAnnounceBussesMessage(context, intent);
			break;
		case CONNECTOR_DISC_DEREGISTER:
			message = new ConnDiscDeregisterMessage(context, intent);
			break;
		// case CONNECTOR_DISC_ADDLISTENER:
		// message = new ConnDiscAddListenerMessage(context,intent);
		// break;
		// case CONNECTOR_DISC_REMLISTENER:
		// message = new ConnDiscRemListenerMessage(context,intent);
		// break;
		default:
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
