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

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.upnp.service.IRemotePeerIdentifiedListener;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 10, 2012
 * 
 */
public class GetIDActionInvocation extends AbstractActionInvocation {

    private static final String TAG = GetIDActionInvocation.class.getCanonicalName();

    private IRemotePeerIdentifiedListener listener;

    public GetIDActionInvocation(Service<RemoteDevice, RemoteService> service,
	    IRemotePeerIdentifiedListener listener) {
	super(service, Action.GET_ID.getName());

	this.listener = listener;
    }

    @Override
    protected ActionCallback populateActionCallback() {
	ActionCallback callback = new ActionCallback(this) {
	    @Override
	    public void success(ActionInvocation invocation) {
		ActionArgumentValue actionArgValue = invocation.getOutput("ResultID");
		String remotePeerID = (String) actionArgValue.getValue();
		Log.d(TAG, "Successfully called action [" + invocation.getAction().getName()
			+ "] peerID [" + remotePeerID + "]");

		// Notify the listener
		if (null != listener) {
		    listener.remoteServiceIdentified(remotePeerID, remoteService);
		}
	    }

	    @Override
	    public void failure(ActionInvocation invocation, UpnpResponse operation,
		    String defaultMsg) {
		Log.e(TAG, "Error when called action [" + invocation.getAction().getName()
			+ "] due to [" + defaultMsg + "]");
		listener.remoteServiceNotIdentified(remoteService);
	    }
	};

	return callback;
    }
}
