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
package org.universAAL.middleware.android.common.modulecontext;

import javax.xml.datatype.DatatypeFactory;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.sodapop.impl.SodaPopImpl;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.serialization.turtle.TurtleParser;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 26, 2012
 * 
 */
public class AndroidModuleContextFactory {

    private static final String TAG = AndroidModuleContextFactory.class.getCanonicalName();

    public synchronized static ModuleContext createModuleContext() {
	if (null != SodaPopImpl.moduleContext) {
	    System.out.println("Android module context was already created...");
	    return SodaPopImpl.moduleContext;
	}

	System.out.println("Creating the android module context...");

	ModuleContext moduleContext = new AndroidModuleContext();
	Container container = moduleContext.getContainer();

	SharedResources.moduleContext = moduleContext;
	try {
	    SharedResources.loadReasoningEngine();
	} catch (ClassNotFoundException e) {
	    Log.e(TAG, "Error when loading Reasoning Engine [" + e.getMessage() + "]");
	}

	// Temporary: we will set here hard-coded that the Android is not the
	// coordinator
	// TODO: move this one to a configuration file!!!
	System.setProperty(SharedResources.uAAL_IS_COORDINATING_PEER, "false");

	System.setProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY,
		"org.universAAL.middleware.android.common.datafactory.DatatypeFactoryImpl");

	SharedResources.setDefaults();

	TurtleUtil.moduleContext = moduleContext;
	container.shareObject(TurtleUtil.moduleContext, new TurtleParser(),
		new Object[] { MessageContentSerializer.class.getName() });
	container.shareObject(TurtleUtil.moduleContext, new TurtleParser(),
		new Object[] { MessageContentSerializerEx.class.getName() });

	// Update the SodaPopImpl with the context - // TODO: it is a temporary
	// workaround!!!
	SodaPopImpl.moduleContext = moduleContext;
	SodaPopImpl.updateContentSerializerParams(new Object[] { MessageContentSerializer.class
		.getName() });

	return moduleContext;
    }

    public static void shareServiceBus(ModuleContext moduleContext, ServiceBus serviceBus) {
	ServiceBusImpl.busFetchParams = new Object[] { ServiceBus.class.getName() };
	ServiceBusImpl.contentSerializerParams = new Object[] { MessageContentSerializer.class
		.getName() };
	moduleContext.getContainer().shareObject(moduleContext, serviceBus,
		ServiceBusImpl.busFetchParams);
    }

    public static void shareContextBus(ModuleContext moduleContext, ContextBus contextBus) {
	ContextBusImpl.busFetchParams = new Object[] { ContextBus.class.getName() };
	ContextBusImpl.contentSerializerParams = new Object[] { MessageContentSerializer.class
		.getName() };
	moduleContext.getContainer().shareObject(moduleContext, contextBus,
		ContextBusImpl.busFetchParams);
    }
}
