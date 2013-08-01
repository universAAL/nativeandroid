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
package org.universAAL.middleware.android.buses.contextbus.data;

import org.universAAL.middleware.android.buses.contextbus.contextpublisher.AndroidContextPublisherProxy;
import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.data.IProvisionsData;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 * 
 */
public class AndroidProvisionsData extends AbstractAndroidContextBusPersistable
		implements IProvisionsData {

	public AndroidProvisionsData(Context context) {
		super(context);
	}

	public void addProvision(ContextPublisher contextPublisher) {
		// Extract the provision ID - we just want to provide a functionality to
		// know if this ContextPublisher was already added
		String provisionID = extractProvisionID(contextPublisher);

		// Persist
		sqliteMngr.addProvision(provisionID);
	}

	public boolean exist(ContextPublisher contextPublisher) {
		// Extract the provision ID
		String provisionID = extractProvisionID(contextPublisher);

		// Persist
		return sqliteMngr.existProvision(provisionID);
	}

	private String extractProvisionID(ContextPublisher contextPublisher) {
		// Cast to Android ContextPublisher
		AndroidContextPublisherProxy androidContextPublisher = (AndroidContextPublisherProxy) contextPublisher;

		// Extract the unique ID - we just want to provide a functionality to
		// know if this ContextPublisher was already added
		String provisionID = androidContextPublisher.getGroundingID();

		return provisionID;
	}
}
