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

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.StringConstants;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.PeerCard;

import android.content.Intent;

public class ModulesIntentFactory {

	public static Intent createInitialize() {
		Intent createdIntent = new Intent(StringConstants.ACTION_INITIALIZE);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		return createdIntent;
	}

	public static Intent createCommModReceived(ChannelMessage message) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_RECEIVED);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		return createdIntent;
	}

	public static Intent createCommModSendR(ChannelMessage message,
			PeerCard receiver) {
		Intent createdIntent = new Intent(StringConstants.ACTION_COMM_MOD_SENDR);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_RECEIVER,
				receiver.toString());
		return createdIntent;
	}

	public static Intent createCommModSendAll(ChannelMessage message) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_SENDALL);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		return createdIntent;
	}

	public static Intent createCommModSendAllR(ChannelMessage message,
			List<PeerCard> receivers) {
		Intent createdIntent = new Intent(
				StringConstants.ACTION_COMM_MOD_SENDALLR);
		createdIntent.addCategory(StringConstants.CATEGORY_MODULES);
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_MESSAGE,
				message.toString());
		List<String> list = new ArrayList<String>();
		for (PeerCard card : receivers) {
			list.add(card.toString());
		}
		createdIntent.putExtra(StringConstants.EXTRAS_CONN_RECEIVERS,
				list.toArray(new String[] {}));
		return createdIntent;
	}

}
