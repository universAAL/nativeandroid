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
package org.universAAL.middleware.android.buses.servicebus.data;

import org.universAAL.middleware.android.buses.servicebus.persistence.AbstractAndroidServiceBusPersistable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 21, 2012
 * 
 */
public class AndroidWaitingCallsData extends
		AbstractAndroidServiceBusPersistable implements IWaitingCallsData {

	public AndroidWaitingCallsData(Context context) {
		super(context);
	}

	public void addWaitingCall(String callID, String actionName,
			String replyToAction, String replyToCategory) {
		// sqliteMngr.open();
		// try {
		sqliteMngr.addWaitingCall(callID, actionName, replyToAction,
				replyToCategory);
		// } finally {
		// sqliteMngr.close();
		// }
	}

	public WaitingCallRowDB getAndRemoveWaitingCall(String callID) {
		// sqliteMngr.open();
		// try {
		return sqliteMngr.removeWaitingCallByCallID(callID);
		// } finally {
		// sqliteMngr.close();
		// }
	}

}
