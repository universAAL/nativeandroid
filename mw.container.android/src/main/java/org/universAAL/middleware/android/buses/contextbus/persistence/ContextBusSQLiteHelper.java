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
package org.universAAL.middleware.android.buses.contextbus.persistence;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteHelper;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.AllProvisionsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.CalledPeersTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ContextEventPatternsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ContextFiltererTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.FiltererContainerTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ProvisionsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.RegistryContextsTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 */
public class ContextBusSQLiteHelper extends AbstractCommonBusSQLiteHelper {// SQLiteOpenHelper
	// {

	// DB name and version
	private static final String DATABASE_NAME = "CONTEXT_BUS.DB";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statements
	private static final String[] sqlCommandsOnCreation = new String[] {
			new RegistryContextsTable().getTableCreateCommand(),
			AllProvisionsTable.TABLE_CREATE, CalledPeersTable.TABLE_CREATE,
			ContextEventPatternsTable.TABLE_CREATE,
			ContextFiltererTable.TABLE_CREATE,
			FiltererContainerTable.TABLE_CREATE, ProvisionsTable.TABLE_CREATE };

	private static String tag = ContextBusSQLiteHelper.class.getName();

	public ContextBusSQLiteHelper(Context context) {
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
		Log.w(tag, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		// TODO: Create a list that will be defined in the top and will contain
		// a list of tables to drop
		// db.execSQL("DROP TABLE IF EXISTS " +
		// LocalServiceSearchResultsTable.TABLE_NAME);

		onCreate(db);
	}
}
