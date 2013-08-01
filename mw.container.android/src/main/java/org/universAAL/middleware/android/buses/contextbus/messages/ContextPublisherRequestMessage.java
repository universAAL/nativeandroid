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
package org.universAAL.middleware.android.buses.contextbus.messages;

//import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.android.common.messages.AbstractMessage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class ContextPublisherRequestMessage extends AbstractMessage {

	private String contextPublisherID;
	private String contextPublisherAction;
	private Bundle extrasSection;

	public ContextPublisherRequestMessage(Context context, Intent intent) {
		super(context, intent);
	}

	public String getContextPublisherID() {
		return contextPublisherID;
	}

	public String getContextPublisherAction() {
		return contextPublisherAction;
	}

	public Bundle getExtrasSection() {
		return extrasSection;
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();

		// Store the major strings explicitly
		contextPublisherID = extractContextPublisherIDFromExtras();
		contextPublisherAction = extractContextPublisherActionFromExtras();

		// Store all extras section implicitly - this will be used when building
		// the ContextEvent...
		extrasSection = intent.getExtras();
	}

	protected String extractContextPublisherIDFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_CONTEXT_PUBLISHER_ID);
	}

	protected String extractContextPublisherActionFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_ACTION_NAME);
	}

}
