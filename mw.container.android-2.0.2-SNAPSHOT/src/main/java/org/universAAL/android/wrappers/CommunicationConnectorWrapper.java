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

import android.util.Log;

public class CommunicationConnectorWrapper implements CommunicationConnector, Receiver, RequestHandler, MembershipListener{
	private final static String TAG = "CommunicationConnectorWrapper";
	public static boolean enableLog=false; //TODO Configure this is just remove it?

	public synchronized String getName() {
		if (enableLog) {
			Log.v(TAG, "Empty getName");
		}
		return "";
	}

	public synchronized String getVersion() {
		if (enableLog) {
			Log.v(TAG, "Empty getVersion");
		}
		return "";
	}

	public synchronized String getDescription() {
		if (enableLog) {
			Log.v(TAG, "Empty getDescription");
		}
		return "";
	}

	public synchronized String getProvider() {
		if (enableLog) {
			Log.v(TAG, "Empty getProvider");
		}
		return "";
	}

	public synchronized void loadConfigurations(Dictionary configurations) {
		if (enableLog) {
			Log.v(TAG, "Empty loadConfigurations");
		}
	}

	public synchronized boolean init() {
		if (enableLog) {
			Log.v(TAG, "Empty init");
		}
		return true;
	}

	public synchronized void dispose() {
		if (enableLog) {
			Log.v(TAG, "Empty dispose");
		}
	}

	public synchronized void multicast(ChannelMessage message,
			List<PeerCard> receivers) throws CommunicationConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty multicast");
		}
	}

	public synchronized void multicast(ChannelMessage message)
			throws CommunicationConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty multicast");
		}
	}

	public synchronized void unicast(ChannelMessage message, String receiver) {
		if (enableLog) {
			Log.v(TAG, "Empty unicast");
		}
	}

	public synchronized void configureConnector(
			List<ChannelDescriptor> channels, String peerName)
			throws CommunicationConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty configureConnector");
		}
	}

	public synchronized void dispose(List<ChannelDescriptor> channels) {
		if (enableLog) {
			Log.v(TAG, "Empty dispose");
		}
	}

	public synchronized List<String> getGroupMembers(String groupName) {
		if (enableLog) {
			Log.v(TAG, "Empty getGroupMembers");
		}
		// TODO HACK for returning own ID as member, so it doesnt leave space in checkpeerthread
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
		if (enableLog) {
			Log.v(TAG, "Empty hasChannel");
		}
		return true;
	}

	public synchronized void getState(OutputStream arg0) throws Exception {
		if (enableLog) {
			Log.v(TAG, "Empty getState");
		}
	}

	public synchronized void receive(Message arg0) {
		if (enableLog) {
			Log.v(TAG, "Empty receive");
		}
	}

	public synchronized void setState(InputStream arg0) throws Exception {
		if (enableLog) {
			Log.v(TAG, "Empty setState");
		}
	}

	public synchronized void block() {
		if (enableLog) {
			Log.v(TAG, "Empty block");
		}
	}

	public synchronized void suspect(Address arg0) {
		if (enableLog) {
			Log.v(TAG, "Empty suspect");
		}
	}

	public synchronized void unblock() {
		if (enableLog) {
			Log.v(TAG, "Empty unblock");
		}
	}

	public synchronized void viewAccepted(View arg0) {
		if (enableLog) {
			Log.v(TAG, "Empty viewAccepted");
		}
	}

	public synchronized Object handle(Message arg0) throws Exception {
		if (enableLog) {
			Log.v(TAG, "Empty handle");
		}
		return null;
	}

}
