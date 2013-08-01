/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.android.buses.contextbus.contextpublisher;

//import org.universAAL.middleware.android.buses.contextbus.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.contextbus.IGroundingIDWrapper;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.ContextRequestsMngr;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.container.ModuleContext;
//import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.util.Constants;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class AndroidContextPublisherProxy extends ContextPublisher implements
		IGroundingIDWrapper {

	protected String packageName;
	protected String contextPublisherGroundingID;
	protected String androidUniqueName;
	protected ContextPublisherGroundingXmlObj contextPublisherGrounding;
	protected Context context;
	private String myID;

	public AndroidContextPublisherProxy(ModuleContext mc/*
														 * AndroidContextBusImpl
														 * androidContextBus
														 */, boolean register,
			ContextProvider providerInfo, String packageName,
			String contextPublisherGroundingID, String androidUniqueName,
			ContextPublisherGroundingXmlObj contextPublisherGrounding,
			Context context) {

		super(mc/* androidContextBus */, providerInfo/* , false */);

		this.packageName = packageName;
		this.contextPublisherGroundingID = contextPublisherGroundingID;
		this.androidUniqueName = androidUniqueName;
		this.contextPublisherGrounding = contextPublisherGrounding;
		this.context = context;
		// PATCH In super(), it already gets registered in the bus, but because
		// the serviceGroundingID is not set yet, it is not properly registered
		// in android registry.
		// Therefore must deregister from the bus and register again, but this
		// time with serviceGroundingID set, so that it gets properly added to
		// the android registry
		theBus.unregister(this.busResourceURI, this);
		this.busResourceURI = theBus
				.register(mc, this, BusMemberType.publisher);
		myID = this.busResourceURI;
		if (register) {
			// myID = androidContextBus.register(this,
			// new ContextEventPattern[] {});
			// myID=this.busResourceURI;

			// Register a broadcast receiver to listen for the action
			ContextRequestsMngr
					.registerToContextPublisherRequestGroundingActions(myID,
							contextPublisherGrounding, context);
		}
	}

	public AndroidContextPublisherProxy(ModuleContext mc/*
														 * AndroidContextBusImpl
														 * androidContextBus
														 */,
			ContextProvider providerInfo, String packageName,
			String groundingID, String androidUniqueName,
			ContextPublisherGroundingXmlObj contextPublisherGrounding,
			Context context, String memberID) {
		this(mc/* androidContextBus */, false, providerInfo, packageName,
				groundingID, androidUniqueName, contextPublisherGrounding,
				context);

		// In this c'tor, no registration is performed, therefore populate the
		// ID's
		// .busResourceURI remains the same as the first time it gets (failed)
		// registered!!! In theory it is not used anywhere, what matters for
		// android port is myID, but take into account.
		// myID =
		// this.busResourceURI;//Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX +
		// memberID;
	}

	@Override
	public void communicationChannelBroken() {
		// This is called when the bus is being stopped.
		// Do nothing...
	}

	public String getPackageName() {
		return packageName;
	}

	public String getGroundingID() {
		return contextPublisherGroundingID;
	}

	public String getAndroidUniqueName() {
		return androidUniqueName;
	}

	public ContextPublisherGroundingXmlObj getContextPublisherGrounding() {
		return contextPublisherGrounding;
	}
}
