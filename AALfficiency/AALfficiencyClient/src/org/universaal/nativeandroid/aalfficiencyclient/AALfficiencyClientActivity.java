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
 */
package org.universaal.nativeandroid.aalfficiencyclient;

import org.universaal.nativeandroid.aalfficiencyclient.utils.Constants;
import org.universaal.nativeandroid.aalfficiencyclient.utils.Scores;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AALfficiencyClientActivity extends TabActivity {
    /** Called when the activity is first created. */
	  
	Scores scores = new Scores();
	 Facebook facebook = new Facebook(Constants.APP_ID);
     private ProgressDialog mProgress;
 	private Handler mRunOnUi = new Handler();	
 	private SharedPreferences mPrefs;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        
        // TabSpec used to create a new tab. 
        TabSpec totalTabSpec = tabHost.newTabSpec(getString( R.string.total));
        TabSpec electricTabSpec = tabHost.newTabSpec(getString( R.string.electricity));
        TabSpec activityTabSpec = tabHost.newTabSpec(getString( R.string.activity));
        TabSpec tipsTabSpec = tabHost.newTabSpec(getString( R.string.tips));
                      
        /*
        * TabSpec setIndicator() is used to set name for the tab. 
        * TabSpec setContent() is used to set content for a particular tab. */
        
        Intent totalIntent = new Intent(this,TotalScoreActivity.class);
        Intent electricIntent = new Intent(this,ElectricScoreActivity.class);
        Intent activityIntent = new Intent(this,ActivityScoreActivity.class);
        Intent tipsIntent = new Intent(this,TipsActivity.class);
        
             
        totalTabSpec.setIndicator(getString( R.string.total)).setContent(totalIntent);
        electricTabSpec.setIndicator(getString( R.string.electricity)).setContent(electricIntent);
        activityTabSpec.setIndicator(getString( R.string.activity)).setContent(activityIntent);
        tipsTabSpec.setIndicator(getString( R.string.tips)).setContent(tipsIntent);
        
        
        /* Add tabSpec to the TabHost to display. */
        tabHost.addTab(totalTabSpec);
        tabHost.addTab(electricTabSpec);
        tabHost.addTab(activityTabSpec);
        tabHost.addTab(tipsTabSpec);
  
        
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
      
}