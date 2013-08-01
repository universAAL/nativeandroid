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
package org.universAAL.middleware.android.buses.common;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusPersistable;
import org.universAAL.middleware.android.buses.common.persistence.tables.rows.BusMemberRowDB;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.bus.model.util.IRegistry;
//import org.universAAL.middleware.sodapop.BusMember;
//import org.universAAL.middleware.sodapop.IRegistry;

import android.content.Context;

//import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public abstract class AbstractAndroidBusMembersRegistry extends
		AbstractCommonBusPersistable implements IRegistry {
	// private final static String TAG = "AndroidServiceBusRegistry";

	protected IAndroidBus androidBus;

	public AbstractAndroidBusMembersRegistry(IAndroidBus androidBus,
			Context context) {
		super(context);

		this.androidBus = androidBus;
	}

	abstract public void addBusMember(String memberID, BusMember busMember);

	public BusMember removeMemberByID(String memberID) {
		BusMemberRowDB row = null;
		// sqliteMngr.open();
		// try {
		row = sqliteMngr.removeBusMemberByMemberID(memberID);
		// } finally {
		// sqliteMngr.close();
		// }
		// This happens due to PATCH at proxy
		if (row == null)
			return null;
		// Initiate the bus member by the given row
		BusMember busMember = createBusMember(row);

		return busMember;
	}

	public BusMember getBusMemberByID(String memberID) {
		BusMemberRowDB row = null;
		// sqliteMngr.open();
		// try {
		row = sqliteMngr.queryBusMemberByID(memberID);
		// } finally {
		// sqliteMngr.close();
		// }
		// This happens due to PATCH at proxy
		if (row == null)
			return null;
		// Initiate the bus member by the given row
		BusMember busMember = createBusMember(row);

		return busMember;
	}

	public String getBusMemberID(BusMember busMember) {
		String registryID = getRegistryID(busMember);

		// sqliteMngr.open();
		// try {
		String busMemberID = sqliteMngr
				.queryBusMemberIDByGroundingID(registryID);

		return busMemberID;
		// }catch(Exception ex){
		// Log.e(TAG, "Failed SQL access at getBusMemberID", ex);
		// return null;
		// } finally {
		// sqliteMngr.close();
		// }
	}

	public BusMember[] getAllBusMembers() {
		BusMemberRowDB[] rows = null;
		// sqliteMngr.open();
		// try {
		rows = sqliteMngr.queryForAllBusMembers();
		// } finally {
		// sqliteMngr.close();
		// }
		BusMember[] busMembers = registryServicesToBusMembers(rows);

		return busMembers;
	}

	public String[] getAllBusMembersIds() {
		// sqliteMngr.open();
		// try {
		return sqliteMngr.queryForAllBusMemberIDs();
		// } finally {
		// sqliteMngr.close();
		// }
	}

	public int getBusMembersCount() {
		// sqliteMngr.open();
		// try {
		return sqliteMngr.queryForBusMembersCount();
		// } finally {
		// sqliteMngr.close();
		// }
	}

	public BusMember[] getBusMembersByPackageName(String packageName) {
		BusMemberRowDB[] rows = null;
		// sqliteMngr.open();
		// try {
		rows = sqliteMngr.queryForBusMembersByPackageName(packageName);
		// } finally {
		// sqliteMngr.close();
		// }

		BusMember[] busMembers = registryServicesToBusMembers(rows);

		return busMembers;
	}

	public void reset() {
		// sqliteMngr.open();
		// try {
		sqliteMngr.removeAllBusMembers();
		// } finally {
		// sqliteMngr.close();
		// }
	}

	protected BusMember[] registryServicesToBusMembers(BusMemberRowDB[] rows) {
		BusMember[] busMembers = new BusMember[rows.length];

		for (int i = 0; i < rows.length; i++) {
			busMembers[i] = createBusMember(rows[i]);
		}
		return busMembers;
	}

	protected abstract BusMember createBusMember(
			BusMemberRowDB registryServicesRowDB);

	protected abstract String getRegistryID(BusMember busMember);
}
