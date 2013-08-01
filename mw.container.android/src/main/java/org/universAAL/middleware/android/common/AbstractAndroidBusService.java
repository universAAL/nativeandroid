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
package org.universAAL.middleware.android.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public abstract class AbstractAndroidBusService extends Service {

	private static final String TAG = AbstractAndroidBusService.class
			.getCanonicalName();

	@Override
	public IBinder onBind(Intent intent) {
		return null; // Don't allow binding...
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);

		Log.d(TAG, "Is about to handle intent [" + intent.getAction() + "]");

		Thread commandThread = new Thread() {

			@Override
			public void run() {
				privateOnStart(intent, startId); // TODO: consider to create a
				// super abstract class that
				// enables this mechanism
			}
		};
		commandThread.start();
	}

	protected abstract void privateOnStart(Intent intent, int startId);
}
