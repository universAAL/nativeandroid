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

import org.teleal.cling.binding.annotations.*;
import org.universAAL.middleware.acl.SodaPopPeer;

/** 
 * SodaPop Peer Proxy delegates all it's methods to real implementation classes. It is
 * used to avoid duplicate upnp annotations through different implementations 
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> 
 * 
 */

@UpnpService(
        serviceId = @UpnpServiceId("SodaPopPeer"),
        serviceType = @UpnpServiceType(value = "SodaPopPeer", version = 1)

)
@UpnpStateVariables(
        {
                @UpnpStateVariable(
                        name = "BusName",
                        datatype = "string",
                        sendEvents = false
                ),
                @UpnpStateVariable(
                        name = "Message",
                        datatype = "string",
                        sendEvents = false
                ),
                @UpnpStateVariable(
                        name = "PeerID",
                        datatype = "string",
                        sendEvents = false
                )                
        }
)
public class ExportingSodaPopPeerProxy implements SodaPopPeer{
	
	SodaPopPeer targetPeer;

	/* Constructor just assigns real SodaPop Peer implementation as a target to delegate methods
	 * @see org.universAAL.middleware.acl.SodaPopPeer#getID()
	 */
	public ExportingSodaPopPeerProxy(SodaPopPeer realPeer) {
		this.targetPeer = realPeer;
	}	

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#getID()
	 */
	@UpnpAction(out = @UpnpOutputArgument(name = "PeerID"))
	public String getID() {
		return targetPeer.getID();
	}

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#joinBus(java.lang.String, java.lang.String)
	 */
	@UpnpAction
	public void joinBus(@UpnpInputArgument(name = "BusName") String arg0,
						@UpnpInputArgument(name = "JoiningPeer", stateVariable="PeerID") String arg1) {
		this.targetPeer.joinBus(arg0, arg1);	
	}

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#leaveBus(java.lang.String, java.lang.String)
	 */
//	@UpnpAction //TODO add input state variables accordingly
	public void leaveBus(String arg0, String arg1) {
		this.targetPeer.leaveBus(arg0, arg1);
	}

	/** Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#noticePeerBusses(java.lang.String, java.lang.String)
	 */
//	@UpnpAction //TODO add input state variables accordingly
	public void noticePeerBusses(String arg0, String arg1) {
		this.targetPeer.noticePeerBusses(arg0, arg1);
	}

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#printStatus()
	 */
	@UpnpAction
	public void printStatus() {
		this.targetPeer.printStatus();
	}

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#processBusMessage(java.lang.String, java.lang.String)
	 */
//	@UpnpAction //TODO add input state variables accordingly
	public void processBusMessage(String arg0, String arg1) {
		this.targetPeer.processBusMessage(arg0, arg1);
	}

	/* Delegates method to SodaPop Peer implementation set through constructor
	 * @see org.universAAL.middleware.acl.SodaPopPeer#replyPeerBusses(java.lang.String, java.lang.String)
	 */
//	@UpnpAction //TODO add input state variables accordingly
	public void replyPeerBusses(String arg0, String arg1) {
		this.targetPeer.replyPeerBusses(arg0, arg1);
	}

	/*
	 * Setter for the targeted real SodaPop Peer implementation
	 */
	public void setRealPeer(SodaPopPeer realPeer) {
		targetPeer = realPeer;
	}
}