package org.universAAL.middleware.android.modules.messages.handlers;

import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.IMessageHandler;
import org.universAAL.middleware.android.modules.ModulesService;

public abstract class AbstractModulesHandler implements IMessageHandler {

	protected ModulesService service;

	public AbstractModulesHandler(ModulesService serv) {
		service = serv;
	}

	public void handleMessage(IMessage message) {
		handleModMessage(message, service);
	}

	public abstract void handleModMessage(IMessage message, ModulesService serv);
}
