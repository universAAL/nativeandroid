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

import org.universaal.nativeandroid.lightserver.common.IConstants;
import org.universaal.nativeandroid.lightserver.controller.listeners.IListener;
import org.universaal.nativeandroid.lightserver.model.LightServerModel;

import android.content.Intent;
import android.util.Log;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public abstract class TurnOnOffLampMessage extends AbstractMessage {
	protected int 		lampNumber;
	protected IListener msgListener;
	
	public TurnOnOffLampMessage(Intent intent, IListener listener) {
		lampNumber 		= getLampNumber(intent);
		msgListener 	= listener;
		
		Log.d(getClass().getCanonicalName(), "Create a message for lamp [" + lampNumber + "]");
	}
	
	private int getLampNumber(Intent intent) {
		// Extract the lamp number
		String lampAsStr = (String) intent.getExtras().get(IConstants.lampNumberArg);
		return Integer.parseInt(lampAsStr); 
	}

	@Override
	public void handle() {
		Log.d(getClass().getCanonicalName(), "Is about to handle message");
		
		// Persist the changes
		String 	lampID 		= String.valueOf(lampNumber);
		boolean lampState 	= getLampState();
		
		Log.d(getClass().getCanonicalName(), "Is about to persist lamp state [" + 
				lampID + "," + lampState + "]");
		
		LightServerModel.getPersistLampMngr().setLampState(lampID, lampState);
		
		// Notify controller that a change occur
		if (null != msgListener) {
			msgListener.notifyStateChange();
		}
	}

	protected abstract boolean getLampState();
}
