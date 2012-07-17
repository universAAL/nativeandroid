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
package org.universAAL.middleware.android.localsodapop.persistence;

import org.universAAL.middleware.android.localsodapop.persistence.tables.BusesTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.ContactedPeersTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.CoordinatorTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.LocalBusesTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.LocalPeerInfoTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.PeersTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class SodaPopPeersSQLiteHelper extends SQLiteOpenHelper {

    // DB name and version
    private static final String DATABASE_NAME = "SODAPOPPEERS.DB";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statements
    private static final String[] sqlCommandsOnCreation = new String[] {
	    LocalPeerInfoTable.TABLE_CREATE, PeersTable.TABLE_CREATE, BusesTable.TABLE_CREATE,
	    LocalBusesTable.TABLE_CREATE, ContactedPeersTable.TABLE_CREATE,
	    CoordinatorTable.TABLE_CREATE };

    private static String TAG = SodaPopPeersSQLiteHelper.class.getName();

    // Join query
    public static final String JOIN_QUERY = "SELECT peers." + PeersTable.COLUMN_ID_PK + ",peers."
	    + PeersTable.COLUMN_PEER_ID + ",peers." + PeersTable.COLUMN_PEER_PROTOCOL + ",peers."
	    + PeersTable.COLUMN_DISCOVERED + ",peers." + PeersTable.COLUMN_BUSSES_RECEIVED
	    + ",buses." + BusesTable.COLUMN_BUS_NAME + " FROM " + PeersTable.TABLE_NAME
	    + " peers LEFT OUTER JOIN " + BusesTable.TABLE_NAME + " buses ON peers."
	    + PeersTable.COLUMN_ID_PK + "=buses." + BusesTable.COLUMN_PEERS_ID_FK;

    public SodaPopPeersSQLiteHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
	for (String cmd : sqlCommandsOnCreation) {
	    database.execSQL(cmd);
	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
		+ ", which will destroy all old data");
	db.execSQL("DROP TABLE IF EXISTS " + PeersTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + BusesTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + ContactedPeersTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + CoordinatorTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + LocalBusesTable.TABLE_NAME);
	db.execSQL("DROP TABLE IF EXISTS " + LocalPeerInfoTable.TABLE_NAME);

	onCreate(db);
    }
}
