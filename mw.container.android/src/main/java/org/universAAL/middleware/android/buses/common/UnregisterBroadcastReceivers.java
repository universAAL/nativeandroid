/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.android.buses.common;

import org.universAAL.middleware.android.common.StringConstants;

import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jul 1, 2012
 * 
 */
public class UnregisterBroadcastReceivers {

	public static void unregisterReceiver(String androidAction,
			String androidContext, Context context) {
		// Send a message to this receiver, specifying to unregister itself
		// Create intent filter
		Intent intent = new Intent(androidAction);
		intent.addCategory(androidContext);

		// Add a flag that informs the receiver to unregister itself
		intent.putExtra(
				StringConstants.EXTRAS_KEY_UNREGISTER_BROADCAST_RECEIVER, true);

		// Send it
		context.sendBroadcast(intent);
	}
}
