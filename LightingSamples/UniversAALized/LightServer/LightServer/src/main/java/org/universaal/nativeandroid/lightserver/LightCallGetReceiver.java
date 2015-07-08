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
import android.util.Log;

//This is where universAAL will send intents with the service request of 'Get lamps'
public class LightCallGetReceiver extends BroadcastReceiver {

	private static final String TAG = "LightCallGetReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received Call to 'Get lamps'");
		// We defined no inputs in this call, simply send the reply
		Intent reply = new Intent(
				intent.getStringExtra(LightServerActivity.EXTRA_REPLYACTION));
		reply.addCategory(intent
				.getStringExtra(LightServerActivity.EXTRA_REPLYCATEGORY));
		reply.putExtra(LightServerActivity.EXTRA_LAMPS, LightServerModel
				.getInstance(context).getLamps());
		context.sendBroadcast(reply);
		Log.d(TAG, "Sent Reply of 'Get Lamps'");
	}

}
