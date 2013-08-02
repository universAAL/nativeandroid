/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.android.buses.servicebus.data;

import org.universAAL.middleware.android.buses.servicebus.persistence.AbstractAndroidServiceBusPersistable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalServiceIndexRowDB;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.service.data.ILocalServicesIndexData;
import org.universAAL.middleware.service.impl.ServiceRealization;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidLocalServicesIndexData extends
		AbstractAndroidServiceBusPersistable implements ILocalServicesIndexData {

	public AndroidLocalServicesIndexData(Context context) {
		super(context); // TODO: need to close the DB ... consider how to do
		// that!!!
	}

	public void addServiceRealization(String id,
			ServiceRealization serviceRealization) {
		// Serialize the ServiceRealization to string (since ServiceRalization
		// is a reource)
		String serviceRealizationAsResource = new TurtleSerializer()
				.serialize(serviceRealization);

		// Persist
		sqliteMngr.addLocalServiceIndex(id, serviceRealizationAsResource);
	}

	public ServiceRealization removeServiceRealization(String id) {
		LocalServiceIndexRowDB localServiceIndexRowDB = null;
		localServiceIndexRowDB = sqliteMngr
				.removeLocalServiceIndexByProcessURI(id);

		// De-serialize the ServiceRealization
		ServiceRealization serviceRealization = extractServiceRealization(localServiceIndexRowDB);

		return serviceRealization;
	}

	public ServiceRealization getServiceRealizationByID(String id) {
		LocalServiceIndexRowDB localServiceIndexRowDB = null;
		localServiceIndexRowDB = sqliteMngr
				.queryLocalServiceIndexByProcessURI(id);

		// De-serialize the ServiceRealization
		ServiceRealization serviceRealization = extractServiceRealization(localServiceIndexRowDB);

		return serviceRealization;
	}

	public ServiceRealization[] getAllServiceRealizations() {
		LocalServiceIndexRowDB[] localServiceIndexRowsDB = null;
		localServiceIndexRowsDB = sqliteMngr.queryForAllLocalServiceIndexes();

		ServiceRealization[] serviceRealizations = new ServiceRealization[localServiceIndexRowsDB.length];

		for (int i = 0; i < localServiceIndexRowsDB.length; i++) {
			serviceRealizations[i] = extractServiceRealization(localServiceIndexRowsDB[i]);
		}

		return serviceRealizations;
	}

	public String[] getServiceRealizationIds() {
		LocalServiceIndexRowDB[] localServiceIndexRowsDB = null;
		localServiceIndexRowsDB = sqliteMngr.queryForAllLocalServiceIndexes();

		String[] serviceRealizationIds = new String[localServiceIndexRowsDB.length];

		for (int i = 0; i < localServiceIndexRowsDB.length; i++) {
			serviceRealizationIds[i] = localServiceIndexRowsDB[i]
					.getProcessURI();
		}

		return serviceRealizationIds;
	}

	private ServiceRealization extractServiceRealization(
			LocalServiceIndexRowDB localServiceIndexRowDB) {
		// De-serialize the ServiceRealization
		String serviceRealizationAsString = localServiceIndexRowDB
				.getServiceRealization();

		Resource r = (Resource) new TurtleSerializer()
				.deserialize(serviceRealizationAsString);
		ServiceRealization serviceRealization = (ServiceRealization) r;

		return serviceRealization;
	}

}
