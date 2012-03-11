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
package org.universAAL.middleware;

import org.teleal.cling.android.AndroidUpnpServiceImpl;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Extension of original UPnP Cling Service to force initializing in the new
 * thread
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis
 *         Dalinkevicius</a>
 * 
 */
public class UPnPAndroidService extends AndroidUpnpServiceImpl {
	private static String LOGTAG = "acl.upnp.android";
	protected UPnPAndroidServiceBinder binder = new UPnPAndroidServiceBinder();

	/*
	 * Creates a UPnP Service in the new thread. Creation function is not changed,
	 * it is only wrapped to the thread to keep the main Android UI thread free of work
	 * @see org.teleal.cling.android.AndroidUpnpServiceImpl#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.i(LOGTAG, "UPnPAndroidService #onCreate called!");
		Thread serviceStartedThread = new Thread(new Runnable() {
			public void run() {
				Log.i(LOGTAG, "Calling Cling UPnP service wrapped to thread!");
				UPnPAndroidService.super.onCreate();
				if (upnpService != null) { //checking upnp service initialization result (it might fail)
					binder.setService(upnpService); //on success setting new fully created upnp service to the previously returned binder
				} else {
					Log.e(LOGTAG, "Starting UPnP service failed! Can not continue!");
				}
			}
		});
		serviceStartedThread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}