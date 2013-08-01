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

import java.util.ArrayList;
import java.util.Dictionary;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.universAAL.middleware.android.connectors.ConnectorDiscWrapper;
//import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.brokers.Broker;
//import org.universAAL.middleware.brokers.control.ControlBroker;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.BrokerMessageFields;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage.AALSpaceMessageTypes;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessageException;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessageFields;
import org.universAAL.middleware.connectors.DiscoveryConnector;
//import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
//import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
//import org.universAAL.middleware.interfaces.aalspace.AALSpaceType;
import org.universAAL.middleware.modules.AALSpaceModule;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.exception.AALSpaceModuleErrorCode;
import org.universAAL.middleware.modules.exception.AALSpaceModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

import android.util.Log;

/**
 * Implementation of the AALSpaceModule
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 */
public class AndroidAALSpaceModuleImpl implements AALSpaceModule, MessageListener,
        SharedObjectListener, ServiceListener, Broker {
	private static final String TAG = "AndroidAALSpaceModuleImpl";
    private String name;
    private String provider;
    private String version;
    private String description;
    private ModuleContext context;
    private ConnectorDiscWrapper wrapDiscovery;
    // Discovery Connectors
//    private List<DiscoveryConnector> discoveryConnectors;
    // Communication Module
    private CommunicationModule communicationModule;
    // ControlBroler
    private AndroidControlBroker controlBoker;
    private boolean initialized = false;
    // the Broker Name to use in order to send the messages with the correct
    // channel
    private String brokerName;

    /**
     * This method configures the AALSpaceModule: -to obtain the reference to
     * all the DiscoveryConnector present in the fw -to obtain the reference to
     * the CommunicationModdule -to obtain the reference to the ControlBroker
     *
     * @return true if initialized with the connectors and the module, false
     *         otherwise
     */
    public boolean init() {
        if (!initialized) {
            try {
                /*if (discoveryConnectors == null
                        || discoveryConnectors.isEmpty()) {

                    LogUtils.logDebug(
                            context,
                            AndroidAALSpaceModuleImpl.class,
                            "AALSpaceModuleImpl",
                            new Object[] { "Fetching the DiscoveryConnector..." },
                            null);
                    Object[] dConnectors = context.getContainer()
                            .fetchSharedObject(
                                    context,
                                    new Object[] { DiscoveryConnector.class
                                            .getName() }, this);
                    if (dConnectors != null && dConnectors.length > 0) {
                        LogUtils.logDebug(context, AndroidAALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl", new Object[] { "Found: "
                                        + dConnectors.length
                                        + " DiscoveryConnector" }, null);
                        // clear or init the list of connectors

                        discoveryConnectors = new ArrayList<DiscoveryConnector>();
                        for (Object ref : dConnectors) {
                            DiscoveryConnector dConnector = (DiscoveryConnector) ref;*/
                            wrapDiscovery.addAALSpaceListener(this);
//                            dConnector.addAALSpaceListener(this);
                        /*    discoveryConnectors.add((DiscoveryConnector) ref);
                        }
                        LogUtils.logDebug(context, AndroidAALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "DiscoveryConnectors fetched" },
                                null);
                    } else {
                        LogUtils.logWarn(context, AndroidAALSpaceModuleImpl.class,
                                "AALSpaceModuleImpl",
                                new Object[] { "No DiscoveryConnector found" },
                                null);
                        initialized = false;
                        return initialized;
                    }
                }*/
                if (communicationModule == null) {
                    Log.d(TAG, "Fetching the CommunicationModule..." );
                    Object cModule = context.getContainer().fetchSharedObject(
                            context,
                            new Object[] { CommunicationModule.class.getName()
                                    .toString() });
                    if (cModule != null) {
                        communicationModule = (CommunicationModule) cModule;
                        Log.d(TAG, "CommunicationModule fetched" );
                    } else {
                        Log.w(TAG, "No CommunicationModule found" );
                        initialized = false;
                        return initialized;
                    }
                }

                if (controlBoker == null) {
                    Log.d(TAG,"Fetching the ControlBroker...");
                    Object cBroker = context.getContainer().fetchSharedObject(
                            context,
                            new Object[] { AndroidControlBroker.class.getName()
                                    .toString() });
                    if (cBroker != null) {
                        Log.d(TAG,"Found a ControlBroker" );
                        controlBoker = (AndroidControlBroker) cBroker;
                        Log.d(TAG, "ControlBroker fetched" );
                    } else {
                        Log.w(TAG, "No ControlBroker found" );
                        initialized = false;
                        return initialized;
                    }
                    Log.d(TAG,"ControlBroker found");
                    // DiscoveryConnector, CommunicationModule and ControlBroker
                    // have been found

                }
                initialized = true;
            } catch (NullPointerException e) {
                Log.e(TAG,"Error while initializing the AALSpaceModule:" ,e);
                initialized = false;
            } catch (ClassCastException e) {
                Log.e(TAG, "Error while casting CommunicationConnector and CommunicationModule: ",e);
                initialized = false;
            }
        }
        if (initialized)
            Log.d(TAG, "AALSpaceModule initialized" );
        return initialized;
    }

    public AndroidAALSpaceModuleImpl(ConnectorDiscWrapper wrapDiscovery, ModuleContext context) {
        this.context = context;
        this.wrapDiscovery=wrapDiscovery;
        /*discoveryConnectors = new ArrayList<DiscoveryConnector>();*/
    }

    public List<AALSpaceCard> getAALSpaces() {
        return this.getAALSpaces(null);
    }

    public List<AALSpaceCard> getAALSpaces(Dictionary<String, String> filters)
            throws AALSpaceModuleException {
        List<AALSpaceCard> spaces = new ArrayList<AALSpaceCard>();
        if (init()) {
            Log.d(TAG, "Searching for the AALSpace with filters: "
                            + filters.toString() + "..." );

            try {
                /*for (DiscoveryConnector dConnector : discoveryConnectors) {*/
                    if (filters != null && filters.size() > 0){
                        spaces.addAll(wrapDiscovery.findAALSpace(filters));
//                    spaces.addAll(dConnector.findAALSpace(filters));
                    } else{
                        spaces.addAll(wrapDiscovery.findAALSpace());
//                        spaces.addAll(dConnector.findAALSpace());
                    }
                /*}*/
            } catch (DiscoveryConnectorException e) {
                Log.e(TAG, "Error during the AALSPace search:",e);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
                        e.toString());
            }
            Log.d(TAG, " AALSpaces." );

        } else {
            Log.w(TAG, "AALSpaceModule cannot be initialized. Returning no AALSpaces" );
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
                    "AALSpaceModule cannot be initialized. Returning no AALSpaces");
        }
        return spaces;
    }

    public synchronized void newAALSpace(AALSpaceCard aalSpaceCard)
            throws AALSpaceModuleException {
        if (init()) {
            Log.d(TAG,"Creating a new AALSpace..." );
            /*for (DiscoveryConnector connector : discoveryConnectors) {*/
                try {
                	wrapDiscovery.announceAALSpace(aalSpaceCard);
//                    connector.announceAALSpace(aalSpaceCard);
                } catch (DiscoveryConnectorException e) {
                    Log.e(TAG, "Error creating the AALSpace: "
                                    + aalSpaceCard.toString() + " due to: "
                                    + e.toString() );
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_INTERACTING_DISCOVERY_CONNECTORS,
                            "Error creating the AALSpace: "
                                    + aalSpaceCard.toString() + " due to: "
                                    + e.toString());
                }
            /*}*/
        } else {
            Log.w(TAG, "AALSpaceModule cannot be initialized. Returning no AALSpace" );
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.NO_DISCOVERY_CONNECTORS,
                    "AALSpaceModule cannot be initialized. Returning no AALSpaces");
        }
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void loadConfigurations(Dictionary configurations) {
        Log.d(TAG, "updating AALSpaceModule properties" );
        if (configurations == null) {
            Log.d(TAG, "AALSpaceModule properties are null" );
            return;
        }
        try {

            this.name = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_NAME);
            this.version = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_VERSION);
            this.description = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_DESCRIPTION);
            this.provider = (String) configurations
                    .get(org.universAAL.middleware.modules.util.Consts.MODULE_PROVIDER);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error during AALSpaceModule properties update" );
        } catch (NullPointerException e) {
            Log.e(TAG, "Error during AALSpaceModule properties update" );
        } catch (Exception e) {
            Log.e(TAG, "Error during AALSpaceModule properties update" );
        }
        Log.d(TAG, "AALSpaceModule properties updated");

    }

    public void leaveAALSpace(PeerCard spaceCoordinator, AALSpaceCard spaceCard) {
        try {
            if (init()) {
                Log.d(TAG, "Creating a new AALSpaceMessage" );
                // prepare the AALSpace Message...
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        new AALSpaceDescriptor(spaceCard,
                                new ArrayList<ChannelDescriptor>()),
                        AALSpaceMessageTypes.LEAVE);
                // ...and wraps it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                Log.d(TAG, "Sending Leave Request message..." );
                communicationModule
                        .send(channelMessage, this, spaceCoordinator);
                Log.d(TAG, "Leave Request message sent." );

            }
        } catch (CommunicationConnectorException e) {
            Log.e(TAG, "Error during the unicast send: " ,e);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void requestToLeave(AALSpaceDescriptor spaceDescriptor) {
        try {
            if (init()) {
                Log.d(TAG, "Creating a new AALSpaceMessage" );

                // prepare the AALSpace Message
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        spaceDescriptor, AALSpaceMessageTypes.REQUEST_TO_LEAVE);
                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                Log.d(TAG,"Sending Leave Request message..." );
                communicationModule.sendAll(channelMessage, this);
                Log.d(TAG, "Leave Request message sent." );

            }
        } catch (CommunicationConnectorException e) {
            Log.e(TAG, "Error during the unicast send: " ,e);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void requestPeerCard(AALSpaceDescriptor spaceDescriptor,
            String peerAddress) {
        try {
            if (init()) {
                Log.d(TAG, "Creating a new AALSpaceMessage" );

                // prepare the AALSpace Message
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        spaceDescriptor, AALSpaceMessageTypes.REQUEST_PEERCARD);
                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                Log.d(TAG, "Sending Request Peer Card message..." );
                communicationModule.send(channelMessage, this, new PeerCard(
                        peerAddress, PeerRole.PEER));
                Log.d(TAG,"Request Peer Card sent." );

            }
        } catch (CommunicationConnectorException e) {
            Log.e(TAG, "Error during the unicast send:" ,e);
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                    "Error during the unicast send: " + e);
        }

    }

    public void newAALSpacesFound(Set<AALSpaceCard> spaceCards) {
        if (spaceCards != null) {
            if (controlBoker != null)
                controlBoker.newAALSpaceFound(spaceCards);
            else
                Log.w(TAG,"NO control Broker found" );
        } else
            Log.w(TAG, "AALSpace card is null" );
    }

    public synchronized void joinAALSpace(PeerCard spaceCoordinator,
            AALSpaceCard spaceCard) {

        if (spaceCoordinator != null && spaceCard != null) {
            Log.d(TAG, "Peer: " + spaceCoordinator.toString()
                            + " is joining the spaceCard: "
                            + spaceCard.toString() );
            try {
                if (init()) {
                    Log.d(TAG, "Creating a new AALSpaceMessage" );
                    // prepare the AALSpace Message

                    AALSpaceDescriptor spaceDesc = new AALSpaceDescriptor(
                            spaceCard, new ArrayList<ChannelDescriptor>());
                    AALSpaceMessage spaceMessage = new AALSpaceMessage(
                            spaceDesc, AALSpaceMessageTypes.JOIN_REQUEST);

                    // ...and wrap it as ChannelMessage
                    List<String> channelName = new ArrayList<String>();
                    channelName.add(getBrokerName());
                    ChannelMessage channelMessage = new ChannelMessage(
                            controlBoker.getmyPeerCard(),
                            spaceMessage.toString(), channelName);

                    Log.d(TAG, "Sending Join Request message..." );
                    communicationModule.send(channelMessage, this,
                            spaceCoordinator);
                    Log.d(TAG, "Join Request message sent." );

                }
            } catch (CommunicationConnectorException e) {
                Log.e(TAG, "Error during the unicast send: " ,e);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.AALSPACE_JOIN_ERROR,
                        "Error during the unicast send: " + e);
            }
        }
        // invalid parameters
        else {
            Log.e(TAG, "PeerCard and/or SpaceCard are null" );
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_JOIN_WRONG_PARAMETERS,
                    "PeerCard and/or SpaceCard are null");
        }

    }

    public void addPeer(AALSpaceDescriptor spaceDescriptor, PeerCard newPeer) {
        if (spaceDescriptor != null && newPeer != null) {
            Log.d(TAG, "Peer: " + newPeer.toString()
                            + " is joining the spaceCard: "
                            + spaceDescriptor.toString() );
            try {
                if (init()) {
                    Log.d(TAG, "Creating a new AALSpaceMessage" );
                    // prepare the AALSpace Message

                    AALSpaceMessage spaceMessage = new AALSpaceMessage(
                            spaceDescriptor, AALSpaceMessageTypes.JOIN_RESPONSE);

                    // ...and wrap it as ChannelMessage
                    List<String> channelName = new ArrayList<String>();
                    channelName.add(getBrokerName());
                    ChannelMessage channelMessage = new ChannelMessage(
                            controlBoker.getmyPeerCard(),
                            spaceMessage.toString(), channelName);

                    Log.d(TAG, "Sending Join Request message..." );
                    communicationModule.send(channelMessage, this, newPeer);
                    Log.d(TAG, "Join Request message queued." );

                }
            } catch (CommunicationConnectorException e) {
                Log.e(TAG, "Error during the unicast send: " ,e);
                throw new AALSpaceModuleException(
                        AALSpaceModuleErrorCode.AALSPACE_JOIN_ERROR,
                        "Error during the unicast send: " + e);
            }
        }
        // invalid parameters
        else {
            Log.w(TAG, "PeerCard and/or SpaceCard are null" );
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_JOIN_WRONG_PARAMETERS,
                    "PeerCard and/or SpaceCard are null");
        }

    }

    public void announceNewPeer(AALSpaceCard spaceCard, PeerCard peerCard) {
        if (spaceCard != null && peerCard != null) {
            Log.d(TAG, "Announcing new Peer: "
                            + peerCard.toString() + " into the AASpace: "
                            + spaceCard.toString() );

            AALSpaceMessage spaceMessage = new AALSpaceMessage(
                    new AALSpaceDescriptor(spaceCard,
                            new ArrayList<ChannelDescriptor>()),
                    AALSpaceMessageTypes.NEW_PEER);

            // ...and wrap it as ChannelMessage
            List<String> channelName = new ArrayList<String>();
            channelName.add(getBrokerName());
            ChannelMessage channelMessage = new ChannelMessage(
                    controlBoker.getmyPeerCard(), spaceMessage.toString(),
                    channelName);

            Log.d(TAG, "Sending New Peer added message..." );
            communicationModule.sendAll(channelMessage, this);
            Log.d(TAG, "New Peer added message queued" );

        } else {
            Log.w(TAG, "Event propagation failed! PeerCard and/or AALSpaceCard are not valid" );
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.AALSPACE_NEW_PEER_ADDED_ERROR,
                    "Event propagation failed! PeerCard and/or AALSpaceCard are not valid");
        }

    }

    public void messageFromSpace(AALSpaceMessage message, PeerCard sender)
            throws AALSpaceModuleException {
        Log.d(TAG, "AALSpaceMessage arrived...queuing" );
        try {
            AALSpaceMessageTypes messageType = message.getMessageType();
            switch (messageType) {
            case JOIN_REQUEST: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() );
                    return;
                }
                Log.d(TAG, "Join request: "
                                + message.toString() );
                controlBoker.joinRequest(message.getSpaceDescriptor()
                        .getSpaceCard(), sender);

            }
                break;
            case JOIN_RESPONSE: {

                Log.d(TAG, "Join response: "
                                + message.toString() );
                if (message.getSpaceDescriptor() != null) {
                    controlBoker.aalSpaceJoined(message.getSpaceDescriptor());
                } else {
                    Log.d(TAG,"The Join Response is not valid" );
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_JOIN_RESPONSE_WRONG_PARAMETERS,
                            "The Join Response is not valid");
                }

            }
                break;
            case NEW_PEER: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() );
                    return;
                }
                Log.d(TAG, "New Peer added: "
                                + message.toString() );
                if (sender != null) {
                    controlBoker.peerFound(sender);
                } else {
                    Log.d(TAG,"The New Peer added has not a valid PeerCard" );
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_NEW_PEER_ERROR,
                            "The New Peer added has not a valid PeerCard");
                }

            }
                break;
            case LEAVE: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() );
                    return;
                }
                Log.d(TAG, "Leave request: "
                                + message.toString() );
                if (sender != null) {
                    controlBoker.peerLost(sender);
                } else {
                    Log.d(TAG, "Leaving Peer  has not a valid PeerCard" );
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                            "The leaving Peer has not a valid PeerCard");
                }
            }
                break;
            case REQUEST_TO_LEAVE: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() );
                    return;
                }
                Log.d(TAG, "Request to leave: "
                                + message.toString() );
                if (message.getSpaceDescriptor() != null) {
                    controlBoker.leaveRequest(message.getSpaceDescriptor());
                } else {
                    Log.d(TAG, "Not a valid AALSpace descriptor" );
                    throw new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.AALSPACE_LEAVE_ERROR,
                            "No valid space descritpro");
                }

            }
                break;
            case REQUEST_PEERCARD: {
                Log.d(TAG, "Request PeerCard: "
                                + message.toString() );
                // check if everything is correct
                if (controlBoker.getmyAALSpaceDescriptor() == null) {
                    Log.d(TAG, "AALSpaceDescritor not yet ready.. "
                                    + message.toString() );
                    return;

                }

                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Not part of the AALSpace requester... "
                                    + message.toString() );
                    return;

                }

                // send my PeerCard
                AALSpaceMessage spaceMessage = new AALSpaceMessage(
                        controlBoker.getmyAALSpaceDescriptor(),
                        AALSpaceMessageTypes.PEERCARD);

                // ...and wrap it as ChannelMessage
                List<String> channelName = new ArrayList<String>();
                channelName.add(getBrokerName());
                ChannelMessage channelMessage = new ChannelMessage(
                        controlBoker.getmyPeerCard(), spaceMessage.toString(),
                        channelName);

                Log.d(TAG, "Sending Peer Card ..." );
                communicationModule.send(channelMessage, this, sender);
                Log.d(TAG, "Peer Card message sent." );
            }
                break;
            case PEERCARD: {
                // check if the peer belongs to by AALSpace
                if (!message
                        .getSpaceDescriptor()
                        .getSpaceCard()
                        .getSpaceID()
                        .equals(controlBoker.getmyAALSpaceDescriptor()
                                .getSpaceCard().getSpaceID())) {
                    Log.d(TAG, "Peer is not part of my AALSpace..dropping it"
                                    + message.toString() );
                    return;
                }
                Log.d(TAG, "PeerCard received: "
                                + message.toString() );
                // check if it is part of my aalspacde

                controlBoker.peerFound(sender);

            }
            default:
                break;
            }

        } catch (ClassCastException e) {
            throw new AALSpaceModuleException(
                    AALSpaceModuleErrorCode.ERROR_MANAGING_AALSPACE_MESSAGE,
                    "The message body is not valid: " + e);
        }

        Log.d(TAG, "AALSpaceMessage queued" );
    }

    public void configureAALSpaceChannel(String group) {
        Log.d(TAG, "Setting the broker group for the AALSpaceModule..."
                        + group );
        this.brokerName = group;
        /*
         * Register me as MessageListener for messages to the channel associated
         * to my broker group
         */
        communicationModule.addMessageListener(this, group);

    }

    public void messageReceived(ChannelMessage message) {
        if (message == null /*&&*/|| message.getContent() == null) {
            Log.d(TAG, "The message received is not valid...dropping it." );
        } else {

            try {
                JSONObject obj = new JSONObject(message.getContent());
                BrokerMessageTypes mtype = BrokerMessageTypes.valueOf(obj
                        .getString(BrokerMessageFields.BROKER_MESSAGE_TYPE));
                switch (mtype) {
                case AALSpaceMessage:
                    AALSpaceMessage spaceMessage = (AALSpaceMessage) unmarshall(message
                            .getContent());
                    Log.d(TAG, "AALSpace message arrived" );
                    messageFromSpace(spaceMessage, message.getSender());
                    break;
                default:
                    Log.e(TAG, "The message received is not valid...dropping it." );
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error during message receive: "
                                + e.toString() );
            }

        }

    }

    public void handleSendError(ChannelMessage message,
            CommunicationConnectorException exception)
            throws AALSpaceModuleException {
        try {
            Log.w(TAG, "Error during message queuing for message: "
                            + message.toString() );
            AALSpaceModuleException spaceException;

            if (message != null && message.getContent() != null) {
                // initialize the exception and throw it
                AALSpaceMessage aMessage = (AALSpaceMessage) unmarshall(message
                        .getContent());
                switch (aMessage.getMessageType()) {
                case JOIN_REQUEST:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_JOIN_REQUEST,
                            exception.toString());
                    break;
                case JOIN_RESPONSE:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_JOIN_RESPONSE,
                            exception.toString());
                    break;
                case NEW_PEER:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_SENDING_NEW_PEER_ADDED,
                            exception.toString());
                    break;
                default:
                    spaceException = new AALSpaceModuleException(
                            AALSpaceModuleErrorCode.ERROR_MANAGING_AALSPACE_MESSAGE,
                            exception.toString());
                    break;
                }
                throw spaceException;
            }
        } catch (AALSpaceMessageException e) {
            Log.e(TAG, "The message received is not valid...dropping it."
                            + e.toString() );
        } catch (Exception e2) {

            Log.e(TAG,"The message received is not valid...dropping it."
                            + e2.toString() );
        }
    }

    public void destroyAALSpace(AALSpaceCard spaceCard) {
        Log.d(TAG, "Destroy the AALSpace: "
                        + spaceCard.toString() );
        // to de-register the AALSpace
        /*for (DiscoveryConnector dConnector : discoveryConnectors) {*/
            try {
            	wrapDiscovery.deregisterAALSpace(spaceCard);
//                dConnector.deregisterAALSpace(spaceCard);
            } catch (Exception e) {
                Log.e(TAG, "Error during destroy AALSpace: "
                                + e.toString() );
            }
        /*}*/

    }

    public void aalSpaceLost(AALSpaceCard spaceCard) {
        // TODO Auto-generated method stub

    }

    public void sharedObjectAdded(Object arg0, Object arg1) {
        if (arg0 != null) {
            if (arg0 instanceof DiscoveryConnector) {// TODO Not really going to happen
                /*DiscoveryConnector connector = (DiscoveryConnector) arg0;
                // check if I already have the same connector
                if (!discoveryConnectors.contains(connector)) {*/
//                    connector.addAALSpaceListener(this);
                    wrapDiscovery.addAALSpaceListener(this);
                /*    discoveryConnectors.add(connector);
                }*/
            } else if (arg0 instanceof CommunicationModule) {
                Log.d(TAG, "New CommunicationModule added..." );
                communicationModule = (CommunicationModule) arg0;
            } else if (arg0 instanceof AndroidControlBroker) {
                Log.d(TAG, "ControkBroker added..." );
                controlBoker = (AndroidControlBroker) arg0;
            }
        }
    }

    public void sharedObjectRemoved(Object arg0) {
        if (arg0 != null) {
            /*if (arg0 instanceof DiscoveryConnector) {
                DiscoveryConnector connector = (DiscoveryConnector) arg0;
                LogUtils.logDebug(context, AndroidAALSpaceModuleImpl.class,
                        "AALSpaceModuleImpl",
                        new Object[] { "Removing the DiscoveryConnector" },
                        null);
                discoveryConnectors.remove(connector);
                if (discoveryConnectors.size() == 0) {
                    initialized = false;
                }
            } else*/ if (arg0 instanceof CommunicationModule) {
                Log.d(TAG, "CommunicationModule removed..." );
                communicationModule = null;
                initialized = false;
            } else if (arg0 instanceof AndroidControlBroker) {
                Log.d(TAG, "ControlBroker removed..." );
                controlBoker = null;
                initialized = false;
            }
        }

    }

    public String getBrokerName() {
        return context.getID();
    }

    public void dispose() {
        // remove me as listener
        context.getContainer().removeSharedObjectListener(this);
        if (communicationModule != null)
            communicationModule.removeMessageListener(this, brokerName);
        /*if (discoveryConnectors != null && discoveryConnectors.size() > 0) {
            for (DiscoveryConnector dConnector : discoveryConnectors) {*/
        wrapDiscovery.removeAALSpaceListener(this);
//                dConnector.removeAALSpaceListener(this);
        /*    }
        }*/

    }

    public List<String> getPeersAddress() {
        if (communicationModule instanceof ConfigurableCommunicationModule) {
            ConfigurableCommunicationModule cCommMode = (ConfigurableCommunicationModule) communicationModule;
//            Map<String, PeerCard> checkedPeer = new HashMap<String, PeerCard>();
            List<String> members = cCommMode.getGroupMembers(brokerName);
            return members;
        }
        return null;
    }

    public void renewAALSpace(AALSpaceCard spaceCard) {
        if (spaceCard != null) {
            /*for (DiscoveryConnector discoveryConnector : discoveryConnectors) {*/
                try {
                	wrapDiscovery.announceAALSpace(spaceCard);
//                    discoveryConnector.announceAALSpace(spaceCard);
                } catch (DiscoveryConnectorException e) {
                    Log.e(TAG, "error during AALSpace renew: "
                                    + spaceCard.toString(),e);
                }
            /*}*/
        }

    }

    public BrokerMessage unmarshall(String message) {
        try {

            JSONObject obj = new JSONObject(message);

            // check if the message can be serialized
            BrokerMessageTypes mtype = BrokerMessageTypes.valueOf(obj
                    .getString(BrokerMessageFields.BROKER_MESSAGE_TYPE));
            if (!mtype.equals(BrokerMessageTypes.AALSpaceMessage)) {
                throw new AALSpaceMessageException(
                        "Cannot unmarshall non-AALSpaceMessage instances: "
                                + message);
            }

            // unmarshall message type
            AALSpaceMessageTypes aalspaceMessageType = AALSpaceMessageTypes
                    .valueOf(obj
                            .getString(AALSpaceMessageFields.AAL_SPACE_MTYPE));

            // unmarshall AALSpaceCard
            AALSpaceCard aalspaceCard = new AALSpaceCard();
            aalspaceCard.setAalSpaceLifeTime(obj
                    .getInt(AALSpaceMessageFields.aalSpaceLifeTime));
            aalspaceCard.setCoordinatorID(obj
                    .getString(AALSpaceMessageFields.peerCoordinatorID));
            aalspaceCard.setDescription(obj
                    .getString(AALSpaceMessageFields.description));
            aalspaceCard.setPeeringChannel(obj
                    .getString(AALSpaceMessageFields.peeringChannel));
            aalspaceCard.setPeeringChannelName(obj
                    .getString(AALSpaceMessageFields.peeringChannelName));
            aalspaceCard.setRetry(obj.getInt(AALSpaceMessageFields.retry));
            aalspaceCard.setSpaceID(obj
                    .getString(AALSpaceMessageFields.spaceID));
            aalspaceCard.setSpaceName(obj
                    .getString(AALSpaceMessageFields.AAL_SPACE_NAME));

            // unmarshall broker channels
            JSONArray brokerChannelsUnserial = obj
                    .getJSONArray(AALSpaceMessageFields.brokerChannels);
            List<ChannelDescriptor> brokerChannels = new ArrayList<ChannelDescriptor>();
            for (int i = 0; i < brokerChannelsUnserial.length(); i++) {
                JSONArray channelSerial = brokerChannelsUnserial
                        .getJSONArray(i);
                ChannelDescriptor desc = new ChannelDescriptor(
                        channelSerial.getString(0), channelSerial.getString(1),
                        channelSerial.getString(2));
                brokerChannels.add(desc);
            }

            // unmarhall the DeployManager's PeerCard
            PeerCard dpM = null;
            if (!obj.isNull(AALSpaceMessageFields.DEPLOY_MANAGER_ID)) {
                dpM = new PeerCard(
                        obj.getString(AALSpaceMessageFields.DEPLOY_MANAGER_ID),
                        PeerRole.valueOf(obj
                                .getString(AALSpaceMessageFields.DEPLOY_MANAGER_ROLE)));
            }

            // build AALSpaceDescriptor
            AALSpaceDescriptor aalSpaceDescriptor = new AALSpaceDescriptor(
                    aalspaceCard, brokerChannels);
            if (dpM != null)
                aalSpaceDescriptor.setDeployManager(dpM);
            AALSpaceMessage aalspaceMessage = new AALSpaceMessage(
                    aalSpaceDescriptor, aalspaceMessageType);

            return aalspaceMessage;

        } catch (JSONException e) {

            throw new AALSpaceMessageException(
                    "Unable to unmashall AALSpaceMessage. Original message: "
                            + message + ". Full Stack: " + e.toString());
        } catch (Exception e) {
            throw new AALSpaceMessageException(
                    "Unable to unmashall AALSpaceMessage. Original message: "
                            + message + ". Full Stack: " + e.toString());
        }
    }

}
