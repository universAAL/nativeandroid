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
package org.universAAL.middleware.android.common;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public interface IAndroidSodaPop {

    public final String prefix = IAndroidSodaPop.class.getCanonicalName();

    // Protocols
    public final String PROTOCOL_UPNP = prefix + ".UPnP";
    public final String PROTOCOL_BLUETOOTH = prefix + ".Bluetooth";

    // Actions
    public final String ACTION_INITIALIZE = prefix + "." + Action.INITIALIZE;
    public final String ACTION_GET_ID = prefix + "." + Action.GET_ID;

    public final String ACTION_NOTICE_JOINING_PEER = prefix + "." + Action.NOTICE_JOINING_PEER;
    public final String ACTION_NOTICE_LEAVING_PEER = prefix + "." + Action.NOTICE_LEAVING_PEER;
    public final String ACTION_NOTICE_JOINING_BUS = prefix + "." + Action.NOTICE_JOINING_BUS;
    public final String ACTION_NOTICE_LEAVING_BUS = prefix + "." + Action.NOTICE_LEAVING_BUS;
    public final String ACTION_NOTICE_PEER_BUSSES = prefix + "." + Action.NOTICE_PEER_BUSSES;
    public final String ACTION_REPLY_PEER_BUSSES = prefix + "." + Action.REPLY_PEER_BUSSES;
    public final String ACTION_PRINT_STATUS = prefix + "." + Action.PRINT_STATUS;

    public final String ACTION_PROCESS_BUS_MESSAGE = prefix + "." + Action.PROCESS_BUS_MESSAGE;
    public final String ACTION_PROPAGAGE_MESSAGE = prefix + "." + Action.PROPAGATE_MESSAGE;

    public final String ACTION_SEND_MESSAGE = prefix + "." + Action.SEND_MESSAGE;

    public final String ACTION_REGISTER = prefix + "." + Action.REGISTER;
    public final String ACTION_UNREGISTER = prefix + "." + Action.UNREGISTER;
    public final String ACTION_PROCESS_BUS_MESSAGE_RSP = prefix + "."
	    + Action.PROCESS_BUS_MESSAGE_RSP;
    public final String ACTION_SERVICE_CALLER_REQUEST = prefix + "." + Action.SERVICE_REQUEST;
    public final String ACTION_SERVICE_CALLER_RESPONSE = prefix + "." + Action.SERVICE_RESPONSE;
    public final String ACTION_CONTEXT_PUBLISHER_REQUEST = prefix + "."
	    + Action.CONTEXT_PUBLISHER_REQUEST;

    // Categories
    public final String CATEGORY_LOCAL_SODAPOP = prefix + ".LOCAL_SODAPOP_CATEGORY";
    public final String CATEGORY_UPNP_SODAPOP = prefix + ".UPNP_SODAPOP_CATEGORY";

    // Keys (in intent extras)
    public final String EXTRAS_KEY_PROTOCOL = prefix + ".key.protocol";
    public final String EXTRAS_KEY_REMOTE_PEER_ID_RSP = prefix + ".key.remotepeerid.for.response";
    public final String EXTRAS_KEY_REMOTE_PEER_IDS_RSP = prefix + ".key.remotepeerids.for.response";
    public final String EXTRAS_KEY_PEER_ID = prefix + ".key.peerid";
    public final String EXTRAS_KEY_PEER_BUSSES = prefix + ".key.peerbusses";
    public final String EXTRAS_KEY_PEER_BUS_NAME = prefix + ".key.peerbus";
    public final String EXTRAS_KEY_BUS_PACKAGE_NAME = prefix + ".key.buspackagename";
    public final String EXTRAS_KEY_BUS_CLASS_NAME = prefix + ".key.busclassname";
    public final String EXTRAS_KEY_ANDROID_SERVICE_NAME = prefix + ".key.androidservicename";
    public final String EXTRAS_KEY_PACKAGE_NAME = prefix + ".key.packageName";
    public final String EXTRAS_KEY_BUS_MESSAGE = prefix + ".key.busMessage";
    public final String EXTRAS_KEY_REPLY_TO = prefix + ".key.replyTo";
    public final String EXTRAS_KEY_SERVICE_CALLEE_ID = prefix + ".key.serviceCalleeID";
    public final String EXTRAS_KEY_OPERATION_NAME = prefix + ".key.operationName";
    public final String EXTRAS_KEY_MESSAGE_ID_IN_REPLY = prefix + ".key.messageIDInReplyTo";
    public final String EXTRAS_KEY_ACTION_NAME = prefix + ".key.actionName";
    public final String EXTRAS_KEY_SERVICE_CALLER_ID = prefix + ".key.serviceCallerID";
    public final String EXTRAS_KEY_UNREGISTER_BROADCAST_RECEIVER = prefix
	    + ".key.unregisterBroadcastReceiver";
    public final String EXTRAS_KEY_CONTEXT_PUBLISHER_ID = prefix + ".key.contextPublisherID";

    // Service grounding metadata tag
    public final String META_DATA_TAG_SERVICE_GROUNDING = "org.universAAL.middleware.service.android.serviceGrounding";

    // Service Request metadata tag
    public final String META_DATA_TAG_SERVICE_REQUEST_GROUNDING = "org.universAAL.middleware.service.android.serviceRequestGrounding";

    // Context Publisher grounding metadata tag
    public final String META_DATA_TAG_CONTEXT_PUBLISHER_GROUNDING = "org.universAAL.middleware.service.android.contextPublisherGrounding";

    // Context Subscriber grounding metadata tag
    public final String META_DATA_TAG_CONTEXT_SUBSCRIBER_GROUNDING = "org.universAAL.middleware.service.android.contextSubcsriberGrounding";

    // Inputs (in UPnP actions)
    public final String UPNP_ACTION_INPUT_BUSSES_NAME = "BussesName";
    public final String UPNP_ACTION_INPUT_BUS_NAME = "BusName";
    public final String UPNP_ACTION_INPUT_BUS_NAMES = "BusNames";
    public final String UPNP_ACTION_INPUT_PEER_ID = "PeerID";
    public final String UPNP_ACTION_INPUT_JOINING_PEER = "JoiningPeer";
    public final String UPNP_ACTION_INPUT_LEAVING_PEER = "LeavingPeer";
    public final String UPNP_ACTION_INPUT_MESSAGE = "Message";

    // Configurations
    public final String CONFIG_HOME_PATH = "/mnt/sdcard/Android/data/org.universAAL.middleware/files";
}
