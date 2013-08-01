package org.universAAL.middleware.android.connectors.messages;

import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.content.Context;
import android.content.Intent;

public class ConnDiscAnnounceBussesMessage extends AbstractConnectorMessage {

	private AALSpaceCard card;

	public AALSpaceCard getCard() {
		return card;
	}

	public ConnDiscAnnounceBussesMessage(Context context, Intent intent) {
		super(context, intent);
	}

	@Override
	protected void extractValuesFromExtrasSection() {
		super.extractValuesFromExtrasSection();
		card = extractCardFromExtras();
	}

}
