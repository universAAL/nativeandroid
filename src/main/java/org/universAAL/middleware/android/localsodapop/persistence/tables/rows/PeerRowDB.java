/**
 * 
S *  OCO Source Materials 
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
package org.universAAL.middleware.android.localsodapop.persistence.tables.rows;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class PeerRowDB {

    private long id;
    private String peerID;
    private String protocol;
    private boolean discovered;
    private boolean receivedBusses;
    private List<BusRowDB> busesNames = new ArrayList<BusRowDB>();

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public String getPeerID() {
	return peerID;
    }

    public void setPeerID(String peerID) {
	this.peerID = peerID;
    }

    public String getProtocol() {
	return protocol;
    }

    public void setProtocol(String protocol) {
	this.protocol = protocol;
    }

    public boolean isDiscovered() {
	return discovered;
    }

    public void setDiscovered(boolean discovered) {
	this.discovered = discovered;
    }

    public void setDiscovered(String discovered) {
	this.discovered = discovered.equals("0") ? false : true;
    }

    public boolean isReceivedBusses() {
	return receivedBusses;
    }

    public void setReceivedBusses(boolean receivedBusses) {
	this.receivedBusses = receivedBusses;
    }

    public void setReceivedBusses(String receivedBusses) {
	this.receivedBusses = receivedBusses.equals("0") ? false : true;
    }

    public List<String> getBusesNames() {
	List<String> busesNamesStrings = new ArrayList<String>();
	for (BusRowDB busName : busesNames) {
	    busesNamesStrings.add(busName.getBusName());
	}

	return busesNamesStrings;
    }

    public void addBus(BusRowDB bus) {
	busesNames.add(bus);
    }

    public String getFormattedBussesNames() {
	// Extract the bus names
	StringBuffer sb = new StringBuffer();
	for (String busName : getBusesNames()) {
	    sb.append(busName);
	    sb.append(",");
	}
	String busNames = "";
	if (sb.length() > 0) {
	    busNames = sb.substring(0, sb.length() - ",".length()); // Cut the
								    // end (",")
	}

	return busNames;
    }

    @Override
    public String toString() {
	return "PeerRowDB [id=" + id + ", peerID=" + peerID + ", protocol=" + protocol
		+ ", discovered=" + discovered + ", busesNames=" + busesNames + "]";
    }
}
