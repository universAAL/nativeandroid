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
package org.universAAL.middleware.acl.upnp.plainjava;

import org.universAAL.middleware.acl.SodaPopPeer;

/**
 * Empty implementation of SodaPopPeer for testing purposes.
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a>
 * 			<a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */

public class MockSodaPopPeer implements SodaPopPeer {

	public void joinBus(String busName, String joiningPeer) {
		System.out.println("#joinBus invoked!");
	}

	public void leaveBus(String busName, String leavingPeer) {
		System.out.println("#leaveBus invoked!");
	}

	public void noticePeerBusses(String peerID, String busNames) {
		System.out.println("#noticePeerBusses invoked!");
	}

	public void replyPeerBusses(String peerID, String busNames) {
		System.out.println("#replyPeerBusses invoked!");
	}

	public void processBusMessage(String busName, String msg) {
		System.out.println("#processBusMessage invoked!");
	}

	public void printStatus() {
		System.out.println("#printStatus invoked!");
	}

	public String getID() {
		System.out.println("#getID invoked!");
		return "MockSodaPop_Peer_ID";
	}
}