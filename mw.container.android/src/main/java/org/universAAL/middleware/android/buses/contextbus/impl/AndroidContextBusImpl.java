/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.android.buses.contextbus.impl;

import java.lang.reflect.Constructor;

import org.universAAL.middleware.android.buses.common.AndroidNameParameterParser;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.AndroidContextPublisherProxy;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.ContextRequestsMngr;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.android.common.IllegalGroundingFormat;
import org.universAAL.middleware.android.common.ReflectionsUtils;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.model.util.IRegistry;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.bus.msg.MessageType;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.owl.ContextBusOntology;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * 
 */
public class AndroidContextBusImpl extends AbstractBus implements ContextBus,
		IAndroidBus {
	private static final String TAG = "ContextBusImpl";
	private static Object[] busFetchParams;
	private static AndroidContextBusImpl theContextBus;
	private static ContextBusOntology contextBusOntology = new ContextBusOntology();
	private static Context androidContext;

	public static Object[] getContextBusFetchParams() {
		return busFetchParams.clone();
	}

	public synchronized void assessContentSerialization(Resource content) {
		if (Constants.debugMode()) {
			Log.d(TAG, "Assessing message content serialization:");

			String str = BusMessage.trySerializationAsContent(content);
			Log.d(TAG,
					"\n      1. serialization dump\n"
							+ str
							+ "\n      2. deserialize & compare with the original resource\n");
			new ResourceComparator().printDiffs(content,
					(Resource) BusMessage.deserializeAsContent(str));
		}
	}

	public static AndroidContextBusImpl startModule(Container c,
			ModuleContext mc, Object[] contextBusShareParams,
			Object[] contextBusFetchParams, Context ac) {
		if (theContextBus == null) {
			OntologyManagement.getInstance().register(mc, contextBusOntology);
			androidContext = ac;
			theContextBus = new AndroidContextBusImpl(mc);
			busFetchParams = contextBusFetchParams;
			c.shareObject(mc, theContextBus, contextBusShareParams);
		}
		return theContextBus;
	}

	public static void stopModule() {
		if (theContextBus != null) {
			OntologyManagement.getInstance().unregister(theContextBus.context,
					contextBusOntology);
			theContextBus.dispose();
			theContextBus = null;
		}
	}

	private AndroidContextBusImpl(ModuleContext mc) {
		super(mc);
		busStrategy.setBus(this);
	}

	protected BusStrategy createBusStrategy(CommunicationModule commModule) {
		return new AndroidContextStrategy(commModule);
	}

	public void addNewRegParams(String memberID,
			ContextEventPattern[] registrParams) {
		if (memberID != null && registrParams != null) {
			Object o = registry.getBusMemberByID(memberID);
			if (o instanceof ContextSubscriber) {
				((AndroidContextStrategy) busStrategy).addRegParams(
						(ContextSubscriber) o, registrParams);
			} else if (o instanceof ContextPublisher) {
				((AndroidContextStrategy) busStrategy).addRegParams(
						(ContextPublisher) o, registrParams);
			}
		}
	}

	public ContextEventPattern[] getAllProvisions(String publisherID) {
		if (publisherID != null) {
			Object o = registry.getBusMemberByID(publisherID);
			if (o instanceof ContextPublisher) {
				return ((AndroidContextStrategy) busStrategy)
						.getAllProvisions((ContextPublisher) o);
			}
		}
		return null;
	}

	public void removeMatchingRegParams(String memberID,
			ContextEventPattern[] oldRegistrParams) {
		if (memberID != null && oldRegistrParams != null) {
			Object o = registry.getBusMemberByID(memberID);
			if (o instanceof ContextSubscriber) {
				((AndroidContextStrategy) busStrategy).removeMatchingRegParams(
						(ContextSubscriber) o, oldRegistrParams);
			} else if (o instanceof ContextPublisher) {
				((AndroidContextStrategy) busStrategy).removeMatchingRegParams(
						(ContextPublisher) o, oldRegistrParams);
			}
		}
	}

	public void brokerContextEvent(String publisherID, ContextEvent msg) {
		assessContentSerialization(msg);
		if (publisherID != null) {
			super.brokerMessage(publisherID, new BusMessage(MessageType.event,
					msg, this));
		}
	}

	public void unregister(String publisherID, ContextPublisher publisher) {
		super.unregister(publisherID, publisher);
		((AndroidContextStrategy) busStrategy).removeRegParams(publisher);
	}

	public void unregister(String subscriberID, ContextSubscriber subscriber) {
		super.unregister(subscriberID, subscriber);
		((AndroidContextStrategy) busStrategy).removeRegParams(subscriber);
	}

	public void handleSendError(ChannelMessage message,
			CommunicationConnectorException e) {
		// TODO Auto-generated method stub
	}

	// Added by IAndroidBus
	public String getName() {
		return getBrokerName();
	}

	public String getPackageName() {
		return AndroidContextBusImpl.class.getPackage().getName();
	}

	public String getClassName() {
		return AndroidContextBusImpl.class.getCanonicalName();
	}

	// Added/overridden by port
	@Override
	protected IRegistry createRegistry() {

		return new AndroidContextBusRegistry(this, androidContext);
	}

	public void sendContextPublishMessage(String contextPublisherID,
			String contextPublisherAction, Bundle extrasSection) {
		// Extract the member ID
		String memberID = contextPublisherID;
		// TODO: move this to some shared method
		// TODO: Check with/out prefix
		// Get the AndroidContextPublisher from the DB
		AndroidContextPublisherProxy androidContextPublisher = (AndroidContextPublisherProxy) registry
				.getBusMemberByID(memberID);

		// Populate the context event
		ContextEvent contextEvent;
		try {
			contextEvent = createContextEvent(contextPublisherAction,
					extrasSection, androidContextPublisher);
		} catch (IllegalGroundingFormat e) {
			String errMsg = e.getMessage() + " for MemberID [" + memberID
					+ "]; AndroidAction [" + contextPublisherAction + "]";
			Log.e(TAG, errMsg);
			return;
		}
		brokerContextEvent(contextPublisherID, contextEvent);
	}

	private ContextEvent createContextEvent(String contextPublisherAction,
			Bundle extrasSection,
			AndroidContextPublisherProxy androidContextPublisher)
			throws IllegalGroundingFormat {

		ContextPublisherGroundingXmlObj publisherGrounding = androidContextPublisher
				.getContextPublisherGrounding();
		ActionXmlObj action = publisherGrounding
				.getActionByName(contextPublisherAction);

		// TODO: check that the action was found

		// Parse the android name
		AndroidNameParameterParser parser = AndroidNameParameterParser
				.parseAndroidName(action.getSubject().getAndroidName());
		String paramValue = extrasSection.getString(parser
				.getAndroidParameter());
		String subjectUri = parser.getAndroidNameWithoutParameter()
				+ paramValue;

		// Initiate the object to be set in the context event
		Constructor ctor = null;
		Object obj = null;
		try {
			ctor = ReflectionsUtils.createCtorThatReceiveStringParam(action
					.getObject().getAndroidExtraParameterJavaClass());
			obj = ReflectionsUtils.invokeCtorWithStringParam(ctor,
					extrasSection.getString(action.getObject()
							.getAndroidExtraParameter()));
		} catch (Throwable th) {

			throw new IllegalAccessError(
					"Unable to map the android intent to the uAAL world due to ["
							+ th.getMessage() + "]");
		}

		ContextEvent contextEvent = ContextEvent.constructSimpleEvent(
				subjectUri, action.getSubject().getUri(), action.getPredicate()
						.getUri(), obj);

		return contextEvent;
	}

	public void unregisterByPackageName(String packageName) {
		// Extract list of member ID's that have the package name
		AndroidContextBusRegistry androidRegistryContexts = getAndroidRegistryContexts();
		BusMember[] members = androidRegistryContexts
				.getBusMembersByPackageName(packageName);

		// Iteratively unregister each member
		for (BusMember busMember : members) {
			if (busMember instanceof ContextPublisher) {
				AndroidContextPublisherProxy publisher = (AndroidContextPublisherProxy) busMember;
				// Unregister from the bus
				unregister(publisher.getMyID(), publisher);

				// Unregister the receivers
				ContextRequestsMngr
						.unreigsterToContextPublisherGroundingActions(
								publisher.getContextPublisherGrounding(),
								androidContext);
			} else if (busMember instanceof ContextSubscriber) {
				AndroidContextSubscriberProxy subscriber = (AndroidContextSubscriberProxy) busMember;

				// Unregister from the bus
				unregister(subscriber.getMyID(), subscriber);
			}
		}
	}

	private AndroidContextBusRegistry getAndroidRegistryContexts() {
		return (AndroidContextBusRegistry) registry;
	}

	// Cheat workaround for port
	public ModuleContext getModuleContex() {
		return this.context;
	}

	// Workaround for port because this was removed but still valid
	public void handleRemoteMessage(BusMessage msg) {
		// sender ID is null for remote messages
		busStrategy.handleMessage(msg, null);
	}

	// PATCH This comes from the infamous proxies patch. Because of that patch,
	// everytime a request is sent, a caller is created. Its membership is
	// checked but it would fail because the caller created on demand is not in
	// the android registry, because the ondemand has a new busResourceURI,
	// while
	// the registry uses myID
	@Override
	public boolean isValidMember(String memberURI) {
		return true;
	}
}
