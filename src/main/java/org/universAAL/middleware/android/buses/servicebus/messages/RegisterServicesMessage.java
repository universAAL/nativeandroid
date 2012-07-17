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
package org.universAAL.middleware.android.buses.servicebus.messages;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.ServiceGroundingXmlMngr;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.ServiceGroundingXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.ServiceRequestGroundingXmlMngr;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ServiceRequestGroundingXmlObj;
import org.universAAL.middleware.android.common.AndroidServiceType;
import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.buses.AbstractGroundingDataWrapper;
import org.universAAL.middleware.android.common.messages.AbstractRegisterAppsMessage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class RegisterServicesMessage extends AbstractRegisterAppsMessage {

    private final static String TAG = RegisterServicesMessage.class.getCanonicalName();

    private List<AndroidServiceCalleeDataWrapper> androidServiceCalleesDataWrapperList = new ArrayList<AndroidServiceCalleeDataWrapper>();
    private List<AndroidServiceCallerDataWrapper> androidServiceCallersDataWrapperList = new ArrayList<AndroidServiceCallerDataWrapper>();

    public RegisterServicesMessage(Context context, Intent intent) {
	super(context, intent);

	// Populate the apps list
	populateAppsList();
    }

    public List<AndroidServiceCalleeDataWrapper> getAndroidServiceCalleesDataWrapperList() {
	return androidServiceCalleesDataWrapperList;
    }

    public List<AndroidServiceCallerDataWrapper> getAndroidServiceCallersDataWrapperList() {

	return androidServiceCallersDataWrapperList;
    }

    @Override
    protected void scanMetadataInApplicationInfoAndUpdateList(Bundle metaData,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo) {
	// Ignore this - since the services suppose to be defined only in
	// Activities / Services
    }

    @Override
    protected void scanMetadataInActivitiesAndUpdateList(Bundle metaData,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo)
	    throws NameNotFoundException {
	scanMetadataForServicesAndUpdateList(metaData, componentNameString, pm, appInfo,
		AndroidServiceType.ACTIVITY);
    }

    @Override
    protected void scanMetadataInServicesAndUpdateList(Bundle metaData, String componentNameString,
	    PackageManager pm, ApplicationInfo appInfo, AndroidServiceType service)
	    throws NameNotFoundException {
	scanMetadataForServicesAndUpdateList(metaData, componentNameString, pm, appInfo,
		AndroidServiceType.SERVICE);
    }

    protected void scanMetadataForServicesAndUpdateList(Bundle metadata,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo,
	    AndroidServiceType androidServiceType) throws NameNotFoundException {

	int serviceGroundingResourceID = extractResourceFromMetadata(metadata,
		IAndroidSodaPop.META_DATA_TAG_SERVICE_GROUNDING);
	int serviceRequestGroundingResourceID = extractResourceFromMetadata(metadata,
		IAndroidSodaPop.META_DATA_TAG_SERVICE_REQUEST_GROUNDING);
	String androidServiceName = componentNameString;
	Resources resources = pm.getResourcesForApplication(appInfo);

	if (0 != serviceGroundingResourceID) {
	    InputStream is = resources.openRawResource(serviceGroundingResourceID);

	    ServiceGroundingXmlObj serviceGroundingXml = ServiceGroundingXmlMngr
		    .populateServiceGroundingXmlObjectFromXml(is, androidServiceType);

	    // Add to the list
	    getAndroidServiceCalleesDataWrapperList().add(
		    new AndroidServiceCalleeDataWrapper(packageName, androidServiceName,
			    serviceGroundingXml));

	    Log.d(TAG, "Found ServiceGrounding for service [" + androidServiceName + "]");
	}

	if (0 != serviceRequestGroundingResourceID) {
	    InputStream is = resources.openRawResource(serviceRequestGroundingResourceID);

	    ServiceRequestGroundingXmlObj serviceRequestGroundingXml = ServiceRequestGroundingXmlMngr
		    .populateServiceRequestGroundingXmlObjectFromXml(is);

	    // Add to the list
	    getAndroidServiceCallersDataWrapperList().add(
		    new AndroidServiceCallerDataWrapper(packageName, androidServiceName,
			    serviceRequestGroundingXml));

	    Log.d(TAG, "Found ServiceRequestGrounding for [" + androidServiceName + "]");
	}
    }

    public static class AndroidServiceCalleeDataWrapper extends AbstractGroundingDataWrapper {

	private ServiceGroundingXmlObj serviceGroundingXmlObj;

	public AndroidServiceCalleeDataWrapper(String packageName, String androidServiceName,
		ServiceGroundingXmlObj serviceGroundingXmlObj) {

	    super(packageName, androidServiceName, androidServiceName
		    + serviceGroundingXmlObj.getUri());

	    this.serviceGroundingXmlObj = serviceGroundingXmlObj;
	}

	public ServiceGroundingXmlObj getServiceGroundingXmlObj() {
	    return serviceGroundingXmlObj;
	}
    }

    public static class AndroidServiceCallerDataWrapper extends AbstractGroundingDataWrapper {

	private ServiceRequestGroundingXmlObj serviceRequestGroundingXmlObj;

	public AndroidServiceCallerDataWrapper(String packageName, String androidUniqueName,
		ServiceRequestGroundingXmlObj serviceRequestGroundingXmlObj) {

	    super(packageName, packageName, androidUniqueName);

	    this.serviceRequestGroundingXmlObj = serviceRequestGroundingXmlObj;
	}

	public ServiceRequestGroundingXmlObj getServiceRequestGroundingXmlObj() {
	    return serviceRequestGroundingXmlObj;
	}
    }
}
