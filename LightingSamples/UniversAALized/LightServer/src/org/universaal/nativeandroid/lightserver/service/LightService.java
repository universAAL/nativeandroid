package org.universaal.nativeandroid.lightserver.service;

import org.universaal.nativeandroid.lightserver.model.LightServerModel;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *
 */
public class LightService extends IntentService {

	public LightService() {
		super("LightService");
	}

	@Override
	protected void onHandleIntent(Intent pIntent) {
		Log.i(LightService.class.getCanonicalName(), "Got intent [" + pIntent.getAction() + "]");
		
		// Forward the message to the model
		LightServerModel.getInstance(this).handleIntent(pIntent);
	}
}
