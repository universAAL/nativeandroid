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

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.http.client.utils.URLEncodedUtils;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.impl.ServiceRealization;
import org.universAAL.middleware.service.owl.Service;
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
	private WeakReference<Context> contextRef;
	private String action=null;
	private String category=null;
	private String replyAction=null;
	private String replyCategory=null;
	private String grounding=null;
	private Hashtable<String,String> inputURItoExtraKEY;
	private Hashtable<String,String> extraKEYtoOutputURI;
	private BroadcastReceiver receiver=null;
	private String spURI=null;
	
	/**
	 * Constructor for the proxy.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @param context
	 *            The Android context.
	 */
	public ServiceCalleeProxy(GroundingParcel parcel, Context context) {
		super(AndroidContext.THE_CONTEXT, prepareProfiles(parcel.getGrounding()));
		contextRef=new WeakReference<Context>(context);
		action=parcel.getAction();
		category=parcel.getCategory();
		replyAction=parcel.getReplyAction();
		replyCategory=parcel.getReplyCategory();
		grounding=prepareGrounding(parcel.getGrounding());
		fillTableIN(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		fillTableOUT(parcel.getLengthOUT(),parcel.getKeysOUT(), parcel.getValuesOUT());
		ServiceProfile[] sps = prepareProfiles(parcel.getGrounding());
		spURI = sps[0].getURI();
		// This is for RAPI. 
		sync();
	}
	
	public void sync(){
		if(MiddlewareService.isGWrequired()){
			switch (Config.getRemoteType()) {
			case AppConstants.REMOTE_TYPE_GW:
				// Does not need syncing in GW, transparent
				break;
			case AppConstants.REMOTE_TYPE_RAPI:
				//Publish as well in the RAPI TODO What if offline!!!!!!!!?????
				RAPIManager.invokeInThread(RAPIManager.PROVIDES, grounding);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Extract the Service Profile information from the grounding.
	 * 
	 * @param parcel
	 *            The parcelable from of the grounding of the metadata.
	 * @return The uAAL Service Profile.
	 */
	private static ServiceProfile[] prepareProfiles(String grounding) {
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
		ServiceProfile sp=(ServiceProfile)parser.deserialize(grounding);
		String tenant=Config.getServerUSR();
		try {
			tenant=URLEncoder.encode(tenant,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// Matchmaking uses SP URI and Process URI (which is built from Service
		// URI) to distinguish callees, so these must be made unique for each
		// tenant despite being the same profile in each of them.
		sp.changeURI(sp.getURI() + tenant);// Add tenant ID to SP URI
		Service serv = sp.getTheService();
		String servURI=serv.getURI();
		serv.changeURI(servURI + tenant);// Add tenant ID to Service URI
		sp.setProperty(Service.PROP_OWLS_PRESENTED_BY, serv);
		String process = sp.getProcessURI();// Process URI = Service URI + "Process" (but maybe suffix will change in future?)
		String suffix=process.substring(servURI.length());// Get the "Process" suffix of the Process URI
		sp.changeProperty(ServiceProfile.PROP_OWLS_PROFILE_HAS_PROCESS, null);// Remove first cause doesnt work well
		sp.setProperty(ServiceProfile.PROP_OWLS_PROFILE_HAS_PROCESS, servURI + tenant + suffix);// Add tenant ID in Process URI
		return new ServiceProfile[] { sp };
	}
	
	/**
	 * Modify the serialized grounding to append tenant ID to relevant URIs.
	 * Equivalent to prepareGrounding but returns it serialized.
	 * 
	 * @param serial
	 *            The grounding of the metadata.
	 * @return The modified uAAL Service Profile.
	 */
	private static String prepareGrounding(String grounding) {
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
		ServiceProfile sp=(ServiceProfile)parser.deserialize(grounding);
		String tenant=Config.getServerUSR();
		try {
			tenant=URLEncoder.encode(tenant,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// Matchmaking uses SP URI and Process URI (which is built from Service
				// URI) to distinguish callees, so these must be made unique for each
				// tenant despite being the same profile in each of them.
		sp.changeURI(sp.getURI() + tenant);// Add tenant ID to SP URI
		Service serv = sp.getTheService();
		String servURI=serv.getURI();
		serv.changeURI(servURI + tenant);// Add tenant ID to Service URI
		sp.setProperty(Service.PROP_OWLS_PRESENTED_BY, serv);
		String process = sp.getProcessURI();// Process URI = Service URI + "Process" (but maybe suffix will change in future?)
		String suffix=process.substring(servURI.length());// Get the "Process" suffix of the Process URI
		sp.changeProperty(ServiceProfile.PROP_OWLS_PROFILE_HAS_PROCESS, null);// Remove first cause doesnt work well
		sp.setProperty(ServiceProfile.PROP_OWLS_PROFILE_HAS_PROCESS, servURI + tenant + suffix);// Add tenant ID in Process URI
		return parser.serialize(sp);
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
	public void close() {
		super.close();
		Context ctxt=this.contextRef.get();
		if (ctxt!=null && receiver!=null){
			ctxt.unregisterReceiver(receiver);
		}
	}

	@Override
	public void handleRequest(BusMessage m) {
		ServiceCall call=(ServiceCall) m.getContent();
		// Extract the origin action and category from the call
		String fromAction=(String)call.getNonSemanticInput(AppConstants.UAAL_META_PROP_FROMACTION);
		String fromCategory=(String)call.getNonSemanticInput(AppConstants.UAAL_META_PROP_FROMCATEGORY);
		Boolean needsOuts=(Boolean)call.getNonSemanticInput(AppConstants.UAAL_META_PROP_NEEDSOUTPUTS);
		boolean isNonIntrusive = fromAction != null	&& fromAction.equals(action) 
				&& fromCategory != null	&& fromCategory.equals(category);
		// This means the serv Intent in the caller proxy is the same as the
		// serv Intent in destination native app. Therefore the destination will
		// already have received it and we must not send the intent to avoid
		// duplication or infinite loops.
		if (!isNonIntrusive) {
			// In this case the serv intents are different because the origin
			// action+cat is being used as a kind of API to call the SCaller.
			// In this case we have to relay the call to the destination native app.
			Context ctxt=contextRef.get();
			if(ctxt!=null){
				// Prepare an intent for sending to Android grounded service
				Intent serv = new Intent(action);
				serv.addCategory(category);
				boolean expecting = false;
				// If a response is expected, prepare a callback receiver (which must be called by uaalized app)
				if((replyAction!=null && !replyAction.isEmpty()) && (replyCategory!=null && !replyCategory.isEmpty())){
					// Tell the destination where to send the reply
					serv.putExtra(AppConstants.ACTION_META_REPLYTOACT, replyAction);
					serv.putExtra(AppConstants.ACTION_META_REPLYTOCAT, replyCategory);
					// Register the receiver for the reply
					receiver=new ServiceCalleeProxyReceiver(m);// TODO Can only handle 1 call at a time per proxy
					IntentFilter filter=new IntentFilter(replyAction);
					filter.addCategory(replyCategory);
					ctxt.registerReceiver(receiver, filter);
					expecting=true;
				} else if (needsOuts!=null && needsOuts.booleanValue()){
					// No reply* fields set, but caller still needs a response,
					// lets build one (does not work for callers outside android MW)
					Random r = new Random();
					String action=AppConstants.ACTION_REPLY+r.nextInt();
					serv.putExtra(AppConstants.ACTION_META_REPLYTOACT, action);
					serv.putExtra(AppConstants.ACTION_META_REPLYTOCAT, Intent.CATEGORY_DEFAULT);
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
				// Flag to avoid feeding back the intent to bus when intent is the same in app and in callerproxy 
				serv.putExtra(AppConstants.ACTION_META_FROMPROXY, true);
				// Send the intent to Android grounded service
				ComponentName started;
				try{
					started=ctxt.startService(serv);//In 5.0 preview it returned null, in API 21, it throws exception
				}catch(IllegalArgumentException e){
					started=null;//Set to null and act as if there is no service: try with broadcast
				}
				if(started==null){
					// No android service was there, try with broadcast. Before, here it used to send failure response
					ctxt.sendBroadcast(serv); // No way to know if received. If no response, bus will timeout (?)
				}else if(!expecting){
					// There is no receiver waiting a response, send success now
					ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
					sendResponse(m,resp);
				}
				//TODO Handle timeout
			}
		}
	}
	
	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		// Empty, we handle asynchronously in handleRequest
		return null;
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
		Context ctxt = contextRef.get();
		if (ctxt != null) {
			if (receiver != null) {
				ctxt.unregisterReceiver(receiver);
				//this method was called from the receiver, so its unregistering itself. Dirty but it works...
				receiver = null;
			}
		}
		// Then send back the response
		sr.setProperty(ServiceRealization.uAAL_SERVICE_PROVIDER,
			    new Resource(busResourceURI));
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
		
		public ServiceCalleeProxyReceiver(BusMessage m) {
			super();
			msg=m;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			ServiceResponse resp;
			CallStatus status = CallStatus.valueOf(intent.getStringExtra(CallStatus.MY_URI));
			if(status!=null){
				resp = new ServiceResponse(status);
			}else{
				resp = new ServiceResponse(CallStatus.succeeded);
			}
			if (extraKEYtoOutputURI!=null && !extraKEYtoOutputURI.isEmpty()){
				VariableSubstitution.putIntentExtrasAsResponseOutputs(intent,resp,extraKEYtoOutputURI);
			}
			sendResponse(msg, resp);
		}
	}
	
	// The following are additions for the management of calls coming from R API
	
	public String getSpURI(){
		return spURI;
	}
	
	/**
	 * This is an auxiliary method that invokes this proxy when a service
	 * request matched in the R API server, and the ServiceCall was sent here
	 * through GCM. We receive a ServiceCall not a BusMessage nor a
	 * ServiceRequest. It sends the response back to the R API rather than
	 * through the inner bus.
	 * 
	 * @param scall
	 *            The ServiceCall as received from R API through GCM.
	 * @param origincall
	 *            The original ServiceCall URI as specified by the server. It is
	 *            not the same as scall.getURI() since that object is created
	 *            here in the client.
	 */
	public void handleCallFromGCM(ServiceCall scall, String origincall) {
		Boolean needsOuts=(Boolean)scall.getNonSemanticInput(AppConstants.UAAL_META_PROP_NEEDSOUTPUTS);
		Context ctxt=contextRef.get();
		if(ctxt!=null){
			// Prepare an intent for sending to Android grounded service
			Intent serv = new Intent(action);
			serv.addCategory(category);
			boolean expecting=false;
			// If a response is expected, prepare a callback receiver (which must be called by uaalized app)
			if((replyAction!=null && !replyAction.isEmpty()) && (replyCategory!=null && !replyCategory.isEmpty())){
				// Tell the destination where to send the reply
				serv.putExtra(AppConstants.ACTION_META_REPLYTOACT, replyAction);
				serv.putExtra(AppConstants.ACTION_META_REPLYTOCAT, replyCategory);
				// Register the receiver for the reply
				receiver=new ServiceCalleeProxyReceiverGCM(origincall);
				IntentFilter filter=new IntentFilter(replyAction);
				filter.addCategory(replyCategory);
				ctxt.registerReceiver(receiver, filter);
				expecting=true;
			} else if (needsOuts!=null && needsOuts.booleanValue()){
				// No reply* fields set, but caller still needs a response, lets
				// build one (does not work for callers outside android MW)
				Random r = new Random();
				String action=AppConstants.ACTION_REPLY+r.nextInt();
				serv.putExtra(AppConstants.ACTION_META_REPLYTOACT, action);
				serv.putExtra(AppConstants.ACTION_META_REPLYTOCAT, Intent.CATEGORY_DEFAULT);
				// Register the receiver for the reply
				receiver=new ServiceCalleeProxyReceiverGCM(origincall);
				IntentFilter filter=new IntentFilter(action);
				filter.addCategory(Intent.CATEGORY_DEFAULT);
				ctxt.registerReceiver(receiver, filter);
				expecting=true;
			}
			// Take the inputs from the call and put them in the intent
			if (inputURItoExtraKEY!=null && !inputURItoExtraKEY.isEmpty()){
				VariableSubstitution.putCallInputsAsIntentExtras(scall, serv, inputURItoExtraKEY);
			}
			// Flag to avoid feeding back the intent to bus when intent is the same in app and in callerproxy 
			serv.putExtra(AppConstants.ACTION_META_FROMPROXY, true);
			// Send the intent to Android grounded service
			ComponentName started=null;
			try {
				// HACK: In android 5.0 it is forbidden to send implicit service intents like this one 
				started=ctxt.startService(serv);
			} catch (Exception e) {
				// Therefore if it fails, fail silently and try again with broadcast receivers
				started=null;
			}
			if(started==null){
				// No android service was there, try with broadcast. Before, here it used to send failure response
				ctxt.sendBroadcast(serv); // No way to know if received. If no response, bus will timeout (?)
			}else if(!expecting){
				// There is no receiver waiting a response, send success now
				ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
				sendResponseGCM(resp, origincall);
			}
			//TODO Handle timeout
		}
	}
	
	/**
	 * Sends the response back to the R API.
	 * 
	 * @param sresp
	 *            The ServiceResponse to deliver
	 * @param callURI
	 *            The ServiceCall URI that originated this response in the server
	 */
	private void sendResponseGCM(final ServiceResponse sresp, final String callURI) {
		// First, since we already have the response (good or bad), remove the receiver
		Context ctxt = contextRef.get();
		if (ctxt != null) {
			if (receiver != null) {
				ctxt.unregisterReceiver(receiver);
				//this method was called from the receiver, so its unregistering itself. Dirty but it works...
				receiver = null;
			}
		}
		StringBuilder strb = new StringBuilder();
		List outputs = sresp.getOutputs();
		if (outputs != null && outputs.size() > 0) {
			for (Iterator iter1 = outputs.iterator(); iter1.hasNext();) {
				Object obj = iter1.next();
				if (obj instanceof ProcessOutput) {
					ProcessOutput output = (ProcessOutput) obj;
					Object value=output.getParameterValue();
					String type="";
					if(value instanceof Resource){
						type=((Resource) value).getType();
					}else if (value instanceof List){
						if( ((List) value).get(0)  instanceof Resource ){
							type=((Resource)((List) value).get(0)).getType();
						}else{
							type=TypeMapper.getDatatypeURI(((List) value).get(0));
						}
					}else{
						type=TypeMapper.getDatatypeURI(((List) value).get(0));
					}
					strb.append(output.getURI()).append("=")
					.append(output.getParameterValue().toString()).append("@").append(type)
					.append("\n");
				} else if (obj instanceof List) {
					List outputLists = (List) obj;
					for (Iterator iter2 = outputLists.iterator(); iter2
							.hasNext();) {
						ProcessOutput output = (ProcessOutput) iter2.next();
						Object value=output.getParameterValue();
						String type="";
						if(value instanceof Resource){
							type=((Resource) value).getType();
						}else if (value instanceof List){
							if( ((List) value).get(0)  instanceof Resource ){
								type=((Resource)((List) value).get(0)).getType();
							}else{
								type=TypeMapper.getDatatypeURI(((List) value).get(0));
							}
						}else{
							type=TypeMapper.getDatatypeURI(((List) value).get(0));
						}
						strb.append(output.getURI())
						.append("=")
						.append(output.getParameterValue()
								.toString()).append("@").append(type).append("\n");
					}
				}
			}
		}
	    strb.append("status=").append(sresp.getCallStatus().toString()).append("\n");
	    strb.append("call=").append(callURI).append("\n");
	    strb.append("TURTLE").append("\n");
	    MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });//TODO throw ex if error
	    strb.append(parser.serialize(sresp));
	    //Send callback response to server
	    RAPIManager.invokeInThread(RAPIManager.RESPONSES, strb.toString());
	}

	/**
	 * Auxiliary class representing the Broadcast Receiver registered by the
	 * middleware where apps will send intents when they have to return a
	 * response to uAAL. This is a variation for the R API through GCM, which
	 * will send the response through R API rather than the inner bus.
	 * 
	 * @author alfiva
	 * 
	 */
	public class ServiceCalleeProxyReceiverGCM extends BroadcastReceiver {
		String callURI;
		
		public ServiceCalleeProxyReceiverGCM(String call) {
			super();
			callURI=call;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			ServiceResponse resp = new ServiceResponse(CallStatus.succeeded);
			if (extraKEYtoOutputURI!=null && !extraKEYtoOutputURI.isEmpty()){
				VariableSubstitution.putIntentExtrasAsResponseOutputs(intent,resp,extraKEYtoOutputURI);
			}
			sendResponseGCM(resp,callURI);
		}
	}

}
