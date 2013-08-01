package org.universAAL.middleware.android.modules.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.android.modules.messages.CommModSendAllMessage;

public class CommModSendAllHandler extends AbstractModulesHandler {

	public CommModSendAllHandler(ModulesService serv) {
		super(serv);
	}

	@Override
	public void handleModMessage(IMessage message, ModulesService serv) {
		CommModSendAllMessage msg = (CommModSendAllMessage) message;
		this.service.getCommunicationModule().sendAll(msg.getMessage());
	}

}
