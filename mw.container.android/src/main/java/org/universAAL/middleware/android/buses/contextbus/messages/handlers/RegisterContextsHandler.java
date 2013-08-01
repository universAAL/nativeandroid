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

import org.universAAL.middleware.android.buses.common.AndroidOntologyManagement;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.AndroidContextPublisherProxy;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ContextSubscriberGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.IRestrictionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.contextbus.messages.RegisterContextsMessage;
import org.universAAL.middleware.android.buses.contextbus.messages.RegisterContextsMessage.AndroidContextPublisherDataWrapper;
import org.universAAL.middleware.android.buses.contextbus.messages.RegisterContextsMessage.AndroidContextSubscriberDataWrapper;
import org.universAAL.middleware.android.common.IllegalGroundingFormat;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
import org.universAAL.middleware.container.ModuleContext;
//import org.universAAL.middleware.android.common.modulecontext.AndroidModuleContextFactory;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.android.localsodapop.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextBusOntology;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class RegisterContextsHandler extends AbstractMessagePersistableHandler {

	public RegisterContextsHandler(ModulesCommWrapper wrapper) {
		super(wrapper);
	}

	private static final String TAG = RegisterContextsHandler.class
			.getCanonicalName();

	@Override
	protected void privateHandleMessage(IMessage message/*
														 * ,
														 * AbstractSodaPopAndroidImpl
														 * sodaPop
														 */) {
		// Cast it to Register Contexts message
		RegisterContextsMessage registerContextsMessage = (RegisterContextsMessage) message;

		// Check if there is anything to handle (for example - it is an
		// application that was installed and doesn't contain ContextPublisher /
		// ContextSubscriber definition)
		if (registerContextsMessage.getAndroidContextPublisherDataWrapperList()
				.isEmpty()
				&& registerContextsMessage
						.getAndroidContextSubscriberDataWrapperList().isEmpty()) {
			Log.d(TAG, "Nothing was found to handle, therefore do nothing!");
			return;
		}

		// TODO Share the ModuleContext
		// AndroidModuleContextFactory.createModuleContext();

		// Get the context
		Context context = registerContextsMessage.getContext();

		// Create AndroidContextBus
		AndroidContextBusImpl androidContextBus = createContextBus();/*
																	 * new
																	 * AndroidContextBusImpl
																	 * (
																	 * sodaPop,
																	 * context);
																	 */

		// Handle each ContextPublisher registration/unregistration separately
		for (AndroidContextPublisherDataWrapper contextPublisherRegistration : registerContextsMessage
				.getAndroidContextPublisherDataWrapperList()) {
			handleContextPublisher(contextPublisherRegistration,
					androidContextBus, context);
		}

		// Handle each ContextSubscriber registration/unregistration separately
		for (AndroidContextSubscriberDataWrapper contextSubscriberRegistration : registerContextsMessage
				.getAndroidContextSubscriberDataWrapperList()) {
			handleContextSubscriber(contextSubscriberRegistration,
					androidContextBus, context);
		}
	}

	private void handleContextPublisher(
			AndroidContextPublisherDataWrapper contextPublisherRegistration,
			AndroidContextBusImpl androidContextBus, Context context) {

		// Extract the grounding xml
		ContextPublisherGroundingXmlObj contextPublisherGroundingXml = contextPublisherRegistration
				.getContextPublisherGroundingXmlObj();

		// Populate the ContentProvider
		ContextProvider contextProviderInfo = new ContextProvider(
				contextPublisherGroundingXml.getUri());
		contextProviderInfo.setType(ContextProviderType.controller);
		contextProviderInfo
				.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });

		// Create AndroidContextPublisher - the c'tor will do all the
		// registration stuff
		new AndroidContextPublisherProxy(androidContextBus.getModuleContex(),
				true, contextProviderInfo,
				contextPublisherRegistration.getPackageName(),
				contextPublisherRegistration.getGroundingID(),
				contextPublisherRegistration.getAndroidUniqueName(),
				contextPublisherGroundingXml, context);
	}

	private void handleContextSubscriber(
			AndroidContextSubscriberDataWrapper contextSubscriberRegistration,
			AndroidContextBusImpl androidContextBus, Context context) {

		Log.d(TAG, "Is about to handle Context subcription");

		// Extract the grounding xml
		ContextSubscriberGroundingXmlObj contextSubscriberGroundingXml = contextSubscriberRegistration
				.getContextSubscriberGroundingXmlObj();

		// Register the ontologies
		registerOntologies(androidContextBus.getModuleContex(),
				contextSubscriberGroundingXml.getOntologies());

		// Populate the initial subscriptions
		ContextEventPattern[] initialSubscriptions;
		try {
			initialSubscriptions = populateInitialSubscriptions(contextSubscriberGroundingXml);
		} catch (IllegalGroundingFormat e) {
			// Log it
			String errMsg = e.getMessage() + " for PackageName ["
					+ contextSubscriberRegistration.getPackageName()
					+ "]; GroundingID ["
					+ contextSubscriberRegistration.getGroundingID()
					+ "]; AndroidUniqueName ["
					+ contextSubscriberRegistration.getAndroidUniqueName()
					+ "]";
			Log.e(TAG, errMsg);
			return;
		}

		// Create AndroidContextSubscriber - the c'tor will do all the
		// registration stuff
		new AndroidContextSubscriberProxy(androidContextBus.getModuleContex(),
				true, initialSubscriptions,
				contextSubscriberRegistration.getPackageName(),
				contextSubscriberRegistration.getGroundingID(),
				contextSubscriberRegistration.getAndroidUniqueName(),
				contextSubscriberGroundingXml, context);
	}

	private static void registerOntologies(ModuleContext mc, String[] ontologies) {

		ContextBusOntology contextOntology = new ContextBusOntology();
		AndroidOntologyManagement.registerOntology(mc, contextOntology);

		// Register the ontologies that the context subscriber uses
		for (String ontologyClass : ontologies) {
			try {
				AndroidOntologyManagement.registerOntology(mc, ontologyClass);
			} catch (Throwable th) {
				String errMsg = "Error when registering ontology ["
						+ ontologyClass + "] due to [" + th.getMessage() + "]";
				Log.e(TAG, errMsg); // TODO: throw here an exception
			}
		}
	}

	private ContextEventPattern[] populateInitialSubscriptions(
			ContextSubscriberGroundingXmlObj contextSubscriberGroundingXml)
			throws IllegalGroundingFormat {

		ContextEventPattern cep = new ContextEventPattern();

		// Extract the action
		ActionXmlObj action = contextSubscriberGroundingXml.getAction();

		MergedRestriction mergedRestriction = null;
		// Iterate over the restrictions
		for (IRestrictionXmlObj restriction : action.getRestrictions()) {
			mergedRestriction = restriction
					.populateRestriction(mergedRestriction);
		}

		return new ContextEventPattern[] { cep };
	}
}
