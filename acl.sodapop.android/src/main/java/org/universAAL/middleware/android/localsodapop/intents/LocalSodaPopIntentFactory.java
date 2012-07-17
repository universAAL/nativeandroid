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
package org.universAAL.middleware.android.localsodapop.intents;

import org.universAAL.middleware.android.common.IAndroidSodaPop;

import android.content.ComponentName;
import android.content.Intent;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class LocalSodaPopIntentFactory {

    public static Intent createNoticeJoiningLocalSodaPopPeer(String localPeerID,
	    String localProtocol) {
	Intent noticeNewLocalSodaPopIntent = new Intent();
	noticeNewLocalSodaPopIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_JOINING_PEER);
	noticeNewLocalSodaPopIntent.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	noticeNewLocalSodaPopIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	noticeNewLocalSodaPopIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);

	return noticeNewLocalSodaPopIntent;
    }

    public static Intent createNoticeLeavingLocalSodaPopPeer(String localPeerID,
	    String localProtocol) {
	Intent noticeLeavingPeerIntent = new Intent();
	noticeLeavingPeerIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_LEAVING_PEER);
	noticeLeavingPeerIntent.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	noticeLeavingPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	noticeLeavingPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);

	return noticeLeavingPeerIntent;
    }

    public static Intent createNoticeLocalSodaPopPeerBuses(String remotePeerID, String localPeerID,
	    String localProtocol, String localPeerBusses) {
	Intent noticePeerBussesIntent = new Intent();
	noticePeerBussesIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_PEER_BUSSES);
	noticePeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	noticePeerBussesIntent
		.putExtra(IAndroidSodaPop.EXTRAS_KEY_REMOTE_PEER_ID_RSP, remotePeerID);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUSSES, localPeerBusses);

	return noticePeerBussesIntent;
    }

    public static Intent createLocalReplyPeerBusses(String remotePeerID, String localPeerID,
	    String localProtocol, String localPeerBusses) {
	Intent replyPeerBussesIntent = new Intent();
	replyPeerBussesIntent.setAction(IAndroidSodaPop.ACTION_REPLY_PEER_BUSSES);
	replyPeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_REMOTE_PEER_ID_RSP, remotePeerID);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUSSES, localPeerBusses);

	return replyPeerBussesIntent;
    }

    public static Intent createJoinLocalBus(String remotePeerIDs, String localPeerID,
	    String localProtocol, String localPeerBus) {
	Intent noticeJoinBus = new Intent();
	noticeJoinBus.setAction(IAndroidSodaPop.ACTION_NOTICE_JOINING_BUS);
	noticeJoinBus.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	noticeJoinBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_REMOTE_PEER_IDS_RSP, remotePeerIDs);
	noticeJoinBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	noticeJoinBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);
	noticeJoinBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, localPeerBus);

	return noticeJoinBus;
    }

    public static Intent createLeaveLocalBus(String remotePeerIDs, String localPeerID,
	    String localProtocol, String localPeerBus) {
	Intent noticeLeaveBus = new Intent();
	noticeLeaveBus.setAction(IAndroidSodaPop.ACTION_NOTICE_LEAVING_BUS);
	noticeLeaveBus.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	noticeLeaveBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_REMOTE_PEER_IDS_RSP, remotePeerIDs);
	noticeLeaveBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);
	noticeLeaveBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, localPeerID);
	noticeLeaveBus.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, localPeerBus);

	return noticeLeaveBus;
    }

    public static Intent createProcessBusMessage(String message, String contextPackageName,
	    String busClassName, String localProtocol) {
	Intent processBusMessage = new Intent();
	processBusMessage.setComponent(new ComponentName(contextPackageName, busClassName));
	processBusMessage.setAction(IAndroidSodaPop.ACTION_PROCESS_BUS_MESSAGE);
	processBusMessage.putExtra(IAndroidSodaPop.EXTRAS_KEY_BUS_MESSAGE, message);
	processBusMessage.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, localProtocol);

	return processBusMessage;
    }

    public static Intent createPropagateBusMessage(String remotePeerIDs, String busName,
	    String message) {
	Intent propagateMessage = new Intent();
	propagateMessage.setAction(IAndroidSodaPop.ACTION_PROPAGAGE_MESSAGE); // TODO:
									      // fix
									      // the
									      // action
									      // misspell
	propagateMessage.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	propagateMessage.putExtra(IAndroidSodaPop.EXTRAS_KEY_REMOTE_PEER_IDS_RSP, remotePeerIDs);
	propagateMessage.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, busName);
	propagateMessage.putExtra(IAndroidSodaPop.EXTRAS_KEY_BUS_MESSAGE, message);

	return propagateMessage;
    }
}
