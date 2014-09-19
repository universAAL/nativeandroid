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
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.ri.gateway.communicator.service.RemoteSpacesManager;

import android.content.Context;
import android.content.Intent;

/**
 * Class that acts as a connection between an Android component and a universAAL
 * wrapper. In this case, between a context subscriber and an intent. The
 * translation is made thanks to metadata grounding.
 * 
 * @author alfiva
 * 
 */
public class ContextSubscriberProxy extends ContextSubscriber implements SharedObjectListener{
	private WeakReference<Context> contextRef;
	private String action=null;
	private String category=null;
	private String remote=null;
	private String grounding=null;
	private Hashtable<String,String> eventVALtoExtraKEY;

	/**
	 * Constructor for the proxy.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @param context
	 *            The Android context.
	 */
	public ContextSubscriberProxy(GroundingParcel parcel, Context context) {
		super(AndroidContext.THE_CONTEXT, prepareSubscriptions(parcel.getGrounding()));
		contextRef=new WeakReference<Context>(context);
		action=parcel.getAction();
		category=parcel.getCategory();
		fillTable(parcel.getLengthOUT(),parcel.getKeysOUT(), parcel.getValuesOUT());
		grounding = parcel.getGrounding();
		// This is for GW or RAPI. RAPI does not need remote tag, but keep using it for coherence
		remote = parcel.getRemote();
		sync();
	}
	
	public void sync(){
		if (MiddlewareService.isGWrequired() && remote != null && !remote.isEmpty()) {
		switch (Config.getRemoteType()) {
			case IntentConstants.REMOTE_TYPE_GW:
				RemoteSpacesManager[] gw = (RemoteSpacesManager[]) AndroidContainer.THE_CONTAINER
						.fetchSharedObject(AndroidContext.THE_CONTEXT,
								new Object[] { RemoteSpacesManager.class.getName() }, this);
				if (gw != null && gw.length > 0) {
					try {
						gw[0].importRemoteContextEvents(this, prepareSubscriptions(grounding));
					} catch (Exception e) {
						System.out.println("Could not import remote events");
					}
				}
				break;
			case IntentConstants.REMOTE_TYPE_RAPI:
				// RAPI only gets 1 pattern at a time, and the grounding is (surprise!) a single pattern
				if(MiddlewareService.isGWrequired()){
					RAPIManager.invokeInThread(RAPIManager.SENDC, grounding);
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Extract the Context Event Pattern information from the grounding.
	 * 
	 * @param serial
	 *            The serialized form of the pattern, taken from the grounding.
	 * @return The deserialized array of patterns.
	 */
	private static ContextEventPattern[] prepareSubscriptions(String serial) {
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() }); //TODO better access to parser
		ContextEventPattern cep=(ContextEventPattern) parser.deserialize(serial);
		return new ContextEventPattern[]{cep};
	}
	
	/**
	 * Extract the table of mappings between event values and extras from the
	 * grounding.
	 * 
	 * @param length
	 *            Amount of entries.
	 * @param keys
	 *            Context Event keys.
	 * @param values
	 *            Extras values.
	 */
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
		boolean isNonIntrusive = fromAction != null	&& fromAction.equals(action) 
				&& fromCategory != null && fromCategory.equals(category);
		// This means the receiver in the publisher proxy is the same as the
		// receiver in destination native app. Therefore the destination will
		// already have received it and we must not send the intent to avoid
		// duplication or infinite loops.
		if (!isNonIntrusive) {
			// In this case the receivers are different because the origin
			// action+cat is being used as a kind of API to call the CPublisher.
			// In this case we have to relay the event to the destination native app.
			Context ctxt=contextRef.get();
			if(ctxt!=null && !isNonIntrusive){
				Intent broadcast = new Intent(action);
				broadcast.addCategory(category);
				if(eventVALtoExtraKEY!=null && !eventVALtoExtraKEY.isEmpty()){
					VariableSubstitution.putEventValuesAsIntentExtras(event, broadcast, eventVALtoExtraKEY);
				}
				// Flag to avoid feeding back the intent to bus when intent is the same in app and in publisherproxy 
				broadcast.putExtra(IntentConstants.ACTION_META_FROMPROXY, true);
				ctxt.sendBroadcast(broadcast);
			}
		}
	}

	// For the GW
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
		// Unimport is only for services
	}

}
