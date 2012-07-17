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
package org.universAAL.middleware.android.upnp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.universAAL.middleware.android.upnp.exceptions.UnknownRemoteDeviceExeption;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 10, 2012
 * 
 */
public class RemoteServicesContainer implements IRemotePeerIdentifiedListener {

    private static RemoteServicesContainer sInstance;
    private static Object sSynch = new Object();

    private List<IRemotePeerStateUpdateListener> listeners = new ArrayList<IRemotePeerStateUpdateListener>();

    private Map<String, Service<RemoteDevice, RemoteService>> remoteServices = new HashMap<String, Service<RemoteDevice, RemoteService>>();

    private Map<String, String> peerIDToDeviceUDN = new HashMap<String, String>();

    public static RemoteServicesContainer getInstance() {
	if (null == sInstance) {
	    synchronized (sSynch) {
		if (null == sInstance) {
		    sInstance = new RemoteServicesContainer();
		}
	    }
	}

	return sInstance;
    }

    public void addListener(IRemotePeerStateUpdateListener listener) {
	listeners.add(listener);
    }

    public void removeListener(IRemotePeerStateUpdateListener listener) {
	listeners.remove(listener);
    }

    public void addRemoteService(Service<RemoteDevice, RemoteService> remoteService) {
	String deviceIdentifier = extractKeyFromDevice(remoteService.getDevice());
	remoteServices.put(deviceIdentifier, remoteService);
    }

    public void remoteServiceIdentified(String peerID,
	    Service<RemoteDevice, RemoteService> remoteService) {
	// Extract the device identifier
	String deviceIdentifier = extractKeyFromDevice(remoteService.getDevice());

	// Update the peer id to device identifier map
	peerIDToDeviceUDN.put(peerID, deviceIdentifier);

	// Notify listeners
	for (IRemotePeerStateUpdateListener curListener : listeners) {
	    curListener.remotePeerAdded(peerID);
	}
    }

    public void remoteServiceNotIdentified(Service<RemoteDevice, RemoteService> remoteService) {
	// Extract the device identifier
	String deviceIdentifier = extractKeyFromDevice(remoteService.getDevice());

	// Remove from the map
	remoteServices.remove(deviceIdentifier);
    }

    public void removeRemoteService(Service<RemoteDevice, RemoteService> sodaPop) {
	String foundPeerID = null;

	// Extract the device identifier
	String deviceIdentifier = extractKeyFromDevice(sodaPop.getDevice());

	for (String curPeerID : peerIDToDeviceUDN.keySet()) {
	    if (peerIDToDeviceUDN.get(curPeerID).equals(deviceIdentifier)) {
		foundPeerID = curPeerID;
		break;
	    }
	}

	// Remove from both maps
	if (null != foundPeerID) {
	    peerIDToDeviceUDN.remove(foundPeerID);
	    // Notify listeners
	    for (IRemotePeerStateUpdateListener curListener : listeners) {
		curListener.remotePeerRemoved(foundPeerID);
	    }
	}
	remoteServices.remove(sodaPop);
    }

    public Service<RemoteDevice, RemoteService> getRemoteService(String peerID)
	    throws UnknownRemoteDeviceExeption {
	// Check if the remote service exists
	if (!peerIDToDeviceUDN.containsKey(peerID)) {
	    throw new UnknownRemoteDeviceExeption("Remote device with peerID [" + peerID
		    + "] is unknown!");
	}

	// Extract the device identifier
	String deviceIdentifier = peerIDToDeviceUDN.get(peerID);

	return remoteServices.get(deviceIdentifier);
    }

    private String extractKeyFromDevice(RemoteDevice device) {
	return device.getIdentity().getUdn().toString();
    }
}
