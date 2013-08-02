package org.universAAL.middleware.android.connectors.messages;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.android.common.messages.AbstractMessage;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.Consts;

import android.content.Context;
import android.content.Intent;

public class AbstractConnectorMessage extends AbstractMessage {

	public AbstractConnectorMessage(Context context, Intent intent) {
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

	protected String extractReceiverFromExtras() {
		return intent.getStringExtra(StringConstants.EXTRAS_CONN_RECEIVER);
	}

	protected AALSpaceCard extractCardFromExtras() {
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(Consts.AALSPaceName,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_NAME));
		props.put(Consts.AALSPaceID,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_ID));
		props.put(Consts.AALSPaceDescription,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_DESC));
		props.put(Consts.AALSpaceCoordinator,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_COORD));
		props.put(Consts.AALSpacePeeringChannelURL,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_CHN));
		props.put(Consts.AALSpacePeeringChannelName,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_CHNNAME));
		props.put(Consts.AALSPaceProfile,
				intent.getStringExtra(StringConstants.EXTRAS_SPACE_PROFILE));
		return new AALSpaceCard(props);
	}

}
