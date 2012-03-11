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

import java.util.Collection;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.registry.Registry;
import org.universAAL.middleware.acl.PeerDiscoveryListener;
import org.universAAL.middleware.acl.SodaPopPeer;

/** 
 * Skeleton implementation of P2P Connector
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> 
 * 
 */

class P2PConnector implements org.universAAL.middleware.acl.P2PConnector{
	
	protected Registry registry;
	protected ControlPoint controlPoint;
	protected Collection<ExportingSodaPopPeerProxy> localSodaPopPeers;
	
	
    P2PConnector(Registry registry, ControlPoint controlPoint, Collection<ExportingSodaPopPeerProxy> localSodaPopPeers){
    	this.registry = registry;
    	this.controlPoint = controlPoint;
    	this.localSodaPopPeers = localSodaPopPeers;
    }

	/* 
	 * @see org.universAAL.middleware.acl.P2PConnector#addPeerDiscoveryListener(org.universAAL.middleware.acl.PeerDiscoveryListener)
	 */
	public void addPeerDiscoveryListener(PeerDiscoveryListener arg0) {
		// TODO add implementation
		
	}

	/* 
	 * @see org.universAAL.middleware.acl.P2PConnector#getProtocol()
	 */
	public String getProtocol() {
		// TODO add implementation
		return null;
	}

	/* 
	 * @see org.universAAL.middleware.acl.P2PConnector#noticeLostBridgedPeer(java.lang.String)
	 */
	public void noticeLostBridgedPeer(String arg0) {
		// TODO add implementation
		
	}

	/* 
	 * @see org.universAAL.middleware.acl.P2PConnector#noticeNewBridgedPeer(org.universAAL.middleware.acl.SodaPopPeer)
	 */
	public void noticeNewBridgedPeer(SodaPopPeer arg0) {
		// TODO add implementation
		
	}

	/* 
	 * @see org.universAAL.middleware.acl.P2PConnector#register(org.universAAL.middleware.acl.SodaPopPeer)
	 */
	public void register(SodaPopPeer arg0) {
		// TODO add implementation
		
	}
}