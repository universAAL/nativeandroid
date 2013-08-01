package org.universAAL.middleware.android.connectors.messages;

import org.universAAL.middleware.connectors.util.ChannelMessage;

import android.content.Context;
import android.content.Intent;

public class ConnCommUnicastMessage extends AbstractConnectorMessage {

	ChannelMessage message;

	public ChannelMessage getMessage() {
		return message;
	}

	public String getReceiver() {
		return receiver;
	}

	String receiver;

	public ConnCommUnicastMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		message = extractMessageFromExtras();
		receiver = extractReceiverFromExtras();
	}

}
