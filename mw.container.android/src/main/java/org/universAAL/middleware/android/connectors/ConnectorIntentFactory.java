package org.universAAL.middleware.android.connectors;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.content.Intent;

public class ConnectorIntentFactory {

	public static Intent createConnCommMulticast(ChannelMessage message) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_CONN_COMM_MULTICAST);
		createdIntent.addCategory(StringConstants.CATEGORY_CONNECTOR);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		return createdIntent;
	}

	public static Intent createConnCommMulticastSome(ChannelMessage message,
			List<PeerCard> receivers) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_CONN_COMM_MULTICASTSOME);
		createdIntent.addCategory(StringConstants.CATEGORY_CONNECTOR);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		List<String> list = new ArrayList<String>();
		for (PeerCard card : receivers) {
			list.add(card.toString());
		}
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_RECEIVERS,
				list.toArray(new String[] {}));
		return createdIntent;
	}

	public static Intent createConnCommUnicast(ChannelMessage message,
			String receiver) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_CONN_COMM_UNICAST);
		createdIntent.addCategory(StringConstants.CATEGORY_CONNECTOR);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_RECEIVER, receiver);
		return createdIntent;
	}

	public static Intent createConnDiscAnnounceBusses(AALSpaceCard card) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_CONN_DISC_ANNOUNCE);
		createdIntent.addCategory(StringConstants.CATEGORY_CONNECTOR);
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_CHN,
				card.getPeeringChannel());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_CHNNAME,
				card.getPeeringChannelName());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_COORD,
				card.getCoordinatorID());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_DESC,
				card.getDescription());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_ID,
				card.getSpaceID());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_NAME,
				card.getSpaceName());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_PROFILE,
				card.getProfile());
		return createdIntent;
	}

	public static Intent createConnDiscDeregister(AALSpaceCard card) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_CONN_DISC_DEREGISTER);
		createdIntent.addCategory(StringConstants.CATEGORY_CONNECTOR);
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_CHN,
				card.getPeeringChannel());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_CHNNAME,
				card.getPeeringChannelName());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_COORD,
				card.getCoordinatorID());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_DESC,
				card.getDescription());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_ID,
				card.getSpaceID());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_NAME,
				card.getSpaceName());
		createdIntent.putExtra(StringConstants.EXTRAS_SPACE_PROFILE,
				card.getProfile());
		return createdIntent;
	}

}
