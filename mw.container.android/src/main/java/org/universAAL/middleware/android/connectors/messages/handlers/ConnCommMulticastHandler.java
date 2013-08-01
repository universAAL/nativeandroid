package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.connectors.messages.ConnCommMulticastMessage;

public class ConnCommMulticastHandler extends AbstractConnectorHandler {

	public ConnCommMulticastHandler(ConnectorService serv) {
		super(serv);
	}

	@Override
	public void handleConnMessage(IMessage message, ConnectorService serv) {
		ConnCommMulticastMessage msg = (ConnCommMulticastMessage) message;
		this.service.getJgroupsCommunicationConnector().multicast(
				msg.getMessage());
	}

}
