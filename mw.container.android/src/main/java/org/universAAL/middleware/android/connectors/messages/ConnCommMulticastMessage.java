package org.universAAL.middleware.android.connectors.messages;

import org.universAAL.middleware.connectors.util.ChannelMessage;

import android.content.Context;
import android.content.Intent;

public class ConnCommMulticastMessage extends AbstractConnectorMessage {

	private ChannelMessage message;

	public ChannelMessage getMessage() {
		return message;
	}

	public ConnCommMulticastMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		message = extractMessageFromExtras();
	}

}
