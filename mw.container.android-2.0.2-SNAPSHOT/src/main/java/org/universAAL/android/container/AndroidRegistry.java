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

import java.util.Hashtable;

import org.universAAL.android.proxies.ContextPublisherProxy;
import org.universAAL.android.proxies.ContextSubscriberProxy;
import org.universAAL.android.proxies.ServiceCalleeProxy;
import org.universAAL.android.proxies.ServiceCallerProxy;
import org.universAAL.android.utils.GroundingParcel;

import android.content.Context;

public class AndroidRegistry {
	public static final int TYPE_CPUBLISHER = 1;
	public static final int TYPE_CSUBSCRIBER = 2;
	public static final int TYPE_SCALLEE = 3;
	public static final int TYPE_SCALLER = 4;
	
	// Each uAAL wrapper is created here and therefore it must stay here in memory-> dont use WeakRefs
	private static Hashtable<String, ContextPublisherProxy> cpublishers = new Hashtable<String, ContextPublisherProxy>();
	private static Hashtable<String, ContextSubscriberProxy> csubscribers = new Hashtable<String, ContextSubscriberProxy>();
	private static Hashtable<String, ServiceCalleeProxy> scallees = new Hashtable<String, ServiceCalleeProxy>();
	private static Hashtable<String, ServiceCallerProxy> scallers = new Hashtable<String, ServiceCallerProxy>();

	public static synchronized void register(String id, GroundingParcel parcel,	int type,  Context context) {
		switch (type) {
		case TYPE_CPUBLISHER:
			ContextPublisherProxy cpub=new ContextPublisherProxy(parcel,context);
			cpublishers.put(id, cpub);
			break;
		case TYPE_CSUBSCRIBER:
			ContextSubscriberProxy csub=new ContextSubscriberProxy(parcel,context);
			csubscribers.put(id, csub);
			break;
		case TYPE_SCALLEE:
			ServiceCalleeProxy scee=new ServiceCalleeProxy(parcel,context);
			scallees.put(id, scee);
			break;
		case TYPE_SCALLER:
			ServiceCallerProxy scer=new ServiceCallerProxy(parcel,context);
			scallers.put(id, scer);
			break;
		default:
			System.out.println("//////registering nothing");
			break;
		}
	}
	
	public static synchronized void unregister(String id, int type){
		switch (type) {
		case TYPE_CPUBLISHER:
			System.out.println("//////unregistering publisher");
			cpublishers.remove(id).close();
			break;
		case TYPE_CSUBSCRIBER:
			System.out.println("//////unregistering subscriber");
			csubscribers.remove(id).close();
			break;
		case TYPE_SCALLEE:
			System.out.println("//////unregistering callee");
			scallees.remove(id).close();
			break;
		case TYPE_SCALLER:
			System.out.println("//////unregistering caller");
			scallers.remove(id).close();
			break;
		default:
			System.out.println("//////unregistering nothing");
			break;
		}
	}
}
