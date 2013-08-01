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
package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.connectors.ConnectorService;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class ConnectorHandlerFactory {

	private static final String TAG = "ConnectorHandlerFactory";

	public static IMessageHandler createHandler(String action,
			ConnectorService serv) {
		IMessageHandler handler = null;

		if (null == handler) {
			switch (Action.getAction(action)) {
			case CONNECTOR_COMM_MULTICASTSOME:
				handler = new ConnCommMulticastSomeHandler(serv);
				break;
			case CONNECTOR_COMM_MULTICAST:
				handler = new ConnCommMulticastHandler(serv);
				break;
			case CONNECTOR_COMM_UNICAST:
				handler = new ConnCommUnicastHandler(serv);
				break;
			case CONNECTOR_DISC_ANNOUNCE:
				handler = new ConnDiscAnnounceBussesHandler(serv);
				break;
			case CONNECTOR_DISC_DEREGISTER:
				handler = new ConnDiscDeregisterHandler(serv);
				break;
			// case CONNECTOR_DISC_ADDLISTENER:
			// handler = new ConnDiscAddListenerHandler(serv);
			// break;
			// case CONNECTOR_DISC_REMLISTENER:
			// handler = new ConnDiscRemListenerHandler(serv);
			// break;
			default:
				break;
			}
		}

		if (null == handler) {
			String errMsg = "Unkown action for handler [" + action + "]";
			Log.e(TAG, errMsg);
			throw new IllegalArgumentException(errMsg);
		}

		return handler;
	}
}
