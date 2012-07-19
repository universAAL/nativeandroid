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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class LampStateChangedBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// Extract the lamp number
		String lampNumber = intent.getStringExtra(IConstants.lampNumberArg);
		
		// Extract the brightness
		int brightness = intent.getIntExtra(IConstants.brightnessArg, -1);
		
		// Construct a string
		String formattedStringForNotification = "";
		if (null != lampNumber && lampNumber.length() > 0 && -1 != brightness) {
			formattedStringForNotification = String.format("Lamp no.%s state changed to %d brightness", lampNumber, brightness);
		} else {
			formattedStringForNotification = "Lamp state was changed, but illegal paramters were received!";
		}
		
		// Display the notification
		displayNotification(formattedStringForNotification, context);
	}
	
	private void displayNotification(String message, Context context) {
		NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		CharSequence contentTitle = context.getResources().getString(R.string.app_name);;
		CharSequence contentText = message;
		Intent notificationIntent = new Intent(context, LampStateChangedBroadCastReceiver.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		final Notification notify = new Notification(R.drawable.ic_launcher, contentText, System.currentTimeMillis());
		notify.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notifier.notify(1, notify);
	}
}
