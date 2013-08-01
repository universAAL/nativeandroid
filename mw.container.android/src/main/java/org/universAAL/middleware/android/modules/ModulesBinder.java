package org.universAAL.middleware.android.modules;

import java.lang.ref.WeakReference;

import org.universAAL.middleware.android.buses.contextbus.impl.AndroidContextBusImpl;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;

import android.os.Binder;

public class ModulesBinder extends Binder {
	private WeakReference<ModulesService> mService;

	public ModulesBinder(ModulesService modulesService) {
		mService = new WeakReference<ModulesService>(modulesService);
	}

	public AndroidContextBusImpl createContextBus() {
		return mService.get().createContextBus();
	}

	public AndroidServiceBusImpl createServiceBus() {
		return mService.get().createServiceBus();
	}

}
