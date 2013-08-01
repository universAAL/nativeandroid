/*	
	Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
	Institute of Information Science and Technologies 
	of the Italian National Research Council 

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
package org.universAAL.middleware.android.connectors.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.util.Log;

import ch.ethz.iks.slp.ServiceLocationEnumeration;
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

/**
 * This thread periodically browses the SLP network in order to find all the
 * AALSpaces registered
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class AndroidSLPBrowser implements Runnable {
	private final static String TAG = "AndroidSLPBrowser";
//	private Locator locator;
	private String aalSpaceServiceType;
	private String filter;
	// private ModuleContext context;
	private List<ServiceListener> listeners;
	private boolean stop = false;
	private static int MAX_RETRY = 3;
	private Set<AALSpaceCard> aalSpaces;

	public AndroidSLPBrowser( String aalSpaceServiceType,
			String filter, 
			List<ServiceListener> listeners) {
//		this.locator = locator;
		this.aalSpaceServiceType = aalSpaceServiceType;
		this.filter = filter;
		this.listeners = listeners;
		// this.context = context;
	}

	public void addListener(ServiceListener listener) {
		this.listeners.add(listener);
		Log.d(TAG, "New listener added!");
	}

	public void removeListener(ServiceListener listener) {
		this.listeners.remove(listener);
		Log.d(TAG, "Listener removed!");
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isStop() {
		return this.stop;
	}

	public void run() {
		aalSpaces = new HashSet<AALSpaceCard>();
		ServiceLocationEnumeration slenum = null;
		ServiceLocationEnumeration attribs = null;
		if (!stop) {
			try {
				System.out.println(">>>>> FINDING: "+new ServiceType(
						aalSpaceServiceType).toString()+" with "+filter);
				slenum = AndroidSLPDiscoveryConnector.getSLPLocator().findServices(new ServiceType(
						aalSpaceServiceType), null, filter);
				System.out.println(">>>>> SLENUMS: "+slenum.hasMoreElements());
				while (slenum.hasMoreElements()) {
					ServiceURL serviceURL = (ServiceURL) slenum.next();
					attribs = AndroidSLPDiscoveryConnector.getSLPLocator().findAttributes(serviceURL, null,
							AALSpaceCard.getSpaceAttributes());
					// FIX JSLP sometimes returns null attributes
					System.out.println(">>>>>> ATTRIBS: "+attribs.hasMoreElements());
					// attribs = locator.findAttributes(new ServiceType(
					// aalSpaceServiceType), null, AALSpaceCard
					// .getSpaceAttributes());
					if (attribs != null) {
						Log.v(TAG, "Unmarshalling AALSpace attributes...");
						AALSpaceCard spaceCard = new AALSpaceCard(
								AndroidSLPDiscoveryConnector
										.unmarshalServiceAttributes(attribs));
						spaceCard
								.setPeeringChannelName("mw.modules.aalspace.osgi");
						spaceCard.setRetry(MAX_RETRY);
						if (spaceCard.getCoordinatorID() != null) {
							aalSpaces.add(spaceCard);
							Log.v(TAG, "AALSpace attributes unmarshalled");
						}
					}
				}
				// Calling all the ServiceListeners
				System.out.println(">>>>>> LISTENERS: "+listeners.size());
				for (ServiceListener listener : listeners) {
					Log.v(TAG, "Calling the AALSpaceModule listeners...");
					listener.newAALSpacesFound(aalSpaces);
				}

			} catch (Exception e) {
				Log.v(TAG, "Error during AALSpace search: ", e);
			}
		}
	}

}
