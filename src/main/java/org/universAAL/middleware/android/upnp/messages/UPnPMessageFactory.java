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
package org.universAAL.middleware.android.upnp.messages;

import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.IMessage;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 18, 2012
 * 
 */
public class UPnPMessageFactory {

    private static final String TAG = UPnPMessageFactory.class.getCanonicalName();

    public static IMessage createMessage(Context context, Intent intent) {
	IMessage message = null;

	String actionAsString = intent.getAction();
	if (null == actionAsString)
	    return null;

	switch (Action.getAction(actionAsString)) {
	case NOTICE_JOINING_PEER:
	    message = new ExportNoticeNewPeer(context, intent);
	    break;
	case NOTICE_LEAVING_PEER:
	    message = new ExportNoticeLeavePeer(context, intent);
	    break;
	case NOTICE_PEER_BUSSES:
	    message = new ExportNoticePeerBussesMessage(context, intent);
	    break;
	case REPLY_PEER_BUSSES:
	    message = new ExportReplyPeerBussesMessage(context, intent);
	    break;
	case NOTICE_JOINING_BUS:
	    message = new ExportJoinBusMessage(context, intent);
	    break;
	case NOTICE_LEAVING_BUS:
	    message = new ExportLeaveBusMessage(context, intent);
	    break;
	case PROPAGATE_MESSAGE:
	    message = new ExportProcessBusMessage(context, intent);
	    break;
	}

	if (null == message) {
	    String errMsg = "Unkown action for message [" + message + "]";
	    Log.e(TAG, errMsg);
	    throw new IllegalArgumentException(errMsg);
	}

	return message;
    }
}
