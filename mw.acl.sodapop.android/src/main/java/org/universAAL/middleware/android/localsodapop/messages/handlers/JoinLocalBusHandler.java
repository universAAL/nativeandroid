////TODO: remove this class, we don't use it, since we access the DB directly (in joinBus / leaveBus)
///**
// * 
// *  OCO Source Materials 
// *      © Copyright IBM Corp. 2012 
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
//package org.universAAL.middleware.android.localsodapop.messages.handlers;
//
//import org.universAAL.middleware.android.common.BusNameWrapper;
//import org.universAAL.middleware.android.common.messages.IMessage;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.android.localsodapop.SodaPopAndroidFactory;
//import org.universAAL.middleware.android.localsodapop.messages.JoinLocalBusMessage;
//
//
///**
// * 
// *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
// *	
// *  Apr 18, 2012
// *
// */
//public class JoinLocalBusHandler extends AbstractMessagePersistableHandler {
//
//	@Override
//	protected void privateHandleMessage(IMessage message) {
//		
//		// Cast it to join message
//		JoinLocalBusMessage joinMessage = (JoinLocalBusMessage) message;
//
//		// Initializing the class
//		AbstractSodaPopAndroidImpl sodaPop = 
//				SodaPopAndroidFactory.createAndroidSodaPop(joinMessage.getContext(), joinMessage.getProtocol(), sodaPopPeersSQLiteMngr);
//
//		// Persist the new bus
//		BusNameWrapper busNameWrapper = 
//				new BusNameWrapper(joinMessage.getBusName(), joinMessage.getPackageName(), joinMessage.getClassName());
//		sodaPop.join(busNameWrapper);
//	}
//
// }
