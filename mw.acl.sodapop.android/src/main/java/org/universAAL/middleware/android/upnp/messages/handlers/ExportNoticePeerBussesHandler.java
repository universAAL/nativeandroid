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

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessageHandler;
import org.universAAL.middleware.android.upnp.actions.NoticePeerBussesActionInvocation;
import org.universAAL.middleware.android.upnp.exceptions.UnknownRemoteDeviceExeption;
import org.universAAL.middleware.android.upnp.messages.ExportNoticePeerBussesMessage;
import org.universAAL.middleware.android.upnp.service.RemoteServicesContainer;
import org.universAAL.middleware.android.upnp.service.UPnPSodaPopPeersService;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 11, 2012
 * 
 */
public class ExportNoticePeerBussesHandler extends AbstractMessageHandler {

    public void handleMessage(IMessage message) throws UnknownRemoteDeviceExeption {
	// Cast it to NoticePeerBusses message
	ExportNoticePeerBussesMessage noticePeerBusses = (ExportNoticePeerBussesMessage) message;

	// Get the remote proxy peer
	Service<RemoteDevice, RemoteService> remoteService = RemoteServicesContainer.getInstance()
		.getRemoteService(noticePeerBusses.getRemotePeerIDForResponse());

	// Initiated the action
	NoticePeerBussesActionInvocation action = new NoticePeerBussesActionInvocation(
		remoteService, noticePeerBusses.getPeerID(), noticePeerBusses.getBusNames());

	// Get the context - it's the service
	UPnPSodaPopPeersService upnpSodaPopService = (UPnPSodaPopPeersService) noticePeerBusses
		.getContext();
	UpnpService upnpService = upnpSodaPopService.getUPnPService();

	// Invoke the action
	action.invoke(upnpService);
    }

}
