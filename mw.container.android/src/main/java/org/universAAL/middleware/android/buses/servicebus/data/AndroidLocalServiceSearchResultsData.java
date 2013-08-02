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

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.servicebus.persistence.AbstractAndroidServiceBusPersistable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalServiceSearchResultRowDB;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.service.data.ILocalServiceSearchResultsData;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidLocalServiceSearchResultsData extends
		AbstractAndroidServiceBusPersistable implements
		ILocalServiceSearchResultsData {

	public AndroidLocalServiceSearchResultsData(Context context) {
		super(context); // TODO: need to close the DB ... consider how to do
		// that!!!
	}

	public void addProfiles(String id, List profiles) {
		// Serialize the Service Profiles
		String[] serializedProfiles = serializeServiceProfiles(profiles);

		// Persist the service profiles
		sqliteMngr.addLocalServicesSearchResultsWithProfiles(id,
				serializedProfiles);
	}

	public List getProfiles(String id) {
		LocalServiceSearchResultRowDB localServiceSearchResultRowDB = sqliteMngr
				.queryForLocalServiceSearchResults(id);

		String[] serializedProfiles = localServiceSearchResultRowDB
				.getProfiles();

		// Initiate a parser
		TurtleSerializer parser = new TurtleSerializer();

		// De-serialize the profiles
		List profiles = new ArrayList();
		for (String serializedProfile : serializedProfiles) {
			profiles.add(parser.deserialize(serializedProfile));
		}

		return profiles;
	}

	public boolean exist(String id) {
		return null != sqliteMngr.queryForLocalServiceSearchResults(id);
	}

	private String[] serializeServiceProfiles(List profiles) {
		String[] serializedProfiles = new String[profiles.size()];

		// Initiate a parser
		TurtleSerializer parser = new TurtleSerializer();

		for (int i = 0; i < profiles.size(); i++) {
			ServiceProfile serviceProfile = (ServiceProfile) profiles.get(i);

			// Serialize current Service Profile
			serializedProfiles[i] = parser.serialize(serviceProfile);
		}

		return serializedProfiles;
	}
}
