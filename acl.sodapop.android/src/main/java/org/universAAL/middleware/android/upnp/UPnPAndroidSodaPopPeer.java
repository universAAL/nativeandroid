/* 
        OCO Source Materials 
        © Copyright IBM Corp. 2011 

        See the NOTICE file distributed with this work for additional 
        information regarding copyright ownership 
        
        Licensed under the Apache License, Version 2.0 (the "License"); 
        you may not use this file except in compliance with the License. 
        You may obtain a copy of the License at 
        
          http://www.apache.org/licenses/LICENSE-2.0 
        
        Unless required by applicable law or agreed to in writing, software 
        distributed under the License is distributed on an "AS IS" BASIS, 
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
        See the License for the specific language governing permissions and 
        limitations under the License. 
 */
package org.universAAL.middleware.android.upnp;

import org.universAAL.middleware.acl.SodaPopPeer;
import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.upnp.intents.UPnPIntentFactory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Empty implementation of SodaPopPeer for testing purposes.
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> <a
 *          href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */

public class UPnPAndroidSodaPopPeer implements SodaPopPeer {

    private static final String TAG = UPnPAndroidSodaPopPeer.class.getCanonicalName();

    private String peerID;
    private Context context;

    public UPnPAndroidSodaPopPeer(String peerID, Context context) {
	super();
	this.peerID = peerID;
	this.context = context;
    }

    public void joinBus(String busName, String joiningPeer) {
	Log.d(TAG, "#joinBus invoked: busName[" + busName + "]; joiningPeer [" + joiningPeer + "]");

	// Create the JoinBus intent
	Intent joinBus = UPnPIntentFactory.createNoticeRemoteSodaPopPeerJoiningBus(joiningPeer,
		IAndroidSodaPop.PROTOCOL_UPNP, busName);

	// Send broadcast message
	context.sendBroadcast(joinBus);
    }

    public void leaveBus(String busName, String leavingPeer) {
	Log.d(TAG, "#leaveBus invoked: busName[" + busName + "]; leavingPeer [" + leavingPeer + "]");

	// Create the LeaveBus intent
	Intent leaveBus = UPnPIntentFactory.createNoticeRemoteSodaPopPeerLeavingBus(leavingPeer,
		IAndroidSodaPop.PROTOCOL_UPNP, busName);

	// Send broadcast message
	context.sendBroadcast(leaveBus);
    }

    public void noticePeerBusses(String busNames, String peerID) {
	Log.d(TAG, "#noticePeerBusses invoked: peerID[" + peerID + "]; busNames [" + busNames + "]");

	// Create the NoticePeerBusses intent
	Intent noticePeerBusses = UPnPIntentFactory.createNoticeRemoteSodaPopPeerBuses(peerID,
		IAndroidSodaPop.PROTOCOL_UPNP, busNames);

	// Send broadcast message
	context.sendBroadcast(noticePeerBusses);
    }

    public void replyPeerBusses(String busNames, String peerID) {
	Log.d(TAG, "#replyPeerBusses invoked: peerID [" + peerID + "]; busNames [" + busNames + "]");

	// Create the ReplyPeerBusses intent
	Intent replyPeerBusses = UPnPIntentFactory.createRemoteReplyPeerBusses(peerID,
		IAndroidSodaPop.PROTOCOL_UPNP, busNames);

	// Send broadcast message
	context.sendBroadcast(replyPeerBusses);
    }

    public void processBusMessage(String busName, String msg) {
	Log.d(TAG, "#processBusMessage invoked: busName [" + busName + "]; msg [" + msg + "]");

	Intent processBusMessage = UPnPIntentFactory.createProcessBusMessage(busName, msg,
		IAndroidSodaPop.PROTOCOL_UPNP);

	// Send broadcast message
	context.sendBroadcast(processBusMessage);
    }

    public void printStatus() {
	Log.d(TAG, "#printStatus invoked!");

	// Create the PrintStatus intent
	Intent printStatusIntent = UPnPIntentFactory.createPrintStatus(peerID,
		IAndroidSodaPop.PROTOCOL_UPNP);

	// Send broadcast message
	context.sendBroadcast(printStatusIntent);
    }

    public String getID() {
	Log.d(TAG, "#getID invoked!");
	return peerID;
    }
}