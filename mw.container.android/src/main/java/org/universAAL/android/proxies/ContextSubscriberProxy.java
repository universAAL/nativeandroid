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
package org.universAAL.android.proxies;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.ri.gateway.communicator.service.RemoteSpacesManager;

import android.content.Context;
import android.content.Intent;

public class ContextSubscriberProxy extends ContextSubscriber implements SharedObjectListener{
	private WeakReference<Context> context; //TODO memory issues?
	private String action=null;
	private String category=null;
	private String grounding=null;
	private Hashtable<String,String> eventVALtoExtraKEY;

	public ContextSubscriberProxy(GroundingParcel parcel, Context context) {
		super(AndroidContext.THE_CONTEXT, prepareSubscriptions(parcel.getGrounding()));
		this.context=new WeakReference<Context>(context);
		this.action=parcel.getAction();
		this.category=parcel.getCategory();
		fillTable(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		// This is for GW
		if(parcel.getRemote()!=null && !parcel.getRemote().isEmpty()){
			grounding=parcel.getGrounding();
			RemoteSpacesManager[] gw = (RemoteSpacesManager[]) AndroidContainer.THE_CONTAINER
					.fetchSharedObject(AndroidContext.THE_CONTEXT,
							new Object[] { RemoteSpacesManager.class.getName() },
							this);
			if(gw!=null && gw.length>0){
				try {
					gw[0].importRemoteContextEvents(this, prepareSubscriptions(grounding));
				} catch (Exception e) {
					System.out.println("Could not import remote events");
				}
			}
		}
	}

	private static ContextEventPattern[] prepareSubscriptions(String serial) {
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() }); //TODO better access to parser
		ContextEventPattern cep=(ContextEventPattern) parser.deserialize(serial);
		return new ContextEventPattern[]{cep};
	}
	
	private void fillTable(int length, String[] keys, String[] values){
		eventVALtoExtraKEY=new Hashtable<String,String>(length);
		for(int i=0; i<length; i++){
			eventVALtoExtraKEY.put(keys[i], values[i]);
		}
	}

	@Override
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleContextEvent(ContextEvent event) {
		// Extract the origin action and category from the event
		String fromAction=(String)event.getProvider().getProperty(IntentConstants.UAAL_META_PROP_FROMACTION);
		String fromCategory=(String)event.getProvider().getProperty(IntentConstants.UAAL_META_PROP_FROMCATEGORY);
		boolean isNonIntrusive = fromAction!=null && fromAction.equals(action) && fromCategory!=null && fromCategory.equals(category);
		// This means the receiver in the publisher proxy is the same as the
		// receiver in destination native app. Therefore the destination will
		// already have received it and we must not send the intent to avoid
		// duplication or infinite loops.
		if (!isNonIntrusive) {
			// In this case the receivers are different because the origin
			// action+cat is being used as a kind of API to call the CPublisher.
			// In this case we have to relay the event to the destination native
			// app.
			Context ctxt=context.get();
			if(ctxt!=null && !isNonIntrusive){
				Intent broadcast = new Intent(action);
				broadcast.addCategory(category);
				if(eventVALtoExtraKEY!=null && !eventVALtoExtraKEY.isEmpty()){
					VariableSubstitution.putEventValuesAsIntentExtras(event, broadcast, eventVALtoExtraKEY);
				}
				ctxt.sendBroadcast(broadcast);
			}
		}
	}

	//For the GW
	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if(grounding!=null && sharedObj!=null && sharedObj instanceof RemoteSpacesManager){
			try {
				((RemoteSpacesManager)sharedObj).importRemoteContextEvents(this, prepareSubscriptions(grounding));
			} catch (Exception e) {
				System.out.println("Could not import remote events");
			}
		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		//Unimport is only for services
	}

}
