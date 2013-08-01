package org.universAAL.middleware.android.modules.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.android.modules.messages.CommModReceivedMessage;

public class CommModReceivedHandler extends AbstractModulesHandler {

	public CommModReceivedHandler(ModulesService serv) {
		super(serv);
	}

	@Override
	public void handleModMessage(IMessage message, ModulesService serv) {
		CommModReceivedMessage msg = (CommModReceivedMessage) message;
		this.service.getCommunicationModule().messageReceived(msg.getMessage());
	}

}
