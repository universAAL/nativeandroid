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
package org.universAAL.middleware.android.common.messages.handlers;

import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessageHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public abstract class AbstractMessagePersistableHandler extends
		AbstractMessageHandler {
	
	public ModulesCommWrapper wrapperModules;

	public AbstractMessagePersistableHandler(ModulesCommWrapper wrapper){
		this.wrapperModules=wrapper;
	}

	private void populateSqliteMngr(Context context) {

	}

	public void handleMessage(IMessage message) {
		populateSqliteMngr(message.getContext());

		// Create the implementation class + the c'tor itself will persist the
		// required data

		privateHandleMessage(message);
	}

	protected abstract void privateHandleMessage(IMessage message);

	// TODO: add another middle class that will have service bus related methods
	// and another class for context bus ones
	protected AndroidServiceBusImpl createServiceBus() {
		return wrapperModules.createServiceBus();
	}

	protected AndroidContextBusImpl createContextBus() {
		return wrapperModules.createContextBus();
	}
}
