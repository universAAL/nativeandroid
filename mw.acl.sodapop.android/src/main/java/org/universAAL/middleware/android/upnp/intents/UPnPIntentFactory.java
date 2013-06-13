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
package org.universAAL.middleware.android.upnp.intents;

import org.universAAL.middleware.android.common.IAndroidSodaPop;

import android.content.Intent;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 18, 2012
 * 
 */
public class UPnPIntentFactory {

    public static Intent createInitialize(String protocol) {
	Intent initializeIntent = new Intent(IAndroidSodaPop.ACTION_INITIALIZE);
	initializeIntent.addCategory(IAndroidSodaPop.CATEGORY_LOCAL_SODAPOP);
	initializeIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, protocol);

	return initializeIntent;
    }

    public static Intent createNoticeJoiningRemoteSodaPopPeer(String remotePeerID,
	    String remoteProtocol) {
	Intent noticeJoiningNewPeerIntent = new Intent();
	noticeJoiningNewPeerIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_JOINING_PEER);
	noticeJoiningNewPeerIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	noticeJoiningNewPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	noticeJoiningNewPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);

	return noticeJoiningNewPeerIntent;
    }

    public static Intent createNoticeLeavingRemoteSodaPopPeer(String remotePeerID,
	    String remoteProtocol) {
	Intent noticeLeavingPeerIntent = new Intent();
	noticeLeavingPeerIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_LEAVING_PEER);
	noticeLeavingPeerIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	noticeLeavingPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	noticeLeavingPeerIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);

	return noticeLeavingPeerIntent;
    }

    public static Intent createNoticeRemoteSodaPopPeerBuses(String remotePeerID,
	    String remoteProtocol, String remotePeerBusses) {
	Intent noticePeerBussesIntent = new Intent();
	noticePeerBussesIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_PEER_BUSSES);
	noticePeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUSSES, remotePeerBusses);

	return noticePeerBussesIntent;
    }

    public static Intent createRemoteReplyPeerBusses(String remotePeerID, String remoteProtocol,
	    String remotePeerBusses) {
	Intent replyPeerBussesIntent = new Intent();
	replyPeerBussesIntent.setAction(IAndroidSodaPop.ACTION_REPLY_PEER_BUSSES);
	replyPeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);
	replyPeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUSSES, remotePeerBusses);

	return replyPeerBussesIntent;
    }

    public static Intent createNoticeRemoteSodaPopPeerJoiningBus(String remotePeerID,
	    String remoteProtocol, String remotePeerBus) {
	Intent noticePeerBussesIntent = new Intent();
	noticePeerBussesIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_JOINING_BUS);
	noticePeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, remotePeerBus);

	return noticePeerBussesIntent;
    }

    public static Intent createNoticeRemoteSodaPopPeerLeavingBus(String remotePeerID,
	    String remoteProtocol, String remotePeerBus) {
	Intent noticePeerBussesIntent = new Intent();
	noticePeerBussesIntent.setAction(IAndroidSodaPop.ACTION_NOTICE_LEAVING_BUS);
	noticePeerBussesIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, remoteProtocol);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, remotePeerID);
	noticePeerBussesIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, remotePeerBus);

	return noticePeerBussesIntent;
    }

    public static Intent createProcessBusMessage(String busName, String msg, String protocol) {
	Intent processBusMessageIntent = new Intent();
	processBusMessageIntent.setAction(IAndroidSodaPop.ACTION_PROCESS_BUS_MESSAGE);
	processBusMessageIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	processBusMessageIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_BUS_NAME, busName);
	processBusMessageIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_BUS_MESSAGE, msg);
	processBusMessageIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, protocol);

	return processBusMessageIntent;
    }

    public static Intent createPrintStatus(String peerID, String protocol) {
	Intent printStatusIntent = new Intent();
	printStatusIntent.setAction(IAndroidSodaPop.ACTION_PRINT_STATUS);
	printStatusIntent.addCategory(IAndroidSodaPop.CATEGORY_UPNP_SODAPOP);
	printStatusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PROTOCOL, protocol);
	printStatusIntent.putExtra(IAndroidSodaPop.EXTRAS_KEY_PEER_ID, peerID);

	return printStatusIntent;
    }
}
