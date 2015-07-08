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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

// This is where universAAL will send intents with the values of Context Events as defined in the metadata
public class LightChangeReceiver extends BroadcastReceiver {
	private static final String TAG = "LightChangeReceiver";
	// An arbitrary notification ID, for rewriting always the same notification
	public static int NOTIF_ID = 8225;

	@Override
	public void onReceive(Context context, Intent intent) {
		// According to our metadata, the subject and the object were placed in these extras
		String lamp = intent.getStringExtra(LightClientActivity.EXTRA_LAMP);
		Integer brightness = intent.getIntExtra(
				LightClientActivity.EXTRA_BRIGHTNESS, -1);
		Log.d(TAG, "Received 'Lamp changed' event: "+lamp+" - brightness - "+brightness);
		String notifText = "";
		if (null != lamp && lamp.length() > 0 && -1 != brightness) {
			notifText = context.getString(R.string.notif_text)
					.replace("{1}", lamp).replace("{2}", brightness.toString());
		} else {
			notifText = "A Lamp brightness changed, but illegal paramters were received!";
		}
		// Build a notification
		Intent notificationIntent = new Intent(context,
				LightClientActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_notif)
				.setContentTitle(context.getString(R.string.notif_title))
				.setContentText(notifText).setContentIntent(contentIntent)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		Notification notif = builder.build();
		// Show the notification
		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIF_ID, notif);
	}
}
