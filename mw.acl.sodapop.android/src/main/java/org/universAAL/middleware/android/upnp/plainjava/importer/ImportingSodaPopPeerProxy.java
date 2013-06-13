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
package org.universAAL.middleware.android.upnp.plainjava.importer;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.universAAL.middleware.android.upnp.actions.GetIDActionInvocation;
import org.universAAL.middleware.android.upnp.actions.IActionInvocation;
import org.universAAL.middleware.android.upnp.service.RemoteServicesContainer;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 10, 2012
 * 
 */
public class ImportingSodaPopPeerProxy {

    private static final String TAG = ImportingSodaPopPeerProxy.class.getCanonicalName();

    public void importDevices(UpnpService upnpService) {
	try {
	    // Add a listener for device registration events
	    upnpService.getRegistry().addListener(createRegistryListener(upnpService));

	    // Broadcast a search message for all devices
	    upnpService.getControlPoint().search(new STAllHeader());

	} catch (Exception ex) {
	    Log.e(TAG, "Error occurred when search for devices [" + ex.getMessage() + "]");
	}
    }

    RegistryListener createRegistryListener(final UpnpService upnpService) {
	return new DefaultRegistryListener() {

	    ServiceId serviceId = new UDAServiceId("SodaPopPeer:1");

	    @Override
	    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		Service<RemoteDevice, RemoteService> sodaPop;
		if ((sodaPop = device.findService(serviceId)) != null) {
		    Log.d(TAG, "Service discovered [" + sodaPop + "]");

		    // Add the remote service
		    addRemoteService(upnpService, sodaPop);
		}
	    }

	    @Override
	    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		Service<RemoteDevice, RemoteService> sodaPop;
		if ((sodaPop = device.findService(serviceId)) != null) {
		    Log.d(TAG, "Service disappeared [" + sodaPop + "]");

		    // Add the remote service
		    removeRemoteService(upnpService, sodaPop);
		}
	    }
	};
    }

    private void addRemoteService(UpnpService upnpService,
	    Service<RemoteDevice, RemoteService> sodaPop) {

	// Add the peer to the map
	RemoteServicesContainer.getInstance().addRemoteService(sodaPop);

	// It's necessary to identify the remote peer by calling the 'GetID'
	// action + add a listener that will be called once the action is
	// replied
	IActionInvocation action = new GetIDActionInvocation(sodaPop,
		RemoteServicesContainer.getInstance());

	// Invoke it
	action.invoke(upnpService);
    }

    private void removeRemoteService(UpnpService upnpService,
	    Service<RemoteDevice, RemoteService> sodaPop) {
	// Remove the peer
	RemoteServicesContainer.getInstance().removeRemoteService(sodaPop);
    }
}
