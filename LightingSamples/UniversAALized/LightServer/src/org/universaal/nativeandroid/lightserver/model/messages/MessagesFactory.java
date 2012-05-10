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
package org.universaal.nativeandroid.lightserver.model.messages;

import java.util.Map;

import org.universaal.nativeandroid.lightserver.Action;
import org.universaal.nativeandroid.lightserver.LightServerActivity;
import org.universaal.nativeandroid.lightserver.controller.listeners.IListener;
import org.universaal.nativeandroid.lightserver.controller.listeners.ListenerServerType;

import android.content.Intent;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class MessagesFactory {
	public static IMessage createMessage(Intent intent, Map<ListenerServerType, IListener> listenersMap) {
		IMessage message = null;
		
		Action action = getAction(intent);
		
		if (null != action) {
			switch (action)
			{
			case TURN_ON:
				message = new TurnOnMessage(intent, listenersMap);
				break;
			case TURN_OFF:
				message = new TurnOffMessage(intent, listenersMap);
				break;
			case GET_CONTROLLED_LAMPS:
				message = new GetControlledLampsMessage(intent);
				break;
			}
		}
		
		return message;
	}
	
	private static Action getAction(Intent intent)
	{
		Action foundAction = null;
		final String activityPackage 	= LightServerActivity.class.getPackage().getName();
		final String intentActionName 	= intent.getAction();
		
		for (Action curAction : Action.values()) {
			if (intentActionName.equals(activityPackage + "." + curAction)) {
				foundAction = curAction;
				break;
			}
		}
		
		return foundAction;
	}
}
