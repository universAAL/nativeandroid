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
 *         Apr 23, 2012
 * 
 */
public class ProcessBusMessageResponse extends AbstractMessage {

	private String messageIDInReplyTo;
	private String serviceCalleeID;
	private String operationNameToRespondTo;
	private String replyTo;

	public ProcessBusMessageResponse(Context context, Intent intent) {
		super(context, intent);
	}

	public String getMessageIDInReplyTo() {
		return messageIDInReplyTo;
	}

	public String getServiceCalleeID() {
		return serviceCalleeID;
	}

	public String getOperationNameToRespondTo() {
		return operationNameToRespondTo;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public Bundle getExtras() {
		return intent.getExtras();
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();

		messageIDInReplyTo = extractMessageIDInReplyToFromExtras();
		serviceCalleeID = extractServiceCalleeIDFromExtras();
		operationNameToRespondTo = extractOperationNameFromExtras();
		replyTo = extractReplyToFromExtras();
	}

	protected String extractMessageIDInReplyToFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_MESSAGE_ID_IN_REPLY);
	}

	protected String extractServiceCalleeIDFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_SERVICE_CALLEE_ID);
	}

	protected String extractOperationNameFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_OPERATION_NAME);
	}

	protected String extractReplyToFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_REPLY_TO);
	}
}
