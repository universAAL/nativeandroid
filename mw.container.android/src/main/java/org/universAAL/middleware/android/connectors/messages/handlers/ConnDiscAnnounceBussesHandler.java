package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.connectors.ConnectorService;
import org.universAAL.middleware.android.connectors.messages.ConnDiscAnnounceBussesMessage;

public class ConnDiscAnnounceBussesHandler extends AbstractConnectorHandler {

	public ConnDiscAnnounceBussesHandler(ConnectorService serv) {
		super(serv);
	}

	@Override
	public void handleConnMessage(IMessage message, ConnectorService serv) {
		ConnDiscAnnounceBussesMessage msg = (ConnDiscAnnounceBussesMessage) message;
		System.out.println(":::::CON DISC ANN BUS HAND announces: "
				+ msg.getCard().getAalSpaceLifeTime() + " ; "
				+ msg.getCard().getRetry());
		this.service.getSlpDiscoveryConnector().announceAALSpace(msg.getCard());
	}

}
