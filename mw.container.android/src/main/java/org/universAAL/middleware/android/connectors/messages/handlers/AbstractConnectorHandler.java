package org.universAAL.middleware.android.connectors.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.connectors.ConnectorService;

public abstract class AbstractConnectorHandler implements IMessageHandler {

	protected ConnectorService service;

	public AbstractConnectorHandler(ConnectorService serv) {
		service = serv;
	}

	public void handleMessage(IMessage message) {
		handleConnMessage(message, service);
	}

	public abstract void handleConnMessage(IMessage message,
			ConnectorService serv);
}
