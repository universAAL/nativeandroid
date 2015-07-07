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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// This is a simple class maintaining the state of the simulated lamps. They are saved in preferences.
public class LightServerModel {

	private static LightServerModel singleton;
	private Context mContext;
	private SharedPreferences mPreferences;
	protected static final Object synchObj = new Object();
	private static final String PREFS_NAME = "LampsPreferences";
	private static final String TAG = "LightServerModel";

	protected LightServerModel(Context context) {
		this.mContext = context;
		mPreferences = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		for (String lamp : getLamps()) {
			if (!mPreferences.contains(lamp)) {
				setLamp(lamp, 0);
			} else {
				setLampIcon(lamp, mPreferences.getInt(lamp, 0));
			}
		}
	}

	public static LightServerModel getInstance(Context context) {
		if (null == singleton) {
			synchronized (synchObj) {
				if (null == singleton) {
					singleton = new LightServerModel(context);
				}
			}
		}
		return singleton;
	}

	public String[] getLamps() {
		// You could define more lights by adding them here
		return new String[] { mContext.getString(R.string.lamp_1),
				mContext.getString(R.string.lamp_2),
				mContext.getString(R.string.lamp_3),
				mContext.getString(R.string.lamp_4) };
	}

	public void setLamp(String lamp, int i) {
		mPreferences.edit().putInt(lamp, i).commit();
		setLampIcon(lamp, i);
		// This method is called everytime a lamp has to change, so send a Context Event in uAAL with this intent
		Log.d(TAG, "Sending 'Lamp changed' event: "+lamp+" - brightness - "+i);
		Intent intent = new Intent(LightServerActivity.ACTION_EVENT);
		intent.putExtra(LightServerActivity.EXTRA_LAMP, lamp);
		intent.putExtra(LightServerActivity.EXTRA_BRIGHTNESS, i);
		mContext.sendBroadcast(intent);
	}
	
	public int getLamp(String lamp){
		return mPreferences.getInt(lamp, 0);
	}

	public void setLampIcon(String lamp, int i) {
		// This is only for updating the UI of the app, nothing to do with uAAL
		Intent intent = new Intent(LightServerActivity.ACTION_INNER);
		intent.putExtra(LightServerActivity.EXTRA_LAMP, lamp);
		intent.putExtra(LightServerActivity.EXTRA_BRIGHTNESS, i);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}

}
