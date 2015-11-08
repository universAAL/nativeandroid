/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.android.receivers.system;

import org.universAAL.android.services.MiddlewareService;
import org.universAAL.android.services.ScanService;
import org.universAAL.android.utils.AppConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast receiver that gets called with PACKAGE_ADDED, PACKAGE_REMOVED and
 * PACKAGE_REPLACED system actions. It relays this information to the Scan
 * Service.
 * 
 * @author alfiva
 * 
 */
public class PackageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Ignore app un/installations if MW is not started, because otherwise it will realize when it starts
		if(MiddlewareService.mStatus!= AppConstants.STATUS_STARTED)return;
		// A change in packages happened. Relay to ScanService who will know what to do.
		Log.v("PackageReceiver", "Received Broadcast: " + intent.getAction());
		Intent start = new Intent(intent);
		start.setClass(context, ScanService.class);
		context.startService(start);
	}

}
