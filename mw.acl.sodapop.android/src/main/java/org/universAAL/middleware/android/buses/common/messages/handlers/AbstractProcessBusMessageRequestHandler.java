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
package org.universAAL.middleware.android.buses.common.messages.handlers;

import org.universAAL.middleware.android.buses.common.messages.ProcessBusMessage;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.android.localsodapop.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 23, 2012
 * 
 */
public abstract class AbstractProcessBusMessageRequestHandler extends
	AbstractMessagePersistableHandler {

    @Override
    protected void privateHandleMessage(IMessage message, AbstractSodaPopAndroidImpl sodaPop) {
	// Create the android bus
	AbstractBus bus = createBus(message, sodaPop);

	// Cast to RequestMessage
	ProcessBusMessage requestMessage = (ProcessBusMessage) message;

	// Initiate a Message object
	BusMessage msg = new BusMessage(requestMessage.getuAALMessage());

	// Handle the given message
	bus.handleRemoteMessage(msg);
    }

    abstract protected AbstractBus createBus(IMessage message, AbstractSodaPopAndroidImpl sodaPop);
}
