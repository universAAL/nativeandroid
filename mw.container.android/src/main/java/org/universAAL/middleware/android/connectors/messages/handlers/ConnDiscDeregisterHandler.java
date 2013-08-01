package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.connectors.messages.ConnDiscDeregisterMessage;

public class ConnDiscDeregisterHandler extends AbstractConnectorHandler {

	public ConnDiscDeregisterHandler(ConnectorService serv) {
		super(serv);
	}

	@Override
	public void handleConnMessage(IMessage message, ConnectorService serv) {
		ConnDiscDeregisterMessage msg = (ConnDiscDeregisterMessage) message;
		this.service.getSlpDiscoveryConnector().deregisterAALSpace(
				msg.getCard());
	}

}
