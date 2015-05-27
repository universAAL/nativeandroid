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
package org.universAAL.android.container;

import java.util.Enumeration;
import java.util.Hashtable;

import org.universAAL.android.proxies.ContextPublisherProxy;
import org.universAAL.android.proxies.ContextSubscriberProxy;
import org.universAAL.android.proxies.ServiceCalleeProxy;
import org.universAAL.android.proxies.ServiceCallerProxy;
import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.AppConstants;

import android.content.Context;
import android.util.Log;

/**
 * Class that works as a singleton where the instances of proxy classes are
 * maintained.
 * 
 * @author alfiva
 * 
 */
public class AndroidRegistry {
	private static final String TAG = "AndroidRegistry";
	// Each uAAL wrapper is created here and therefore it must stay here in memory-> dont use WeakRefs
	private static Hashtable<String, ContextPublisherProxy> cpublishers = new Hashtable<String, ContextPublisherProxy>();
	private static Hashtable<String, ContextSubscriberProxy> csubscribers = new Hashtable<String, ContextSubscriberProxy>();
	private static Hashtable<String, ServiceCalleeProxy> scallees = new Hashtable<String, ServiceCalleeProxy>();
	private static Hashtable<String, ServiceCallerProxy> scallers = new Hashtable<String, ServiceCallerProxy>();
	// I need this for R API: links SP URI to ID, which I need when receiving callbacks from R API
	private static Hashtable<String, String> scalleesAux = new Hashtable<String, String>();

	/**
	 * Create and store in memory an instance of a proxy class for a given
	 * grounding.
	 * 
	 * @param id
	 *            The unique ID of the grounding.
	 * @param parcel
	 *            The Parcelable representation of the grounding.
	 * @param type
	 *            The type of uAAL wrapper.
	 * @param context
	 *            The Android context.
	 */
	public static synchronized void register(String id, GroundingParcel parcel,	int type,  Context context) {
		switch (type) {
		case AppConstants.TYPE_CPUBLISHER:
			Log.d(TAG, "Registering publisher");
			ContextPublisherProxy cpub=new ContextPublisherProxy(parcel,context);
			cpublishers.put(id, cpub);
			break;
		case AppConstants.TYPE_CSUBSCRIBER:
			Log.d(TAG, "Registering subscriber");
			ContextSubscriberProxy csub=new ContextSubscriberProxy(parcel,context);
			csubscribers.put(id, csub);
			break;
		case AppConstants.TYPE_SCALLEE:
			Log.d(TAG, "Registering scallee");
			ServiceCalleeProxy scee=new ServiceCalleeProxy(parcel,context);
			scallees.put(id, scee);
			if(scee.getSpURI()!=null){
				scalleesAux.put(scee.getSpURI(),id);
			}
			break;
		case AppConstants.TYPE_SCALLER:
			Log.d(TAG, "Registering scaller");
			ServiceCallerProxy scer=new ServiceCallerProxy(parcel,context);
			scallers.put(id, scer);
			break;
		default:
			Log.d(TAG, "Registering nothing");
			break;
		}
	}
	
	/**
	 * Remove an instance of a proxy class for a given grounding.
	 * 
	 * @param id
	 *            The unique ID of the grounding.
	 * @param type
	 *            The type of uAAL wrapper.
	 */
	public static synchronized void unregister(String id, int type){
		switch (type) {
		case AppConstants.TYPE_CPUBLISHER:
			Log.d(TAG, "Unregistering publisher");
			cpublishers.remove(id).close();
			break;
		case AppConstants.TYPE_CSUBSCRIBER:
			Log.d(TAG, "Unregistering subscriber");
			csubscribers.remove(id).close();
			break;
		case AppConstants.TYPE_SCALLEE:
			Log.d(TAG, "Unregistering callee");
			ServiceCalleeProxy scee = scallees.remove(id);
			scee.close();
			if(scee.getSpURI()!=null){
				scalleesAux.remove(scee.getSpURI());
			}
			break;
		case AppConstants.TYPE_SCALLER:
			Log.d(TAG, "Unregistering caller");
			scallers.remove(id).close();
			break;
		default:
			Log.d(TAG, "Unregistering nothing");
			break;
		}
	}
	
	/**
	 * Special method that unregisters all the registered proxies. This is to be
	 * used when uAAL is closing, thus avoiding to scan individual packages and
	 * unregister them one by one.
	 */
	public static synchronized void unregisterAll() {
		Log.d(TAG, "Unregistering everything");
		Enumeration<ContextPublisherProxy> cpubs=cpublishers.elements();
		while(cpubs.hasMoreElements()){
			cpubs.nextElement().close();
		}
		cpublishers.clear();
		
		Enumeration<ContextSubscriberProxy> csubs=csubscribers.elements();
		while(csubs.hasMoreElements()){
			csubs.nextElement().close();
		}
		csubscribers.clear();
		
		Enumeration<ServiceCallerProxy> scers=scallers.elements();
		while(scers.hasMoreElements()){
			scers.nextElement().close();
		}
		scallers.clear();
		
		Enumeration<ServiceCalleeProxy> scees=scallees.elements();
		while(scees.hasMoreElements()){
			scees.nextElement().close();
		}
		scallees.clear();
		scalleesAux.clear();
	}
	
	/**
	 * Auxiliary method to access the list of registered callees and call
	 * directly the one addressed from GCM
	 * 
	 * @param uri
	 *            URI of the ServiceProfile which matching originated a call
	 *            from R API through GCM.
	 * @return The instance of ServiceCalleeProxy representing it
	 */
	public static ServiceCalleeProxy getCallee(String uri) {
		String id=scalleesAux.get(uri);
		if(id!=null){
			return scallees.get(id);
		}
		return null;		
	}
	
	/**
	 * Requests all proxies to sync to the remote node through either GW or R
	 * API.
	 */
	public static void sync(){
		Log.d(TAG, "Syncing proxies");
		Enumeration<ContextPublisherProxy> cpubs=cpublishers.elements();
		while(cpubs.hasMoreElements()){
			cpubs.nextElement().sync();
		}
		
		Enumeration<ContextSubscriberProxy> csubs=csubscribers.elements();
		while(csubs.hasMoreElements()){
			csubs.nextElement().sync();
		}
		
		Enumeration<ServiceCallerProxy> scers=scallers.elements();
		while(scers.hasMoreElements()){
			scers.nextElement().sync();
		}
		
		Enumeration<ServiceCalleeProxy> scees=scallees.elements();
		while(scees.hasMoreElements()){
			scees.nextElement().sync();
		}
	}
}
