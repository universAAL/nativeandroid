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
package org.universAAL.middleware.android.upnp.actions;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.InvalidValueException;
import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.IAndroidSodaPop;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 2, 2012
 * 
 */
public class ProcessBusMessageAction extends AbstractActionInvocation {

    private static final String TAG = ProcessBusMessageAction.class.getCanonicalName();

    public ProcessBusMessageAction(Service<RemoteDevice, RemoteService> service, String busName,
	    String message) {

	super(service, Action.PROPAGATE_MESSAGE.getName());

	try {
	    // Throws InvalidValueException if the value is of wrong type
	    setInput(IAndroidSodaPop.UPNP_ACTION_INPUT_BUS_NAME, busName);
	    setInput(IAndroidSodaPop.UPNP_ACTION_INPUT_MESSAGE, message);

	    Log.d(TAG,
		    "ProcessBusMessageAction was initialized with Message that is addressed to bus ["
			    + busName + "]");
	} catch (InvalidValueException ex) {
	    Log.e(TAG, "Error when setting inputs [" + ex.getMessage() + "]");
	}
    }

}
