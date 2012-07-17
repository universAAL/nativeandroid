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
package org.universAAL.middleware.android.upnp.messages.handlers;

import java.util.Collection;

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.upnp.messages.ExportNoticeNewPeer;
import org.universAAL.middleware.android.upnp.plainjava.DeviceFactory;
import org.universAAL.middleware.android.upnp.service.UPnPSodaPopPeersService;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class ExportNoticeNewPeerHandler implements IMessageHandler {

    private static final String TAG = ExportNoticeNewPeerHandler.class.getCanonicalName();

    public void handleMessage(IMessage message) {
	// Cast it to export local peer message
	ExportNoticeNewPeer exportNoticeNewPeerMessage = (ExportNoticeNewPeer) message;

	Log.d(TAG,
		"Is about to export SodaPop peer with ID ["
			+ exportNoticeNewPeerMessage.getPeerID() + "]");

	// Export the peer
	exportPeer(exportNoticeNewPeerMessage);
    }

    private void exportPeer(ExportNoticeNewPeer exportNoticeNewPeerMessage) {
	// Get the context - it's the service
	UPnPSodaPopPeersService upnpSodaPopService = (UPnPSodaPopPeersService) exportNoticeNewPeerMessage
		.getContext();
	UpnpService upnpService = upnpSodaPopService.getUPnPService();

	try {
	    LocalDevice createdDevice = new DeviceFactory().createDevice(
		    upnpSodaPopService.getDeviceDescription(),
		    exportNoticeNewPeerMessage.getPeerID(), upnpSodaPopService);
	    upnpService.getRegistry().addDevice(createdDevice);
	    // new
	    // DeviceFactory().getParsedDevice(upnpSodaPopService.getDeviceDescription(),
	    // "some string for UDN"));
	} catch (Throwable th) {
	    Log.e(TAG, "Error when exporting peer [" + exportNoticeNewPeerMessage.getPeerID() + "]");
	}

	// Force refresh
	upnpService.getControlPoint().search();
	Collection<Device> devices = upnpService.getControlPoint().getRegistry().getDevices();
	System.out.println("Searching for devices...");
	while (devices.isEmpty()) {
	    /*
	     * This is just a primitive lock in case SodaPop peer is taking some
	     * time to load. In case if there are other upnp devices list of
	     * them will be also displayed
	     */
	    devices = upnpService.getControlPoint().getRegistry().getDevices();
	}
	System.out.println("Something found!");
	for (Device device : upnpService.getControlPoint().getRegistry().getDevices()) {
	    System.out.println(device.toString());
	}
    }
}
