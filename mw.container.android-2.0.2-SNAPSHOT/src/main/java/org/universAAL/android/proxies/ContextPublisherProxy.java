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

import java.util.Hashtable;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ContextPublisherProxy extends ContextPublisher {
	private String action=null;
	private String category=null;
	private String turtleEvent=null;
	private ContextPublisherProxyReceiver receiver=null;
	private Hashtable<String,String> extraKEYtoEventVAL;
	
	public ContextPublisherProxy(GroundingParcel parcel,
			Context context) {
		super(AndroidContext.THE_CONTEXT, prepareProvider(parcel));
		this.action=parcel.getAction();
		this.category=parcel.getCategory();
		this.turtleEvent=parcel.getGrounding();
		fillTable(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		this.receiver=new ContextPublisherProxyReceiver();
		IntentFilter filter=new IntentFilter(this.action);
		filter.addCategory(this.category);
		context.registerReceiver(receiver, filter);//TODO use the other longer register method
	}

	private static ContextProvider prepareProvider(GroundingParcel parcel){
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
		ContextEvent event=(ContextEvent) parser.deserialize(VariableSubstitution.cleanContextEvent(parcel.getGrounding()));
		ContextProvider prov=event.getProvider();
		// This is for identifying the origin of the event, to avoid duplications in csub later
		prov.setProperty(IntentConstants.UAAL_META_PROP_FROMACTION, parcel.getAction()); 
		prov.setProperty(IntentConstants.UAAL_META_PROP_FROMCATEGORY, parcel.getCategory());
		return prov;
	}
	
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
	
	public class ContextPublisherProxyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ContextEvent event;
			MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
					.fetchSharedObject(AndroidContext.THE_CONTEXT,
							new Object[] { MessageContentSerializerEx.class
									.getName() });
			if(extraKEYtoEventVAL!=null && !extraKEYtoEventVAL.isEmpty()){
				String turtleReplaced=VariableSubstitution.putIntentExtrasAsEventValues(intent, turtleEvent, extraKEYtoEventVAL);
				event=(ContextEvent) parser.deserialize(turtleReplaced);
			}else{
				event=(ContextEvent) parser.deserialize(turtleEvent);
			}
			// Cant Improve this. Must make a copy of the event so that URI is new. Timestamp and Provider are set by bus
			ContextEvent cev = new ContextEvent(event.getRDFSubject(),
					event.getRDFPredicate());
			cev.setConfidence(event.getConfidence());
			cev.setExpirationTime(event.getExpirationTime());
			publish(cev);
		}
	}

}
