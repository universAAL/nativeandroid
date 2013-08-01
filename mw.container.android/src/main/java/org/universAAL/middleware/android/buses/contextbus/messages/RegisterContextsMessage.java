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
package org.universAAL.middleware.android.buses.contextbus.messages;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.ContextPublisherGroundingXmlMngr;
import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects.ContextPublisherGroundingXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.ContextSubscriberGroundingXmlMngr;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ContextSubscriberGroundingXmlObj;
import org.universAAL.middleware.android.common.AbstractGroundingDataWrapper;
import org.universAAL.middleware.android.common.AndroidServiceType;
import org.universAAL.middleware.android.common.StringConstants;
//import org.universAAL.middleware.android.common.IAndroidSodaPop;
//import org.universAAL.middleware.android.common.buses.AbstractGroundingDataWrapper;
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
 *         Jun 19, 2012
 * 
 */
public class RegisterContextsMessage extends AbstractRegisterAppsMessage {

	private static final String TAG = RegisterContextsMessage.class
			.getCanonicalName();

	private List<AndroidContextPublisherDataWrapper> androidContextPublisherDataWrapperList = new ArrayList<AndroidContextPublisherDataWrapper>();
	private List<AndroidContextSubscriberDataWrapper> androidContextSubscriberDataWrapperList = new ArrayList<AndroidContextSubscriberDataWrapper>();

	public RegisterContextsMessage(Context context, Intent intent) {
		super(context, intent);

		// Populate the apps list
		populateAppsList();
	}

	public List<AndroidContextPublisherDataWrapper> getAndroidContextPublisherDataWrapperList() {
		return androidContextPublisherDataWrapperList;
	}

	public List<AndroidContextSubscriberDataWrapper> getAndroidContextSubscriberDataWrapperList() {
		return androidContextSubscriberDataWrapperList;
	}

	@Override
	protected void scanMetadataInApplicationInfoAndUpdateList(Bundle metaData,
			String componentNameString, PackageManager pm,
			ApplicationInfo appInfo) throws NameNotFoundException {

		scanMetadataForContextsAndUpdateList(metaData, componentNameString, pm,
				appInfo);
	}

	@Override
	protected void scanMetadataInServicesAndUpdateList(Bundle metaData,
			String componentNameString, PackageManager pm,
			ApplicationInfo appInfo, AndroidServiceType service)
			throws NameNotFoundException {

		scanMetadataForContextsAndUpdateList(metaData, componentNameString, pm,
				appInfo);
	}

	@Override
	protected void scanMetadataInActivitiesAndUpdateList(Bundle metaData,
			String componentNameString, PackageManager pm,
			ApplicationInfo appInfo) throws NameNotFoundException {

		scanMetadataForContextsAndUpdateList(metaData, componentNameString, pm,
				appInfo);
	}

	protected void scanMetadataForContextsAndUpdateList(Bundle metadata,
			String componentNameString, PackageManager pm,
			ApplicationInfo appInfo) throws NameNotFoundException {

		int contextPublisherGroundingResourceID = extractResourceFromMetadata(
				metadata,
				StringConstants.META_DATA_TAG_CONTEXT_PUBLISHER_GROUNDING);
		int contextSubscriberGroundingResourceID = extractResourceFromMetadata(
				metadata,
				StringConstants.META_DATA_TAG_CONTEXT_SUBSCRIBER_GROUNDING);
		String androidUniqueName = componentNameString;
		Resources resources = pm.getResourcesForApplication(appInfo);

		if (0 != contextPublisherGroundingResourceID) {
			InputStream is = resources
					.openRawResource(contextPublisherGroundingResourceID);

			ContextPublisherGroundingXmlObj contextPublisherGroundingXml = ContextPublisherGroundingXmlMngr
					.populateContextPublisherGroundingXmlObjectFromXml(is);

			// Add to the list
			androidContextPublisherDataWrapperList
					.add(new AndroidContextPublisherDataWrapper(packageName,
							androidUniqueName, contextPublisherGroundingXml));

			Log.d(TAG, "Found ContextPublisherGrounding for ["
					+ androidUniqueName + "]");
		}

		if (0 != contextSubscriberGroundingResourceID) {
			InputStream is = resources
					.openRawResource(contextSubscriberGroundingResourceID);

			ContextSubscriberGroundingXmlObj contextSubscriberGroundingXml = ContextSubscriberGroundingXmlMngr
					.populateContextSubscriberGroundingXmlObjectFromXml(is);

			// Add to the list
			androidContextSubscriberDataWrapperList
					.add(new AndroidContextSubscriberDataWrapper(packageName,
							androidUniqueName, contextSubscriberGroundingXml));

			Log.d(TAG, "Found ContextSubscriberGrounding for ["
					+ androidUniqueName + "]");
		}
	}

	public static class AndroidContextPublisherDataWrapper extends
			AbstractGroundingDataWrapper {

		private ContextPublisherGroundingXmlObj contextPublisherGroundingXmlObj;

		public AndroidContextPublisherDataWrapper(String packageName,
				String androidUniqueName,
				ContextPublisherGroundingXmlObj contextPublisherGroundingXmlObj) {

			super(packageName, packageName, androidUniqueName);

			this.contextPublisherGroundingXmlObj = contextPublisherGroundingXmlObj;
		}

		public ContextPublisherGroundingXmlObj getContextPublisherGroundingXmlObj() {
			return contextPublisherGroundingXmlObj;
		}
	}

	public static class AndroidContextSubscriberDataWrapper extends
			AbstractGroundingDataWrapper {

		private ContextSubscriberGroundingXmlObj contextSubscriberGroundingXmlObj;

		public AndroidContextSubscriberDataWrapper(
				String packageName,
				String androidUniqueName,
				ContextSubscriberGroundingXmlObj contextSubscriberGroundingXmlObj) {

			super(packageName, packageName, androidUniqueName);

			this.contextSubscriberGroundingXmlObj = contextSubscriberGroundingXmlObj;
		}

		public ContextSubscriberGroundingXmlObj getContextSubscriberGroundingXmlObj() {
			return contextSubscriberGroundingXmlObj;
		}
	}
}
