/*

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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
package org.universAAL.middleware.android.modules.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.brokers.Broker;
import org.universAAL.middleware.brokers.control.ExceptionUtils;
import org.universAAL.middleware.brokers.control.WaitForResponse;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.brokers.message.control.ControlMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage;
import org.universAAL.middleware.brokers.message.deploy.DeployMessage.DeployMessageType;
import org.universAAL.middleware.brokers.message.deploy.DeployMessageException;
import org.universAAL.middleware.brokers.message.deploy.DeployMessageFields;
import org.universAAL.middleware.brokers.message.deploy.DeployNotificationPayload;
import org.universAAL.middleware.brokers.message.deploy.DeployPayload;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.interfaces.mpa.UAPPCard;
import org.universAAL.middleware.interfaces.mpa.UAPPPartStatus;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.DeployManager;
import org.universAAL.middleware.managers.api.DeployManagerEventHandler;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.listener.MessageListener;

import android.util.Log;

/**
 * The Control Broker
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class AndroidControlBroker implements SharedObjectListener, Broker,
        MessageListener {
	private static final String TAG = "AndroidControlBroker";
    private ModuleContext context;
    private AALSpaceModule aalSpaceModule;
    private ConfigurableCommunicationModule communicationModule;
    private AALSpaceEventHandler aalSpaceEventHandler;
    private AALSpaceManager aalSpaceManager;
    private DeployManager deployManager;
//    private DeployConnector deployConnector;
    private boolean initialized = false;
    private HashMap<String, WaitForResponse> openTransaction = new HashMap<String, WaitForResponse>();

//    private static String TMP_DEPLOY_FOLDER = "etc" + File.separatorChar
//            + "tmp" + File.separatorChar + "installations" + File.separatorChar;

    private class Response {
        ControlMessage msg;
        PeerCard sender;
    }

    public AndroidControlBroker(ModuleContext context) {
        this.context = context;
        init();

    }

    private CommunicationModule getCommunicationModule() {
        Log.d(TAG, "Fetching the CommunicationModule..." );
        Object[] refs = context.getContainer()
                .fetchSharedObject(
                        context,
                        new Object[] { CommunicationModule.class.getName()
                                .toString() }, this);
        if (refs != null && refs[0] instanceof ConfigurableCommunicationModule) {
            communicationModule = (ConfigurableCommunicationModule) refs[0];
            Log.d(TAG, "CommunicationModule fetched" );
        } else {
            Log.w(TAG,"No CommunicationModule found" );
        }
        return communicationModule;
    }

    private AALSpaceEventHandler getAALSpaceEventHandler() {
        Log.v(TAG,  "Fetching the AALSpaceEventHandler..." );
        Object[] refs = context.getContainer()
                .fetchSharedObject(
                        context,
                        new Object[] { AALSpaceEventHandler.class.getName()
                                .toString() }, this);
        if (refs != null) {
            Log.v(TAG,  "AALSpaceEventHandler found!" );
            aalSpaceEventHandler = (AALSpaceEventHandler) refs[0];

        } else {
            Log.d(TAG, "No AALSpaceEventHandler found" );
            return null;
        }
        return aalSpaceEventHandler;
    }

    private AALSpaceManager getAALSpaceManager() {
        Log.d(TAG, "Fetching the AALSpaceManager..." );
        Object[] refs = context.getContainer().fetchSharedObject(context,
                new Object[] { AALSpaceManager.class.getName().toString() },
                this);
        if (refs != null) {
            Log.d(TAG,"AALSpaceManager found!" );
            aalSpaceManager = (AALSpaceManager) refs[0];

        } else {
            Log.d(TAG, "No AALSpaceManager found" );
        }
        return aalSpaceManager;
    }

    private AALSpaceModule getAALSpaceModule() {
        Log.d(TAG,"Fetching the AALSpaceModule..." );
        Object[] refs = context.getContainer().fetchSharedObject(context,
                new Object[] { AALSpaceModule.class.getName().toString() },
                this);
        if (refs != null) {

            Log.d(TAG, "AALSpaceModule found!" );
            aalSpaceModule = (AALSpaceModule) refs[0];

        } else {
            Log.w(TAG, "No AALSpaceModule found");
        }
        return aalSpaceModule;

    }

    public boolean init() {
        if (!initialized) {

            Log.d(TAG, "Fetching the AALSpaceModule..." );
            Object[] refs = context.getContainer().fetchSharedObject(context,
                    new Object[] { AALSpaceModule.class.getName().toString() },
                    this);
            if (refs != null) {

                Log.d(TAG, "AALSpaceModule found!" );
                aalSpaceModule = (AALSpaceModule) refs[0];
                Log.d(TAG, "AALSpaceModule fetched" );
            } else {
                Log.d(TAG,"No AALSpaceModule found" );
                initialized = false;
                return initialized;
            }

            Log.d(TAG, "Fetching the AALSpaceEventHandler..." );
            refs = context.getContainer().fetchSharedObject(
                    context,
                    new Object[] { AALSpaceEventHandler.class.getName()
                            .toString() }, this);
            if (refs != null) {

                Log.d(TAG, "AALSpaceEventHandler found!" );
                aalSpaceEventHandler = (AALSpaceEventHandler) refs[0];

            } else {
                Log.d(TAG, "No AALSpaceEventHandler");
                initialized = false;
                return initialized;
            }

            Log.d(TAG, "Fetching the AALSpaceManager...");
            refs = context.getContainer()
                    .fetchSharedObject(
                            context,
                            new Object[] { AALSpaceManager.class.getName()
                                    .toString() }, this);
            if (refs != null) {

                Log.d(TAG, "AALSpaceManager found!" );
                aalSpaceManager = (AALSpaceManager) refs[0];

            } else {
                Log.d(TAG, "No AALSpaceManager" );
                initialized = false;
                return initialized;
            }

            Log.d(TAG, "Fetching the CommunicationModule..." );
            refs = context.getContainer().fetchSharedObject(
                    context,
                    new Object[] { CommunicationModule.class.getName()
                            .toString() }, this);
            if (refs != null
                    && refs[0] instanceof ConfigurableCommunicationModule) {
                communicationModule = (ConfigurableCommunicationModule) refs[0];
                communicationModule.addMessageListener(this, getBrokerName());
                Log.d(TAG, "CommunicationModule fetched" );
            } else {
                Log.d(TAG, "No CommunicationModule found" );
                initialized = false;
                return initialized;
            }
            Log.d(TAG, "Fetching the DeployManager..." );
            refs = context.getContainer().fetchSharedObject(context,
                    new Object[] { DeployManager.class.getName().toString() },
                    this);
            if (refs != null) {

                Log.d(TAG, "DeployManager found!" );
                deployManager = (DeployManager) refs[0];
                Log.d(TAG, "DeployManager fetched" );
            } else {
                Log.d(TAG,"No DeployManager found" );
            }
/*
            LogUtils.logDebug(context, AndroidControlBroker.class, "controlBroker",
                    new Object[] { "Fetching the DeployConnector..." }, null);
            refs = context.getContainer()
                    .fetchSharedObject(
                            context,
                            new Object[] { DeployConnector.class.getName()
                                    .toString() }, this);
            if (refs != null) {

                LogUtils.logDebug(context, AndroidControlBroker.class,
                        "controlBroker",
                        new Object[] { "DeployConnector found!" }, null);
                deployConnector = (DeployConnector) refs[0];
                LogUtils.logDebug(context, AndroidControlBroker.class,
                        "controlBroker",
                        new Object[] { "DeployConnector fetched" }, null);
            } else {
                LogUtils.logDebug(context, AndroidControlBroker.class,
                        "controlBroker",
                        new Object[] { "No DeployConnector found" }, null);
            }*/
        }
        initialized = true;
        return initialized;

    }

    public List<AALSpaceCard> discoverAALSpace(
            Dictionary<String, String> filters) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return null;
        }
        return aalSpaceModule.getAALSpaces(filters);
    }

    public void buildAALSpace(AALSpaceCard aalSpaceCard) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized. " );
            return;
        }
        aalSpaceModule.newAALSpace(aalSpaceCard);
    }

    public void sharedObjectAdded(Object arg0, Object arg1) {
        if (arg0 != null && arg0 instanceof AALSpaceModule) {
            Log.d(TAG,"AALSpaceModule registered..." );
            aalSpaceModule = (AALSpaceModule) arg0;
        }
        if (arg0 != null && arg0 instanceof CommunicationModule) {
            Log.d(TAG, "CommunicationModule registered...");
            if (communicationModule instanceof ConfigurableCommunicationModule)
                communicationModule = (ConfigurableCommunicationModule) arg0;
            communicationModule.addMessageListener(this, getBrokerName());

        }

        if (arg0 != null && arg0 instanceof AALSpaceManager) {
            Log.d(TAG, "AALSpaceManager registered..." );
            aalSpaceManager = (AALSpaceManager) arg0;

        }

        if (arg0 != null && arg0 instanceof AALSpaceEventHandler) {
            Log.d(TAG, "AALSpaceEventHandler registered..." );
            aalSpaceEventHandler = (AALSpaceEventHandler) arg0;
        }
        if (arg0 != null && arg0 instanceof DeployManager) {
            Log.d(TAG, "DeployManager registered..." );
            deployManager = (DeployManager) arg0;
        }
        /*if (arg0 != null && arg0 instanceof DeployConnector) {
            Log.d(TAG,context, AndroidControlBroker.class, "controlBroker",
                    new Object[] { "DeployConnector registered..." }, null);
            deployConnector = (DeployConnector) arg0;
        }*/
    }

    public void sharedObjectRemoved(Object arg0) {
        if (arg0 instanceof AALSpaceEventHandler) {
            Log.i(TAG, "AALSpaceEventHandler unregistered!" );
            aalSpaceEventHandler = null;
            initialized = false;
        } else if (arg0 instanceof AALSpaceManager) {
            Log.i(TAG, "AALSpaceManager unregistered!" );
            aalSpaceManager = null;
            initialized = false;
        } else if (arg0 instanceof DeployManager) {
            Log.i(TAG, "DeployManager unregistered!");
            deployManager = null;
            initialized = false;

        } else if (arg0 instanceof AALSpaceModule) {
            Log.i(TAG,  "AALSpaceModule unregistered!");
            aalSpaceModule = null;
            initialized = false;

        } else if (arg0 instanceof CommunicationModule) {
            Log.i(TAG,  "CommunicationModule unregistered!" );
            try {
                communicationModule
                        .removeMessageListener(this, getBrokerName());
            } catch (Exception e) {

            }
            communicationModule = null;
            initialized = false;
        }/* else if (arg0 instanceof DeployConnector) {
            LogUtils.logInfo(context, AndroidControlBroker.class, "controlBroker",
                    new Object[] { "DeployConnector unregistered!" }, null);
            deployConnector = null;
            initialized = false;
        }*/

    }

    public void joinRequest(AALSpaceCard spaceCard, PeerCard sender) {
        if (getAALSpaceEventHandler() == null) {
            Log.w(TAG, "ControlBroker not initialized. Join aborted" );
            return;
        }
        aalSpaceEventHandler.joinRequest(spaceCard, sender);

    }

    public void leaveRequest(AALSpaceDescriptor spaceDescriptor) {
        if (getAALSpaceEventHandler() == null) {
            Log.w(TAG, "ControlBroker not initialized. Leave aborted" );
            return;
        }
        aalSpaceEventHandler.leaveRequest(spaceDescriptor);
    }

    public void requestToLeave(AALSpaceDescriptor spaceDescriptor) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized. Request to leave aborted" );
            return;
        }
        aalSpaceModule.requestToLeave(spaceDescriptor);
    }

    public void peerLost(PeerCard sender) {
        if (getAALSpaceEventHandler() == null) {
            Log.w(TAG, "ControlBroker not initialized. Peer Lost message aborted" );
            return;
        }
        aalSpaceEventHandler.peerLost(sender);

    }

    public void join(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized. Join message aborted" );
            return;
        }
        aalSpaceModule.joinAALSpace(spaceCoordinator, spaceCard);
    }

    /**
     * This method returns the PeerCard of the current MW instance
     *
     * @return PeerCard
     */
    public PeerCard getmyPeerCard() {
        if (getAALSpaceManager() == null) {
            Log.w(TAG, "ControlBroker not initialized. Fetching the PeerCard aborted" );
            return null;
        }
        return aalSpaceManager.getMyPeerCard();
    }

    /**
     * This method returns the AALSpaceDescriptor of my AALSpace
     *
     * @return
     */
    public AALSpaceDescriptor getmyAALSpaceDescriptor() {
        if (getAALSpaceManager() == null) {
            Log.w(TAG, "ControlBroker not initialized. Fetching the PeerCard aborted" );
            return null;
        }
        return aalSpaceManager.getAALSpaceDescriptor();
    }

    public void aalSpaceJoined(AALSpaceDescriptor descriptor) {
        if (getAALSpaceEventHandler() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceEventHandler.aalSpaceJoined(descriptor);
    }

    public void peerFound(PeerCard peer) {
        if (getAALSpaceEventHandler() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceEventHandler.peerFound(peer);
    }

    public void newAALSpaceFound(Set<AALSpaceCard> spaceCards) {
        if (getAALSpaceEventHandler() == null) {
            return;
        }
        aalSpaceEventHandler.newAALSpacesFound(spaceCards);

    }

    /**
     * Only configures the communication channels by creating a list of channels
     * for the Communication Module
     *
     * @param communicationChannels
     */
    public void configureChannels(
            List<ChannelDescriptor> communicationChannels, String peerName) {
        if (getCommunicationModule() == null) {
            Log.w(TAG,"ControlBroker not initialized." );
            return;
        }
        communicationModule.addMessageListener(this, this.getBrokerName());
        communicationModule.configureChannels(communicationChannels, peerName);

    }

    /**
     * Configures the peering channel by configuring the AALSpaceModule and by
     * creating a new channel for the Communication Module
     *
     * @param peeringChannel
     */
    public void configurePeeringChannel(ChannelDescriptor peeringChannel,
            String peerName) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceModule
                .configureAALSpaceChannel(peeringChannel.getChannelName());
        List<ChannelDescriptor> channel = new ArrayList<ChannelDescriptor>();
        channel.add(peeringChannel);
        configureChannels(channel, peerName);

    }

    public void resetModule(List<ChannelDescriptor> channels) {
        if (getCommunicationModule() == null) {
            Log.w(TAG, "ControlBroker not initialized.");
            return;
        }
        communicationModule.dispose(channels);
    }

    public void destroyAALSpace(AALSpaceCard spaceCard) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG,"ControlBroker not initialized." );
            return;
        }
        aalSpaceModule.destroyAALSpace(spaceCard);
    }

    public void leaveAALSpace(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceModule.leaveAALSpace(spaceCoordinator, spaceCard);
    }

    public void addNewPeer(AALSpaceDescriptor spaceDescriptor, PeerCard peer) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceModule.addPeer(spaceDescriptor, peer);
    }

    public void newPeerAdded(AALSpaceCard spaceCard, PeerCard peer) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized." );
            return;
        }
        aalSpaceModule.announceNewPeer(spaceCard, peer);
    }

    /**
     * This method allows to request the installation of an uApp part to a
     * target node
     *
     * @param target
     *            The node into which to install the part
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void requestToUninstallPart(PeerCard target, UAPPCard card) {
    	//TODO Deploy to  be done later
    }

    /**
     * This method allows to request the installation of an uApp part to a
     * target node
     *
     * @param partAsZip
     *            The part serialized as a String. The payload of the
     *            DeployMessage has to be a string
     * @param target
     *            The node into which to install the part
     * @param card
     *            The reference information of the part of the application
     *            within a service to install
     */
    public void requestToInstallPart(byte[] partAsZip, PeerCard target,
            UAPPCard card) {
    	//TODO Deploy to be done later
    }

    /**
     *
     * @param mpaCard
     * @param partID
     * @param peer
     *            The peer notifying the staus of the part
     * @param partStatus
     */
    public void notifyRequestToInstallPart(UAPPCard mpaCard, String partID,
            UAPPPartStatus partStatus) {
        if (!init()) {
            Log.w(TAG, "ControlBroker not initialized.");
            return;
        }
        if (deployManager.isDeployCoordinator()) {
            // notify the local deploy manager
            if (deployManager instanceof DeployManagerEventHandler)
                ((DeployManagerEventHandler) deployManager)
                        .installationPartNotification(mpaCard, partID,
                                aalSpaceManager.getMyPeerCard(), partStatus);

        } else {
            // send the message to the remote DeployManager
            DeployNotificationPayload notificationPayload = new DeployNotificationPayload(
                    null, mpaCard, partID, partStatus);
            DeployMessage deployMessage = new DeployMessage(
                    DeployMessageType.PART_NOTIFICATION, notificationPayload);

            // ...and wrap it as ChannelMessage
            List<String> channelName = new ArrayList<String>();
            channelName.add(getBrokerName());
            ChannelMessage channelMessage = new ChannelMessage(getmyPeerCard(),
                    deployMessage.toString(), channelName);
            communicationModule.send(channelMessage, this, aalSpaceManager
                    .getAALSpaceDescriptor().getDeployManager());
        }
    }

    public String getBrokerName() {
        return context.getID();
    }

    public void handleSendError(ChannelMessage message,
            CommunicationConnectorException e) {
        Log.e(TAG, "Error while sending the message: "
                        + message.toString() + " error: " ,e);
    }

    public void messageReceived(ChannelMessage message) {
        if (!init()) {
            Log.w(TAG, "ControlBroker not initialized. Dropping the message" );
            return;
        }
        deployManager = getDeployManager();
        if (message != null) {
            try {
                JSONObject obj = new JSONObject(message.getContent());
                BrokerMessageTypes mtype = BrokerMessageTypes.valueOf(obj
                        .getString(BrokerMessageFields.BROKER_MESSAGE_TYPE));
                BrokerMessage msg = (BrokerMessage) unmarshall(message
                        .getContent());

                switch (mtype) {
                case DeployMessage:
                    handleDeployMessage(message.getSender(),
                            (DeployMessage) msg);
                    break;

                case ControlMessage:
                    handleControlMessage(message.getSender(),
                            (ControlMessage) msg);
                    break;

                default:
                    break;
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error during message receive: ",e);
            }

        }

    }

    private void handleControlMessage(PeerCard sender, ControlMessage msg) {
        switch (msg.getMessageType()) {
        case GET_ATTRIBUTES: {
            handleGetAttributes(sender, msg.getTransactionId(),
                    msg.getAttributes());
        }
            break;
        case GET_ATTRIBUTES_RESPONSE: {
            WaitForResponse req = openTransaction.get(msg.getTransactionId());
            if (req != null) {
                req.addResponse(msg);
            }
        }
            break;
        case MATCH_ATTRIBUTES: {
            handleMatchAttributes(sender, msg.getTransactionId(),
                    msg.getAttributeFilter());
        }
            break;
        case MATCH_ATTRIBUTES_RESPONSE: {
            WaitForResponse req = openTransaction.get(msg.getTransactionId());
            if (req != null) {
                Response response = new Response();
                response.msg = msg;
                response.sender = sender;
                req.addResponse(response);
            }
        }
            break;

        default:
            throw new UnsupportedOperationException(
                    "Unable to handle Control Message of type: "
                            + msg.getMessageType());
        }
    }

    private void handleMatchAttributes(PeerCard sender, String transactionId,
            Map<String, Serializable> attributeValues) {

        ControlMessage controlMsg = prepareMatchingResponse(transactionId,attributeValues);
        CommunicationModule bus = getCommunicationModule();
        List<String> chName = new ArrayList<String>();
        chName.add(getBrokerName());
        ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
                controlMsg.toString(), chName);
        bus.send(chMsg, this, sender);
    }

    private ControlMessage prepareMatchingResponse(String transactionId,
            Map<String, Serializable> attributeValues) {
        boolean match = true;


        HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();
        Set<String> names = attributeValues.keySet();
        for (String name : names) {
            Object value = context.getProperty(name);
            if (value == null) {
                match = false;
                break;
            }
            if (attributeValues.get(name) != null
                    && value.equals(attributeValues.get(name)) == false) {
                match = false;
                break;
            }
            if (value instanceof Serializable) {
                attributes.put(name, (Serializable) value);
            } else {
                attributes.put(name, value.toString());
            }
        }
        return new ControlMessage(
                aalSpaceManager.getAALSpaceDescriptor(), transactionId,
                attributes, match);
    }



    private void handleGetAttributes(PeerCard sender, String transactionId,
            List<String> requestedAttributes) {

        ControlMessage controlMsg = prepareGetAttributesResponse(transactionId,requestedAttributes);

        CommunicationModule bus = getCommunicationModule();
        List<String> chName = new ArrayList<String>();
        chName.add(getBrokerName());
        ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
                controlMsg.toString(), chName);
        bus.send(chMsg, this, sender);
    }

    private ControlMessage prepareGetAttributesResponse(String transactionId,
            List<String> requestedAttributes) {
        HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();
        for (String name : requestedAttributes) {
            Object value = context.getProperty(name);
            if (value == null)
                continue;
            if (value instanceof Serializable) {
                attributes.put(name, (Serializable) value);
            } else {
                attributes.put(name, value.toString());
            }
        }
        return new ControlMessage(
                aalSpaceManager.getAALSpaceDescriptor(), transactionId,
                attributes);

    }

    private void handleDeployMessage(PeerCard sender, DeployMessage msg) {
    	//TODO Deploy to be done later
    }

    public void installArtefactLocally(String serializedPart) {
    	//TODO Deploy to be done later
    }

    private DeployManager getDeployManager() {
        Log.d(TAG, "Fetching the DeployManager..." );
        if (deployManager == null) {
            Object[] refs = context.getContainer().fetchSharedObject(context,
                    new Object[] { DeployManager.class.getName().toString() },
                    this);
            if (refs != null) {

                Log.d(TAG, "DeployManager found!" );
                deployManager = (DeployManager) refs[0];
                Log.d(TAG, "DeployManager fetched" );
                return deployManager;
            } else {
                Log.w(TAG, "No DeployManager found" );
                return null;
            }
        } else
            return deployManager;

    }

	/*
	 * private DeployConnector getDeployConnector() { }
	 */// TODO Deploy to be done later

    public List<String> getPeersAddress() {
        return aalSpaceModule.getPeersAddress();
    }

    public void requestPeerCard(String peerAddress) {
        if (getAALSpaceModule() == null) {
            Log.w(TAG, "ControlBroker not initialized. Request to leave aborted" );
            return;
        }
        aalSpaceModule.requestPeerCard(aalSpaceManager.getAALSpaceDescriptor(),
                peerAddress);
    }

    /*
     * public void configureDeployMessage() { if (getCommunicationModule() ==
     * null) { return; } communicationModule.addMessageListener(this,
     * aalSpaceManager .getGroupName(this)); }
     */// TODO Deploy to be done later

    public void dispose() {
        context.getContainer().removeSharedObjectListener(this);
        if (communicationModule == null)
            return;
        communicationModule.removeMessageListener(this, getBrokerName());

    }

    public void renewAALSpace(AALSpaceCard spaceCard) {
        aalSpaceModule.renewAALSpace(spaceCard);
    }

    public void signalAALSpaceStatus(AALSpaceStatus status,
            AALSpaceDescriptor spaceDescriptor) {

        // ControlPayload payload = new ControlPayload(getBrokerName(),
        // UUID.randomUUID().toString(), "", status);
        // ControlMessage message = new
        // ControlMessage(aalSpaceManager.getmyPeerCard(), getBrokerName(),
        // null, payload, ControlMessageType.AALSPACE_EVENT);
        // communicationModule.sendAll(message, this);
    }

    private BrokerMessage unmarshallDeployMessage(JSONObject obj,
            String original) {
        DeployMessage deployMessage = null;
        try {

            DeployPayload deployPayload = null;

            // unmarshall the message type
            DeployMessageType deployMessageType = DeployMessageType.valueOf(obj
                    .getString(DeployMessageFields.DEPLOY_MTYPE));

            // unmarhsall MPACard
            String uappCardServiceId = obj
                    .getString(DeployMessageFields.UAPP_CARD_SERVICE_ID);
            String uappCardPartId = obj
                    .getString(DeployMessageFields.UAPP_CARD_PART_ID);

            String uappCardName = obj.getString(DeployMessageFields.UAPP_NAME);
            String uappCardID = obj.getString(DeployMessageFields.UAPP_ID);
            String uappCardDescr = obj.getString(DeployMessageFields.UAPP_DESC);
            UAPPCard mpaCard = new UAPPCard(uappCardServiceId, uappCardID,
                    uappCardPartId, uappCardName, uappCardDescr);

            int payloadType = obj.getInt(DeployMessageFields.DEPLOY_PAYLOAD);
            if (payloadType == 1) {

                // unmarhall DeployPayload
                // byte[] thePart =
                // obj.getString(DeployMessageFields.PART).getBytes();
                JSONArray bytes = obj.getJSONArray(DeployMessageFields.PART)
                        .getJSONArray(0);
                byte[] thePart = new byte[bytes.length()];
                for (int i = 0; i < thePart.length; i++) {
                    thePart[i] = (byte) bytes.getInt(i);
                }
                deployPayload = new DeployPayload(thePart, mpaCard);

            } else if (payloadType == 2) {

                // unmarhsall DeployNotificationPayload
                String mpaPartID = obj.getString(DeployMessageFields.PART_ID);
                UAPPPartStatus mpaPartStatus = UAPPPartStatus.valueOf(obj
                        .getString(DeployMessageFields.PART_STATUS));

                deployPayload = new DeployNotificationPayload(null, mpaCard,
                        mpaPartID, mpaPartStatus);
            }
            deployMessage = new DeployMessage(deployMessageType, deployPayload);

        } catch (JSONException e) {
            final String MSG = "Unable to unmarshall message due to JSON parsing issue:";
            Log.d(TAG, MSG , e);
            Log.d(TAG, MSG + ExceptionUtils.stackTraceAsString(e) );
            new DeployMessageException(MSG + e.toString(), e);
        } catch (Exception e) {
            final String MSG = "Unable to unmarshall message due to generic error: ";
            Log.d(TAG, MSG , e);
            Log.d(TAG, MSG + ExceptionUtils.stackTraceAsString(e) );
            new DeployMessageException(MSG + e.toString(), e);
        }
        return deployMessage;
    }

    public BrokerMessage unmarshall(String message) {
        JSONObject obj = null;
        BrokerMessageTypes mtype = null;
        try {
            obj = new JSONObject(message);
            // check if the message can be serialized
            mtype = BrokerMessageTypes.valueOf(obj
                    .getString(BrokerMessageFields.BROKER_MESSAGE_TYPE));
            switch (mtype) {
            case DeployMessage:
                return unmarshallDeployMessage(obj, message);
            case ControlMessage:
                return unmarshallControlMessage(obj, message);
            }
        } catch (Exception e) {
            final String MSG = "Unable to unmarshall message due to JSON parsing issue, while retriving file "
                    + BrokerMessageFields.BROKER_MESSAGE_TYPE + ":";
            Log.d(TAG, MSG , e);
            Log.d(TAG, MSG + ExceptionUtils.stackTraceAsString(e) );
            new DeployMessageException(MSG + e.toString(), e);
        }
        return null;

    }

    private BrokerMessage unmarshallControlMessage(JSONObject obj,
            String original) throws Exception {
        return ControlMessage.unmarshall(original);
    }

    public Map<String, Serializable> requestPeerAttributes(
            List<String> attributes, PeerCard target, int limit, int timeout) {

        if ( target.equals(aalSpaceManager.getMyPeerCard() ) ) {
            ControlMessage response = prepareGetAttributesResponse("local", attributes);
            return response.getAttributeValues();
        }

        CommunicationModule bus = getCommunicationModule();
        ControlMessage controlMsg = new ControlMessage(
                aalSpaceManager.getAALSpaceDescriptor(), attributes);
        List<String> chName = new ArrayList<String>();
        chName.add(getBrokerName());
        ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
                controlMsg.toString(), chName);
        WaitForResponse<ControlMessage> waiter = new WaitForResponse<ControlMessage>(
                limit, timeout);
        openTransaction.put(controlMsg.getTransactionId(), waiter);
        bus.send(chMsg, this, target);
        ControlMessage response = waiter.getFirstReponse();
        openTransaction.remove(controlMsg.getTransactionId());
        return response.getAttributeValues();
    }

    public Map<PeerCard, Map<String, Serializable>> findMatchingPeers(
            Map<String, Serializable> filter, int limit, int timeout) {
        CommunicationModule bus = getCommunicationModule();
        ControlMessage controlMsg = new ControlMessage(
                aalSpaceManager.getAALSpaceDescriptor(), filter);
        List<String> chName = new ArrayList<String>();
        chName.add(getBrokerName());
        ChannelMessage chMsg = new ChannelMessage(getmyPeerCard(),
                controlMsg.toString(), chName);
        WaitForResponse<Response> waiter = new WaitForResponse<Response>(limit, timeout);
        openTransaction.put(controlMsg.getTransactionId(), waiter);
        bus.sendAll(chMsg, this);
        List<Response> responses = new ArrayList<AndroidControlBroker.Response>();
        handleLocalMatchingPeers(controlMsg, responses);
        responses.addAll( waiter.getReponses() );
        HashMap<PeerCard, Map<String, Serializable>> results = new HashMap<PeerCard, Map<String, Serializable>>();
        for (Response response : responses) {
            if ( response.msg.getMatchFilter() == false ) {
                continue;
            }
            Map<String, Serializable> values = response.msg
                    .getAttributeValues();
            results.put(response.sender, values);
        }
        openTransaction.remove(controlMsg.getTransactionId());
        return results;
    }

    private void handleLocalMatchingPeers(ControlMessage controlMsg,
            List<Response> responses) {

        Response r = new Response();
        r.msg = prepareMatchingResponse(controlMsg.getTransactionId(),controlMsg.getAttributeFilter() );
        r.sender = aalSpaceManager.getMyPeerCard();
        responses.add( r );
    }

}
