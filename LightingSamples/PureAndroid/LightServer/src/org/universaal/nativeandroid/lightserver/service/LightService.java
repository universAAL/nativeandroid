package org.universaal.nativeandroid.lightserver.service;

import org.universaal.nativeandroid.lightserver.model.LightServerModel;

import android.app.IntentService;
import android.content.Intent;

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
		// Forward the message to the model
		LightServerModel.getInstance(this).handleIntent(pIntent);
		
//		String replyTo = (String) pIntent.getExtras().get("inReplyToAction");
//		
//		Intent response = new Intent(replyTo);
//		response.addCategory(Intent.CATEGORY_DEFAULT);
//		
//		sendBroadcast(response);
	}
}
