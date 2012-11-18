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
package org.universAAL.middleware.android.upnp.messages.handlers;

import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.upnp.messages.ExportProcessBusMessage;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class UPnPSodaPopHandlerFactory {

    private static final String TAG = UPnPSodaPopHandlerFactory.class.getCanonicalName();

    public static IMessageHandler createHandler(String action) {
	IMessageHandler handler = null;

	if (null == handler) {
	    switch (Action.getAction(action)) {
	    case NOTICE_JOINING_PEER:
		handler = new ExportNoticeNewPeerHandler();
		break;
	    case NOTICE_LEAVING_PEER:
		handler = new ExportNoticeLeavePeerHandler();
		break;
	    case NOTICE_PEER_BUSSES:
		handler = new ExportNoticePeerBussesHandler();
		break;
	    case REPLY_PEER_BUSSES:
		handler = new ExportReplyPeerBussesHandler();
		break;
	    case NOTICE_JOINING_BUS:
		handler = new ExportJoinBusHandler();
		break;
	    case NOTICE_LEAVING_BUS:
		handler = new ExportLeaveBusHandler();
		break;
	    case PROPAGATE_MESSAGE:
		handler = new ExportProcessBusMessageHandler();
		break;
	    }
	}

	if (null == handler) {
	    String errMsg = "Unkown action for handler [" + action + "]";
	    Log.e(TAG, errMsg);
	    throw new IllegalArgumentException(errMsg);
	}

	return handler;
    }
}
