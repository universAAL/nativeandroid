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
package org.universAAL.middleware.android.buses.contextbus.data.factory;

import org.universAAL.middleware.android.buses.contextbus.data.AndroidAllPropsOfDomainData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidAllPropsOfSubjectData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidAllProvisionsData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidAllSubjectsWithPropData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidCalledPeersData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidFiltererContainerData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidNonIndexedPropData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidNumCalledPeersData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidProvisionsData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidSpecificDomainAndPropData;
import org.universAAL.middleware.android.buses.contextbus.data.AndroidSpecificSubjectAndPropData;
import org.universAAL.middleware.context.data.IAllProvisionData;
import org.universAAL.middleware.context.data.ICalledPeers;
import org.universAAL.middleware.context.data.IFiltererContainer;
import org.universAAL.middleware.context.data.INumCalledPeersData;
import org.universAAL.middleware.context.data.IPropsData;
import org.universAAL.middleware.context.data.IProvisionsData;
import org.universAAL.middleware.context.data.factory.AbstractContextStrategyDataFactory;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class AndroidContextStrategyDataFactory extends AbstractContextStrategyDataFactory {

    private Context context;

    public AndroidContextStrategyDataFactory(Context context) {
	this.context = context;
    }

    public IPropsData createAllPropsOfDomain() {
	return new AndroidAllPropsOfDomainData(context);
    }

    public IPropsData createAllPropsOfSubject() {
	return new AndroidAllPropsOfSubjectData(context);
    }

    public IAllProvisionData createAllProvisions() {
	return new AndroidAllProvisionsData();
    }

    public IPropsData createAllSubjectsWithProp() {
	return new AndroidAllSubjectsWithPropData(context);
    }

    public ICalledPeers createCalledPeers() {
	return new AndroidCalledPeersData(context);
    }

    public IFiltererContainer createFiltererContainer(String containerKey) {
	return new AndroidFiltererContainerData(context, containerKey);
    }

    public INumCalledPeersData createNumCalledPeersData() {
	return new AndroidNumCalledPeersData(context);
    }

    public IProvisionsData createProvisionsData() {
	return new AndroidProvisionsData(context);
    }

    public IPropsData createSpecificDomainAndProp() {
	return new AndroidSpecificDomainAndPropData(context);
    }

    public IPropsData createSpecificSubjectAndProp() {
	return new AndroidSpecificSubjectAndPropData(context);
    }

    public IPropsData createNonIndexedProps() {
	return new AndroidNonIndexedPropData(context);
    }
}
