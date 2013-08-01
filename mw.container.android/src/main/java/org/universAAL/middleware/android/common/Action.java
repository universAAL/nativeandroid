/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
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
 *         Apr 8, 2012
 * 
 */
public enum Action {
	CONNECTOR_COMM_MULTICASTSOME("ConnectorCommMulticastsome"), CONNECTOR_COMM_MULTICAST(
			"ConnectorCommMulticast"), CONNECTOR_COMM_UNICAST(
			"ConnectorCommUnicast"), CONNECTOR_DISC_ANNOUNCE(
			"ConnectorDiscAnnounce"), CONNECTOR_DISC_DEREGISTER(
			"ConnectorDiscDeregister"), CONNECTOR_DISC_ADDLISTENER(
			"ConnectorDiscAddlistener"), CONNECTOR_DISC_REMLISTENER(
			"ConnectorDiscRemlistener"), MODULE_COMM_ADD_LISTENER(
			"ModuleCommAddListener"), MODULE_COMM_REM_LISTENER(
			"ModuleCommRemListener"), MODULE_COMM_RECEIVED("ModuleCommReceived"), MODULE_COMM_SENDALL(
			"ModuleCommSendall"), MODULE_COMM_SENDALL_L("ModuleCommSendallL"), MODULE_COMM_SENDALL_R(
			"ModuleCommSendallR"), MODULE_COMM_SENDALL_RL("ModuelCommSendallRL"), MODULE_COMM_SEND_R(
			"ModuleCommSendR"), MODULE_COMM_SEND_RL("ModuleCommSendRL"),

	INITIALIZE("Initialize"), // TODO: consider to change the mechanism - map
	// from action to upnp action name!!!
	GET_ID("GetID"), NOTICE_JOINING_PEER("NoticeJoiningPeer"), NOTICE_LEAVING_PEER(
			"NoticeLeavingPeer"), NOTICE_JOINING_BUS("JoinBus"), NOTICE_LEAVING_BUS(
			"LeaveBus"), NOTICE_PEER_BUSSES("NoticePeerBusses"), REPLY_PEER_BUSSES(
			"ReplyPeerBusses"), PRINT_STATUS("PrintStatus"), SEND_MESSAGE(
			"SendMessage"), REGISTER("Register"), UNREGISTER("Unregister"), PROCESS_BUS_MESSAGE(
			"ProcessBusMessage"), PROCESS_BUS_MESSAGE_RSP(
			"ProcessBusMessageResponse"), PROPAGATE_MESSAGE("ProcessBusMessage"), SERVICE_REQUEST(
			"ProcessServiceRequest"), SERVICE_RESPONSE("ProcessServiceResponse"), CONTEXT_PUBLISHER_REQUEST(
			"ProcessContextPublisherRequest");

	private String name;

	private Action(String name) {
		this.name = name;
	}

	public static Action getAction(String pActionAsString) {
		Action foundAction = null;

		for (Action curAction : Action.values()) {
			if (pActionAsString.endsWith("." + curAction.toString())) {
				foundAction = curAction;
				break;
			}
		}
		return foundAction;
	}

	public String getName() {
		return name;
	}
}
