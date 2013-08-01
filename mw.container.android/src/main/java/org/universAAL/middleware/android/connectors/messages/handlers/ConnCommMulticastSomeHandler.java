package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.connectors.messages.ConnCommMulticastSomeMessage;

public class ConnCommMulticastSomeHandler extends AbstractConnectorHandler {

	public ConnCommMulticastSomeHandler(ConnectorService serv) {
		super(serv);
	}

	@Override
	public void handleConnMessage(IMessage message, ConnectorService serv) {
		ConnCommMulticastSomeMessage msg = (ConnCommMulticastSomeMessage) message;
		this.service.getJgroupsCommunicationConnector().multicast(
				msg.getMessage(), msg.getReceivers());
	}

}
