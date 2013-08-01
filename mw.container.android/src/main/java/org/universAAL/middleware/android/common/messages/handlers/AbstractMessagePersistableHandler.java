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

//import org.universAAL.middleware.android.buses.contextbus.AndroidContextBus;
import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
//import org.universAAL.middleware.android.buses.servicebus.AndroidServiceBus;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessageHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
//import org.universAAL.middleware.android.common.modulecontext.AndroidModuleContextFactory;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.android.localsodapop.SodaPopAndroidFactory;
//import org.universAAL.middleware.android.localsodapop.persistence.SodaPopPeersSQLiteMngr;

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

//	protected SodaPopPeersSQLiteMngr sodaPopPeersSQLiteMngr;

	private void populateSqliteMngr(Context context) {
//		if (null == sodaPopPeersSQLiteMngr) {
//			sodaPopPeersSQLiteMngr = new SodaPopPeersSQLiteMngr(context);
//		}
	}

	public void handleMessage(IMessage message) {
		populateSqliteMngr(message.getContext());

		// Create the implementation class + the c'tor itself will persist the
		// required data
//		AbstractSodaPopAndroidImpl sodaPop = SodaPopAndroidFactory
//				.createAndroidSodaPop(message.getContext(),
//						message.getProtocol(), sodaPopPeersSQLiteMngr);

		privateHandleMessage(message/*, sodaPop*/);
	}

	protected abstract void privateHandleMessage(IMessage message/*,
			AbstractSodaPopAndroidImpl sodaPop*/);

	// TODO: add another middle class that will have service bus related methods
	// and another class for context bus ones
	protected AndroidServiceBusImpl createServiceBus(/*IMessage message,
			AbstractSodaPopAndroidImpl sodaPop*/) {
//		// Get the context
//		Context context = message.getContext();
//
//		// Create the android service bus
//		AndroidServiceBus bus = new AndroidServiceBus(sodaPop, context);
//
//		// Share the bus
//		ModuleContext mc = AndroidModuleContextFactory.createModuleContext();
//		AndroidModuleContextFactory.shareServiceBus(mc, bus);
		return wrapperModules.createServiceBus();
	}

	protected AndroidContextBusImpl createContextBus(/*IMessage message,
			AbstractSodaPopAndroidImpl sodaPop*/) {
//		// Get the context
//		Context context = message.getContext();
//		// Create the android context bus
//		AndroidContextBus bus = new AndroidContextBus(sodaPop, context);
//		// Share the bus
//		ModuleContext mc = AndroidModuleContextFactory.createModuleContext();
//		AndroidModuleContextFactory.shareContextBus(mc, bus);
//		context = ContextEmulator.createContextEmulator();
		return wrapperModules.createContextBus();
	}
}
