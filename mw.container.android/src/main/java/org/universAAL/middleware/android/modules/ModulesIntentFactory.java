package org.universAAL.middleware.android.modules;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Intent;

public class ModulesIntentFactory {

	public static Intent createInitialize() {
		Intent createdIntent = new Intent(StringConstants.ACTION_INITIALIZE);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		return createdIntent;
	}

	public static Intent createCommModReceived(ChannelMessage message) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_RECEIVED);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		return createdIntent;
	}

	public static Intent createCommModSendR(ChannelMessage message,
			PeerCard receiver) {
		Intent createdIntent = new Intent(StringConstants.ACTION_COMM_MOD_SENDR);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_RECEIVER,
				receiver.toString());
		return createdIntent;
	}

	public static Intent createCommModSendAll(ChannelMessage message) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_SENDALL);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		return createdIntent;
	}

	public static Intent createCommModSendAllR(ChannelMessage message,
			List<PeerCard> receivers) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_SENDALLR);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
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

}
