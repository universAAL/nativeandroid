package org.universAAL.middleware.android.modules.messages;

import org.universAAL.middleware.connectors.util.ChannelMessage;

import android.content.Context;
import android.content.Intent;

public class CommModReceivedMessage extends AbstractModulesMessage {

	private ChannelMessage message;

	public ChannelMessage getMessage() {
		return message;
	}

	public CommModReceivedMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		message = extractMessageFromExtras();
	}

}
