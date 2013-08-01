package org.universAAL.middleware.android.modules.messages;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Context;
import android.content.Intent;

public class CommModSendRMessage extends AbstractModulesMessage {

	PeerCard receiver;
	ChannelMessage message;

	public ChannelMessage getMessage() {
		return message;
	}

	public PeerCard getReceiver() {
		return receiver;
	}

	public CommModSendRMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		receiver = extractPeerFromExtras();
		message = extractMessageFromExtras();
	}

}
