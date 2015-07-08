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

//This is where universAAL will send intents with the service request of 'Turn on' and 'Turn off'
public class LightCallTurnReceiver extends BroadcastReceiver {

	private static final String TAG = "LightCallTurnReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		// As defined in metadata, this call has an input in this extra
		String lamp = intent.getStringExtra(LightServerActivity.EXTRA_LAMP);
		// Because we defined this receiver to get both 'on' and 'off', find out which one was called
		if (action.equals(LightServerActivity.ACTION_CALL_ON)) {
			LightServerModel.getInstance(context).setLamp(lamp, 100);
			Log.d(TAG, "Received Call to 'Turn ON ' lamp " + lamp);
		} else if (action.equals(LightServerActivity.ACTION_CALL_OFF)) {
			LightServerModel.getInstance(context).setLamp(lamp, 0);
			Log.d(TAG, "Received Call to 'Turn OFF ' lamp " + lamp);
		}
		// Find out where to send the reply.
		String replyAct=intent.getStringExtra(LightServerActivity.EXTRA_REPLYACTION);
		String replyCat=intent.getStringExtra(LightServerActivity.EXTRA_REPLYCATEGORY);
		// Send the reply.  The reply is empty, it is just say it succeeded (with an extra) and avoid timeout
		if(replyAct!=null && replyCat!=null){
			Intent reply = new Intent(replyAct);
			reply.addCategory(replyCat);
			reply.putExtra("http://ontology.universAAL.org/uAAL.owl#CallStatus", "call_succeeded");
			context.sendBroadcast(reply);
		}
		Log.d(TAG, "Sent Success Reply of 'Turn On/Off'");
	}
}
