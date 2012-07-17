/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.android.localsodapop;

import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.localsodapop.persistence.SodaPopPeersSQLiteMngr;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 8, 2012
 * 
 */
public class SodaPopAndroidFactory {

    public static AbstractSodaPopAndroidImpl createAndroidSodaPop(Context context, String protocol,
	    SodaPopPeersSQLiteMngr sqliteMngr) {
	AbstractSodaPopAndroidImpl sodaPop = null;

	if (IAndroidSodaPop.PROTOCOL_UPNP.equalsIgnoreCase(protocol)) {
	    sodaPop = new SodaPopAndroidUPnPImpl(context, sqliteMngr);
	} else {
	    throw new IllegalArgumentException("protocol [" + protocol
		    + "] is not supported! Currently only [" + IAndroidSodaPop.PROTOCOL_UPNP
		    + "] is supported.");
	}

	return sodaPop;
    }
}
