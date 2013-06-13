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
package org.universAAL.middleware.android.buses.servicebus;

import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;
import org.universAAL.middleware.android.buses.servicebus.service.AndroidServiceBusService;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.AndroidServiceCalleeProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.ServiceRequestsMngr;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy.WaitingCall;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.impl.ServiceBusImpl;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.model.util.IRegistry;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.util.Constants;

import android.content.Context;
import android.os.Bundle;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 18, 2012
 * 
 */
public class AndroidServiceBus extends ServiceBusImpl implements IAndroidBus {

    protected Context context;

    public AndroidServiceBus(ModuleContext mc, Context context) {
	super(mc);

	this.context = context;
    }

    public String getName() {
	return getBusName();
    }

    public String getPackageName() {
	return AndroidServiceBusService.class.getPackage().getName();
    }

    public String getClassName() {
	return AndroidServiceBusService.class.getCanonicalName();
    }

    public void unregisterByPackageName(String packageName) {
	// Extract list of member ID's that have the package name
	AndroidServiceBusRegistry androidRegistryServices = getAndroidRegistryServices();
	BusMember[] members = androidRegistryServices.getBusMembersByPackageName(packageName);

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
			serviceCaller.getServiceRequestGrounding(), context);
	    }
	}
    }

    public void handleResponse(String messageIDInReplyTo, String serviceCalleeID,
	    String operationNameToRespondTo, String replyTo, Bundle extras) {
	// Cast to AndroidServiceStrategy
	AndroidServiceStrategy strategy = getAndroidServiceStrategy();

	strategy.sendResponse(messageIDInReplyTo, serviceCalleeID, operationNameToRespondTo,
		replyTo, extras);
    }

    public void sendServiceRequest(String serviceCallerID, String actionName,
	    Bundle extrasSectionOfOriginalServiceCallerRequest) {

	// Extract the member ID
	String memberID = serviceCallerID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		.length()); // TODO:
			    // move
			    // this
			    // to
			    // some
			    // shared
			    // method

	// Get the AndroidServiceCaller from the DB
	AndroidServiceCallerProxy androidServiceCaller = (AndroidServiceCallerProxy) registry
		.getBusMemberByID(memberID);

	// Send the service call
	WaitingCall waitingCall = androidServiceCaller.sendServiceRequest(actionName,
		extrasSectionOfOriginalServiceCallerRequest);

	// Persist the service call
	if (null != waitingCall) {
	    // Check if the requestor waits for response, only if he waits -
	    // save it
	    if (!StringUtils.isEmpty(waitingCall.getReplyToActionParameterValue())
		    && !StringUtils.isEmpty(waitingCall.getReplyToCategoryParameterValue())) {
		AndroidServiceStrategy androidServiceStrategy = getAndroidServiceStrategy();

		// Persist the call
		androidServiceStrategy.saveCall(waitingCall.getCallID(), actionName,
			waitingCall.getReplyToActionParameterValue(),
			waitingCall.getReplyToCategoryParameterValue());
	    }
	}
    }

    @Override
    protected IRegistry createReigstry() {
	// TODO: consider how get context in other way (not via the sodapop)
	Context context = ((AbstractSodaPopAndroidImpl) sodapop).getContext();

	return new AndroidServiceBusRegistry(this, context);
    }

    @Override
    protected BusStrategy createBusStrategy(SodaPop sodapop) {
	return new AndroidServiceStrategy(sodapop, context);
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
}
