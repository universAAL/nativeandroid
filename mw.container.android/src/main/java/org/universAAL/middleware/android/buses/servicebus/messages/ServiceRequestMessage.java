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
package org.universAAL.middleware.android.buses.servicebus.messages;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.android.common.messages.AbstractMessage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 21, 2012
 * 
 */
public class ServiceRequestMessage extends AbstractMessage {

	private String serviceCallerID;
	private String serviceCallerAction;
	private Bundle extrasSection;

	public ServiceRequestMessage(Context context, Intent intent) {
		super(context, intent);
	}

	public String getServiceCallerID() {
		return serviceCallerID;
	}

	public String getServiceCallerAction() {
		return serviceCallerAction;
	}

	public Bundle getExtrasSection() {
		return extrasSection;
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();

		// Store the major strings explicitly
		serviceCallerID = extractServiceCallerIDFromExtras();
		serviceCallerAction = extractServiceCallerActionFromExtras();

		// Store all extras section implicitly - this will be used when building
		// the ServiceCall
		extrasSection = intent.getExtras();
	}

	protected String extractServiceCallerIDFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_SERVICE_CALLER_ID);
	}

	protected String extractServiceCallerActionFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_ACTION_NAME);
	}

}
