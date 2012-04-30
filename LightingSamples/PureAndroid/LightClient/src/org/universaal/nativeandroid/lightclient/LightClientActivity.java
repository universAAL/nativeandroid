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
package org.universaal.nativeandroid.lightclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class LightClientActivity extends Activity implements OnTouchListener
{
	private final static boolean serviceMode = true;
	
	private final static String lightServerPackage = "org.universaal.nativeandroid.lightserver";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Add touch listeners
        addTouchListeners();
    }

	private void addTouchListeners() {
		addTouchListener(R.id.btnOn);
		addTouchListener(R.id.btnOff);
	}

	private void addTouchListener(int btn) {
		Button button = (Button)findViewById(btn);
        button.setOnTouchListener(this);
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch(v.getId())
			{
			case R.id.btnOn:
				handleOnClicked();
				break;
			case R.id.btnOff:
				handleOffClicked();
				break;
			case R.id.btnScale:
				handleScaleClicked();
				break;
			default:
				break;
			}
		}
		
		return false; // To make the button seen as clicked
	}
	
	private void handleOffClicked() {
		Intent lightServerIntent = new Intent(lightServerPackage + ".TURNOFF");
		addSelectedLampNumber(lightServerIntent);
		invokeIntent(lightServerIntent);
	}

	private void handleOnClicked() {
		Intent lightServerIntent = new Intent(lightServerPackage + ".TURNON");
		addSelectedLampNumber(lightServerIntent);
		invokeIntent(lightServerIntent);
	}
	
	private void invokeIntent(Intent pIntent) {
		if (serviceMode) {
			startService(pIntent); 
		}
		else {
			startActivity(pIntent);
		}
	}
	
	private void addSelectedLampNumber(Intent pLightServerIntent) {
		pLightServerIntent.putExtra("lamp_number", getSelectedLamp());
	}
	
	private void handleScaleClicked() {
		System.out.println("Scale clicked with value [" + 
				((EditText)findViewById(R.id.editTextScalePercents)).getText() + "]");
	}
	
	private int getSelectedLamp() {
		int selectedLamp = 1; // By default set it to the first lamp
		RadioGroup rg = (RadioGroup)findViewById(R.id.groupLamps);
		int rbId = rg.getCheckedRadioButtonId();
		switch (rbId)
		{
		case R.id.radioLamp1:
			selectedLamp = 1;
			break;
		case R.id.radioLamp2:
			selectedLamp = 2;
			break;
		case R.id.radioLamp3:
			selectedLamp = 3;
			break;
		case R.id.radioLamp4:
			selectedLamp = 4;
			break;
		}
		
		return selectedLamp;
	}
}