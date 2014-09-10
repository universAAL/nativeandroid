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
package org.universaal.nativeandroid.lightserver.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.universaal.nativeandroid.lightserver.common.IConstants;
import org.universaal.nativeandroid.lightserver.controller.listeners.IListener;
import org.universaal.nativeandroid.lightserver.controller.listeners.ListenerServerType;
import org.universaal.nativeandroid.lightserver.model.messages.IMessage;
import org.universaal.nativeandroid.lightserver.model.messages.MessagesFactory;

import android.content.Context;
import android.content.Intent;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class LightServerModel {
	protected static LightServerModel lightServerModel;
	protected static final Object synchObj = new Object();
	
	protected Map<ListenerServerType, IListener> 	listenersMap;
	protected PersistLampsMngr 						persistLampMngr;
	
	private Context context;
	
	protected LightServerModel(Context context) {
		this.context = context;
		
		listenersMap = new HashMap<ListenerServerType, IListener>();
		
		persistLampMngr = new PersistLampsMngr(context);
	}
	
	public static LightServerModel getInstance(Context context) {
		if (null == lightServerModel) {
			synchronized (synchObj) {
				if (null == lightServerModel) {
					lightServerModel = new LightServerModel(context);
				}
			}
		}
		
		return lightServerModel;
	}
	
	public static LightServerModel getInstance() {
		if (null == lightServerModel) {
			throw new NullPointerException("The instance is not initialized!");
		}
		
		return getInstance(null); // In that case the instance is already initialized, therefore the given parameter is not used
	}
	
	public void addListener(ListenerServerType type, IListener listener) {
		listenersMap.put(type, listener);
	}
	
	public void handleIntent(Intent intent) {
		// Create the message
		IMessage message = MessagesFactory.createMessage(intent, listenersMap);
		
		if (null != message) {
			// Handle the intent
			message.handle();
		}
	}
	
	public Map<String, Boolean> getLamps() {
		return persistLampMngr.getLamps();
	}
	
	public static PersistLampsMngr getPersistLampMngr() {
		return lightServerModel.persistLampMngr;
	}

	public static void responseControlledLampsRequest(String action, String category) {
		Collection<String> lampsList = getPersistLampMngr().getLamps().keySet();
		
		// Convert to string array
		String[] lampsArr = lampsList.toArray(new String[0]);
		
		// Build the intent
		Intent reply = new Intent(action);
		reply.addCategory(category);
		
		// Set the lamps
		reply.putExtra(IConstants.lampNumberArrayArg, lampsArr);
		
		// Send it as a broadcast message
		getInstance().context.sendBroadcast(reply);
	}
	
	public static void sendLampStateChangeMessage(String lampID, boolean lampState) {
		Intent intent = new Intent(IConstants.lampStateChangedAction);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(IConstants.lampNumberArg, lampID);
		intent.putExtra(IConstants.brightnessArg, lampState ? 100 : 0);
		
		getInstance().context.sendBroadcast(intent);
	}
}
