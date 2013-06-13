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
package org.universAAL.middleware.android.buses.servicebus.data;

import org.universAAL.middleware.android.buses.servicebus.persistence.AbstractAndroidServiceBusPersistable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalWaitingCallerRowDB;
import org.universAAL.middleware.service.data.ILocalWaitingCallersData;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 22, 2012
 * 
 */
public class AndroidLocalWaitingCallersData extends AbstractAndroidServiceBusPersistable implements
	ILocalWaitingCallersData {

    public AndroidLocalWaitingCallersData(Context context) {
	super(context);
    }

    public void addLocalWaitier(String msgID, String callerID) {
	sqliteMngr.open();
	try {
	    sqliteMngr.addLocalWaitingCaller(msgID, callerID);
	} finally {
	    sqliteMngr.close();
	}
    }

    public String getAndRemoveLocalWaiterCallerID(String msgID) {
	String callerID = null;
	sqliteMngr.open();
	try {
	    LocalWaitingCallerRowDB row = sqliteMngr.removeLocalWaitingCaller(msgID);
	    if (null != row) {
		callerID = row.getCallerID();
	    }
	} finally {
	    sqliteMngr.close();
	}

	return callerID;
    }
}
