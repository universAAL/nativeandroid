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
import java.util.Random;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Class that acts as a connection between an Android component and a universAAL
 * wrapper. In this case, between a service callee, an intent, and a broadcast
 * receiver for the response. The translation is made thanks to metadata
 * grounding.
 * 
 * @author alfiva
 * 
 */
public class ServiceCalleeProxy extends ServiceCallee {
	private WeakReference<Context> context;
	private String action=null;
	private String category=null;
	private String replyAction=null;
	private String replyCategory=null;
	private ServiceCalleeProxyReceiver receiver=null;
	private Hashtable<String,String> inputURItoExtraKEY;
	private Hashtable<String,String> extraKEYtoOutputURI;

	/**
	 * Constructor for the proxy.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @param context
	 *            The Android context.
	 */
	public ServiceCalleeProxy(GroundingParcel parcel, Context context) {
		super(AndroidContext.THE_CONTEXT, prepareProfiles(parcel));
		this.context=new WeakReference<Context>(context);
		this.action=parcel.getAction();
		this.category=parcel.getCategory();
		this.replyAction=parcel.getReplyAction();
		this.replyCategory=parcel.getReplyCategory();
		fillTableIN(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		fillTableOUT(parcel.getLengthOUT(),parcel.getKeysOUT(), parcel.getValuesOUT());
	}
	
	/**
	 * Extract the Service Profile information from the grounding.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @return The uAAL Service Profile.
	 */
	private static ServiceProfile[] prepareProfiles(GroundingParcel parcel) {
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
		ServiceProfile sp=(ServiceProfile)parser.deserialize(parcel.getGrounding());
		return new ServiceProfile[]{sp};
	}
	
	/**
	 * Extract the table of mappings between inputs and extras from the
	 * grounding.
	 * 
	 * @param length
	 *            Amount of entries.
	 * @param keys
	 *            Input keys.
	 * @param values
	 *            Extras ID values.
	 */
	private void fillTableIN(int length, String[] keys, String[] values){
		inputURItoExtraKEY=new Hashtable<String,String>(length);
		for(int i=0; i<length; i++){
			inputURItoExtraKEY.put(keys[i], values[i]);
		}
	}
	
	/**
	 * Extract the table of mappings between extras and outputs from the
	 * grounding.
	 * 
	 * @param length
	 *            Amount of entries.
	 * @param keys
	 *            Extras keys.
	 * @param values
	 *            Outputs values.
	 */
	private void fillTableOUT(int length, String[] keys, String[] values){
		extraKEYtoOutputURI=new Hashtable<String,String>(length);
		for(int i=0; i<length; i++){
			extraKEYtoOutputURI.put(keys[i], values[i]);
		}
	}

	@Override
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleRequest(BusMessage m) {
		ServiceCall call=(ServiceCall) m.getContent();
		// Extract the origin action and category from the call
		String fromAction=(String)call.getNonSemanticInput(IntentConstants.UAAL_META_PROP_FROMACTION);
		String fromCategory=(String)call.getNonSemanticInput(IntentConstants.UAAL_META_PROP_FROMCATEGORY);
		Boolean needsOuts=(Boolean)call.getNonSemanticInput(IntentConstants.UAAL_META_PROP_NEEDSOUTPUTS);
		boolean isNonIntrusive = fromAction!=null && fromAction.equals(action) && fromCategory!=null && fromCategory.equals(category);
		// This means the serv Intent in the caller proxy is the same as the
		// serv Intent in destination native app. Therefore the destination will
		// already have received it and we must not send the intent to avoid
		// duplication or infinite loops.
		if (!isNonIntrusive) {
			// In this case the serv intents are different because the origin
			// action+cat is being used as a kind of API to call the SCaller.
			// In this case we have to relay the call to the destination native app.
			Context ctxt=context.get();
			if(ctxt!=null){
				// Prepare an intent for sending to Android grounded service
				Intent serv = new Intent(action);
				serv.addCategory(category);
				boolean expecting=false;
				// If a response is expected, prepare a callback receiver (which must be called by uaalized app) TODO If reply* fields not set???
				if((replyAction!=null && !replyAction.isEmpty()) && (replyCategory!=null && !replyCategory.isEmpty())){
					// Tell the destination where to send the reply
					serv.putExtra("replyToAction", replyAction);
					serv.putExtra("replyToCategory", replyCategory);
					// Register the receiver for the reply
					receiver=new ServiceCalleeProxyReceiver(m);
					IntentFilter filter=new IntentFilter(replyAction);
					filter.addCategory(replyCategory);
					ctxt.registerReceiver(receiver, filter);
					expecting=true;
				} else if (needsOuts!=null && needsOuts.booleanValue()){
					// No reply* fields set, but caller still needs a response, lets build him some (does not work for callers outside android MW)
					Random r = new Random();
					String action=IntentConstants.ACTION_REPLY+r.nextInt();
					serv.putExtra("replyToAction", action);
					serv.putExtra("replyToCategory", Intent.CATEGORY_DEFAULT);
					// Register the receiver for the reply
					receiver=new ServiceCalleeProxyReceiver(m);
					IntentFilter filter=new IntentFilter(action);
					filter.addCategory(Intent.CATEGORY_DEFAULT);
					ctxt.registerReceiver(receiver, filter);
					expecting=true;
				}
				// Take the inputs from the call and put them in the intent
				if (inputURItoExtraKEY!=null && !inputURItoExtraKEY.isEmpty()){
					VariableSubstitution.putCallInputsAsIntentExtras(call, serv, inputURItoExtraKEY);
				}
				// Send the intent to Android grounded service
				ComponentName started=ctxt.startService(serv);
				if(started==null){
					// No service in android was actually there, send error response
					ServiceResponse resp = new ServiceResponse(CallStatus.serviceSpecificFailure);
					resp.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Could not find the Android grounded service"));
					sendResponse(m,resp);
				}else if(!expecting){
					// There is no receiver waiting a response, send success now
					ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
					sendResponse(m,resp);
				}
				//TODO Handle timeout
			}
		}
	}
									
	/**
	 * Sends the response back to the service bus.
	 * 
	 * @param msg
	 *            The message that originated the call.
	 * @param sr
	 *            The response to return.
	 */
	private void sendResponse(final BusMessage msg, final ServiceResponse sr) {
		// First, since we already have the response (good or bad), remove the receiver
		Context ctxt = context.get();
		if (ctxt != null) {
			if (receiver != null) {
				ctxt.unregisterReceiver(receiver);
				receiver = null;
			}
		}
		// Then send back the response
		BusMessage reply = msg.createReply(sr);
		if (reply != null) {
			((ServiceBus) theBus).brokerReply(busResourceURI, reply);
		}
	}

	/**
	 * Auxiliary class representing the Broadcast Receiver registered by the
	 * middleware where apps will send intents when they have to return a
	 * response to uAAL.
	 * 
	 * @author alfiva
	 * 
	 */
	public class ServiceCalleeProxyReceiver extends BroadcastReceiver {
		BusMessage msg;
		
		public ServiceCalleeProxyReceiver() {
			super();
		}
		
		public ServiceCalleeProxyReceiver(BusMessage m) {
			super();
			msg=m;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("//////onReceive");
			ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
			if (extraKEYtoOutputURI!=null && !extraKEYtoOutputURI.isEmpty()){
				VariableSubstitution.putIntentExtrasAsResponseOutputs(intent,resp,extraKEYtoOutputURI);
			}
			sendResponse(msg, resp);
		}
	}

	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		// Empty, we handle asynchronously in handleRequest
		return null;
	}

}
