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
package org.universAAL.middleware.android.common.messages;

import java.util.Iterator;

import org.universAAL.middleware.android.buses.servicebus.messages.RegisterServicesMessage;
import org.universAAL.middleware.android.common.AndroidServiceType;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public abstract class AbstractRegisterAppsMessage extends AbstractMessage {

    private final static String TAG = RegisterServicesMessage.class.getCanonicalName();

    protected String packageName;

    public AbstractRegisterAppsMessage(Context context, Intent intent) {
	super(context, intent);
    }

    @Override
    protected void extractValuesFromExtrasSection() {
	super.extractValuesFromExtrasSection();

	// Extract package name
	packageName = extractPackageNameFromExtras();
    }

    protected void populateAppsList() {

	String componentNameString;
	ComponentName componentName;

	Log.d(TAG, "Is about to search for services registrtion in package [" + packageName + "]");

	PackageManager pm = context.getPackageManager();

	try {
	    ApplicationInfo appInfo = pm.getApplicationInfo(packageName,
		    PackageManager.GET_META_DATA);
	    componentNameString = packageName;
	    scanMetadataInApplicationInfoAndUpdateList(appInfo.metaData, componentNameString, pm,
		    appInfo);

	    // Activities
	    PackageInfo packInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
	    if (packInfo.activities != null) {
		for (int i = 0; i < packInfo.activities.length; i++) {
		    componentNameString = packInfo.activities[i].name;

		    componentName = new ComponentName(packageName, componentNameString);
		    ActivityInfo ai = pm.getActivityInfo(componentName,
			    PackageManager.GET_META_DATA);
		    scanMetadataInActivitiesAndUpdateList(ai.metaData, componentNameString, pm,
			    appInfo);
		}
	    }

	    // Services
	    packInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES);
	    if (packInfo.services != null) {
		for (int i = 0; i < packInfo.services.length; i++) {
		    componentNameString = packInfo.services[i].name;

		    componentName = new ComponentName(packageName, componentNameString);
		    ServiceInfo si = pm.getServiceInfo(componentName, PackageManager.GET_META_DATA);

		    scanMetadataInServicesAndUpdateList(si.metaData, componentNameString, pm,
			    appInfo, AndroidServiceType.SERVICE);
		}
	    }
	} catch (NameNotFoundException e) {
	    // Do nothing, just log it
	    Log.e(TAG,
		    "Error when scanning package [" + packageName + "]; Error [" + e.getMessage()
			    + "]");
	}
    }

    protected abstract void scanMetadataInApplicationInfoAndUpdateList(Bundle metaData,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo)
	    throws NameNotFoundException;

    protected abstract void scanMetadataInServicesAndUpdateList(Bundle metaData,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo,
	    AndroidServiceType service) throws NameNotFoundException;

    protected abstract void scanMetadataInActivitiesAndUpdateList(Bundle metaData,
	    String componentNameString, PackageManager pm, ApplicationInfo appInfo)
	    throws NameNotFoundException;

    protected int extractResourceFromMetadata(Bundle metaData, String metdataName) {
	int resourceID = 0;

	if (null == metaData) {
	    return resourceID;
	}
	Iterator<String> metaTags = metaData.keySet().iterator();

	while (metaTags.hasNext()) {
	    String tag = metaTags.next();
	    if (metdataName.equals(tag)) {
		String resourceIDAsStr = metaData.get(tag).toString();
		resourceID = Integer.parseInt(resourceIDAsStr);
	    }
	}
	return resourceID;
    }
}
