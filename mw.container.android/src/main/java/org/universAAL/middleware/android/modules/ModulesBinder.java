/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
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
