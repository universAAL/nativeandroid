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
package org.universaal.nativeandroid.lightserver;

import java.util.Map;

import org.universaal.nativeandroid.lightserver.controller.listeners.IListener;
import org.universaal.nativeandroid.lightserver.controller.listeners.LampStateChangeListener;
import org.universaal.nativeandroid.lightserver.controller.listeners.ListenerServerType;
import org.universaal.nativeandroid.lightserver.model.LightServerModel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class LightServerActivity extends Activity 
{
    protected LightServerController controller;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        
        controller = new LightServerController(this);
        
        // Check if the activity was invoked by intent that cotntains an action
        controller.handleIntent(getIntent());
        
        controller.populateLampsState();
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
		controller.populateLampsState();
	}

    public void turnOnOffLamp(FrameLayout pFrame, boolean pOnStatus) {
    	pFrame.setBackgroundColor(pOnStatus ? Color.RED : Color.BLUE);
    }

	/*****************************************************************************************/
	///////////////////// Controller ///////////////////////////
	public class LightServerController {
		// Activity
		LightServerActivity lightServerActivity;
		
		// Model
		LightServerModel 	lightServerModel;
		
		public LightServerController(LightServerActivity activity) {
			// Keep reference to the activity
			lightServerActivity = activity;
			
			// Initiate the model
			populateModel();
		}
		
		public void handleIntent(Intent intent) {
			lightServerModel.handleIntent(intent);
		}

		public void populateLampsState() {
			// Ask the model to provide the lamp list TODO: need to populate the UI dynamically depends on the lamp list
			Map<String, Boolean> lampsStates = lightServerModel.getLamps();
			
			// Populate the UI 
			for (String lampID : lampsStates.keySet()) {
				updateLampStateInUI(lampID, lampsStates.get(lampID));
			}
		}

		private void updateLampStateInUI(String lampID, final Boolean lampState) {
			int parsedLampIDAsInt = 0;
			try {
				parsedLampIDAsInt = Integer.parseInt(lampID);
			}
			catch (NumberFormatException e) {
				Log.e(getClass().getCanonicalName(), "Illegal lamp ID [" + lampID + "]");
				return;
			}
			
			final int lampIDAsInt = parsedLampIDAsInt;
			
			runOnUiThread(new Thread()
				{
					@Override
					public void run() {
						int id = getResources().getIdentifier("lamp" + lampIDAsInt, "id", getPackageName());
						FrameLayout frame = (FrameLayout) findViewById(id);
						
						lightServerActivity.turnOnOffLamp(frame, lampState);
					}
				}
			);
		}

		private void populateModel() {
			lightServerModel = LightServerModel.getInstance(lightServerActivity);
			
			// Add listeners
			addListeners();
		}

		private void addListeners() {
			// Create lamp state change listener
			IListener listener = new LampStateChangeListener(this);
			lightServerModel.addListener(ListenerServerType.LampTurnOnOff, listener);
		}
	}
	
	/*****************************************************************************************/
}