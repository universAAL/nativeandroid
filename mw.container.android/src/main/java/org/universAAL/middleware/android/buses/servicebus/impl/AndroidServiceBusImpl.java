/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.middleware.android.buses.servicebus.impl;

import java.util.HashMap;

import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.AndroidServiceCalleeProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.ServiceRequestsMngr;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy.WaitingCall;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.model.util.IRegistry;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
//import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.AvailabilitySubscriber;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
//import org.universAAL.middleware.service.impl.ServiceStrategy;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owl.ServiceBusOntology;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
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
public class AndroidServiceBusImpl extends AbstractBus implements ServiceBus,
		IAndroidBus {
	private static final String TAG ="AndroidServiceBusImpl";
	private static Object[] busFetchParams;
	private static AndroidServiceBusImpl theServiceBus = null;
	private static ServiceBusOntology serviceOntology = new ServiceBusOntology();
	private static ModuleContext mc;
	private static Context androidContext;

	public static Object[] getServiceBusFetchParams() {
		return busFetchParams.clone();
	}

	public synchronized void assessContentSerialization(Resource content) {
		if (Constants.debugMode()) {
			Log.d(TAG, "Assessing message content serialization:" );

			String str = BusMessage.trySerializationAsContent(content);
			Log.d(TAG, "\n      1. serialization dump\n"+ str+
							"\n      2. deserialize & compare with the original resource\n" );
			new ResourceComparator().printDiffs(content,
					(Resource) BusMessage.deserializeAsContent(str));
		}
	}

	public static AndroidServiceBusImpl startModule(Container c, ModuleContext mc,
			Object[] serviceBusShareParams, Object[] serviceBusFetchParams, Context ac) {
		if (theServiceBus == null) {
			AndroidServiceBusImpl.mc = mc;
			OntologyManagement.getInstance().register(mc, serviceOntology);
			androidContext=ac;
			theServiceBus = new AndroidServiceBusImpl(mc);
			busFetchParams = serviceBusFetchParams;
			c.shareObject(mc, theServiceBus, serviceBusShareParams);
		}
		return theServiceBus;
	}

	public static void stopModule() {
		if (theServiceBus != null) {
			OntologyManagement.getInstance().unregister(mc, serviceOntology);
			theServiceBus.dispose();
			theServiceBus = null;
		}
	}

	private AndroidServiceBusImpl(ModuleContext mc) {
		super(mc);
		busStrategy.setBus(this);
	}

//	public AndroidServiceBusImpl(ModuleContext mc/* SodaPop sodaPop */, Context ctxt) {
//		super(mc/* sodaPop */);
//
//		this.androidContext = ctxt;
//	}

	public static ModuleContext getModuleContext() {
		return mc;
	}

	/**
	 * @see org.universAAL.middleware.service.ServiceBus#addAvailabilitySubscription(String,
	 *      AvailabilitySubscriber, ServiceRequest)
	 */
	public void addAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber, ServiceRequest request) {
		if (callerID != null
				&& registry.getBusMemberByID(callerID) instanceof ServiceCaller) {
			((AndroidServiceStrategy) busStrategy).addAvailabilitySubscription(
					callerID, subscriber, request);
		}
	}

	/**
	 * @see org.universAAL.middleware.service.ServiceBus#addNewServiceProfiles(String,
	 *      ServiceProfile[])
	 */
	public void addNewServiceProfiles(String calleeID,
			ServiceProfile[] realizedServices) {
		if (calleeID != null) {
			((AndroidServiceStrategy) busStrategy).addRegParams(calleeID,
					realizedServices);
		}
	}

	/**
	 * @see org.universAAL.middleware.service.ServiceBus#getAllServices(String)
	 */
	public ServiceProfile[] getAllServices(String callerID) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see ServiceBus#getMatchingServices(String, Service)
	 */
	public ServiceProfile[] getMatchingServices(String callerID, Service s) {
		return ((AndroidServiceStrategy) busStrategy).getAllServiceProfiles(s
				.getType());
	}

	/**
	 * @see ServiceBus#getMatchingService(String, String)
	 */
	public ServiceProfile[] getMatchingServices(String callerID, String s) {
		return ((AndroidServiceStrategy) busStrategy).getAllServiceProfiles(s);
	}

	/**
	 * @see ServiceBus#getMatchingService(String)
	 */
	public HashMap getMatchingServices(String s) {
		return ((AndroidServiceStrategy) busStrategy)
				.getAllServiceProfilesWithCalleeIDs(s);
	}

	/**
	 * @see ServiceBus#getMatchingServices(String, String[])
	 */
	public ServiceProfile[] getMatchingServices(String callerID,
			String[] keywords) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see ServiceBus#removeAvailabilitySubscription(String,
	 *      AvailabilitySubscriber, String)
	 */
	public void removeAvailabilitySubscription(String callerID,
			AvailabilitySubscriber subscriber, String requestURI) {
		if (callerID != null
				&& registry.getBusMemberByID(callerID) instanceof ServiceCaller) {
			((AndroidServiceStrategy) busStrategy).removeAvailabilitySubscription(
					callerID, subscriber, requestURI);
		}
	}

	/**
	 * @see ServiceBus#removeMatchingRegParams(String, ServiceProfile[])
	 */
	public void removeMatchingProfiles(String calleeID,
			ServiceProfile[] realizedServices) {
		if (calleeID != null) {
			((AndroidServiceStrategy) busStrategy).removeMatchingRegParams(calleeID,
					realizedServices);
		}
	}

	/**
	 * @see AbstractBus#brokerMessage(String, BusMessage)
	 */
	public void brokerReply(String calleeID, BusMessage response) {
		if (calleeID != null) {
			super.brokerMessage(calleeID, response);
		}
	}

	/**
	 * @see AbstractBus#brokerMessage(String, BusMessage)
	 */
	public void brokerRequest(String callerID, BusMessage request) {
		if (callerID != null) {
			super.brokerMessage(callerID, request);
		}
	}

	/**
	 * @see ServiceBus#unregister(String, ServiceCallee)
	 */
	public void unregister(String calleeID, ServiceCallee callee) {
		if (calleeID != null) {
			((AndroidServiceStrategy) busStrategy).removeRegParams(calleeID);
			super.unregister(calleeID, callee);
		}
	}

	/**
	 * @see ServiceBus#unregister(String, ServiceCaller)
	 */
	public void unregister(String callerID, ServiceCaller caller) {
		if (callerID != null) {
			super.unregister(callerID, caller);
		}
	}

	@Override
	protected BusStrategy createBusStrategy(CommunicationModule commModule) {
		return new AndroidServiceStrategy(commModule/*, context*/);
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
		return AndroidServiceBusImpl.class.getPackage().getName();
	}

	public String getClassName() {
		return AndroidServiceBusImpl.class.getCanonicalName();
	}

	// Added/overridden by port

	@Override
	protected IRegistry createRegistry() {
		// Context context = ((AbstractSodaPopAndroidImpl)
		// sodapop).getContext();

		return new AndroidServiceBusRegistry(this, androidContext);
	}

	public WaitingCallRowDB getWaitingCall(String callID) {
		AndroidServiceStrategy androidServiceStrategy = (AndroidServiceStrategy) busStrategy;

		return androidServiceStrategy.getCall(callID);
	}

	private AndroidServiceStrategy getAndroidServiceStrategy() {
		return (AndroidServiceStrategy) busStrategy;
	}

	private AndroidServiceBusRegistry getAndroidRegistryServices() {
		return (AndroidServiceBusRegistry) registry;
	}

	public void unregisterByPackageName(String packageName) {
		// Extract list of member ID's that have the package name
		AndroidServiceBusRegistry androidRegistryServices = getAndroidRegistryServices();
		BusMember[] members = androidRegistryServices
				.getBusMembersByPackageName(packageName);

		// Iteratively unregister each member
		for (BusMember busMember : members) {
			if (busMember instanceof ServiceCallee) {
				AndroidServiceCalleeProxy serviceCallee = (AndroidServiceCalleeProxy) busMember;
				// Unregister from the bus
				unregister(serviceCallee.getMyID(), serviceCallee);
			} else if (busMember instanceof ServiceCaller) {
				AndroidServiceCallerProxy serviceCaller = (AndroidServiceCallerProxy) busMember;

				// Unregister from the bus
				unregister(serviceCaller.getMyID(), serviceCaller);

				// Unregister the receivers
				ServiceRequestsMngr.unreigsterToServiceRequestGroundingActions(
						serviceCaller.getServiceRequestGrounding(), androidContext);
			}
		}
	}

	public void handleResponse(String messageIDInReplyTo,
			String serviceCalleeID, String operationNameToRespondTo,
			String replyTo, Bundle extras) {
		// Cast to AndroidServiceStrategy
		AndroidServiceStrategy strategy = getAndroidServiceStrategy();

		strategy.sendResponse(messageIDInReplyTo, serviceCalleeID,
				operationNameToRespondTo, replyTo, extras);
	}

	public void sendServiceRequest(String serviceCallerID, String actionName,
			Bundle extrasSectionOfOriginalServiceCallerRequest) {

		// Extract the member ID
		String memberID = serviceCallerID;/*
				.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.length()); // TODO:
		// move		// this		// to		// some		// shared		// method*/
		//TODO: Check with/out prefix
		// Get the AndroidServiceCaller from the DB
		AndroidServiceCallerProxy androidServiceCaller = (AndroidServiceCallerProxy) registry
				.getBusMemberByID(memberID);

		// Send the service call
		WaitingCall waitingCall = androidServiceCaller.sendServiceRequest(
				actionName, extrasSectionOfOriginalServiceCallerRequest);

		// Persist the service call
		if (null != waitingCall) {
			// Check if the requestor waits for response, only if he waits -
			// save it
			if (!StringUtils.isEmpty(waitingCall
					.getReplyToActionParameterValue())
					&& !StringUtils.isEmpty(waitingCall
							.getReplyToCategoryParameterValue())) {
				AndroidServiceStrategy androidServiceStrategy = getAndroidServiceStrategy();

				// Persist the call
				androidServiceStrategy.saveCall(waitingCall.getCallID(),
						actionName,
						waitingCall.getReplyToActionParameterValue(),
						waitingCall.getReplyToCategoryParameterValue());
			}
		}
	}

	// Workaround for port because this was removed but still valid
	public void handleRemoteMessage(BusMessage msg) {
		// sender ID is null for remote messages
		busStrategy.handleMessage(msg, null);
	}
	// Workaround for port
	public PeerCard getCoordinator() {
		try {
			return new PeerCard(this.aalSpaceManager.getAALSpaceDescriptor()
					.getSpaceCard().getCoordinatorID(), PeerRole.COORDINATOR);
		} catch (NullPointerException e) {
			return null;
		}

	}

	// PATCH This comes from the infamous proxies patch. Because of that patch,
	// everytime a request is sent, a caller is created. Its membership is
	// checked but it would fail because the caller created on demand is not in
	// the android registry, because the ondemand has a new busResourceURI, while
	// the registry uses myID
	@Override
	public boolean isValidMember(String memberURI) {
		return true;
	}
}
