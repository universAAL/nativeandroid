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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// Here it receives replies to the 'Get lamps' call. It just updates the UI.
public class LightReplyReceiver extends BroadcastReceiver {

	private static final String TAG = "LightReplyReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Just copies extras into a new inner Intent. It could simple reuse the
		// same received intent, but that's up to the app logic, to update the
		// UI, not part of the example.
		Log.d(TAG, "Received Reply of 'Get lamps'");
		Intent inner = new Intent(LightClientActivity.ACTION_INNER);
		inner.putExtra(LightClientActivity.EXTRA_LAMPS,
				intent.getStringArrayExtra(LightClientActivity.EXTRA_LAMPS));
		LocalBroadcastManager.getInstance(context).sendBroadcast(inner);
	}

}
