package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.connectors.messages.ConnCommUnicastMessage;

public class ConnCommUnicastHandler extends AbstractConnectorHandler {

	public ConnCommUnicastHandler(ConnectorService serv) {
		super(serv);
	}

	@Override
	public void handleConnMessage(IMessage message, ConnectorService serv) {
		ConnCommUnicastMessage msg = (ConnCommUnicastMessage) message;
		this.service.getJgroupsCommunicationConnector().unicast(
				msg.getMessage(), msg.getReceiver());
	}

}
