package org.universAAL.android.proxies;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.android.utils.VariableSubstitution;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.aapi.AapiServiceRequest;
import org.universAAL.ri.gateway.communicator.service.RemoteSpacesManager;
import org.universAAL.ri.gateway.eimanager.ImportEntry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ServiceCallerProxy extends ServiceCaller implements SharedObjectListener {
	private WeakReference<Context> context; //TODO memory issues?
	private String action=null;
	private String category=null;
	private String replyAction=null;
	private String replyCategory=null;
	private String remote=null;
	private ServiceCallerProxyReceiver receiver=null;
	private String grounding=null;
	private Hashtable<String,String> extraKEYtoInputURI;
	private Hashtable<String,String> outputURItoExtraKEY;
	private ImportEntry entry=null;
	
	public ServiceCallerProxy(GroundingParcel parcel, Context context) {
		super(AndroidContext.THE_CONTEXT);
		this.context=new WeakReference<Context>(context);
		this.action=parcel.getAction();
		this.category=parcel.getCategory();
		this.replyAction=parcel.getReplyAction();
		this.replyCategory=parcel.getReplyCategory();
		this.grounding=parcel.getGrounding();
		fillTable1(parcel.getLengthIN(),parcel.getKeysIN(), parcel.getValuesIN());
		fillTable2(parcel.getLengthOUT(),parcel.getKeysOUT(), parcel.getValuesOUT());
		this.receiver=new ServiceCallerProxyReceiver();
		IntentFilter filter=new IntentFilter(this.action);
		filter.addCategory(this.category);
		context.registerReceiver(receiver, filter);//TODO use the other longer register method
		// This is for GW
		if(parcel.getRemote()!=null && !parcel.getRemote().isEmpty()){
			remote=parcel.getRemote();
			RemoteSpacesManager[] gw = (RemoteSpacesManager[]) AndroidContainer.THE_CONTAINER
					.fetchSharedObject(AndroidContext.THE_CONTEXT,
							new Object[] { RemoteSpacesManager.class.getName() },
							this);
			if(gw!=null && gw.length>0){
				try { //In remote tag must be: URIoftheservice@NAMESPACEoftheserver
					String[] uris=remote.split("@");
					entry=gw[0].importRemoteService(this, uris[0], uris[1]);
				} catch (Exception e) {
					System.out.println("Could not import remote services");
				}
			}
		}
	}

	private void fillTable1(int length, String[] keys, String[] values) {
		if (length == 0) {
			extraKEYtoInputURI = null;
		} else {
			extraKEYtoInputURI = new Hashtable<String, String>(length);
			for (int i = 0; i < length; i++) {
				extraKEYtoInputURI.put(keys[i], values[i]);
			}
		}
	}

	private void fillTable2(int length, String[] keys, String[] values) {
		if (length == 0) {
			outputURItoExtraKEY = null;
		} else {
			outputURItoExtraKEY = new Hashtable<String, String>(length);
			for (int i = 0; i < length; i++) {
				outputURItoExtraKEY.put(keys[i], values[i]);
			}
		}
	}
	
	public ServiceCallerProxyReceiver getReceiver() {
		return receiver;
	}

	@Override
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleResponse(String reqID, ServiceResponse response) {
		Context ctxt=context.get();
		if(ctxt!=null && (replyAction!=null && !replyAction.isEmpty()) && (replyCategory!=null && !replyCategory.isEmpty())){
			//If the app is really interested in the result
			Intent start = new Intent(replyAction);
			start.addCategory(replyCategory);
			if(outputURItoExtraKEY!=null && !outputURItoExtraKEY.isEmpty()){
				VariableSubstitution.putResponseOutputsAsIntentExtras(response, start, outputURItoExtraKEY);
			}
//			ctxt.startService(start);//TODO Allow activities and services too?
			ctxt.sendBroadcast(start);
			//TODO Send to replyact/cat that the app said at first (embed into sreq, then in sresp in callee and then read here)
		}
	}
	
	public class ServiceCallerProxyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("//////onReceive");
			MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
					.fetchSharedObject(AndroidContext.THE_CONTEXT,
							new Object[] { MessageContentSerializerEx.class
									.getName() });
			ServiceRequest sr;
			if(extraKEYtoInputURI!=null && !extraKEYtoInputURI.isEmpty()){
				String turtleReplaced=VariableSubstitution.putIntentExtrasAsRequestInputs(intent, grounding, extraKEYtoInputURI);
				sr=(ServiceRequest)parser.deserialize(turtleReplaced);
			}else{
				sr=(ServiceRequest)parser.deserialize(grounding);
			}
			// This is for identifying the origin of the request, to avoid duplications in callee later
			// TODO I have to make this hack to convert the SR into an AAPI SR in order to inject metadata.
			// It would be soooo much easier if ServiceRequest allowed setProperty of AAPI metadata directly...
			AapiServiceRequest srmeta=new AapiServiceRequest(sr.getURI());
			srmeta.setProperty(ServiceRequest.PROP_AGGREGATING_FILTER, sr.getProperty(ServiceRequest.PROP_AGGREGATING_FILTER));
			srmeta.setProperty(ServiceRequest.PROP_REQUESTED_SERVICE, sr.getProperty(ServiceRequest.PROP_REQUESTED_SERVICE));
			srmeta.setProperty(ServiceRequest.PROP_REQUIRED_PROCESS_RESULT, sr.getProperty(ServiceRequest.PROP_REQUIRED_PROCESS_RESULT));
			srmeta.setProperty(ServiceRequest.PROP_uAAL_SERVICE_CALLER, sr.getProperty(ServiceRequest.PROP_uAAL_SERVICE_CALLER));
			srmeta.setProperty(ServiceRequest.PROP_uAAL_INVOLVED_HUMAN_USER, sr.getProperty(ServiceRequest.PROP_uAAL_INVOLVED_HUMAN_USER));
			srmeta.addInput(IntentConstants.UAAL_META_PROP_FROMACTION, action);
			srmeta.addInput(IntentConstants.UAAL_META_PROP_FROMCATEGORY, category);
			Resource[] outputs=sr.getRequiredOutputs();
			if(outputs.length>0){
				srmeta.addInput(IntentConstants.UAAL_META_PROP_NEEDSOUTPUTS, Boolean.TRUE);
				//TODO  I have to add this flag metadata because otherwise callee doesnt know if an output is really needed
			}
			sendRequest(srmeta);
		}
	}
	
	//For the GW
	public void sharedObjectAdded(Object sharedObj, Object removeHook) {
		if(remote!=null && !remote.isEmpty() && sharedObj!=null && sharedObj instanceof RemoteSpacesManager){
			try {
				String[] uris=remote.split("@");
				entry=((RemoteSpacesManager)sharedObj).importRemoteService(this, uris[0], uris[1]);
			} catch (Exception e) {
				System.out.println("Could not import remote services");
			}
		}
	}

	public void sharedObjectRemoved(Object removeHook) {
		if(entry!=null && removeHook!=null && removeHook instanceof RemoteSpacesManager){
			try {
				((RemoteSpacesManager)removeHook).unimportRemoteService(entry);
			} catch (Exception e) {
				System.out.println("Could not unimport remote services");
			}
		}
	}
}
