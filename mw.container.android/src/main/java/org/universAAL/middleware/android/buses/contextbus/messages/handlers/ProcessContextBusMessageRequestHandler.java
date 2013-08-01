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
package org.universAAL.middleware.android.buses.contextbus.messages.handlers;

//import org.universAAL.middleware.android.buses.common.messages.handlers.AbstractProcessBusMessageRequestHandler;
import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.ProcessBusMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
//import org.universAAL.middleware.android.common.messages.handlers.AbstractProcessBusMessageRequestHandler;
//import org.universAAL.middleware.android.common.messages.handlers.BusMessage;
//import org.universAAL.middleware.android.common.messages.handlers.ProcessBusMessage;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.sodapop.AbstractBus;
import org.universAAL.middleware.bus.msg.BusMessage;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 27, 2012
 * 
 */
// TODO: If PersistableHandler is only used fro sql for sodapop, try to remove
// it and make this extend something more simple (follow examples in connector
// or modules)
public class ProcessContextBusMessageRequestHandler extends
		AbstractMessagePersistableHandler {

	// @Override
	// protected AbstractBus createBus(IMessage message/*,
	// AbstractSodaPopAndroidImpl sodaPop*/) {
	// return createContextBus(message, sodaPop);
	// }

	public ProcessContextBusMessageRequestHandler(ModulesCommWrapper wrapper) {
		super(wrapper);
	}

	@Override
	protected void privateHandleMessage(IMessage message) {
		// Create the android bus
		AndroidContextBusImpl bus = createContextBus();
		// Cast to RequestMessage
		ProcessBusMessage requestMessage = (ProcessBusMessage) message;
		// Initiate a Message object
		BusMessage msg = new BusMessage(requestMessage.getuAALMessage());
		// Handle the given message
		bus.handleRemoteMessage(msg);
	}
}
