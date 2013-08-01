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
package org.universAAL.middleware.android.common.messages;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public abstract class AbstractMessage implements IMessage {

	private final static String TAG = "AbstractMessage";

	protected Context context;
	protected Intent intent;

	protected String protocol;

	public AbstractMessage(Context context, Intent intent) {
		this.context = context;
		this.intent = intent;

		extractValuesFromExtrasSection();
	}

	public void handle(IMessageHandler handler) {
		try {
			handler.handleMessage(this);
		} catch (Throwable th) {
			Log.e(TAG, "Error when handling the message due to ", th);
		}
	}

	public Context getContext() {
		return context;
	}

	public String getProtocol() {
		return protocol;
	}

	protected void extractValuesFromExtrasSection() {
		protocol = extractProtocolFromExtras();
	}

	protected String extractProtocolFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_PROTOCOL);
	}

	protected String extractPeerIDFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_PEER_ID);
	}

	protected String extractPeerBussesFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_PEER_BUSSES);
	}

	protected String extractPeerBusNameFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_PEER_BUS_NAME);
	}

	protected String extractBusPackageNameFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_BUS_PACKAGE_NAME);
	}

	protected String extractBusClassNameFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_BUS_CLASS_NAME);
	}

	protected String extractRemotePeerIDsForResponseFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_REMOTE_PEER_IDS_RSP);
	}

	protected String extractPackageNameFromExtras() {
		return extractStringFromExtras(StringConstants.EXTRAS_KEY_PACKAGE_NAME);
	}

	protected String extractStringFromExtras(String key) {
		String value = intent.getStringExtra(key);
		if (null == value) {
			value = "";
		}

		return value;
	}
}
