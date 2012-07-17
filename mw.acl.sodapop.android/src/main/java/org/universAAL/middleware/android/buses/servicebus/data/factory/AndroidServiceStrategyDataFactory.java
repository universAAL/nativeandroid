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
package org.universAAL.middleware.android.buses.servicebus.data.factory;

import org.universAAL.middleware.android.buses.servicebus.data.AndroidLocalServiceSearchResultsData;
import org.universAAL.middleware.android.buses.servicebus.data.AndroidLocalServicesIndexData;
import org.universAAL.middleware.android.buses.servicebus.data.AndroidLocalWaitingCallersData;
import org.universAAL.middleware.android.buses.servicebus.data.AndroidWaitingCallsData;
import org.universAAL.middleware.android.buses.servicebus.data.IWaitingCallsData;
import org.universAAL.middleware.service.data.ILocalServiceSearchResultsData;
import org.universAAL.middleware.service.data.ILocalServicesIndexData;
import org.universAAL.middleware.service.data.ILocalWaitingCallersData;
import org.universAAL.middleware.service.data.factory.AbstractServiceStrategyDataFactory;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidServiceStrategyDataFactory extends AbstractServiceStrategyDataFactory {

    private Context context;

    public AndroidServiceStrategyDataFactory(Context context) {
	this.context = context;
    }

    public ILocalServicesIndexData createLocalServicesIndexData() {
	return new AndroidLocalServicesIndexData(context);
    }

    public ILocalServiceSearchResultsData createLocalServiceSearchResultsData() {
	return new AndroidLocalServiceSearchResultsData(context);
    }

    public ILocalWaitingCallersData createLocalWaitingCallersData() {
	return new AndroidLocalWaitingCallersData(context);
    }

    // Contains the call itself and relevant only for the Android
    public IWaitingCallsData createWaitingCallsData() {
	return new AndroidWaitingCallsData(context);
    }
}
