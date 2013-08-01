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
package org.universAAL.middleware.android.buses.contextbus.data;

import java.util.ArrayList;
import java.util.Collection;

import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.FiltererContainerRowDB;
import org.universAAL.middleware.context.data.IFiltererContainer;
import org.universAAL.middleware.context.data.IPropsData;
import org.universAAL.middleware.context.data.factory.IContextStrategyDataFactory;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 * 
 */
public abstract class AbstractAndroidPropsData extends
		AbstractAndroidContextBusPersistable implements IPropsData {

	private IContextStrategyDataFactory factory;

	public AbstractAndroidPropsData(Context context) {
		super(context);

		factory = new AndroidContextStrategyDataFactory(context);
	}

	public Collection getAllFiltererContainers() {
		Collection containers = null;

		// sqliteMngr.open();
		// try {
		// Query for the container
		FiltererContainerRowDB[] rowsDB = sqliteMngr
				.queryForFiltererContainersByContainerType(getContainerType());

		containers = createFiltererContainers(rowsDB);

		// } finally {
		// sqliteMngr.close();
		// }

		return containers;
	}

	public IAndroidFiltererContainerData getFiltererContainer(String key) {
		IAndroidFiltererContainerData container = null;

		// sqliteMngr.open();
		// try {
		// Query for the container
		FiltererContainerRowDB rowDB = sqliteMngr.queryForFiltererContainer(
				key, getContainerType());
		if (null == rowDB) {
			// The container was not found - create an empty one
			rowDB = sqliteMngr.addFiltererContainer(key, getContainerType());
		}

		container = createFiltererContainer(rowDB);

		// } finally {
		// sqliteMngr.close();
		// }

		return container;
	}

	protected IAndroidFiltererContainerData createFiltererContainer(
			FiltererContainerRowDB rowDB) {
		// Initiate the container
		IAndroidFiltererContainerData container = (IAndroidFiltererContainerData) factory
				.createFiltererContainer(rowDB.getContainerKey());

		// Set the type
		container.setContainerType(rowDB.getContainerType());

		return container;
	}

	private Collection<IFiltererContainer> createFiltererContainers(
			FiltererContainerRowDB[] rowsDB) {
		Collection<IFiltererContainer> containers = new ArrayList<IFiltererContainer>();

		for (FiltererContainerRowDB rowDB : rowsDB) {
			containers.add(createFiltererContainer(rowDB));
		}

		return containers;
	}

	abstract protected String getContainerType();
}
