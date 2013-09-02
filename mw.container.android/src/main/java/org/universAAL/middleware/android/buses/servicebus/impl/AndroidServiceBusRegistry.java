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
package org.universAAL.middleware.android.buses.servicebus.impl;

import org.universAAL.middleware.android.buses.common.AbstractAndroidBusMembersRegistry;
import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteHelper.MemberType;
import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteMngr;
import org.universAAL.middleware.android.buses.common.persistence.tables.rows.BusMemberRowDB;
import org.universAAL.middleware.android.buses.servicebus.persistence.ServiceBusSQLiteMngr;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.AndroidServiceCalleeProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.ServiceGroundingXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ServiceRequestGroundingXmlObj;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.util.IRegistryListener;
import org.universAAL.middleware.container.ModuleContext;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidServiceBusRegistry extends
		AbstractAndroidBusMembersRegistry {
	private final static String TAG = "AndroidServiceBusRegistry";

	public AndroidServiceBusRegistry(AndroidServiceBusImpl androidServiceBus,
			Context context) {
		super(androidServiceBus, context);
	}

	@Override
	public void addBusMember(String memberID, BusMember busMember) {

		if (busMember instanceof AndroidServiceCalleeProxy) {
			addServiceCalleeAsBusMember(memberID, busMember);
		} else if (busMember instanceof AndroidServiceCallerProxy) {
			addServiceCallerAsBusMember(memberID, busMember);
		}
	}

	@Override
	protected BusMember createBusMember(BusMemberRowDB registryServicesRowDB) {
		BusMember member = null;

		switch (MemberType.valueOf(registryServicesRowDB.getMemberType())) {
		case SERVICE_CALLEE:
			member = createAndroidServiceCallee(registryServicesRowDB);
			break;
		case SERVICE_CALLER:
			member = createAndroidServiceCaller(registryServicesRowDB);
			break;
		}

		return member;
	}

	protected void addServiceCalleeAsBusMember(String memberID,
			BusMember busMember) {
		// Cast the BusMember to AndroidServiceCallee
		AndroidServiceCalleeProxy androidServiceCallee = (AndroidServiceCalleeProxy) busMember;

		// Package name
		String packageName = androidServiceCallee.getPackageName();
		if (packageName == null) {
			// Because of PATCH at constructor of proxy
			Log.w(TAG,
					"Unable to add Service Callee to the Android Registry" +
					" because it is not properly initialized. This usually" +
					" happens during ServiceCalleeProxy super.init, but " +
					"should be solved by itself");
			return;
		}
		// Service grounding ID
		String serviceGroundingID = androidServiceCallee
				.getServiceGroundingID();

		// Android service name
		String androidServiceName = androidServiceCallee
				.getAndroidServiceName();

		// Serialize the service xml to byte array
		byte[] serviceGroundingXmlAsByteArr = androidServiceCallee
				.getServiceGrounding().serialize();

		// Persist this
			sqliteMngr.addBusMember(memberID, packageName, serviceGroundingID,
					MemberType.SERVICE_CALLEE, androidServiceName,
					serviceGroundingXmlAsByteArr);
	}

	protected void addServiceCallerAsBusMember(String memberID,
			BusMember busMember) {
		// Cast the BusMember to AndroidServiceCaller
		AndroidServiceCallerProxy androidServiceCaller = (AndroidServiceCallerProxy) busMember;

		// Package name
		String packageName = androidServiceCaller.getPackageName();
		if (packageName == null) {
			// Because of PATCH at constructor of proxy
			Log.w(TAG,
					"Unable to add Service Caller to the Android Registry" +
					" because it is not properly initialized. This usually" +
					" happens during ServiceCallerProxy super.init, but " +
					"should be solved by itself");
			return;
		}

		// Service Request grounding ID
		String serviceRequestGroundingID = androidServiceCaller
				.getServiceRequestGroundingID();

		// Android unique name
		String androidUniqueName = androidServiceCaller.getAndroidUniqueName();

		// Serialize the service grounding xml to byte array
		byte[] serviceRequestGroundingXmlAsByteArr = androidServiceCaller
				.getServiceRequestGrounding().serialize();

		// Persist this
			sqliteMngr.addBusMember(memberID, packageName,
					serviceRequestGroundingID, MemberType.SERVICE_CALLER,
					androidUniqueName, serviceRequestGroundingXmlAsByteArr);
	}

	protected AndroidServiceCalleeProxy createAndroidServiceCallee(
			BusMemberRowDB registryServicesRowDB) {
		AndroidServiceCalleeProxy androidServiceCallee = new AndroidServiceCalleeProxy(
				getAndroidServiceBusMC(),
				registryServicesRowDB.getPackageName(),
				registryServicesRowDB.getGroundingID(),
				registryServicesRowDB.getAndroidUniqueName(),
				ServiceGroundingXmlObj.deserialize(registryServicesRowDB
						.getGroundingXml()), context,
				registryServicesRowDB.getMemberID());

		return androidServiceCallee;
	}

	protected BusMember createAndroidServiceCaller(
			BusMemberRowDB registryServicesRowDB) {
		AndroidServiceCallerProxy androidServiceCaller = new AndroidServiceCallerProxy(
				getAndroidServiceBusMC(),
				registryServicesRowDB.getPackageName(),
				registryServicesRowDB.getGroundingID(),
				registryServicesRowDB.getAndroidUniqueName(),
				ServiceRequestGroundingXmlObj.deserialize(registryServicesRowDB
						.getGroundingXml()), context,
				registryServicesRowDB.getMemberID());

		return androidServiceCaller;
	}

	@Override
	protected String getRegistryID(BusMember busMember) {
		String registryID=null;
		if (busMember instanceof AndroidServiceCalleeProxy) {
			registryID = ((AndroidServiceCalleeProxy) busMember)
					.getServiceGroundingID();
		} else if (busMember instanceof AndroidServiceCallerProxy) {
			registryID = ((AndroidServiceCallerProxy) busMember)
					.getServiceRequestGroundingID();
		}
		// This is because when BusMember is being initialized the registry ID
		// is still null
		if(registryID!=null){
			return registryID;
		}else{
			return "";
		}
	}

	protected ModuleContext getAndroidServiceBusMC() {
		return ((AndroidServiceBusImpl) androidBus).getModuleContext();
	}

	@Override
	protected AbstractCommonBusSQLiteMngr createSQLiteMngr(Context context) {
		return new ServiceBusSQLiteMngr(context);
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