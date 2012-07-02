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
package org.universAAL.middleware.android.acl.upnp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.registry.RegistrationException;
import org.universAAL.middleware.acl.upnp.android.R;
import org.universAAL.middleware.acl.upnp.plainjava.DeviceFactory;
import org.universAAL.middleware.android.UPnPAndroidService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

/**
 * Demonstration activity to start publish the Soda Pop Peer on Android device
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a>
 *          <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class Runner extends Activity {

	private static String LOGTAG = "acl.upnp.android";
	private static AndroidUpnpService upnpService = null;
	private InputStream deviceDescription;
	private boolean devicePublished = false;

	private ServiceConnection upnpBindServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i(LOGTAG, "onServiceConnected called");
			upnpService = (AndroidUpnpService) binder;
			Log.i(LOGTAG, "Casted service object: " + upnpService);
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i(LOGTAG, "onServiceDiscconnected called");
			upnpService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOGTAG, "onCreate called");
		deviceDescription = getResources().openRawResource(
				R.raw.device_description); // Getting the input stream to read for UPnP device description
		setContentView(R.layout.main);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(LOGTAG, "onStarted called");
		//On activity start we bind (and by that also create) to upnp service
		Intent upnpServiceIntent = new Intent(getApplicationContext(), 
				UPnPAndroidService.class); //Intent to start extended threaded service
		bindService(upnpServiceIntent, upnpBindServiceConnection,
				Context.BIND_AUTO_CREATE); //Regular bind to the service
	}

	public void publishSodaPopPeer(View v) {
		/* Button click method to publish SodaPop Peer.
		 * If service is up and running it will happen instantly, but
		 * if service is still being initialized this thread will be put to sleep
		 * by binders #get() method. Because of possible hold method must be
		 * called in the new thread to avoid ANR dialog on Android
		 */
		if(deviceDescription == null || devicePublished){
			/* Device description becomes null if you already read it all before
			 * or if there was an exception opening the resource file. Either way
			 * there is no point to continue, because device is already published or
			 * can not be published at all
			 * OR
			 * The device was already published
			 */
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					Log.i(LOGTAG, "Creating SodaPopPeerLocalDevice");
					Log.i(LOGTAG, "Description:\n" + deviceDescription);
					// Publishing of SodaPop Peer. Device is created from xml description
					upnpService.getRegistry().addDevice(
							new DeviceFactory().getParsedDevice(deviceDescription, "some string for UDN"));
					devicePublished = true;
					// Force refresh
					upnpService.getControlPoint().search();
					Collection<Device> devices = upnpService.getControlPoint()
							.getRegistry().getDevices();
					System.out.println("Searching for devices...");
					while (devices.isEmpty()) {
						/* This is just a primitive lock in case SodaPop peer is taking some time to
						 * load. In case if there are other upnp devices list of them will be also displayed
						 */
						devices = upnpService.getControlPoint().getRegistry().getDevices();
					}
					System.out.println("Something found!");
					for (Device device : upnpService.getControlPoint().getRegistry()
							.getDevices()) {
						System.out.println(device.toString());
					}
				} catch (RegistrationException e) {
					e.printStackTrace();
				} catch (LocalServiceBindingException e) {
					e.printStackTrace();
				} catch (ValidationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}