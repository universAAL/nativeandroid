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
package org.universAAL.middleware.android.localsodapop.messages.handlers;

import org.universAAL.middleware.android.common.Action;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class LocalSodaPopPeerHandlerFactory {

    private static final String TAG = LocalSodaPopPeerHandlerFactory.class.getCanonicalName();

    public static IMessageHandler createHandler(String action) {
	IMessageHandler handler = null;

	switch (Action.getAction(action)) {
	case INITIALIZE:
	    handler = new InitializeLocalSodaPopHandler();
	    break;
	case NOTICE_JOINING_BUS:
	    handler = new JoinBusHandler();
	    break;
	case NOTICE_LEAVING_BUS:
	    handler = new LeaveBusHandler();
	    break;
	case NOTICE_JOINING_PEER:
	    handler = new NoticeJoiningPeerHandler();
	    break;
	case NOTICE_LEAVING_PEER:
	    handler = new NoticeLeavingPeerHandler();
	    break;
	case NOTICE_PEER_BUSSES:
	    handler = new NoticePeerBussesHandler();
	    break;
	case REPLY_PEER_BUSSES:
	    handler = new ReplyPeerBussesHandler();
	    break;
	case PROCESS_BUS_MESSAGE:
	    handler = new ProcessBusMessageHandler();
	    break;
	case PRINT_STATUS:
	    handler = new PrintStatusHanlder();
	    break;
	}

	if (null == handler) {
	    String errMsg = "Unkown action for handler [" + action + "]";
	    Log.e(TAG, errMsg);
	    throw new IllegalArgumentException(errMsg);
	}

	return handler;
    }
}
