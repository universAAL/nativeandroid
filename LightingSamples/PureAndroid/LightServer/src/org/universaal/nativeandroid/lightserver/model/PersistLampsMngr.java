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
package org.universaal.nativeandroid.lightserver.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universaal.nativeandroid.lightserver.common.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class PersistLampsMngr
{
	private static final String PREFS_NAME 		= "LampsPreferences";
	private static final String LampsListKey 	= "LampsList";
	private static final String LampsDelim		= ";";
	
	private SharedPreferences sharedPreferences;
	
	public PersistLampsMngr(Context pContext) {
		sharedPreferences = pContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public void addLamp(String lampID) {
		addLampIfNotExist(lampID);
	}
	
	public void setLampState(String lampID, boolean isTurnOn) {
		SharedPreferences.Editor editor = getEditor();
		
		// Add the lamp if not exist
		addLamp(lampID);
		
		// Update the lamp state
		editor.putBoolean(lampID, isTurnOn);

		// Save
		editor.commit();
	}
	
	public Map<String, Boolean> getLamps() {
		Map<String, Boolean> lamps = new HashMap<String, Boolean>();
		
		// Get the lamp list
		List<String> lampsList = getLampList();
		
		for (String lampID : lampsList) {
			lamps.put(lampID, getLampState(lampID));
		}
		
		return lamps;
	}
	
	private List<String> getLampList() {
		String lampsListStr = getPreferences().getString(LampsListKey, "");
		Log.d(getClass().getCanonicalName(), "Lamp list was extracted [" + lampsListStr + "]");
		List<String> lampsList;;
		if (StringUtils.isEmpty(lampsListStr)) {
			lampsList = new ArrayList<String>();
		}
		else {
			lampsList = Arrays.asList(lampsListStr.split(LampsDelim));
		}
		return lampsList;
	}
	
	private boolean getLampState(String lampID) {
		return getPreferences().getBoolean(lampID, false);
	}
	
	
	private void addLampIfNotExist(String lampID) {
		List<String> lampsList = getLampList();
		if (!lampsList.contains(lampID)) {
			// Get current value
			String lampsListAsStr = StringUtils.toString(lampsList, "", LampsDelim);
			
			// Add the given value
			if (!StringUtils.isEmpty(lampsListAsStr)) {
				lampsListAsStr += LampsDelim;
			}
			lampsListAsStr += lampID;
			
			// Save
			SharedPreferences.Editor editor = getEditor();
			editor.putString(LampsListKey, lampsListAsStr);
			Log.d(getClass().getCanonicalName(), "Save key [" + LampsListKey + "] Value [" + lampsListAsStr + "]");
			editor.commit();
		}
	}
	
	private SharedPreferences getPreferences() {
		return sharedPreferences;
	}
	
	private SharedPreferences.Editor getEditor() {
		SharedPreferences settings = getPreferences();
		SharedPreferences.Editor editor = settings.edit();
		
		return editor;
	}
	
	// For DEBUG only
    @SuppressWarnings("unused")
	private void clearLampList() {
    	SharedPreferences.Editor ed = getEditor();
    	ed.remove(LampsListKey);
    	ed.commit();
    }
}
