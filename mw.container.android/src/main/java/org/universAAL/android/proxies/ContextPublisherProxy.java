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
import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Class that acts as a connection between an Android component and a universAAL
 * wrapper. In this case, between a receiver and a context publisher. The
 * translation is made thanks to metadata grounding.
 * 
 * @author alfiva
 * 
 */
public class ContextPublisherProxy extends ContextPublisher {
	private WeakReference<Context> contextRef=null;
	private String action=null;
	private String category=null;
	private String grounding=null;
	private Hashtable<String,String> extraKEYtoEventVAL;
	private ContextPublisherProxyReceiver receiver=null;
	
	/**
	 * Constructor for the proxy.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @param context
	 *            The Android context.
	 */
	public ContextPublisherProxy(GroundingParcel parcel,
			Context context) {
		super(AndroidContext.THE_CONTEXT, prepareProvider(parcel));
		contextRef=new WeakReference<Context>(context);
		action=parcel.getAction();
		category=parcel.getCategory();
		grounding=parcel.getGrounding();
		fillTable(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		receiver=new ContextPublisherProxyReceiver();
		IntentFilter filter=new IntentFilter(this.action);
		filter.addCategory(category);
		context.registerReceiver(receiver, filter);//TODO use the other longer register method
		sync();
	}
	
	public void sync(){
		//Nothing. Publishing does not need syncing. For now.
	}

	/**
	 * Extract the Context Provider information from the grounding.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @return The uAAL Context Provider.
	 */
	private static ContextProvider prepareProvider(GroundingParcel parcel){
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
		ContextEvent event=(ContextEvent) parser.deserialize(VariableSubstitution.cleanContextEvent(parcel.getGrounding()));
		ContextProvider prov=event.getProvider();
		// This is for identifying the origin of the event, to avoid duplications in csub later
		prov.setProperty(AppConstants.UAAL_META_PROP_FROMACTION, parcel.getAction()); 
		prov.setProperty(AppConstants.UAAL_META_PROP_FROMCATEGORY, parcel.getCategory());
		return prov;
	}
	
	/**
	 * Extract the table of mappings between extras and event values from the
	 * grounding.
	 * 
	 * @param length
	 *            Amount of entries.
	 * @param keys
	 *            Intent extra keys.
	 * @param values
	 *            Context Event values.
	 */
	private void fillTable(int length, String[] keys, String[] values){
		extraKEYtoEventVAL=new Hashtable<String,String>(length);
		for(int i=0; i<length; i++){
			extraKEYtoEventVAL.put(keys[i], values[i]);
		}
	}

	@Override
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void close() {
		super.close();
		Context ctxt=this.contextRef.get();
		if (ctxt!=null && receiver!=null){
			ctxt.unregisterReceiver(receiver);
		}
	}

	/**
	 * Auxiliary class representing the Broadcast Receiver registered by the
	 * middleware where apps will send intents when they want to send a context
	 * event to uAAL.
	 * 
	 * @author alfiva
	 * 
	 */
	public class ContextPublisherProxyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra(AppConstants.ACTION_META_FROMPROXY, false)) {
				// The intent comes from a SubscriberProxy. It wouldnt have sent it
				// if its receiver and this one where the same, but that doesnt
				// count when it comes from the bus alone. This fixes that.
				return;
			}
			ContextEvent event;
			MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
					.fetchSharedObject(AndroidContext.THE_CONTEXT,
							new Object[] { MessageContentSerializerEx.class
									.getName() });
			if(extraKEYtoEventVAL!=null && !extraKEYtoEventVAL.isEmpty()){
				String turtleReplaced=VariableSubstitution.putIntentExtrasAsEventValues(intent, grounding, extraKEYtoEventVAL);
				event=(ContextEvent) parser.deserialize(turtleReplaced);
			}else{
				event=(ContextEvent) parser.deserialize(grounding);
			}
			// Cant Improve this. Must make a copy of the event so that URI is new. Timestamp and Provider are set by bus
			ContextEvent cev = new ContextEvent(event.getRDFSubject(),
					event.getRDFPredicate());
			cev.setConfidence(event.getConfidence());
			cev.setExpirationTime(event.getExpirationTime());
			publish(cev);
			// If RAPI, send it to server. If GW it is automatic by the running GW
			if (MiddlewareService.isGWrequired() && Config.getRemoteType() == AppConstants.REMOTE_TYPE_RAPI) {
				ContextEvent cev2=(ContextEvent)cev.deepCopy();//Prevent concurrent change of cev!!!!
				cev2.changeProperty(ContextEvent.PROP_CONTEXT_PROVIDER, null);//The single publisher in RAPI will send ANY event
				String serial = parser.serialize(cev2);
				if (serial != null){
					RAPIManager.invokeInThread(RAPIManager.SENDC, serial);
				}//TODO error
			}
		}
	}

}
