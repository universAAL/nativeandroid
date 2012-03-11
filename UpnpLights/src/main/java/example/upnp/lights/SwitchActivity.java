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
package example.upnp.lights;

import java.util.Vector;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 
 * Light Controller based on Cling library (upnp protocol)
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> 
 * 
 */
public class SwitchActivity extends Activity{

	private Vector<ButtonCheckBoxContainer> devicesCBS = new Vector();
	private AndroidUpnpService upnp;
	private ServiceConnection bindConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			upnp = (AndroidUpnpService) service;
			Log.i("upnp", "casted to Android upnp service");
			upnp.getControlPoint().search();
			Log.i("upnp", "search done");
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i("upnp", "Service discconnected called");
			upnp = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent intent = new Intent(this, AndroidUpnpServiceImpl.class);
		bindService(intent, bindConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("upnp", "main activity started");
	}

	public void aaa(View v) {
		ScrollView sv = new ScrollView(this);  
		LinearLayout ll = new LinearLayout(this);  
		ll.setOrientation(LinearLayout.VERTICAL);  
		sv.addView(ll);
		
		TextView tv = new TextView(this);
		tv.setText("List of devices visible right now!");  
		ll.addView(tv);
		
		devicesCBS.clear();
		for(Device device : upnp.getControlPoint().getRegistry().getDevices()) {  
			CheckBox cb = new CheckBox(this);  
			cb.setText(device.getDisplayString());
			ll.addView(cb);
			devicesCBS.add(new ButtonCheckBoxContainer(device, cb));
		}
		
		Button b = new Button(this);
		b.setText("BACK");
		OnClickListener btn_back = new OnClickListener() {
			public void onClick(View arg0) {
				setContentView(R.layout.main);
			}
		};
		b.setOnClickListener(btn_back);
		ll.addView(b); 

		Button b2 = new Button(this);
		b2.setText("Turn ON Selected!");
		OnClickListener btn_on_selected = new OnClickListener() {
			
			public void onClick(View arg0) {
				for(ButtonCheckBoxContainer container : devicesCBS){
					if(container.getCheckBox().isChecked()){
						Service switchPowerService = container.getDevice().findService(new ServiceId(
								"upnp-org", "SwitchPower.0001"));
						if(switchPowerService == null){
							switchPowerService = container.getDevice().findService(new ServiceId(
									"upnp-org", "SwitchPower"));
						}
						if(switchPowerService != null){
							executeAction(upnp, switchPowerService, true);
						}
						else{
							Log.i("upnp","Does not have SwitchPower service! SKIPING IT: "+container.getDevice().toString());
						}						
					}
				}
			}
		};
		b2.setOnClickListener(btn_on_selected);
		ll.addView(b2);
		
		this.setContentView(sv);
		
		Log.i("upnp", "button click");
		Log.i("upnp", "\tdevices:");
		for (Device device : upnp.getRegistry().getDevices()) {
			Log.i("upnp", device.toString());
		}
	}

	public void SearchLAN(View v) {
		Log.i("upnp", "search started");
		upnp.getControlPoint().search();
		Log.i("upnp", "search done");
	}

	public void on(View v) {
		Log.i("upnp", "lights on!");
		for (Device device : upnp.getControlPoint().getRegistry().getDevices()) {
			Service switchPowerService = device.findService(new ServiceId(
					"upnp-org", "SwitchPower.0001"));
			if(switchPowerService == null){
				switchPowerService = device.findService(new ServiceId(
						"upnp-org", "SwitchPower"));
			}
			if(switchPowerService != null){
				executeAction(upnp, switchPowerService, false);
			}
			else{
				Log.i("upnp", device.toString()+" doesn not have SwitchPower service! SKIP IT!");
			}
		}
		Log.i("upnp", "light should be off!");
	}

	void executeAction(AndroidUpnpService upnp, Service switchPowerService, Boolean state) {
		ActionInvocation setTargetInvocation = new SetTargetActionInvocation(
				switchPowerService, state);

		// Executes asynchronous in the background
		upnp.getControlPoint().execute(new ActionCallback(setTargetInvocation) {
			@Override
			public void success(ActionInvocation invocation) {
				assert invocation.getOutput().length == 0;
				System.out.println("Successfully called action!");
			}

			@Override
			public void failure(ActionInvocation invocation,
					UpnpResponse operation, String defaultMsg) {
				System.err.println(defaultMsg);
			}
		});
	}
}

class SetTargetActionInvocation extends ActionInvocation {
/*
 * This example is designed for 2 kinds of light services:
 * intel upnp network light and Cling library upnp browser examples.
 * This is why we check for different interfaces which are identical accept for the
 * different case first letter.
 */
	SetTargetActionInvocation(Service service, Boolean state) {
		super(service.getAction("SetTarget"));
		try {
			// Throws InvalidValueException if the value is of wrong type
			setInput("newTargetValue", state);
			Log.i("upnp","Intel Network Light discovered.");
		} catch (IllegalArgumentException ex) {
			setInput("NewTargetValue", state);
			Log.i("upnp","Cling Library Light example discovered.");
		}
	}
}