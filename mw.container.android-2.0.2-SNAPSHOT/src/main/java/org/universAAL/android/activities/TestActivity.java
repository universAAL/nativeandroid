/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)

	OCO Source Materials
	(C) Copyright IBM Corp. 2011
	
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
package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.services.MiddlewareService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class TestActivity extends Activity implements OnTouchListener {
	private static String TAG = "TestActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Create");
		setContentView(R.layout.main);
		addTouchListeners();
		Log.v(TAG, "Created");
	};

	private void addTouchListeners() {
		Button button = (Button) findViewById(R.id.startMiddleware);
		button.setOnTouchListener(this);
		Button button2 = (Button) findViewById(R.id.stopMiddleware);
		button2.setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.startMiddleware:
				Intent startServiceIntent = new Intent(this, MiddlewareService.class);
				this.startService(startServiceIntent);
				break;
			case R.id.stopMiddleware:
				Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
				this.stopService(stopServiceIntent);
				break;
			default:
				break;
			}
		}
		return false;
	}

}
