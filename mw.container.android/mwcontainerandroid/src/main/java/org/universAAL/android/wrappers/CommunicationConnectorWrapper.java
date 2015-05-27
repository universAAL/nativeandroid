/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.android.wrappers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.managers.api.AALSpaceManager;

/**
 * A mock up class imitating the jGroups communication module, but providing a
 * transparent implementation (does nothing). This can be used when there is no
 * WiFi data connection available, and the middleware will keep working,
 * ignorant of the implementation.
 * 
 * @author alfiva
 * 
 */
public class CommunicationConnectorWrapper implements CommunicationConnector,
		Receiver, RequestHandler, MembershipListener {

	public synchronized String getName() {
		return "";
	}

	public synchronized String getVersion() {
		return "";
	}

	public synchronized String getDescription() {
		return "";
	}

	public synchronized String getProvider() {
		return "";
	}

	public synchronized void loadConfigurations(Dictionary configurations) {
	}

	public synchronized boolean init() {
		return true;
	}

	public synchronized void dispose() {
	}

	public synchronized void multicast(ChannelMessage message,
			List<PeerCard> receivers) throws CommunicationConnectorException {
	}

	public synchronized void multicast(ChannelMessage message)
			throws CommunicationConnectorException {
	}

	public synchronized void unicast(ChannelMessage message, String receiver) {
	}

	public synchronized void configureConnector(
			List<ChannelDescriptor> channels, String peerName)
			throws CommunicationConnectorException {
	}

	public synchronized void dispose(List<ChannelDescriptor> channels) {
	}

	public synchronized List<String> getGroupMembers(String groupName) {
		// TODO HACK for returning own ID as member, so it doesnt leave space in
		// checkpeerthread
		AALSpaceManager manager = (AALSpaceManager) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { AALSpaceManager.class.getName()
								.toString() });
		ArrayList<String> list = new ArrayList<String>();
		if (manager.getMyPeerCard() != null) {
			list.add(manager.getMyPeerCard().getPeerID());
		}
		return list;
	}

	public synchronized boolean hasChannel(String channelName) {
		return true;
	}

	public synchronized void getState(OutputStream arg0) throws Exception {
	}

	public synchronized void receive(Message arg0) {
	}

	public synchronized void setState(InputStream arg0) throws Exception {
	}

	public synchronized void block() {
	}

	public synchronized void suspect(Address arg0) {
	}

	public synchronized void unblock() {
	}

	public synchronized void viewAccepted(View arg0) {
	}

	public synchronized Object handle(Message arg0) throws Exception {
		return null;
	}

}
