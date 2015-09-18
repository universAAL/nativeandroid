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
package org.universaal.nativeandroid.lightclient;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class LightClientActivity extends AppCompatActivity {
	// Using this same prefix in client and server allows communicating through Android even if uAAL is not on
	// If you change this namespace (and also in the manifest and metadata) it will only communicate through uAAL
	public static final String PREFIX = "org.universaal.nativeandroid.light.";
	// Actions and extras for Intent-based communication
	public static final String ACTION_INNER = PREFIX + "client.INNER";
	public static final String ACTION_CALL_GETLAMPS = PREFIX + "CALL_GETLAMPS";
	public static final String ACTION_REPLY_GETLAMPS = PREFIX + "REPLY_GETLAMPS";
	public static final String ACTION_CALL_ON = PREFIX + "CALL_ON";
	public static final String ACTION_CALL_OFF = PREFIX + "CALL_OFF";
	public static final String EXTRA_LAMP = "lamp";
	public static final String EXTRA_BRIGHTNESS = "brightness";
	public static final String EXTRA_LAMPS = "lamps";
	// These extras are for saying where to send the replies. When using uAAL, the values are in exactly these extras
	public static final String EXTRA_REPLYACTION = "org.universAAL.android.action.META_REPLYTOACT";
	public static final String EXTRA_REPLYCATEGORY = "org.universAAL.android.action.META_REPLYTOCAT";
	private static final String TAG = "LightClientActivity";

	private BroadcastReceiver mReceiver = new InnerReceiver();
	private String mSelectedLamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mSelectedLamp = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ACTION_INNER);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(mReceiver);
	}

	// These methods react to button clicks and send the appropriate Intent to uAAL
	public void onButtonOffClicked(View v) {
		if (mSelectedLamp != null) {
			Log.d(TAG, "Calling 'Turn OFF' on "+mSelectedLamp);
			Intent intent = new Intent(ACTION_CALL_OFF);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra(EXTRA_LAMP, mSelectedLamp);
			sendBroadcast(intent);
		}
	}

	public void onButtonOnClicked(View v) {
		if (mSelectedLamp != null) {
			Log.d(TAG, "Calling 'Turn ON' on "+mSelectedLamp);
			Intent intent = new Intent(ACTION_CALL_ON);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra(EXTRA_LAMP, mSelectedLamp);
			sendBroadcast(intent);
		}
	}

	public void onButtonGetLampsClicked(View v) {
		Log.d(TAG, "Calling 'Get lamps'");
		Intent intent = new Intent(ACTION_CALL_GETLAMPS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(EXTRA_REPLYACTION, ACTION_REPLY_GETLAMPS);
		intent.putExtra(EXTRA_REPLYCATEGORY, Intent.CATEGORY_DEFAULT);
		sendBroadcast(intent);
	}

	// This receiver (and its usage) is only for handling UI list of lamps of the app
	public class InnerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] lampsArr = intent.getStringArrayExtra(EXTRA_LAMPS);
			if(lampsArr==null){
			    Log.e("LightCientAcitvity", "The list of lamps is empty!!! Something went wrong...");
			    return;
			}
			RadioGroup lampsGroup = (RadioGroup) findViewById(R.id.radioGroup1);
			lampsGroup.removeAllViews();
			mSelectedLamp=null;
			for (String lamp : lampsArr) {
				RadioButton lampButton = new RadioButton(
						getApplicationContext());
				lampButton.setText(lamp);
				lampButton.setTextColor(getResources().getColor(R.color.primary_dark));
				lampButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						mSelectedLamp = ((RadioButton) v).getText().toString();
					}
				});
				lampsGroup.addView(lampButton);
			}
		}
	}
}
