/* 
        OCO Source Materials 
        � Copyright IBM Corp. 2011 

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
package org.universAAL.middleware.android.upnp.plainjava.exporter;

import org.teleal.cling.binding.annotations.*;
import org.universAAL.middleware.acl.SodaPopPeer;

/**
 * SodaPop Peer Proxy delegates all it's methods to real implementation classes.
 * It is used to avoid duplicate upnp annotations through different
 * implementations
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> <a
 *          href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */

@UpnpService(serviceId = @UpnpServiceId(value = "SodaPopPeer:1"), serviceType = @UpnpServiceType(value = "SodaPopPeer", version = 1)

)
@UpnpStateVariables({
	@UpnpStateVariable(name = "BusName", datatype = "string", sendEvents = false),
	@UpnpStateVariable(name = "Message", datatype = "string", sendEvents = false),
	@UpnpStateVariable(name = "PeerID", datatype = "string", sendEvents = false) })
public class ExportingSodaPopPeerProxy implements SodaPopPeer {

    SodaPopPeer targetPeer;

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#getID()
     */
    @UpnpAction(out = @UpnpOutputArgument(name = "ResultID", stateVariable = "PeerID"))
    public String getID() {
	return targetPeer.getID();
    }

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#joinBus(java.lang.String,
     * java.lang.String)
     */
    @UpnpAction
    public void joinBus(@UpnpInputArgument(name = "BusName") String arg0,
	    @UpnpInputArgument(name = "JoiningPeer", stateVariable = "PeerID") String arg1) {
	this.targetPeer.joinBus(arg0, arg1);
    }

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#leaveBus(java.lang.String,
     * java.lang.String)
     */
    @UpnpAction
    public void leaveBus(@UpnpInputArgument(name = "BusName") String arg0,
	    @UpnpInputArgument(name = "LeavingPeer", stateVariable = "PeerID") String arg1) {
	this.targetPeer.leaveBus(arg0, arg1);
    }

    /**
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#noticePeerBusses(java.lang.String,
     *      java.lang.String)
     */
    @UpnpAction
    public void noticePeerBusses(
	    @UpnpInputArgument(name = "BussesName", stateVariable = "BusName") String arg0,
	    @UpnpInputArgument(name = "PeerID", stateVariable = "PeerID") String arg1) {
	this.targetPeer.noticePeerBusses(arg0, arg1);
    }

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#printStatus()
     */
    @UpnpAction
    public void printStatus() {
	this.targetPeer.printStatus();
    }

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see
     * org.universAAL.middleware.acl.SodaPopPeer#processBusMessage(java.lang
     * .String, java.lang.String)
     */
    @UpnpAction
    public void processBusMessage(@UpnpInputArgument(name = "BusName") String arg0,
	    @UpnpInputArgument(name = "Message", stateVariable = "Message") String arg1) {
	this.targetPeer.processBusMessage(arg0, arg1);
    }

    /*
     * Delegates method to SodaPop Peer implementation set through constructor
     * 
     * @see
     * org.universAAL.middleware.acl.SodaPopPeer#replyPeerBusses(java.lang.String
     * , java.lang.String)
     */
    @UpnpAction
    public void replyPeerBusses(
	    @UpnpInputArgument(name = "PeerID", stateVariable = "PeerID") String arg0,
	    @UpnpInputArgument(name = "BussesName", stateVariable = "BusName") String arg1) {
	this.targetPeer.replyPeerBusses(arg1, arg0);
    }

    /*
     * Setter for the targeted real SodaPop Peer implementation
     */
    public void setRealPeer(SodaPopPeer realPeer) {
	targetPeer = realPeer;
    }
}