///**
// * 
// *  OCO Source Materials 
// *      � Copyright IBM Corp. 2012 
// *
// *      See the NOTICE file distributed with this work for additional 
// *      information regarding copyright ownership 
// *       
// *      Licensed under the Apache License, Version 2.0 (the "License"); 
// *      you may not use this file except in compliance with the License. 
// *      You may obtain a copy of the License at 
// *       	http://www.apache.org/licenses/LICENSE-2.0 
// *       
// *      Unless required by applicable law or agreed to in writing, software 
// *      distributed under the License is distributed on an "AS IS" BASIS, 
// *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// *      See the License for the specific language governing permissions and 
// *      limitations under the License. 
// *
// */
//package org.universAAL.middleware.android.common.messages;
//
//import org.universAAL.middleware.android.common.Action;
//import org.universAAL.middleware.android.localsodapop.messages.InitializeMessage;
//import org.universAAL.middleware.android.localsodapop.messages.JoinBusMessage;
//import org.universAAL.middleware.android.localsodapop.messages.JoinLocalBusMessage;
//import org.universAAL.middleware.android.localsodapop.messages.LeaveBusMessage;
//import org.universAAL.middleware.android.localsodapop.messages.LeaveLocalBusMessage;
//import org.universAAL.middleware.android.localsodapop.messages.NoticeLeavingPeerMessage;
//import org.universAAL.middleware.android.localsodapop.messages.NoticeNewPeerMessage;
//import org.universAAL.middleware.android.localsodapop.messages.NoticePeerBussesMessage;
//import org.universAAL.middleware.android.localsodapop.messages.PrintStatusMessage;
//import org.universAAL.middleware.android.localsodapop.messages.ReplyPeerBussesMessage;
//
//import android.content.Context;
//import android.content.Intent;
//
//
///**
// * 
// *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
// *	
// *  Apr 8, 2012
// *
// */
//public class MessageFactory {
//
//	public static IMessage createMessage(Context context, Intent intent) {
//		IMessage message = null;
//		
//		String actionAsString = intent.getAction();
//		if (null == actionAsString) return null;
//		
//		switch(Action.getAction(actionAsString))
//		{
//		case INITIALIZE:
//			message = new InitializeMessage(context, intent);
//			break;
//		case NOTICE_NEW_LOCAL_SODAPOP:
//		case NOTICE_JOINING_PEER:
//			message = new NoticeNewPeerMessage(context, intent);
//			break;
//		case NOTICE_LEAVING_PEER:
//			message = new NoticeLeavingPeerMessage(context, intent);
//			break;
//		case NOTICE_PEER_BUSSES:
//			message = new NoticePeerBussesMessage(context, intent);
//			break;
//		case REPLY_PEER_BUSSES:
//			message = new ReplyPeerBussesMessage(context, intent);
//			break;
//		case JOINING_LOCAL_BUS:
//			message = new JoinLocalBusMessage(context, intent);
//			break;
//		case LEAVING_LOCAL_BUS:
//			message = new LeaveLocalBusMessage(context, intent);
//			break;
//		case PRINT_STATUS:
//			message = new PrintStatusMessage(context, intent);
//			break;
//		case NOTICE_JOINING_BUS:
//			message = new JoinBusMessage(context, intent);
//			break;
//		case NOTICE_LEAVING_BUS:
//			message = new LeaveBusMessage(context, intent);
//			break;
//		}
//		
//		return message;
//	
//	}
// }