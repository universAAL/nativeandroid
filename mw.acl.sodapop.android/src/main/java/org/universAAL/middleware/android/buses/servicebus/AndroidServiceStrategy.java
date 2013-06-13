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

import org.universAAL.middleware.android.buses.servicebus.data.IWaitingCallsData;
import org.universAAL.middleware.android.buses.servicebus.data.factory.AndroidServiceStrategyDataFactory;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.AndroidServiceCalleeProxy;
import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.service.data.factory.IServiceStrategyDataFactory;
import org.universAAL.middleware.service.impl.ServiceStrategy;
import org.universAAL.middleware.util.Constants;

import android.content.Context;
import android.os.Bundle;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidServiceStrategy extends ServiceStrategy {

    private static final int INTERVAL_BETWEEN_COORDINATOR_CHECKS_MILI_SECS = 2 * 1000;
    private static final long AMOUNT_OF_COORDINATOR_CHECKS_TRIALS = 60;

    public AndroidServiceStrategy(CommunicationModule cm, Context context) {
	super(cm);

	// Check if the coordinator is known and fill it accordingly
	theCoordinator = queryForCoordinator();
    }

    @Override
    protected IServiceStrategyDataFactory createServiceStrategyDataFactory() {
	// TODO: consider how get context in other way (not via the sodapop)
	Context context = ((AbstractSodaPopAndroidImpl) sodapop).getContext();
	return new AndroidServiceStrategyDataFactory(context);
    }

    @Override
    protected void waitForCoordinatorToBeKnown() throws InterruptedException {
	// Sleep and check if the coordinator is known - do that iteratively -
	// break after some trials

	// Start checking
	for (int i = 0; i < AMOUNT_OF_COORDINATOR_CHECKS_TRIALS; i++) {
	    // Check for coordinator
	    String coordinator = queryForCoordinator();
	    if (null != coordinator) {
		theCoordinator = coordinator;
		break;
	    }

	    // Sleep
	    sleep(INTERVAL_BETWEEN_COORDINATOR_CHECKS_MILI_SECS);
	}
    }

    @Override
    protected void notifyOnFoundCoordinator() {
	// A coordinator has been found, update the SodaPop database

	// Just make sure that a coordinator was indeed found
	if (!StringUtils.isEmpty(theCoordinator)) {
	    // Cast to Android SodaPop
	    AbstractSodaPopAndroidImpl androidSodapop = (AbstractSodaPopAndroidImpl) sodapop;

	    // Update with the known coordinator
	    androidSodapop.updateCoordinator(theCoordinator);
	}
    }

    protected String queryForCoordinator() {
	// Cast to Android SodaPop
	AbstractSodaPopAndroidImpl androidSodapop = (AbstractSodaPopAndroidImpl) sodapop;

	return androidSodapop.getCoordinator();
    }

    public void sendResponse(String messageIDInReplyTo, String serviceCalleeID,
	    String operationNameToRespondTo, String replyTo, Bundle extras) {
	// Get the AndroidServiceCallee
	String memberID = serviceCalleeID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		.length());
	AndroidServiceCalleeProxy androidServiceCallee = (AndroidServiceCalleeProxy) getBusMember(memberID);

	// Handle the response
	androidServiceCallee.handleResponse(messageIDInReplyTo, operationNameToRespondTo, replyTo,
		extras);
    }

    public void saveCall(String callID, String actionName, String replyToAction,
	    String replyToCategory) {
	// Get the interface
	IWaitingCallsData waitingCallsData = getWaitingCallsData();

	// Add the waiting call
	waitingCallsData.addWaitingCall(callID, actionName, replyToAction, replyToCategory);
    }

    public WaitingCallRowDB getCall(String callID) {
	// Get the interface
	IWaitingCallsData waitingCallsData = getWaitingCallsData();

	// Get the waiting call
	return waitingCallsData.getAndRemoveWaitingCall(callID);
    }

    private IWaitingCallsData getWaitingCallsData() {
	return ((AndroidServiceStrategyDataFactory) createServiceStrategyDataFactory())
		.createWaitingCallsData();
    }
}
