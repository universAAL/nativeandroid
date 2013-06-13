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
package org.universAAL.middleware.android.buses.servicebus.persistence;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteHelper;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalServiceSearchResultsTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalServicesIndexTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalWaitingCallerTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.RegistryServicesTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.ServiceProfileTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.WaitingCallTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 */
public class ServiceBusSQLiteHelper extends AbstractCommonBusSQLiteHelper { // SQLiteOpenHelper
									    // {

    // DB name and version
    private static final String DATABASE_NAME = "SERVICE_BUS.DB";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statements
    private static final String[] sqlCommandsOnCreation = new String[] {
	    new RegistryServicesTable().getTableCreateCommand(),
	    LocalServicesIndexTable.TABLE_CREATE, LocalServiceSearchResultsTable.TABLE_CREATE,
	    ServiceProfileTable.TABLE_CREATE, LocalWaitingCallerTable.TABLE_CREATE,
	    WaitingCallTable.TABLE_CREATE };

    // Join query
    public static final String SERVICE_SEARCH_RESULTS_SERVICE_PROFILES_JOIN_QUERY = "SELECT localServiceSearchResults."
	    + LocalServiceSearchResultsTable.COLUMN_SERVICE_REALIZATION_ID
	    + ",serviceProfiles."
	    + ServiceProfileTable.COLUMN_CONTENT
	    + " FROM "
	    + LocalServiceSearchResultsTable.TABLE_NAME
	    + " localServiceSearchResults LEFT OUTER JOIN "
	    + ServiceProfileTable.TABLE_NAME
	    + " serviceProfiles ON localServiceSearchResults."
	    + LocalServiceSearchResultsTable.COLUMN_ID_PK
	    + "=serviceProfiles."
	    + ServiceProfileTable.COLUMN_SERVICES_ID_FK
	    + " WHERE localServiceSearchResults."
	    + LocalServiceSearchResultsTable.COLUMN_SERVICE_REALIZATION_ID + "=";

    private static String tag = ServiceBusSQLiteHelper.class.getName();

    public ServiceBusSQLiteHelper(Context context) {
	super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
	for (String cmd : sqlCommandsOnCreation) {
	    database.execSQL(cmd);
	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	Log.w(tag, "Upgrading database from version " + oldVersion + " to " + newVersion
		+ ", which will destroy all old data");
	// TODO: Create a list that will be defined in the top and will contain
	// a list of tables to drop
	db.execSQL("DROP TABLE IF EXISTS " + RegistryServicesTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + LocalServiceSearchResultsTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + LocalServicesIndexTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + ServiceProfileTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + WaitingCallTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + LocalWaitingCallerTable.TABLE_NAME);

	onCreate(db);
    }
}
