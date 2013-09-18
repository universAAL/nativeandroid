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

import org.universAAL.middleware.android.buses.common.AbstractAndroidBusMembersRegistry;
import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteHelper.MemberType;
import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteMngr;
import org.universAAL.middleware.android.buses.common.persistence.tables.rows.BusMemberRowDB;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.AndroidContextPublisherProxy;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ContextSubscriberGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.persistence.ContextBusSQLiteMngr;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.IRegistryListener;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class AndroidContextBusRegistry extends AbstractAndroidBusMembersRegistry {

    public AndroidContextBusRegistry(IAndroidBus androidBus, Context context) {
	super(androidBus, context);
    }

    @Override
    public void addBusMember(String memberID, BusMember busMember) {

	if (busMember instanceof AndroidContextPublisherProxy) {
	    addContextPublisherAsBusMember(memberID, busMember);
	} else if (busMember instanceof AndroidContextSubscriberProxy) {
	    addContextSubscriberAsBusMember(memberID, busMember);
	}
    }

    @Override
    protected BusMember createBusMember(BusMemberRowDB registryServicesRowDB) {
	BusMember member = null;

	switch (MemberType.valueOf(registryServicesRowDB.getMemberType())) {
	case CONTEXT_PUBLISHER:
	    member = createAndroidContextPublisherAsBusMember(registryServicesRowDB);
	    break;
	case CONTEXT_SUBSCRIBER:
	    member = createAndroidContextSubscriberAsBusMember(registryServicesRowDB);
	    break;
	}

	return member;
    }

    protected void addContextPublisherAsBusMember(String memberID, BusMember busMember) {
	// Cast the BusMember to AndroidContextPublisher
	AndroidContextPublisherProxy androidContextPublisherProxy = (AndroidContextPublisherProxy) busMember;

	// Package name
	String packageName = androidContextPublisherProxy.getPackageName();

	// Service Context Publisher Grounding ID
	String contextPublisherGroundingID = androidContextPublisherProxy.getGroundingID();

	// Android unique name
	String androidUniqueName = androidContextPublisherProxy.getAndroidUniqueName();

	// Serialize the service publisher grounding xml to byte array
	byte[] contextPublisherGroundingXmlAsByteArr = androidContextPublisherProxy
		.getContextPublisherGrounding().serialize();

	// Persist this
	sqliteMngr.open();
	try {
	    sqliteMngr.addBusMember(memberID, packageName, contextPublisherGroundingID,
		    MemberType.CONTEXT_PUBLISHER, androidUniqueName,
		    contextPublisherGroundingXmlAsByteArr);
	} finally {
	    sqliteMngr.close();
	}
    }

    protected void addContextSubscriberAsBusMember(String memberID, BusMember busMember) {
	// Cast the BusMember to AndroidContextSubscriber
	AndroidContextSubscriberProxy androidContextSubscriberProxy = (AndroidContextSubscriberProxy) busMember;

	// Package name
	String packageName = androidContextSubscriberProxy.getPackageName();

	// Service Context Publisher Grounding ID
	String contextPublisherGroundingID = androidContextSubscriberProxy.getGroundingID();

	// Android unique name
	String androidUniqueName = androidContextSubscriberProxy.getAndroidUniqueName();

	// Serialize the service subscriber grounding xml to byte array
	byte[] contextPublisherGroundingXmlAsByteArr = androidContextSubscriberProxy
		.getContextSubscriberGrounding().serialize();

	// Persist this
	sqliteMngr.open();
	try {
	    sqliteMngr.addBusMember(memberID, packageName, contextPublisherGroundingID,
		    MemberType.CONTEXT_SUBSCRIBER, androidUniqueName,
		    contextPublisherGroundingXmlAsByteArr);
	} finally {
	    sqliteMngr.close();
	}
    }

    protected AndroidContextPublisherProxy createAndroidContextPublisherAsBusMember(
	    BusMemberRowDB registryServicesRowDB) {

	ContextPublisherGroundingXmlObj groundingXml = ContextPublisherGroundingXmlObj
		.deserialize(registryServicesRowDB.getGroundingXml());

	// Populate the ContentProvider
	ContextProvider contextProviderInfo = new ContextProvider(groundingXml.getUri());
	contextProviderInfo.setType(ContextProviderType.controller);
	contextProviderInfo
		.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });

	AndroidContextPublisherProxy androidContextPublisher = new AndroidContextPublisherProxy(
		getAndroidContextBus(), contextProviderInfo,
		registryServicesRowDB.getPackageName(), registryServicesRowDB.getGroundingID(),
		registryServicesRowDB.getAndroidUniqueName(), groundingXml, context,
		registryServicesRowDB.getMemberID());

	return androidContextPublisher;
    }

    protected BusMember createAndroidContextSubscriberAsBusMember(
	    BusMemberRowDB registryServicesRowDB) {

	AndroidContextSubscriberProxy androidContextSubscriber = new AndroidContextSubscriberProxy(
		getAndroidContextBus(), registryServicesRowDB.getPackageName(),
		registryServicesRowDB.getGroundingID(),
		registryServicesRowDB.getAndroidUniqueName(),
		ContextSubscriberGroundingXmlObj.deserialize(registryServicesRowDB
			.getGroundingXml()), context, registryServicesRowDB.getMemberID());

	return androidContextSubscriber;
    }

    @Override
    protected String getRegistryID(BusMember busMember) {
	// Cast to grounding wrapper
	IGroundingIDWrapper groundingIDWrapper = (IGroundingIDWrapper) busMember;

	String registryID = groundingIDWrapper.getGroundingID();

	return registryID;
    }

    protected AndroidContextBus getAndroidContextBus() {
	return (AndroidContextBus) androidBus;
    }

    @Override
    protected AbstractCommonBusSQLiteMngr createSQLiteMngr(Context context) {
	return new ContextBusSQLiteMngr(context);
    }

	public boolean addRegistryListener(IRegistryListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeRegistryListener(IRegistryListener listener) {
		// TODO Auto-generated method stub
		return false;
	}
}
