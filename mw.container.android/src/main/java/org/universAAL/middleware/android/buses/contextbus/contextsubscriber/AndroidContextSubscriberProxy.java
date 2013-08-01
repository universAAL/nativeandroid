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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber;

import org.universAAL.middleware.android.buses.common.AndroidNameParameterParser;
//import org.universAAL.middleware.android.buses.contextbus.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.contextbus.IGroundingIDWrapper;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ContextSubscriberGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.SubjectFilterXmlObj;
import org.universAAL.middleware.android.common.IntentUtils;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class AndroidContextSubscriberProxy extends ContextSubscriber implements
		IGroundingIDWrapper {

	private static final String TAG = AndroidContextSubscriberProxy.class
			.getCanonicalName();

	protected String packageName;
	protected String contextSubscriberGroundingID;
	protected String androidUniqueName;
	protected ContextSubscriberGroundingXmlObj contextSubscriberGrounding;
	protected Context context;

	private String myID;

	public AndroidContextSubscriberProxy(ModuleContext mc/*
														 * AndroidContextBusImpl
														 * androidContextBus
														 */, boolean register,
			ContextEventPattern[] initialSubscriptions, String packageName,
			String contextSubscriberGroundingID, String androidUniqueName,
			ContextSubscriberGroundingXmlObj contextSubscriberGrounding,
			Context context) {

		super(mc/* androidContextBus */, initialSubscriptions/* , false */);

		this.packageName = packageName;
		this.contextSubscriberGroundingID = contextSubscriberGroundingID;
		this.androidUniqueName = androidUniqueName;
		this.contextSubscriberGrounding = contextSubscriberGrounding;
		this.context = context;
		// PATCH In super(), it already gets registered in the bus, but because
		// the serviceGroundingID is not set yet, it is not properly registered
		// in android registry.
		// Therefore must deregister from the bus and register again, but this
		// time with serviceGroundingID set, so that it gets properly added to
		// the android registry
		theBus.unregister(this.busResourceURI, this);
		this.busResourceURI = theBus.register(mc, this,
				BusMemberType.subscriber);
		myID = this.busResourceURI;
		if (register) {
			// myID = androidContextBus.register(this, initialSubscriptions);
			if (initialSubscriptions != null) {
				// This was done at super, but failed because of patch
				// TODO Check that it really fails there, and I am not
				// duplicating
				addNewRegParams(initialSubscriptions);
			}
			// myID = this.busResourceURI;
			// populateLocalID(myID);
		}
	}

	public AndroidContextSubscriberProxy(ModuleContext mc/*
														 * AndroidContextBusImpl
														 * androidContextBus
														 */,
			String packageName, String groundingID, String androidUniqueName,
			ContextSubscriberGroundingXmlObj contextSubscriberGrounding,
			Context context, String memberID) {
		this(mc/* androidContextBus */, false, null, packageName, groundingID,
				androidUniqueName, contextSubscriberGrounding, context);

		// In this c'tor, no registration is performed, therefore populate the
		// ID's
		// .busResourceURI remains the same as the first time it gets (failed)
		// registered!!! In theory it is not used anywhere, what matters for
		// android port is myID, but take into account.
		// myID =
		// this.busResourceURI;//Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX +
		// memberID;
		// populateLocalID(memberID);
	}

	@Override
	public void communicationChannelBroken() {
		// This is called when the bus is being stopped.
		// Do nothing...
	}

	@Override
	public void handleContextEvent(ContextEvent contextEvent) {
		// Here we should map between the uAAL world to the Android one

		// Extract the action
		ActionXmlObj action = contextSubscriberGrounding.getAction();

		// Make sure that the predicate is the correct one
		if (!contextEvent.getRDFPredicate().equals(
				action.getPredicatePropName())) {
			Log.w(TAG,
					"Got context event with predicate ["
							+ contextEvent.getRDFPredicate()
							+ "] that is not equal to the action predicate that ["
							+ action.getPredicatePropName() + "]");
			return;
		}

		// Initiate the intent to be sent
		Intent intent = new Intent(action.getAndroidAction());

		// Extract the subject URI from the event
		String contextEventSubjectURI = contextEvent.getSubjectURI();

		for (SubjectFilterXmlObj subjectFilter : action.getSubjectFilters()) {
			// Extract the subject android name
			String subjectAndroidName = subjectFilter.getAndroidName();

			// Parse it
			AndroidNameParameterParser parser = AndroidNameParameterParser
					.parseAndroidName(subjectAndroidName);

			// Add the subject parameter to the extras
			if (null != parser) {
				if (contextEventSubjectURI.contains(parser
						.getAndroidNameWithoutParameter())) {
					String inputAndroidValue = contextEventSubjectURI
							.substring(parser.getAndroidNameWithoutParameter()
									.length(), contextEventSubjectURI.length());

					// Add the input value to the intent
					intent.putExtra(parser.getAndroidParameter(),
							inputAndroidValue);

					// Found a matching, don't continue to fine other one
					break;
				}
			}
		}

		// Add the object to the extras - currently support
		IntentUtils.addToExtra(action.getObject().getAndroidExtraParameter(),
				action.getObject().getAndroidExtraParameterJavaClass(),
				contextEvent.getRDFObject(), intent);

		// Send a broadcast message
		context.sendBroadcast(intent);
	}

	public String getPackageName() {
		return packageName;
	}

	public String getGroundingID() {
		return contextSubscriberGroundingID;
	}

	public String getAndroidUniqueName() {
		return androidUniqueName;
	}

	public ContextSubscriberGroundingXmlObj getContextSubscriberGrounding() {
		return contextSubscriberGrounding;
	}
}
