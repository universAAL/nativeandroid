package org.universAAL.middleware.android.modules.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.android.modules.messages.CommModSendRMessage;

public class CommModSendRHandler extends AbstractModulesHandler {

	public CommModSendRHandler(ModulesService serv) {
		super(serv);
	}

	@Override
	public void handleModMessage(IMessage message, ModulesService serv) {
		CommModSendRMessage msg = (CommModSendRMessage) message;
		this.service.getCommunicationModule().send(msg.getMessage(),
				msg.getReceiver());
	}

}
