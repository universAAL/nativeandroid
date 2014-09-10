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
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.utils.IntentConstants;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.ServiceCall;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
//It is a wakefulBr because it will call the MWService, and device should not sleep when sending the intent there!
public class GCMReceiver extends WakefulBroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("GCMReceiver", "%%%%%%%%%%%%% RECEIVED!!!!! %%%%%%%%%%%%%");
		if(MiddlewareService.mSettingRemoteType!=IntentConstants.REMOTE_TYPE_RAPI){
			return;//For now, double check. TODO make sure not needed
		}
		
		String method=intent.getStringExtra("method");
		MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { MessageContentSerializerEx.class
								.getName() });
		
		if (method.equals("SENDC")) {
			Log.d("GCMReceiver",
					"RECEIVED CONTEXT EVENT FROM SERVER WITH SUBJ: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_SUBJECT)
							+ " PRED: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_PREDICATE)
							+ " OBJ: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_OBJECT));
			
			if(parser!=null){
				String serial=intent.getStringExtra("param");
				if(serial!=null){
					ContextEvent cev=(ContextEvent)parser.deserialize(serial);
					if(cev!=null){
						DefaultContextPublisher cp=new DefaultContextPublisher(AndroidContext.THE_CONTEXT, cev.getProvider());
						cp.publish(cev);//Cheat: Deliver the context event as an impostor. The receiver will get it.
						cp.close();
						cp=null;
						//TODO check performance of creating a publisher per call (it eases the reuse of providerinfo)
					}
				}
			}
			
		} else if (method.equals("CALLS")) {
			Log.d("GCMReceiver",
					"RECEIVED SERVICE CALL FROM SERVER WITH INPUTS: "
							+ intent.getExtras().keySet().toString());
//			ServiceResponse sresp;
			if(parser!=null){
				String serial=intent.getStringExtra("param");
				String spuri=intent.getStringExtra("to");
				String origincall=intent.getStringExtra("call");
				if(serial!=null && spuri!=null){
					ServiceCall scall=(ServiceCall)parser.deserialize(serial);
					if(scall!=null){
						ServiceCalleeProxy calleeProxy = AndroidRegistry.getCallee(spuri);
						if(calleeProxy!=null){
							// I cannot use a caller impostor here because I receive a ServiceCall, not a ServiceRequest
							calleeProxy.handleCallFromGCM(scall, origincall);
	//						DefaultServiceCaller sc=new DefaultServiceCaller(AndroidContext.THE_CONTEXT);
	//						sresp=sc.call(scall);//Deliver the request as an impostor. The callee will get it and return resp.
	//						sc.close();
	//						sc=null;
							//TODO check performance of creating a caller per call (it eases the reuse of nothing!)
							//Now build the param with the outputs and the sresp and so on
//							StringBuilder strb = new StringBuilder();
//						    List outputs = sresp.getOutputs();
//						    if (outputs != null && outputs.size() > 0) {
//							for (Iterator iter1 = outputs.iterator(); iter1.hasNext();) {
//							    Object obj = iter1.next();
//							    if (obj instanceof ProcessOutput) {
//								ProcessOutput output = (ProcessOutput) obj;
//								strb.append(output.getURI()).append("=")
//									.append(output.getParameterValue().toString())
//									.append("\n");
//							    } else if (obj instanceof List) {
//								List outputLists = (List) obj;
//								for (Iterator iter2 = outputLists.iterator(); iter2
//									.hasNext();) {
//								    ProcessOutput output = (ProcessOutput) iter2.next();
//								    strb.append(output.getURI())
//									    .append("=")
//									    .append(output.getParameterValue()
//										    .toString()).append("\n");
//								}
//							    }
//							}
//						    }
//						    strb.append("status=").append(sresp.getCallStatus().toString()).append("\n");
//						    strb.append("call=").append(intent.getStringExtra("call"));
//						    strb.append("TURTLE").append("\n");
//						    strb.append(parser.serialize(sresp));
//						    //Send callback response to server
//						    RAPIManager.invokeInThread(RAPIManager.RESPONSES, strb.toString());
						}
					}
				}
			}
		}else{
			Log.d("GCMReceiver","RECEIVED SOMETHING??? FROM SERVER");
		}
	}

}
