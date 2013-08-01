package org.universAAL.middleware.android.connectors.messages;

import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Context;
import android.content.Intent;

public class ConnCommMulticastSomeMessage extends AbstractConnectorMessage {

	private ChannelMessage message;

	public ChannelMessage getMessage() {
		return message;
	}

	public List<PeerCard> getReceivers() {
		return receivers;
	}

	private List<PeerCard> receivers;

	public ConnCommMulticastSomeMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		message = extractMessageFromExtras();
		receivers = extractReceiversFromExtras();
	}

}
