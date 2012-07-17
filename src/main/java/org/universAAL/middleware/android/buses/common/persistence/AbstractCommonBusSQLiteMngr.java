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
package org.universAAL.middleware.android.buses.common.persistence;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteHelper.MemberType;
import org.universAAL.middleware.android.buses.common.persistence.tables.AbstractBusMembersTable;
import org.universAAL.middleware.android.buses.common.persistence.tables.rows.BusMemberRowDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 */
public abstract class AbstractCommonBusSQLiteMngr {

    private static final String TAG = AbstractCommonBusSQLiteMngr.class.getName();

    private static final Object sync = new Object();

    // Database fields
    protected SQLiteDatabase database;
    protected AbstractCommonBusSQLiteHelper dbHelper;

    public AbstractCommonBusSQLiteMngr(Context context, AbstractCommonBusSQLiteHelper helper) {
	dbHelper = helper;
    }

    public void open() throws SQLException {
	synchronized (sync) {
	    database = dbHelper.getWritableDatabase(); // TODO: find another
						       // mechanism for
						       // open/close DB's
	}
    }

    public void close() {
	dbHelper.close();
    }

    /**
     * 
     * 
     * 'RegistryServices' table Methods
     * 
     */
    public BusMemberRowDB queryBusMemberByID(String memberID) {
	BusMemberRowDB registryService = queryForRegistryServiceByMemberID(memberID);

	return registryService;
    }

    public String queryBusMemberIDByGroundingID(String groundingID) {
	BusMemberRowDB registryService = null;
	String busMemberID = null;

	Cursor cursor = database.query(getRegistryTableName(), AbstractBusMembersTable.allColumns,
		AbstractBusMembersTable.COLUMN_GROUNDING_ID + " = ?", new String[] { groundingID },
		null, null, null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    registryService = cursorToRegistryService(cursor);
	    busMemberID = registryService.getMemberID();
	}

	cursor.close();

	return busMemberID;
    }

    public BusMemberRowDB[] queryForAllBusMembers() {
	BusMemberRowDB[] registryServices = null;

	Cursor cursor = database.query(getRegistryTableName(), AbstractBusMembersTable.allColumns,
		null, null, null, null, null);

	// Extract the content
	registryServices = cursorToRegistryServices(cursor);

	// Make sure to close the cursor
	cursor.close();

	return registryServices;
    }

    public String[] queryForAllBusMemberIDs() {
	BusMemberRowDB[] busMembers = queryForAllBusMembers();

	String[] busMemberIds = new String[busMembers.length];

	for (int i = 0; i < busMembers.length; i++) {
	    busMemberIds[i] = busMembers[i].getMemberID();
	}

	return busMemberIds;
    }

    public int queryForBusMembersCount() {
	return queryForAllBusMembers().length;
    }

    public BusMemberRowDB[] queryForBusMembersByPackageName(String packageName) {
	BusMemberRowDB[] registryServices;

	Cursor cursor = database.query(getRegistryTableName(), AbstractBusMembersTable.allColumns,
		AbstractBusMembersTable.COLUMN_PACKAGE_NAME + " = ?", new String[] { packageName },
		null, null, null);

	// Extract the content
	registryServices = cursorToRegistryServices(cursor);

	// Make sure to close the cursor
	cursor.close();
	return registryServices;
    }

    public BusMemberRowDB removeBusMemberByMemberID(String memberID) {
	Log.d(TAG, "Is about to remove BusMember [" + memberID + "]");

	BusMemberRowDB registryServicesRowDB = queryBusMemberByID(memberID);

	int numOfDeletedRows = database.delete(getRegistryTableName(),
		AbstractBusMembersTable.COLUMN_MEMBER_ID + " = ?", new String[] { memberID });

	Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from ["
		+ getRegistryTableName() + "]");

	return registryServicesRowDB;
    }

    public void removeAllBusMembers() {
	Log.d(TAG, "Is about to remove all BusMembers");

	int numOfDeletedRows = database.delete(getRegistryTableName(), null, null);

	Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from ["
		+ getRegistryTableName() + "]");
    }

    public void addBusMember(String memberID, String packageName, String groundingID,
	    MemberType memberType, String androidUniqueName, byte[] groundingXmlAsByteArr) {
	long insertedRegistryService = insertRegistryService(memberID, packageName, groundingID,
		memberType, androidUniqueName, groundingXmlAsByteArr);
	Log.d(TAG, "RegistryService insert status [" + insertedRegistryService + "]");
    }

    private long insertRegistryService(String memberID, String packageName, String groundingID,
	    MemberType memberType, String androidUniqueName, byte[] groundingXmlAsByteArr) {
	Log.d(TAG, "Is about to insert RegistryService MemberID [" + memberID + "]; PackageName ["
		+ packageName + "]; GroundingID [" + groundingID + "]; AndroidUniqueName ["
		+ androidUniqueName + "]");

	// Populate the values to insert
	ContentValues valuesForRegistryServiceTable = new ContentValues();
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_MEMBER_ID, memberID);
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_PACKAGE_NAME, packageName);
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_GROUNDING_ID, groundingID);
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_MEMBER_TYPE,
		String.valueOf(memberType));
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_ANDROID_UNIQUE_NAME,
		androidUniqueName);
	valuesForRegistryServiceTable.put(AbstractBusMembersTable.COLUMN_GROUNDING_XML,
		groundingXmlAsByteArr);

	// Insert the new row to the RegistryServices table
	return database.insert(getRegistryTableName(), null, valuesForRegistryServiceTable);
    }

    private BusMemberRowDB queryForRegistryServiceByMemberID(String memberID) {
	BusMemberRowDB registryService = null;

	Cursor cursor = database.query(getRegistryTableName(), AbstractBusMembersTable.allColumns,
		AbstractBusMembersTable.COLUMN_MEMBER_ID + " = ?", new String[] { memberID }, null,
		null, null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    registryService = cursorToRegistryService(cursor);
	}

	cursor.close();

	return registryService;
    }

    /**
     * 
     * 
     * Common Methods
     * 
     * 
     */

    private BusMemberRowDB cursorToRegistryService(Cursor cursor) {
	BusMemberRowDB registryServicesRowDB = new BusMemberRowDB();
	registryServicesRowDB.setMemberID(cursor.getString(0));
	registryServicesRowDB.setPackageName(cursor.getString(1));
	registryServicesRowDB.setMemberType(cursor.getString(2));
	registryServicesRowDB.setGroundingID(cursor.getString(3));
	registryServicesRowDB.setAndroidUniqueName(cursor.getString(4));
	registryServicesRowDB.setGroundingXml(cursor.getBlob(5));

	return registryServicesRowDB;
    }

    private BusMemberRowDB[] cursorToRegistryServices(Cursor cursor) {
	List<BusMemberRowDB> busMembers = new ArrayList<BusMemberRowDB>();

	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    BusMemberRowDB registryServicesRowDB = cursorToRegistryService(cursor);
	    busMembers.add(registryServicesRowDB);
	    cursor.moveToNext();
	}
	return busMembers.toArray(new BusMemberRowDB[0]);
    }

    abstract protected String getRegistryTableName();
}
