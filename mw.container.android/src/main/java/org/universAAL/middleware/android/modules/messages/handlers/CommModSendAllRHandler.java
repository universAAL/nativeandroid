package org.universAAL.middleware.android.modules.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.android.modules.messages.CommModSendAllRMessage;

public class CommModSendAllRHandler extends AbstractModulesHandler {

	public CommModSendAllRHandler(ModulesService serv) {
		super(serv);
	}

	@Override
	public void handleModMessage(IMessage message, ModulesService serv) {
		CommModSendAllRMessage msg = (CommModSendAllRMessage) message;
		this.service.getCommunicationModule().sendAll(msg.getMessage(),
				msg.getReceivers());
	}

}
