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
package org.universAAL.middleware.android.connectors.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.util.Util;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
import org.universAAL.middleware.connectors.CommunicationConnector;
import org.universAAL.middleware.connectors.communication.jgroups.util.Codec;
import org.universAAL.middleware.connectors.communication.jgroups.util.Consts;
import org.universAAL.middleware.connectors.communication.jgroups.util.CryptUtil;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorErrorCode;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.connectors.util.ExceptionUtils;
//import org.universAAL.middleware.container.ModuleContext;
//import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Context;
import android.util.Log;

/**
 * JGroup communication connector implementation
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 * @author <a href="mailto:filippo.palumbo@isti.cnr.it">Filippo Palumbo</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 */
public class AndroidJGroupsCommunicationConnector implements CommunicationConnector,
        Receiver, RequestHandler, MembershipListener {
	
	private final static String TAG = "AndroidJGroupsCommunicationConnector";
    private String name;
    private String version;
    private String description;
    private String provider;
    private boolean enableRemoteChannelConfigurarion;
//    private CommunicationModule communicationModule;
    // maps the channel name with the channel instance
    // The channel name is in the XXX.space configuration file (ex. Home.space)
    private final Map<String, JChannel> channelMap = new HashMap<String, JChannel>();
    private MessageDispatcher disp;
    // Security stuff
    private boolean security = false;
    private String key;
    private String enableRemoteChannelURL = null;
	private ModulesCommWrapper wrapperCommunication;

    public AndroidJGroupsCommunicationConnector(ModulesCommWrapper wrapper)
            /*throws Exception*/ {
        wrapperCommunication = wrapper;

        security = Boolean.parseBoolean(System.getProperty(
                "universaal.security.enabled", "false"));

        if (security == false) {
            return;
        } else {
            try {
				initializeSecurity();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    }

    private void initializeSecurity() throws Exception {
        final String METHOD = "initializeSecurity";
        Log.d(TAG, "Security enabled" + security);
        String fileName = System.getProperty("bouncycastle.key");
        Log.d(TAG, "Security key file : "+ fileName +" bouncycastle.key" + security);
        File file = new File(fileName, "bouncycastle.key");
        boolean exists = file.exists();
        if (!exists) {
            // disable security
        	Log.w(TAG, "Security disabled, key file not found");
            throw new Exception("Security disabled. Key file not found.");
        } else {
            // It returns true if File or directory exists
            // init the cryptoUtil
            try {
                CryptUtil.init(fileName, new Codec() {
                    public byte[] encode(byte[] data) {
                        return org.bouncycastle.util.encoders.Base64
                                .encode(data);
                    }

                    public byte[] decode(String data) {
                        return org.bouncycastle.util.encoders.Base64
                                .decode(data);
                    }
                });
            } catch (Exception ex) {
            	Log.e(TAG, "Error while initializing the CryptoUtil: ",ex);
                throw new Exception("Security disabled. Key file not found.");
            }
            Log.d(TAG, "Connector Up");
        }
    }

    public void configureConnector(List<ChannelDescriptor> channels,
            String peerName) throws CommunicationConnectorException {
    	Log.d(TAG, "Configuring the JChannel and the ReceiverAdapter...");
/*        communicationModule = (CommunicationModule) context.getContainer()
                .fetchSharedObject(context,
                        new Object[] { CommunicationModule.class.getName() });*/
        for (final ChannelDescriptor element : channels) {
            try {
                JChannel ch = null;
                if (channelMap.containsKey(element.getChannelName())) {
					Log.w(TAG, "The channel: " + element.getChannelName()
							+ " is already configured");
                } else {
                    try {
                        ch = configureJChannel(element);
                    } catch (Exception e) {
                    	Log.e(TAG, "Error configuringing the JChannel",e);
                        throw new CommunicationConnectorException(
                                CommunicationConnectorErrorCode.NEW_CHANNEL_ERROR,
                                "Error configuringing the JChannel: " + e);

                    }

                    if (ch != null) {
                        ch.setDiscardOwnMessages(true);
                        ch.setReceiver(this);
                        // nome logico del peer che esegue la join. Il nome
                        // logico associato ad un UUID

                        ch.setName(peerName);
                        // ch.setAddressGenerator(new AddressGenerator() {
                        // public Address generateAddress() {
                        // return PayloadUUID.randomUUID("prova");
                        // }
                        // });
                        ch.connect(element.getChannelName());
                        // associates the channel name with the channel instance
                        channelMap.put(element.getChannelName(), ch);
                    }
                }
            } catch (Exception e) {
            	Log.e(TAG, "Error configuringing the JChannel and the ReceiverAdapter: ",e);
                throw new CommunicationConnectorException(
                        CommunicationConnectorErrorCode.NEW_CHANNEL_ERROR,
                        "Error configuringing the JChannel and the ReceiverAdapter: "
                                + e);
            }
        }
        Log.d(TAG, "JChannel and ReceiverAdapter configured.");
    }

    /**
     * Strategy for initializing the jGroups Communication channel: -if
     * enableRemoteChannelConfiguration is true -> init jGroups channel with
     * default constructor -if enableRemoteChannelConfiguration is false and and
     * URL for jGroups conf. channel is provided -> init with URL -if
     * enableRemoteChannelConfiguration is false and a configuration XML is
     * provided -> init with XML
     *
     * @param element
     * @return
     * @throws Exception
     */
    private JChannel configureJChannel(ChannelDescriptor element)
            throws Exception {
        JChannel ch = null;
        if (enableRemoteChannelConfigurarion == false) {
			Log.d(TAG,
					"Remote channel configuration disabled using default JGroup cluster configuration for channel "
							+ element.getChannelName());
            return new JChannel();
        }
        URL urlConfig = null;
        if (enableRemoteChannelConfigurarion && enableRemoteChannelURL != null) {
            urlConfig = new URL(enableRemoteChannelURL);
        }
        if (enableRemoteChannelConfigurarion && urlConfig == null
                && element.getChannelDescriptorFileURL() != null) {
            urlConfig = new URL(element.getChannelDescriptorFileURL());
        }
        // Set up the jChannel from the URL or the value
        if (urlConfig != null) {
            try {
                ch = createSharedChannel(urlConfig);
            } catch (Exception e) {
				Log.i(TAG,
						"Failed to load remote configuration for "
								+ element.getChannelName()
								+ " from URL -> "
								+ urlConfig
								+ " due to internal exception "
								+ ExceptionUtils.stackTraceAsString(e)
								+ "\n Trying to initializee the channels locally");
			}
        }
        if (enableRemoteChannelConfigurarion && ch == null
                && element.getChannelValue() == null) {
			Log.i(TAG, "No local configuration for " + element.getChannelName()
					+ "\nFalling back to JGroup default cluster configuration");
			return new JChannel();
        }
        if (enableRemoteChannelConfigurarion && ch == null
                && element.getChannelValue() != null) {
            // Try from the InputStream
            InputStream channelValue = new ByteArrayInputStream(element
                    .getChannelValue().getBytes());
            try {
                ch = createSharedChannel(channelValue);
            } catch (Exception e) {
				Log.e(TAG,
						"Failed to load local configuration for "
								+ element.getChannelName()
								+ " due to internal exception "
								+ ExceptionUtils.stackTraceAsString(e)
								+ "\n Falling back to JGroup default cluster configuration");
			}

        }
        if (ch != null) {
            return ch;
        } else {
            throw new CommunicationConnectorException(
                    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
                    "Unable to load channel configuration from anysource");
        }
    }

    public void dispose(List<ChannelDescriptor> channels) {
    	Log.d(TAG,"Reset the JGroupCommunicationConnector...");
        try {
            if (channelMap != null) {
                for (ChannelDescriptor channel : channels) {
                    Util.close(channelMap.get(channel.getChannelName()));
                }
            }
        } catch (Exception e) {
        	Log.e(TAG,"Error while resetting the Communication connector: ",e);
        } finally {
            Set<String> keys = channelMap.keySet();
            // check if to close some channels
            for (String key : keys) {
                if (channelMap.get(key).isOpen()) {
                    Util.close(channelMap.get(key));
                }
            }
            for (ChannelDescriptor channelDesc : channels) {
                channelMap.remove(channelDesc.getChannelName());
            }
        }
        Log.d(TAG,"JGroupCommunicationConnector reset");

    }

    public void dispose() {
    	Log.d(TAG,"Reset the JGroupCommunicationConnector...");
        try {
            if (channelMap != null) {
                Set<String> keys = channelMap.keySet();
                for (String key : keys) {
                    Util.close(channelMap.get(key));
                }
            }
        } catch (Exception e) {
        	Log.e(TAG,"Error while resetting the Communication connector: ",e);
        } finally {
            channelMap.clear();
        }
        Log.d(TAG,"JGroupCommunicationConnector reset");
    }

    private JChannel createSharedChannel(URL channelURL) throws Exception {
        try {
            ProtocolStackConfigurator config = ConfiguratorFactory
                    .getStackConfigurator(channelURL);
            List<ProtocolConfiguration> protocols = config.getProtocolStack();
            // ProtocolConfiguration transport = protocols.get(0);
            // transport.getProperties().put(Global.SINGLETON_NAME,
            // transport.getProtocolName());
            return new JChannel(config);
        } catch (Exception e) {
        	Log.e(TAG,"Unable to initialize the JGroup channel with URL: "+channelURL,e);
            throw new CommunicationConnectorException(
                    CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
                    "Unable to initialize the JGroup channel with URL: "
                            + channelURL, e);
        }

    }

    private JChannel createSharedChannel(InputStream channelValue)
            throws Exception {
        try {
            ProtocolStackConfigurator config = ConfiguratorFactory
                    .getStackConfigurator(channelValue);
            List<ProtocolConfiguration> protocols = config.getProtocolStack();
            // ProtocolConfiguration transport = protocols.get(0);
            // transport.getProperties().put(Global.SINGLETON_NAME,
            // transport.getProtocolName());
            return new JChannel(config);

        } catch (Exception e) {
			Log.e(TAG, "Unable to initialize the JGroup channel with URL: "
					+ channelValue.toString() + " -> ", e);
			throw new CommunicationConnectorException(
					CommunicationConnectorErrorCode.CHANNEL_INIT_ERROR,
					"Unable to initialize the JGroup channel with URL: "
							+ channelValue.toString() + " -> " + e.toString());
		}
    }

    /**
     * This method selects the channels to which to send the message
     *
     * @param message
     * @return
     */
    private List selectJChannels(List channelNames) {
        List selectedJChannels = new ArrayList();
        for (int i = 0; i < channelNames.size(); i++) {
            String channel = (String) channelNames.get(i);
            if (channelMap.containsKey(channel))
                selectedJChannels.add(channelMap.get(channel));
        }
        return selectedJChannels;
    }

    public synchronized void unicast(ChannelMessage message, String receiver)
            throws CommunicationConnectorException {
        final String METHOD = "unicast";
        if (message.getChannelNames() == null
                || message.getChannelNames().isEmpty()) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
                    "No channel name specified");
            return;
        }
        if (message.getChannelNames().size() > 1) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.MULTIPLE_RECEIVERS,
                    "Too much receivers specified for unicast");
            return;
        }
        // get the first and only channel
        String targetChannel = (String) message.getChannelNames().get(0);
        JChannel ch = channelMap.get(targetChannel);
        if (ch == null) {
            logAndThrowComExec(
                    METHOD,
                    CommunicationConnectorErrorCode.CHANNEL_NOT_FOUND,
                    "The channel name:"
                            + targetChannel
                            + " was not found. It is either not configured or it has been deleted");
            return;
        }
        View view = ch.getView();
        if (view == null) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.NOT_CONNECTED_TO_CHANNEL,
                    "Unable to get the View on the channel " + targetChannel
                            + " We may not be connected to it");
            return;
        }
        Address dst = null;
        final Message msg;
        /*
         * //FIX The android peer is not shown as member of the channel thus
         * joining fails
         */
        for (Address address : view.getMembers()) {
            if (receiver.equals(ch.getName(address))) {
                dst = address;
                break;
            }
        }
        if (dst == null) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.RECEIVER_NOT_EXISTS,
                    "Trying to send message to " + receiver
                            + " but it is not a memeber of " + ch.getName()
                            + "/" + ch.getClusterName());
            return;
        }
        if (security) {
            try {
                msg = new Message(dst, null, CryptUtil.encrypt(message
                        .toString()));
            } catch (Throwable t) {
                logAndThrowComExec(
                        METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Failed to encrypt the message due to internal exception",
                        t);
                return;
            }
        } else {
            msg = new Message(dst, null, message.toString());
        }

        try {
            ch.send(msg);
        } catch (Exception t) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                    "Error sending unicast message " + message
                            + " due to internal exception", t);
            return;
        }
    }

    private void logAndThrowComExec(String method,
            CommunicationConnectorErrorCode code, String msg, Throwable t) {
    	Log.e(TAG, msg,t);
        throw new CommunicationConnectorException(
                CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED, msg, t);

    }

    private void logAndThrowComExec(String method,
            CommunicationConnectorErrorCode code, String msg) {
    	Log.e(TAG, msg);
        throw new CommunicationConnectorException(
                CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED, msg);
    }

    public synchronized void multicast(ChannelMessage message)
            throws CommunicationConnectorException {
        final String METHOD = "multicast";

        if (message.getChannelNames() == null) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
                    "No channel name specified");
            return;
        }
        // send message to all brokers of any kind
        List selectedChannel = selectJChannels(message.getChannelNames());
        for (int i = 0; i < selectedChannel.size(); i++) {
            JChannel channel = (JChannel) selectedChannel.get(i);
            Message msg = null;
            try {
                if (security) {
                    msg = new Message(null, null, CryptUtil.encrypt(message
                            .toString()));
                } else {
                    Serializable aux = (Serializable) message.toString();
                    msg = new Message(null, null, aux);
                }
            } catch (Throwable e) {
                logAndThrowComExec(METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Error during cretaion of multicast message", e);
                return;
            }
            try {
                channel.send(msg);
            } catch (Throwable e) {
                logAndThrowComExec(METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Sending broadcast message " + msg.toString(), e);
                return;
            }
        }

    }

    public synchronized void multicast(ChannelMessage message,
            List<PeerCard> receivers) throws CommunicationConnectorException {
        final String METHOD = "multicast";
        final List channels = message.getChannelNames();
        if (channels == null) {
            logAndThrowComExec(METHOD,
                    CommunicationConnectorErrorCode.NO_CHANNEL_SPECIFIED,
                    "No channel name specified");
            return;
        }

        List selectedChannel = selectJChannels(channels);

        if (selectedChannel == null || selectedChannel.isEmpty()) {
            logAndThrowComExec(
                    METHOD,
                    CommunicationConnectorErrorCode.CHANNEL_NOT_FOUND,
                    "No destination channel found among the list :"
                            + Arrays.toString(channels.toArray())
                            + "They were either not configured or deleted");
            return;
        }

        // TODO Add log message or error if some of the destination does not
        // exists on the channels

        // Send the message to the selected channels and selected
        // receivers
        for (int i = 0; i < selectedChannel.size(); i++) {
            JChannel channel = (JChannel) selectedChannel.get(i);
            Message msg = null;
            View view = channel.getView();
            List<Address> list = view.getMembers();

            // creation of the list of address to be excluded from
            // broadcast
            List<String> removeList = new ArrayList<String>();
            for (Address address : list) {
                removeList.add(channel.getName(address));
            }
            List<String> nodeIDsAsString = new ArrayList<String>();
            for (PeerCard nodeID : receivers) {
                nodeIDsAsString.add(nodeID.getPeerID());
            }
            removeList.removeAll(nodeIDsAsString);
            List<Address> removeAddressList = new ArrayList<Address>();
            for (Address address : list) {
                for (String removeAdressString : removeList) {
                    if (channel.getName(address).equals(removeAdressString))
                        removeAddressList.add(address);
                }
            }
            //
            if (security) {
                try {
                    msg = new Message(null, null, CryptUtil.encrypt(message
                            .toString()));
                } catch (Throwable t) {
                    logAndThrowComExec(
                            METHOD,
                            CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                            "Failed to encrypt the message due to internal exception",
                            t);
                    return;
                }
            } else {
                msg = new Message(null, null, message.toString());
            }

            RequestOptions opts = new RequestOptions();
            opts.setExclusionList((Address[]) removeAddressList.toArray());
            disp = new MessageDispatcher(channel, null, null, this);

            try {
                disp.sendMessage(msg, opts);
            } catch (Throwable e) {
                logAndThrowComExec(
                        METHOD,
                        CommunicationConnectorErrorCode.SEND_MESSAGE_ERROR,
                        "Unable to broadcast the message:" + message.toString(),
                        e);
                return;
            }
        }

    }

    public void loadConfigurations(Dictionary configurations) {
        Log.d(TAG, "updating JGroups Connector properties");
        if (configurations == null) {
            // TODO We should reset the configuration to the default properties
        	Log.d(TAG, "JGroups Connector properties are null");
            return;
        }
        try {
            this.name = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_NAME);
            this.version = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_VERSION);
            this.description = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_DESCRIPTION);
            this.provider = (String) configurations
                    .get(org.universAAL.middleware.connectors.util.Consts.CONNECTOR_PROVIDER);
            this.enableRemoteChannelConfigurarion = Boolean
                    .valueOf((String) configurations
                            .get(Consts.ENABLE_REMOTE_CHANNEL_CONFIG));
            this.enableRemoteChannelURL = (String) configurations
                    .get(Consts.ENABLE_REMOTE_CHANNEL_URL_CONFIG);
        } catch (Throwable t) {
        	Log.e(TAG, "Error during JGroups properties update",t);
        }
        Log.d(TAG, "JGroups Connector properties updated");
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void receive(Message msg) {
        try {
            /*JGroups 2.2 only
            if (msg.isFlagSet(Flag.INTERNAL)) {
                LogUtils.logWarn(context, JGroupsCommunicationConnector.class,
                        METHOD, "Skipping internal JGroups packet");
                return;
            }*/
            String msgBuffer = new String(msg.getBuffer());
            if (security) {
                msgBuffer = CryptUtil.decrypt((String) msg.getObject());
            }
            ChannelMessage channelMessage = ChannelMessage.unmarhall(msgBuffer);
//            communicationModule.messageReceived(channelMessage);
            wrapperCommunication.messageReceived(channelMessage);
        } catch (Exception ex) {
        	Log.e(TAG, "Failed to unmarhall message due to exception ",ex);
        }
    }

    public void getState(OutputStream output) throws Exception {
        // TODO Auto-generated method stub

    }

    public void setState(InputStream input) throws Exception {
        // TODO Auto-generated method stub

    }

    public void block() {
        // TODO Auto-generated method stub

    }

    public void suspect(Address suspectedMbr) {

    }

    public void unblock() {
        // TODO Auto-generated method stub

    }

    public void viewAccepted(View newView) {

    }

    public String toString() {
        return "Name: " + this.name + " Version: " + this.version
                + " Description: " + this.description + " Provider: "
                + this.provider;
    }

    public Object handle(Message msg) throws Exception {
        ChannelMessage channelMessage = ChannelMessage.unmarhall(new String(msg
                .getBuffer()));
        wrapperCommunication.messageReceived(channelMessage);
        return null;
    }

    public boolean init() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<String> getGroupMembers(String groupName) {
        List<String> members = new ArrayList<String>();
        if (channelMap.get(groupName) != null
                && channelMap.get(groupName).getView() != null) {
            List<Address> addresses = channelMap.get(groupName).getView()
                    .getMembers();
            for (Address address : addresses) {
                members.add(channelMap.get(groupName).getName(address));
            }
        }
        return members;

    }

    public boolean hasChannel(String channelName) {
        return channelMap.containsKey(channelName);
    }
    
	public byte[] getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setState(byte[] arg0) {
		// TODO Auto-generated method stub
		InputStream in = new ByteArrayInputStream(arg0);
		try {
			setState(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
