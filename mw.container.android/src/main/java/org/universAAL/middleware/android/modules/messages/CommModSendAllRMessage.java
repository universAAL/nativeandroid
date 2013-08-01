package org.universAAL.middleware.android.modules.messages;

import java.util.List;

import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Context;
import android.content.Intent;

public class CommModSendAllRMessage extends AbstractModulesMessage {

	ChannelMessage message;
	List<PeerCard> receivers;

	public List<PeerCard> getReceivers() {
		return receivers;
	}

	public ChannelMessage getMessage() {
		return message;
	}

	public CommModSendAllRMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		message = extractMessageFromExtras();
		receivers = extractReceiversFromExtras();
	}

}
