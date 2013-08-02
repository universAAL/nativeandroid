/*
        Coyright 2007-2014 CNR-ISTI, http://isti.cnr.it
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
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.universAAL.middleware.android.connectors.ConnectorCommWrapper;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.modules.CommunicationModule;
import org.universAAL.middleware.modules.ConfigurableCommunicationModule;
import org.universAAL.middleware.modules.exception.CommunicationModuleErrorCode;
import org.universAAL.middleware.modules.exception.CommunicationModuleException;
import org.universAAL.middleware.modules.listener.MessageListener;

import android.content.Context;
import android.util.Log;

/**
 * CommunicationModule implementation
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AndroidCommunicationModuleImpl implements CommunicationModule,
        ConfigurableCommunicationModule, 
        RejectedExecutionHandler {
	private static final String TAG = "AndroidCommunicationModuleImpl";
    // Module properties
    private String name;
    private String provider;
    private String version;
    private String description;
//    private ModuleContext context;
//    private CommunicationConnector communicationConnector;
//    private AALSpaceModule aalSpaceModule;
    private ConcurrentMap<String, List<MessageListener>> messageListeners;
    private boolean initialized = false;

    // ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> messageQueue;
	private ConnectorCommWrapper wrapperConnector;
	private Context androidContext;
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_TIME = 10;

    /**
     * This method configures the CommunicationModule: -to obtain the reference
     * to all the CommunicationConnector present in the fw
     */
    public boolean init() {

        if (!initialized) {

            messageQueue = new LinkedBlockingQueue<Runnable>();
            // this.executor = new
            // ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,
            // KEEP_ALIVE_TIME, TimeUnit.SECONDS,messageQueue, this);
            // test michele
            // executor.allowCoreThreadTimeOut(true);

            messageListeners = new ConcurrentHashMap<String, List<MessageListener>>();
            Log.d(TAG,  "Configuring the CommunicationModule...");
            try {
            	Log.d(TAG,  "Fetching the CommunicationConnector...");
            } catch (NullPointerException e) {
            	Log.e(TAG,  "Error while fetching the CommunicationConnector",e);
                initialized = false;
                return initialized;
            }
        }
        initialized=true;//
        return initialized;
    }

    public void dispose(List<ChannelDescriptor> channels) {
    	wrapperConnector.dispose(channels);
    }

    public void dispose() {
    }

    public AndroidCommunicationModuleImpl(ConnectorCommWrapper wrapper, Context ctxt) {
    	wrapperConnector=wrapper;
    	androidContext=ctxt;
    	//Added androidContext for a cheat workaround for the buses
    }

    /**
     * This method returns the list of MessageListener given the list of
     * channelNames
     *
     * @param channelNames
     * @return
     */
    private List<MessageListener> getMessageListeners(List<String> channelNames) {

        List<MessageListener> listeners = new ArrayList<MessageListener>();
        for (String channelName : channelNames) {
            if (messageListeners.containsKey(channelName))
                listeners.addAll(messageListeners.get(channelName));
        }
        return listeners;

    }

    public void messageReceived(ChannelMessage channelMessage) {
        try {
            List<MessageListener> listeners = getMessageListeners(channelMessage
                    .getChannelNames());
            if (listeners != null && !listeners.isEmpty()) {
            	Log.d(TAG,  "Dispatching the message to the brokers: "
                      + channelMessage.toString());
                ListIterator<MessageListener> iterator = listeners
                        .listIterator();
                while (iterator.hasNext()) {
                    iterator.next().messageReceived(channelMessage);
                }

            }
        } catch (NullPointerException e) {
        	Log.e(TAG,  "Error during message reception: ",e);
            throw new CommunicationModuleException(
                    CommunicationModuleErrorCode.ERROR_MESSAGE_RECEPTION,
                    "Error during message reception: "
                            + channelMessage.toString());
        }

    }

    public void addMessageListener(MessageListener listener, String channelName) {
        if (listener != null && channelName.length()>0) {
            if (messageListeners.get(channelName) == null) {
                List<MessageListener> list = new CopyOnWriteArrayList<MessageListener>();

                list.add(listener);
                messageListeners.put(channelName, list);
            } else {
                if (!messageListeners.get(channelName).contains(listener))
                    messageListeners.get(channelName).add(listener);
            }

        } else
        	Log.w(TAG,  "The MessageListener specified is null");
    }

    public MessageListener getListenerByNameAndType(String name, Class clz) {
        for (MessageListener ml : messageListeners.get(name))
            if (clz.isInstance(ml))
                return ml;
        return null;
    }

    public void removeMessageListener(MessageListener listener,
            String channelName) {
        if (listener != null && channelName.length()>0) {
            if (messageListeners.get(channelName) != null) {
                messageListeners.get(channelName).remove(listener);
            }
        } else
        	Log.w(TAG,  "Cannot remove an invalid MessageListener");
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public String getVersion() {
        return version;
    }

    public void loadConfigurations(Dictionary configurations) {
        // TODO Auto-generated method stub

    }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    	Log.w(TAG,  "The message cannot be managed because the thread bounds and queue capacities are reached.");
        throw new CommunicationModuleException(
                CommunicationModuleErrorCode.ERROR_MANAGING_MESSAGE,
                "The message cannot be managed because the thread bounds and queue capacities are reached.");
    }

    public void configureChannels(List<ChannelDescriptor> channels,
            String peerName) {
    	wrapperConnector.configureConnector(channels, peerName);
    }

    /**
     * Unicast
     */
    public void send(ChannelMessage message, MessageListener listener,
            PeerCard receiver) {
        try {
            /*
             * executor.execute(new UnicastExecutor(message,
             * communicationConnector, message.getReceiver().get(0), listener));
             */
            List<MessageListener> listeners = new ArrayList<MessageListener>();
            listeners.add(listener);
			Thread t = new Thread(new AndroidUnicastExecutor(message, receiver,
					listeners, wrapperConnector));
            t.start();
        } catch (Throwable e) {
        	Log.e(TAG,  "Error during message handling: ",e);
            throw new CommunicationModuleException(
                    CommunicationModuleErrorCode.ERROR_MESSAGE_FORMAT,
                    "Error during message handling, due to internal excepetion. Message was:"
                            + message.toString(), e);
        }

    }

    /**
     * Unicast
     */
    public void send(ChannelMessage message, PeerCard receiver) {
        try {
            // fetch the listeners associated to the broker
            List<MessageListener> listeners = getMessageListeners(message
                    .getChannelNames());
			Thread t = new Thread(new AndroidUnicastExecutor(message, receiver,
					listeners, wrapperConnector));
            t.start();

        } catch (NullPointerException e) {
        	Log.e(TAG,  "Error during message handling: ",e);
            throw new CommunicationModuleException(
                    CommunicationModuleErrorCode.ERROR_MESSAGE_FORMAT,
                    "Error during message handling :" + message.toString());
        } catch (Exception e) {

        	Log.e(TAG,  "Error during message handling: ", e);
            throw new CommunicationModuleException(
                    CommunicationModuleErrorCode.ERROR_MESSAGE_FORMAT,
                    "Error during message handling, due to internal excepetion. Message was:"
                            + message.toString(), e);
        }

    }

    /**
     * Multicast
     */
    public void sendAll(ChannelMessage message, List<PeerCard> receivers,
            MessageListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * Multicast
     */
    public void sendAll(ChannelMessage message, List<PeerCard> recipients) {
        // TODO Auto-generated method stub

    }

    /**
     * Broadcast
     */
    public void sendAll(ChannelMessage message) {
        // TODO Auto-generated method stub

    }

    /**
     * Broadcast with message listener
     */
    public void sendAll(ChannelMessage message, MessageListener listener) {
        /*
         * executor.execute(new BroadcastExecutor(message,
         * communicationConnector, listener));
         */
        try {
            Thread t = new Thread(new AndroidBroadcastExecutor(message,
                    /*communicationConnector,*/ listener/*, context*/,wrapperConnector));
            t.start();
        } catch (Throwable e) {
        	Log.e(TAG,  "Error during message handling: ", e);
            throw new CommunicationModuleException(
                    CommunicationModuleErrorCode.ERROR_MESSAGE_FORMAT,
                    "Error during message handling, due to internal excepetion. Message was:"
                            + message.toString(), e);
        }
    }

    public List<String> getGroupMembers(String group) {
        return wrapperConnector.getGroupMembers(group);
    }

    public boolean hasChannel(String channelName) {
        return wrapperConnector.hasChannel(channelName);
    }

    // Cheat workaround for the buses
	public Context getContext() {
		return this.androidContext;
	}
}
