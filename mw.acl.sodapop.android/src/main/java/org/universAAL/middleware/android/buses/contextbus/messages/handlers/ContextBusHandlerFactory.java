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
package org.universAAL.middleware.android.buses.contextbus.messages.handlers;

import org.universAAL.middleware.android.buses.contextbus.messages.ProcessContextBusMessageRequestHandler;
import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class ContextBusHandlerFactory {

    private static final String TAG = ContextBusHandlerFactory.class.getCanonicalName();

    public static IMessageHandler createHandler(String action) {
	IMessageHandler handler = null;

	if (null == action)
	    return null;

	switch (Action.getAction(action)) {
	case INITIALIZE:
	    handler = new InitializeContextBusHandler();
	    break;
	case REGISTER:
	    handler = new RegisterContextsHandler();
	    break;
	case UNREGISTER:
	    handler = new UnregisterContextsHandler();
	    break;
	case CONTEXT_PUBLISHER_REQUEST:
	    handler = new ContextPublisherRequestHandler();
	    break;
	case PROCESS_BUS_MESSAGE:
	    handler = new ProcessContextBusMessageRequestHandler();
	    break;
	}

	if (null == handler) {
	    String errMsg = "Unkown action for handler [" + action + "]";
	    Log.e(TAG, errMsg);
	    throw new IllegalArgumentException(errMsg);
	}

	return handler;
    }
}
