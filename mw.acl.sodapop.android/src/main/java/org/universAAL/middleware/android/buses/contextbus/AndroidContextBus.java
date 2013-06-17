/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.android.buses.contextbus;

import java.lang.reflect.Constructor;

import org.universAAL.middleware.android.buses.common.AndroidNameParameterParser;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.AndroidContextPublisherProxy;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.ContextRequestsMngr;
import org.universAAL.middleware.android.buses.contextbus.service.AndroidContextBusService;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.android.common.ReflectionsUtils;
import org.universAAL.middleware.android.common.exceptions.IllegalGroundingFormat;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.impl.ContextBusImpl;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.BusStrategy;
import org.universAAL.middleware.bus.model.util.IRegistry;
import org.universAAL.middleware.util.Constants;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 18, 2012
 * 
 */
public class AndroidContextBus extends ContextBusImpl implements IAndroidBus {

    private static final String TAG = AndroidContextBus.class.getCanonicalName();

    protected Context context;

    public AndroidContextBus(ModuleContext mc, Context context) {
	super(mc);

	this.context = context;
    }

    public String getName() {
	return getBrokerName();
    }

    public String getPackageName() {
	return AndroidContextBusService.class.getPackage().getName();
    }

    public String getClassName() {
	return AndroidContextBusService.class.getCanonicalName();
    }

    @Override
    protected IRegistry createReigstry() {
	// TODO: consider how get context in other way (not via the sodapop)
	Context context = ((AbstractSodaPopAndroidImpl) sodapop).getContext();

	return new AndroidContextBusRegistry(this, context);
    }

    @Override
    protected BusStrategy createBusStrategy(SodaPop sodapop) {
	return new AndroidContextStrategy(sodapop);
    }

    public void sendContextPublishMessage(String contextPublisherID, String contextPublisherAction,
	    Bundle extrasSection) {
	// Extract the member ID
	String memberID = contextPublisherID.substring(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		.length()); // TODO: move this to some shared method

	// Get the AndroidContextPublisher from the DB
	AndroidContextPublisherProxy androidContextPublisher = (AndroidContextPublisherProxy) registry
		.getBusMemberByID(memberID);

	// Populate the context event
	ContextEvent contextEvent;
	try {
	    contextEvent = createContextEvent(contextPublisherAction, extrasSection,
		    androidContextPublisher);
	} catch (IllegalGroundingFormat e) {
	    String errMsg = e.getMessage() + " for MemberID [" + memberID + "]; AndroidAction ["
		    + contextPublisherAction + "]";
	    Log.e(TAG, errMsg);
	    return;
	}

	sendMessage(contextPublisherID, contextEvent);
    }

    private ContextEvent createContextEvent(String contextPublisherAction, Bundle extrasSection,
	    AndroidContextPublisherProxy androidContextPublisher) throws IllegalGroundingFormat {

	ContextPublisherGroundingXmlObj publisherGrounding = androidContextPublisher
		.getContextPublisherGrounding();
	ActionXmlObj action = publisherGrounding.getActionByName(contextPublisherAction);

	// TODO: check that the action was found

	// Parse the android name
	AndroidNameParameterParser parser = AndroidNameParameterParser.parseAndroidName(action
		.getSubject().getAndroidName());
	String paramValue = extrasSection.getString(parser.getAndroidParameter());
	String subjectUri = parser.getAndroidNameWithoutParameter() + paramValue;

	// Initiate the object to be set in the context event
	Constructor ctor = null;
	Object obj = null;
	try {
	    ctor = ReflectionsUtils.createCtorThatReceiveStringParam(action.getObject()
		    .getAndroidExtraParameterJavaClass());
	    obj = ReflectionsUtils.invokeCtorWithStringParam(ctor,
		    extrasSection.getString(action.getObject().getAndroidExtraParameter()));
	} catch (Throwable th) {

	    throw new IllegalAccessError(
		    "Unable to map the android intent to the uAAL world due to [" + th.getMessage()
			    + "]");
	}

	ContextEvent contextEvent = ContextEvent.constructSimpleEvent(subjectUri, action
		.getSubject().getUri(), action.getPredicate().getUri(), obj);

	return contextEvent;
    }

    public void unregisterByPackageName(String packageName) {
	// Extract list of member ID's that have the package name
	AndroidContextBusRegistry androidRegistryContexts = getAndroidRegistryContexts();
	BusMember[] members = androidRegistryContexts.getBusMembersByPackageName(packageName);

	// Iteratively unregister each member
	for (BusMember busMember : members) {
	    if (busMember instanceof ContextPublisher) {
		AndroidContextPublisherProxy publisher = (AndroidContextPublisherProxy) busMember;
		// Unregister from the bus
		unregister(publisher.getMyID(), publisher);

		// Unregister the receivers
		ContextRequestsMngr.unreigsterToContextPublisherGroundingActions(
			publisher.getContextPublisherGrounding(), context);
	    } else if (busMember instanceof ContextSubscriber) {
		AndroidContextSubscriberProxy subscriber = (AndroidContextSubscriberProxy) busMember;

		// Unregister from the bus
		unregister(subscriber.getMyID(), subscriber);
	    }
	}
    }

    private AndroidContextBusRegistry getAndroidRegistryContexts() {
	return (AndroidContextBusRegistry) registry;
    }
}
