/*
	Copyright 2015 ITACA-SABIEN, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
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
package org.universaal.nativeandroid.lightserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class LightServerActivity extends AppCompatActivity {
	// Using this same prefix in client and server allows communicating through Android even if uAAL is not on
	// If you want to force communication only through uAAL, you need to change it (better change it in client)
	public static final String PREFIX = "org.universaal.nativeandroid.light.";
	// Actions and extras for Intent-based communication
	public static final String ACTION_INNER = PREFIX+"server.INNER";
	public static final String ACTION_EVENT = PREFIX+"EVENT_LIGHTCHANGE";
	public static final String ACTION_CALL_ON = PREFIX + "CALL_ON";
	public static final String ACTION_CALL_OFF = PREFIX + "CALL_OFF";
	public static final String EXTRA_LAMP = "lamp";
	public static final String EXTRA_BRIGHTNESS = "brightness";
	public static final String EXTRA_LAMPS = "lamps";
	// These extras are for knowing where to send the replies. When using uAAL, the values are in exactly these extras
	public static final String EXTRA_REPLYACTION = "org.universAAL.android.action.META_REPLYTOACT";
	public static final String EXTRA_REPLYCATEGORY = "org.universAAL.android.action.META_REPLYTOCAT";
	private BroadcastReceiver mReceiver = new LampReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ACTION_INNER);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mReceiver, filter);
		
		updateUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(mReceiver);
	}
	
	public void updateUI(){
		ImageView img1 = (ImageView) findViewById(R.id.imageView1);
		ImageView img2 = (ImageView) findViewById(R.id.imageView2);
		ImageView img3 = (ImageView) findViewById(R.id.imageView3);
		ImageView img4 = (ImageView) findViewById(R.id.imageView4);

		LightServerModel model = LightServerModel.getInstance(getApplicationContext());
		
		if(model.getLamp(getString(R.string.lamp_1))>0){
			img1.setColorFilter(Color.YELLOW);
		}else{
			img1.setColorFilter(Color.GRAY);
		}
		
		if(model.getLamp(getString(R.string.lamp_2))>0){
			img2.setColorFilter(Color.YELLOW);
		}else{
			img2.setColorFilter(Color.GRAY);
		}
		
		if(model.getLamp(getString(R.string.lamp_3))>0){
			img3.setColorFilter(Color.YELLOW);
		}else{
			img3.setColorFilter(Color.GRAY);
		}
		
		if(model.getLamp(getString(R.string.lamp_4))>0){
			img4.setColorFilter(Color.YELLOW);
		}else{
			img4.setColorFilter(Color.GRAY);
		}
	}

	public class LampReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI();
		}
	}
}
