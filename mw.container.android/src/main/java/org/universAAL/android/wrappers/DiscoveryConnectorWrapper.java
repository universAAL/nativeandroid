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
package org.universAAL.android.wrappers;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.universAAL.middleware.connectors.DiscoveryConnector;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.util.Log;

/**
 * A mock up class imitating the jSLP discovery module, but providing a
 * transparent implementation (does nothing). This can be used when there is no
 * WiFi data connection available, and the middleware will keep working, ignorant
 * of the implementation.
 * 
 * @author alfiva
 * 
 */
public class DiscoveryConnectorWrapper implements DiscoveryConnector, SharedObjectListener{
	private final static String TAG = "DiscoveryConnectorWrapper";
	public static boolean enableLog=false; //TODO Configure this is just remove it?

	public synchronized String getName() {
		if (enableLog) {
			Log.v(TAG, "Empty getName ");
		}
		return "";
	}

	public synchronized String getVersion() {
		if (enableLog) {
			Log.v(TAG, "Empty getVersion ");
		}
		return "";
	}

	public synchronized String getDescription() {
		if (enableLog) {
			Log.v(TAG, "Empty getDescription ");
		}
		return "";
	}

	public synchronized String getProvider() {
		if (enableLog) {
			Log.v(TAG, "Empty getProvider ");
		}
		return "";
	}

	public synchronized void loadConfigurations(Dictionary configurations) {
		if (enableLog) {
			Log.v(TAG, "Empty loadConfigurations ");
		}
	}

	public synchronized boolean init() {
		if (enableLog) {
			Log.v(TAG, "Empty init ");
		}
		return false;
	}

	public synchronized void dispose() {
		if (enableLog) {
			Log.v(TAG, "Empty dispose ");
		}
	}

	public synchronized String getSDPPRotocol() {
		if (enableLog) {
			Log.v(TAG, "Empty getSDPPRotocol ");
		}
		return "";
	}

	public synchronized List<AALSpaceCard> findAALSpace(
			Dictionary<String, String> filters)
			throws DiscoveryConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty findAALSpace ");
		}
		ArrayList<AALSpaceCard> list = new ArrayList<AALSpaceCard>();
		return list;
	}

	public synchronized List<AALSpaceCard> findAALSpace()
			throws DiscoveryConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty findAALSpace ");
		}
		ArrayList<AALSpaceCard> list = new ArrayList<AALSpaceCard>();
		return list;
	}

	public synchronized void announceAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty announceAALSpace ");
		}
	}

	public synchronized void deregisterAALSpace(AALSpaceCard spaceCard)
			throws DiscoveryConnectorException {
		if (enableLog) {
			Log.v(TAG, "Empty deregisterAALSpace ");
		}
	}

	public synchronized void addAALSpaceListener(ServiceListener listener) {
		if (enableLog) {
			Log.v(TAG, "Empty addAALSpaceListener ");
		}
	}

	public synchronized void removeAALSpaceListener(ServiceListener listener) {
		if (enableLog) {
			Log.v(TAG, "Empty removeAALSpaceListener ");
		}
	}

	public synchronized void sharedObjectAdded(Object sharedObj,
			Object removeHook) {
		if (enableLog) {
			Log.v(TAG, "Empty sharedObjectAdded ");
		}
	}

	public synchronized void sharedObjectRemoved(Object removeHook) {
		if (enableLog) {
			Log.v(TAG, "Empty sharedObjectRemoved ");
		}
	}

}
