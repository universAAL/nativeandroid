package org.universAAL.middleware.android.modules.messages;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.android.common.messages.AbstractMessage;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Context;
import android.content.Intent;

public class AbstractModulesMessage extends AbstractMessage {

	public AbstractModulesMessage(Context context, Intent intent) {
		super(context, intent);
	}

	protected ChannelMessage extractMessageFromExtras() {
		String msg = intent.getStringExtra(StringConstants.EXTRAS_CONN_MESSAGE);
		try {
			return ChannelMessage.unmarhall(msg);
		} catch (Exception e) {
			return null;
		}
	}

	protected List<PeerCard> extractReceiversFromExtras() {
		List<PeerCard> list = new ArrayList<PeerCard>();
		String[] receivers = intent
				.getStringArrayExtra(StringConstants.EXTRAS_CONN_RECEIVERS);
		for (int i = 0; i < receivers.length; i++) {
			list.add(new PeerCard(receivers[i]));
		}
		return list;
	}

	protected PeerCard extractPeerFromExtras() {
		String receiver = intent
				.getStringExtra(StringConstants.EXTRAS_CONN_RECEIVER);
		return new PeerCard(receiver);
	}

}
