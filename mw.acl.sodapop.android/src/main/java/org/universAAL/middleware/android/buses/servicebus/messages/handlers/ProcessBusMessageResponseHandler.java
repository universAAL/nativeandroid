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
package org.universAAL.middleware.android.buses.servicebus.messages.handlers;

import org.universAAL.middleware.android.buses.servicebus.AndroidServiceBus;
import org.universAAL.middleware.android.buses.servicebus.messages.ProcessBusMessageResponse;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.android.localsodapop.messages.handlers.AbstractMessagePersistableHandler;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 23, 2012
 * 
 */
public class ProcessBusMessageResponseHandler extends AbstractMessagePersistableHandler {

    @Override
    protected void privateHandleMessage(IMessage message, AbstractSodaPopAndroidImpl sodaPop) {
	// Create the android service bus
	AndroidServiceBus bus = createServiceBus(message, sodaPop);

	// Cast to ResponseMessage
	ProcessBusMessageResponse responseMessage = (ProcessBusMessageResponse) message;

	// Ask the AndroidServiceBus to handle the response
	bus.handleResponse(responseMessage.getMessageIDInReplyTo(),
		responseMessage.getServiceCalleeID(),
		responseMessage.getOperationNameToRespondTo(), responseMessage.getReplyTo(),
		responseMessage.getExtras());
    }
}
