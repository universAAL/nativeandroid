/*
	Copyright 2011-2012 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
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
 */package org.universaal.nativeandroid.aalfficiencyclient;

import java.util.ArrayList;

import org.universaal.nativeandroid.aalfficiencyclient.utils.Constants;
import org.universaal.nativeandroid.aalfficiencyclient.utils.Scores;
import org.universaal.nativeandroid.aalfficiencyclient.utils.Tip;
import org.universaal.nativeandroid.aalfficiencyclient.utils.TipsListAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class TipsActivity extends Activity{
	
	private final static String TAG 	= AALfficiencyClientActivity.class.getCanonicalName();
	private final static String prefix 	= AALfficiencyClientActivity.class.getPackage().getName();
	
	private BroadcastReceiver registeredReceiver = null;
	private BroadcastReceiver registeredReceiver2 = null;
	
	private String[] Advices;
	private ArrayList<Tip> tips = new ArrayList<Tip>();
	private LinearLayout layout;
	private TipsListAdapter tipsAdapter;

	private ProgressDialog dialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.tips);

        final Spinner cmbOpciones = (Spinner)findViewById(R.id.typesList);
        
        final ListView tipsListView = (ListView) findViewById(R.id.listTips);
        tipsAdapter = new TipsListAdapter(this, R.layout.list_item_layout);
        tipsListView.setAdapter(tipsAdapter);
        
        layout = (LinearLayout)findViewById(R.id.tipsLayout);
        
        final String[] datos =
            new String[]{getString(R.string.all),
        		getString(R.string.electricitySaving),getString(R.string.physicalActivity)};
        dialog = ProgressDialog.show(getParent(),getString(R.string.loading),getString( R.string.wait));   
        getAdvices();
        
                     
        ArrayAdapter<String> adaptador =
                new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, datos);
            
            adaptador.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
             
            cmbOpciones.setAdapter(adaptador);

            cmbOpciones.setOnItemSelectedListener(
            	new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                        android.view.View v, int position, long id) {
                    	tipsAdapter.clear();
                    	String selected = parent.getItemAtPosition(position).toString();
                    	if (selected.compareTo(getString(R.string.all))==0){
                    		for (Tip t : tips){
                        		tipsAdapter.add(t);
                    		}
                    	}
                    	else if (selected.compareTo(getString(R.string.electricitySaving))==0){
                    		for (Tip t : tips){
                    			if (t.getType().compareTo("Electricity")==0)
                        		tipsAdapter.add(t);
                    		}
                    	}
                    	else {
                    		for (Tip t : tips){
                    			if (t.getType().compareTo("Activity")==0)
                        		tipsAdapter.add(t);
                    		}
                    		
                    	}
                         
                    }
             
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
            });
            
        
	}
	 
	private void invokeIntent(Intent intent) {
 		sendBroadcast(intent);
 	}
	   
	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
			Intent registeredIntent = super.registerReceiver(receiver, filter);
			
			// Keep the register
			registeredReceiver = receiver;
			
			return registeredIntent;
		}	
	public Intent registerReceiver2(BroadcastReceiver receiver, IntentFilter filter) {
		Intent registeredIntent = super.registerReceiver(receiver, filter);
		
		// Keep the register
		registeredReceiver2 = receiver;
		
		return registeredIntent;
	}	

    private void getAdvices(){
    	
    	// Unregister the previous registration (if exists)
		if (null != registeredReceiver) {
			unregisterReceiver(registeredReceiver);
			registeredReceiver = null;
		}
		
		// Register for receiver that will wait for the response
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
			
				//unregisterReceiver(this);
				
				runOnUiThread(
						new Thread() {
							
							public void run() {
								analyzeGetTipsResponse(intent);
							}
						}
				);
			}

		};
		
		// Action name for the reply
		String actionNameForReply = receiver.getClass().getName();
		
		// Category for the reply
		String category = Intent.CATEGORY_DEFAULT;
		
		// Add a filter to the receiver
		IntentFilter filter = new IntentFilter(actionNameForReply);
		filter.addCategory(category);
		registerReceiver(receiver, filter);
		
		// Create the intent that will be sent as a broadcast message
		Intent intent = new Intent(prefix + ".GET_ADVICES");
		intent.putExtra(Constants.replyToActionArg, actionNameForReply);
		intent.putExtra(Constants.replyToCategoryArg, category);
		
		invokeIntent(intent);    
    }

    private void analyzeGetTipsResponse(Intent intent) {
    		
	   String[] advicesArr = intent.getStringArrayExtra("tips");
	   
	   for (int i=0;i<advicesArr.length;i++){
		   getAdviceInfo(advicesArr[i]);
	   }
	   
   }

    private void getAdviceInfo(String uri){
    	// 	Unregister the previous registration (if exists)
		if (null != registeredReceiver2) {
			unregisterReceiver(registeredReceiver2);
			registeredReceiver2 = null;
		}
		
		// Register for receiver that will wait for the response
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
			
				//unregisterReceiver(this);
				
				runOnUiThread(
						new Thread() {
							
							public void run() {
								analyzeAdviceInfoResponse(intent);
							}
						}
				);
			}

		};
		
		// Action name for the reply
		String actionNameForReply = receiver.getClass().getName();
		
		// Category for the reply
		String category = Intent.CATEGORY_DEFAULT;
		
		// Add a filter to the receiver
		IntentFilter filter = new IntentFilter(actionNameForReply);
		filter.addCategory(category);
		registerReceiver2(receiver, filter);
		
		// Create the intent that will be sent as a broadcast message
		Intent intent = new Intent(prefix + ".GET_ADVICE_INFO");
		intent.putExtra(Constants.replyToActionArg, actionNameForReply);
		intent.putExtra(Constants.replyToCategoryArg, category);
		intent.putExtra("advice_uri", uri);
		
		invokeIntent(intent);    
    }

    private void analyzeAdviceInfoResponse(Intent intent) {
    	
    	Bundle adviceBundle = intent.getBundleExtra("advice");
    	String type = adviceBundle.getString("type");
    	String text = adviceBundle.getString("text");
 	   	Tip tip = new Tip();
    	tip.setText(text);
    	tip.setType(type);
    	
    	boolean flag = false;
    	
    	for (Tip t : tips){
    		if (t.getText().compareTo(text)==0)
    			flag = true;
    	}
    	
    	if (!flag){
    		tips.add(tip);
    		tipsAdapter.add(tip);
    	}
    	dialog.dismiss();
    }
	
}
