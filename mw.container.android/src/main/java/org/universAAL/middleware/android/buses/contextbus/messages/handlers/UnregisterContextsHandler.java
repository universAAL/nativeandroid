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

import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.UnregisterAppsMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessagePersistableHandler;
//import org.universAAL.middleware.android.common.modulecontext.AndroidModuleContextFactory;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.android.localsodapop.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;

//import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class UnregisterContextsHandler extends
		AbstractMessagePersistableHandler {

	public UnregisterContextsHandler(ModulesCommWrapper wrapper) {
		super(wrapper);
	}

	@Override
	protected void privateHandleMessage(IMessage message/*
														 * ,
														 * AbstractSodaPopAndroidImpl
														 * sodaPop
														 */) {
		// Cast it to UnregisterProfiles message
		UnregisterAppsMessage unregisterServicesMessage = (UnregisterAppsMessage) message;

		// TODO Share the ModuleContext
		// AndroidModuleContextFactory.createModuleContext();

		// Get the context
		// Context context = unregisterServicesMessage.getContext();

		// Create AndroidContextBus
		AndroidContextBusImpl androidContextBus = createContextBus();// new
																		// AndroidContextBusImpl(sodaPop,
																		// context);

		// Unregister
		androidContextBus.unregisterByPackageName(unregisterServicesMessage
				.getPackageName());
	}
}
