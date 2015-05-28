/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.android.receivers.system;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.container.AndroidRegistry;
import org.universAAL.android.proxies.ServiceCalleeProxy;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.ServiceCall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Receiver of GCM messages in the system. It is a wakefulBr because it will
 * call the MWService, and device should not sleep when sending the intent
 * there!
 * 
 * @author alfiva
 * 
 */
public class GCMReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Config.getRemoteType() != AppConstants.REMOTE_TYPE_RAPI) {
			return;// For now, double check. TODO make sure not needed
		}

		String method = intent.getStringExtra("method");
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });

		if (method.equals("SENDC")) {
			if (parser != null) {
				String serial = intent.getStringExtra("param");
				if (serial != null) {
					ContextEvent cev = (ContextEvent) parser
							.deserialize(serial);
					if (cev != null) {
						DefaultContextPublisher cp = new DefaultContextPublisher(
								AndroidContext.THE_CONTEXT, cev.getProvider());
						cp.publish(cev); // Cheat: Deliver the context event as an impostor. The receiver will get it.
						cp.close();
						cp = null;
						// TODO check performance of creating a publisher per call (it eases the reuse of providerinfo)
					}
				}
			}

		} else if (method.equals("CALLS")) {
			if (parser != null) {
				String serial = intent.getStringExtra("param");
				String spuri = intent.getStringExtra("to");
				String origincall = intent.getStringExtra("call");
				if (serial != null && spuri != null) {
					ServiceCall scall = (ServiceCall) parser
							.deserialize(serial);
					if (scall != null) { //TODO Use wakeful service calling?
						ServiceCalleeProxy calleeProxy = AndroidRegistry
								.getCallee(spuri);
						if (calleeProxy != null) {
							// I cannot use a caller impostor here because I receive a ServiceCall, not a ServiceRequest
							calleeProxy.handleCallFromGCM(scall, origincall);
						}
					}
				}
			}
		} else {
			// Received something unexpected from server. What to do?
		}
		setResultCode(Activity.RESULT_OK); //This is to tell GCM we did get the message, although not mandatory
	}

}
