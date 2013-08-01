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
package org.universAAL.middleware.container.android.run;

import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.modules.ModulesIntentFactory;
import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.container.android.R;
//import org.universAAL.middleware.container.android.R.id;
//import org.universAAL.middleware.container.android.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */

public class SodaPopActivity extends Activity implements OnTouchListener {
	private static String TAG = "acl.sodapop.android";

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.main);

		// Add touch listeners
		addTouchListeners();
	};

	private void addTouchListeners() {
		Button button = (Button) findViewById(R.id.startSodaPop);
		button.setOnTouchListener(this);
		button = (Button) findViewById(R.id.startConnector);
		button.setOnTouchListener(this);
		button = (Button) findViewById(R.id.startModules);
		button.setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.startSodaPop:
				sendBroadcastMsg();
				break;
			case R.id.startConnector:
				startConnectorService();
				break;
			case R.id.startModules:
				startModulesService();
				break;
			default:
				break;
			}
		}

		return false; // To make the button seen as clicked
	}

	private void sendBroadcastMsg() {
		Intent startServiceIntent = new Intent(this,
				AndroidContainerService.class);
		this.startService(startServiceIntent);
	}

	private void startConnectorService() {
		Intent startServiceIntent = new Intent(this, ConnectorService.class);
		this.startService(startServiceIntent);
	}

	private void startModulesService() {
		// Intent startServiceIntent = new Intent(this,
		// ModulesService.class);
		Intent broadcastServiceIntent = ModulesIntentFactory.createInitialize();
		this.sendBroadcast(broadcastServiceIntent);
	}
}
