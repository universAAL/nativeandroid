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
package org.universAAL.middleware.android.localsodapop.service;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.localsodapop.messages.LocalSodaPopMessageFactory;
import org.universAAL.middleware.android.localsodapop.messages.handlers.LocalSodaPopPeerHandlerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public class LocalSodaPopPeerService extends Service {

    private static final String TAG = LocalSodaPopPeerService.class.getCanonicalName();

    // /**
    // * Class for clients to access. Because we know this service always
    // * runs in the same process as its clients, we don't need to deal with
    // * IPC.
    // */
    // public class LocalBinder extends Binder {
    // LocalSodaPopPeerService getService() {
    // return LocalSodaPopPeerService.this;
    // }
    // }

    @Override
    public IBinder onBind(Intent intent) {
	return null; // Don't allow binding...
    }

    @Override
    public void onStart(Intent intent, int startId) {
	super.onStart(intent, startId);

	Log.d(TAG, "Is about to handle intent [" + intent.getAction() + "]");

	// Analyze the intent - create the message
	final IMessage message = LocalSodaPopMessageFactory.createMessage(this, intent);

	// Create the handler
	final IMessageHandler handler = LocalSodaPopPeerHandlerFactory.createHandler(intent
		.getAction());

	Thread commandThread = new Thread() {

	    @Override
	    public void run() {
		// Handle the message
		message.handle(handler);
	    }

	};
	commandThread.start();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }
}
