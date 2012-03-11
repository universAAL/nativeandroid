/*
	OCO Source Materials
            © Copyright IBM Corp. 2011

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
package example.metadata;

/**
 * The example of extracting meta-data of Android components within manifest.xml file on package install
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a>
 *
 */
import java.util.Iterator;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MetaDataExtractorService extends Service {
	final static String logTag = "MetaDataExtract";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		int i;
		String packageName = intent.getExtras().getString("packageName");
		String componentNameString;
		ComponentName componentName;

		Log.i(logTag,
				"Service started! Need to extract meta data from package: "
						+ packageName);
		PackageManager pm = getPackageManager();

		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			if (appInfo.metaData != null) {
				Log.i(logTag, "Application contains meta:");
				this.logPrint(appInfo.metaData);
			}

			PackageInfo packInfo = pm.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES); // getting a list of
													// activities within package
			if (packInfo.activities != null) {
				Log.i(logTag, "Activities list received:");
				for (i = 0; i < packInfo.activities.length; i++) {
					componentNameString = packInfo.activities[i].name;

					Log.i(logTag, "\t" + componentNameString + " meta:");
					componentName = new ComponentName(packageName,
							componentNameString); // forming a special object
													// for pointing component
					ActivityInfo ai = pm.getActivityInfo(componentName,
							PackageManager.GET_META_DATA);

					this.logPrint(ai.metaData);
				}
			}

			packInfo = pm.getPackageInfo(packageName,
					PackageManager.GET_SERVICES);
			if (packInfo.services != null) {
				Log.i(logTag, "Services list received:");
				for (i = 0; i < packInfo.services.length; i++) {
					componentNameString = packInfo.services[i].name;

					Log.i(logTag, "\t" + componentNameString + " meta:");
					componentName = new ComponentName(packageName,
							componentNameString);
					ServiceInfo si = pm.getServiceInfo(componentName,
							PackageManager.GET_META_DATA);

					this.logPrint(si.metaData);
				}
			}

			packInfo = pm.getPackageInfo(packageName,
					PackageManager.GET_RECEIVERS);
			if (packInfo.receivers != null) {
				Log.i(logTag, "Receivers list received:");
				for (i = 0; i < packInfo.receivers.length; i++) {
					componentNameString = packInfo.receivers[i].name;

					Log.i(logTag, "\t" + componentNameString + " meta:");
					componentName = new ComponentName(packageName,
							componentNameString);
					ActivityInfo ri = pm.getReceiverInfo(componentName,
							PackageManager.GET_META_DATA);

					this.logPrint(ri.metaData);
				}
			}

			packInfo = pm.getPackageInfo(packageName,
					PackageManager.GET_PROVIDERS);
			if (packInfo.providers != null) {
				Log.i(logTag, "Providers list received:");
				for (i = 0; i < packInfo.providers.length; i++) {
					componentNameString = packInfo.providers[i].name;

					Log.i(logTag, "\t" + componentNameString + " meta:");
					// componentName = new ComponentName(packageName,
					// componentNameString);
					ProviderInfo pi = pm.resolveContentProvider("provider",
							PackageManager.GET_META_DATA);

					this.logPrint(pi.metaData);
				}
			}
		} catch (NameNotFoundException e) {
			Log.e(logTag,
					"Requested package name: "
							+ packageName
							+ " was not found! (Most likely you have just removed this package)");
		}
	}

	private void logPrint(Bundle metaData) {
		Iterator<String> metaTags = metaData.keySet().iterator(); // returns a
																	// set of
																	// metadata
																	// tags as
																	// strings
																	// in a set
		while (metaTags.hasNext()) {
			String tag = metaTags.next();
			Log.i(logTag, "\t\t" + tag + " == " + metaData.get(tag).toString());
		}
		return;
	}
}