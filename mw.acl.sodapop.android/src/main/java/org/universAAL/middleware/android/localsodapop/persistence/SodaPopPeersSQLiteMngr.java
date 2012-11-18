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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.android.localsodapop.persistence.tables.BusesTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.ContactedPeersTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.CoordinatorTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.LocalBusesTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.LocalPeerInfoTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.PeersTable;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.BusRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.ContactedPeerRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.LocalBusRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.LocalPeerInfoRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.PeerRowDB;

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
 */
public class SodaPopPeersSQLiteMngr {

    // Database fields
    private SQLiteDatabase database;
    private SodaPopPeersSQLiteHelper dbHelper;

    private static final String[] tablesToClearInReset = new String[] {
	    LocalPeerInfoTable.TABLE_NAME, LocalBusesTable.TABLE_NAME, PeersTable.TABLE_NAME,
	    BusesTable.TABLE_NAME, ContactedPeersTable.TABLE_NAME, CoordinatorTable.TABLE_NAME };

    private static final String TAG = SodaPopPeersSQLiteMngr.class.getName();

    public SodaPopPeersSQLiteMngr(Context context) {
	dbHelper = new SodaPopPeersSQLiteHelper(context);
    }

    public void open() throws SQLException {
	database = dbHelper.getWritableDatabase();
    }

    public void close() {
	dbHelper.close();
    }

    /**
     * 
     * 
     * 'LocalPeerInfo' table Methods
     * 
     * 
     */
    public LocalPeerInfoRowDB setLocalPeerInfo(String localPeerID) {
	LocalPeerInfoRowDB localPeerInfo = queryLocalPeerInfo();
	if (null == localPeerInfo) {
	    Log.d(TAG, "LocalPeer is about to set with ID [" + localPeerID + "]");

	    // The local peer info wasn't set, therefore create it
	    localPeerInfo = insertLocalPeerInfo(localPeerID);
	}

	return localPeerInfo;
    }

    private LocalPeerInfoRowDB insertLocalPeerInfo(String localPeerID) {
	ContentValues valuesForLocalPeerInfoTable = new ContentValues();
	valuesForLocalPeerInfoTable.put(LocalPeerInfoTable.COLUMN_LOCAL_PEER_ID, localPeerID);

	// Insert the new row to the LocalPeerInfo table
	database.insert(LocalPeerInfoTable.TABLE_NAME, null, valuesForLocalPeerInfoTable);
	return new LocalPeerInfoRowDB(localPeerID);
    }

    public LocalPeerInfoRowDB queryLocalPeerInfo() {

	LocalPeerInfoRowDB localPeerInfo = null;

	Cursor cursor = database.query(LocalPeerInfoTable.TABLE_NAME,
		new String[] { LocalPeerInfoTable.COLUMN_LOCAL_PEER_ID }, null, null, null, null,
		null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    localPeerInfo = cursorToLocalPeerInfo(cursor);
	}
	cursor.close();

	return localPeerInfo;
    }

    /**
     * 
     * 
     * 'Peers' table Methods
     * 
     * 
     */

    public PeerRowDB addPeer(String peerID, String peerProtocol, boolean discovered,
	    boolean receivedBusses) {
	// First query for peer
	PeerRowDB foundRow = queryPeerByPeerID(peerID);

	// If not exist - then add it
	if (null == foundRow) {
	    long insertId = insertPeer(peerID, peerProtocol, discovered, receivedBusses);

	    // Query and extract the just newly created peer
	    foundRow = queryPeerByID(insertId);

	    Log.d(TAG, "New peer was inserted successfully [" + foundRow.toString() + "]");
	} else {
	    Log.d(TAG, "Peer with ID [" + peerID
		    + "] already exists therefore no need to insert a new one");
	    if (discovered && !foundRow.isDiscovered()) {
		updatePeerAsDiscovered(peerID);
	    }
	}
	return foundRow;
    }

    public void removePeerByID(String peerID) {
	Log.d(TAG, "Is about to remove peer [" + peerID + "]");

	int numOfDeletedRows = database.delete(PeersTable.TABLE_NAME, PeersTable.COLUMN_PEER_ID
		+ "= ?", new String[] { peerID });

	Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from [" + PeersTable.TABLE_NAME
		+ "]");
    }

    private long insertPeer(String peerID, String peerProtocol, boolean discovered,
	    boolean receivedBusses) {
	Log.d(TAG, "Is about to insert peer: " + "PeerID [" + peerID + "] Protocol ["
		+ peerProtocol + "] Discovered [" + discovered + "]" + "] ReceivedBusses ["
		+ receivedBusses + "]");

	// Populate the values to insert
	ContentValues valuesFoPeersTable = new ContentValues();
	valuesFoPeersTable.put(PeersTable.COLUMN_PEER_ID, peerID);
	valuesFoPeersTable.put(PeersTable.COLUMN_PEER_PROTOCOL, peerProtocol);
	valuesFoPeersTable.put(PeersTable.COLUMN_DISCOVERED, discovered ? "1" : "0");
	valuesFoPeersTable.put(PeersTable.COLUMN_BUSSES_RECEIVED, receivedBusses ? "1" : "0");

	// Insert the new row to the Peers table
	return database.insert(PeersTable.TABLE_NAME, null, valuesFoPeersTable);
    }

    private void updatePeerAsDiscovered(String peerID) {
	ContentValues valuesFoPeersTable = new ContentValues();
	valuesFoPeersTable.put(PeersTable.COLUMN_DISCOVERED, "1");

	int updatedRowsCount = database.update(PeersTable.TABLE_NAME, valuesFoPeersTable,
		PeersTable.COLUMN_PEER_ID + "= ?", new String[] { peerID });
	if (1 != updatedRowsCount) {
	    Log.e(TAG, "Only one row should be found for the peer ID [" + peerID
		    + "]; were found [" + updatedRowsCount + "] rows");
	} else {
	    Log.d(TAG, "PeerID [" + peerID + "] has been updated with DISCOVERED = 1!");
	}
    }

    private void updatePeerAsReceivedBusses(String peerID) {
	ContentValues valuesFoPeersTable = new ContentValues();
	valuesFoPeersTable.put(PeersTable.COLUMN_BUSSES_RECEIVED, "1");

	int updatedRowsCount = database.update(PeersTable.TABLE_NAME, valuesFoPeersTable,
		PeersTable.COLUMN_PEER_ID + "= ?", new String[] { peerID });
	if (1 != updatedRowsCount) {
	    Log.e(TAG, "Only one row should be found for the peer ID [" + peerID
		    + "]; were found [" + updatedRowsCount + "] rows");
	} else {
	    Log.d(TAG, "PeerID [" + peerID + "] has been updated with RECEIVED_BUSSES = 1!");
	}
    }

    /**
     * Query for peer from the DB with the given Peer ID
     * 
     * @param peerID
     * @param database
     * @return the row from the DB the represents the peer
     */
    public PeerRowDB queryPeerByPeerID(String peerID) {
	Map<String, Object> map = new HashMap<String, Object>();
	map.put(PeersTable.TABLE_NAME + "." + PeersTable.COLUMN_PEER_ID, "'" + peerID + "'");
	return queryForSinglePeer(map);
    }

    /**
     * Query for peer from the DB with the given ID (the internal DB ID)
     * 
     * @param id
     * @param database
     * @return the row from the DB the represents the peer
     */
    public PeerRowDB queryPeerByID(long id) {
	Map<String, Object> map = new HashMap<String, Object>();
	map.put(PeersTable.TABLE_NAME + "." + PeersTable.COLUMN_ID_PK, id);
	return queryForSinglePeer(map);
    }

    /**
     * Query for all peers from the DB
     * 
     * @param database
     * @return list of rows from the DB that represents all peers
     */
    public Collection<PeerRowDB> queryForAllPeers() {
	String query = SodaPopPeersSQLiteHelper.JOIN_QUERY;
	Cursor cursor = database.rawQuery(query, null);
	Collection<PeerRowDB> peers = getPeersFromCursor(cursor);

	// Close the cursor
	cursor.close();
	return peers;
    }

    public Collection<PeerRowDB> queryForAllPeersByBusName(String busName) {
	Map<String, Object> map = new HashMap<String, Object>();
	map.put(BusesTable.TABLE_NAME + "." + BusesTable.COLUMN_BUS_NAME, "'" + busName + "'");
	String query = SodaPopPeersSQLiteHelper.JOIN_QUERY + " WHERE "
		+ populateSelectionForQuery(map);

	Log.d(TAG, "Is about to query for peers with bus name [" + busName + "] select query ["
		+ query + "]");

	Cursor cursor = database.rawQuery(query, null);
	Collection<PeerRowDB> peers = getPeersFromCursor(cursor);

	// Close the cursor
	cursor.close();
	return peers;
    }

    private Collection<PeerRowDB> getPeersFromCursor(Cursor cursor) {
	Map<Long, PeerRowDB> peersMap = new HashMap<Long, PeerRowDB>();

	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    PeerRowDB peerRowDB = null;
	    long id = cursor.getLong(0);
	    if (peersMap.containsKey(id)) {
		peerRowDB = peersMap.get(id);
	    } else {
		peerRowDB = cursorToPeer(cursor);
		peersMap.put(id, peerRowDB);
	    }
	    // Add bus name
	    String busName = cursor.getString(5);
	    if (!StringUtils.isEmpty(busName)) {
		BusRowDB bus = new BusRowDB();
		bus.setBusName(busName);
		peerRowDB.addBus(bus);
	    }
	    cursor.moveToNext();
	}

	return peersMap.values();
    }

    /**
     * Query for a single peer from the DB
     * 
     * @param keyValueMap
     *            key-values map that will be used for the selection
     * @param database
     * @return single row from the DB that represents the peer
     */
    private PeerRowDB queryForSinglePeer(Map<String, Object> keyValueMap) {
	PeerRowDB newPeer = null;
	String query = SodaPopPeersSQLiteHelper.JOIN_QUERY + " WHERE "
		+ populateSelectionForQuery(keyValueMap);
	Log.d(TAG, "Is about to perform a query [" + query + "]");
	Cursor cursor = database.rawQuery(query, null);

	Collection<PeerRowDB> peers = getPeersFromCursor(cursor);

	cursor.close();
	if (!peers.isEmpty()) {
	    newPeer = peers.iterator().next();
	}
	return newPeer;
    }

    /**
     * 
     * 
     * 'Buses' table Methods
     * 
     * 
     */
    public PeerRowDB addBusesToPeer(String peerID, String peerProtocol, String[] bussesNames) {
	// Check if the peer exist
	PeerRowDB peer = queryPeerByPeerID(peerID);

	// If not exist add a new one with discovered = false
	if (null == peer) {
	    peer = addPeer(peerID, peerProtocol, false, true);
	} else if (!peer.isReceivedBusses()) {
	    updatePeerAsReceivedBusses(peerID);
	}

	// Extract the current buses names
	List<String> currentBusesNames = peer.getBusesNames();
	// Add the NEW buses
	for (String curBusName : bussesNames) {
	    if (!StringUtils.isEmpty(curBusName) && !currentBusesNames.contains(curBusName)) {
		insertBus(peer, curBusName);
	    }
	}

	// Return the refresh peer (to include also the added buses)
	return queryPeerByPeerID(peerID);
    }

    public void removeBus(String peerID, String busName) {
	Log.d(TAG, "Is about to remove bus [" + busName + "] from peer [" + peerID + "]");

	// Query for the peer
	PeerRowDB peer = queryPeerByPeerID(peerID);

	if (null != peer) {
	    int numOfDeletedRows = database.delete(BusesTable.TABLE_NAME,
		    BusesTable.COLUMN_PEERS_ID_FK + " = ? AND " + BusesTable.COLUMN_BUS_NAME
			    + " = ?", new String[] { String.valueOf(peer.getId()), busName });

	    Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from ["
		    + BusesTable.TABLE_NAME + "]");
	}
    }

    private void insertBus(PeerRowDB peer, String busName) {
	Log.d(TAG,
		"Is about to insert bus: BusName [" + busName + "] related to Peer ["
			+ peer.toString() + "]");
	// Populate the values to insert
	ContentValues valuesForBusesTable = new ContentValues();
	valuesForBusesTable.put(BusesTable.COLUMN_BUS_NAME, busName);
	valuesForBusesTable.put(BusesTable.COLUMN_PEERS_ID_FK, peer.getId());

	// Insert the new row to the Peers table
	long insertedID = database.insert(BusesTable.TABLE_NAME, null, valuesForBusesTable);

	Log.d(TAG, "New bus: BusName [" + busName + "] related to Peer [" + peer.toString()
		+ "] was inserted successfully [" + insertedID + "]");
    }

    /**
     * 
     * 
     * 'LocalBuses' table Methods
     * 
     * 
     */
    public LocalBusRowDB addLocalBus(String busName, String packageName, String className) {
	long insertId = insertLocalBus(busName, packageName, className);

	// Query and extract the just newly created local bus
	LocalBusRowDB newLocalBusRow = queryLocalBusTableByID(insertId);

	Log.d(TAG, "New local bus was inserted successfully [" + newLocalBusRow.toString() + "]");

	return newLocalBusRow;
    }

    private long insertLocalBus(String busName, String packageName, String className) {
	Log.d(TAG, "Is about to insert local bus BusName [" + busName + "] packageName ["
		+ packageName + "] className [" + className + "]");

	// Populate the values to insert
	ContentValues valuesForLocalBusTable = new ContentValues();
	valuesForLocalBusTable.put(LocalBusesTable.COLUMN_BUS_NAME, busName);
	valuesForLocalBusTable.put(LocalBusesTable.COLUMN_BUS_PACKAGE_NAME, packageName);
	valuesForLocalBusTable.put(LocalBusesTable.COLUMN_BUS_CLASS_NAME, className);

	// Insert the new row to the Peers table
	return database.insert(LocalBusesTable.TABLE_NAME, null, valuesForLocalBusTable);
    }

    private LocalBusRowDB queryLocalBusTableByID(long insertId) {
	LocalBusRowDB localBus = null;

	Cursor cursor = database.query(LocalBusesTable.TABLE_NAME, LocalBusesTable.allColumns,
		LocalBusesTable.COLUMN_ID_PK + " = ?", new String[] { String.valueOf(insertId) },
		null, null, null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    localBus = cursorToLocalBus(cursor);
	}

	cursor.close();

	return localBus;
    }

    public List<LocalBusRowDB> queryForAllLocalBuses() {
	List<LocalBusRowDB> localBuses = new ArrayList<LocalBusRowDB>();

	Cursor cursor = database.query(LocalBusesTable.TABLE_NAME, LocalBusesTable.allColumns,
		null, null, null, null, null);

	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    LocalBusRowDB localBusRowDB = cursorToLocalBus(cursor);
	    localBuses.add(localBusRowDB);
	    cursor.moveToNext();
	}
	// Make sure to close the cursor
	cursor.close();
	return localBuses;
    }

    public LocalBusRowDB queryForLocalBusByName(String busName) {
	LocalBusRowDB localBus = null;

	Cursor cursor = database.query(LocalBusesTable.TABLE_NAME, LocalBusesTable.allColumns,
		LocalBusesTable.COLUMN_BUS_NAME + " = ?", new String[] { busName }, null, null,
		null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    localBus = cursorToLocalBus(cursor);
	}

	cursor.close();

	return localBus;
    }

    public void removeLocalBus(String busName) {
	Log.d(TAG, "Is about to remove local bus [" + busName + "]");

	int numOfDeletedRows = database.delete(LocalBusesTable.TABLE_NAME,
		LocalBusesTable.COLUMN_BUS_NAME + " = ?", new String[] { busName });

	Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from [" + BusesTable.TABLE_NAME
		+ "]");
    }

    /**
     * 
     * 
     * 'Contacted Peers' table Methods
     * 
     * 
     */
    public void addContactedPeer(String contactedPeerID) {
	// First verify that this peer doesn't exist in the DB
	ContactedPeerRowDB contacedPeer = queryForContactedPeerByPeerID(contactedPeerID);

	if (null == contacedPeer) {
	    long id = insertContactedPeer(contactedPeerID);
	    Log.d(TAG, "New contacted peer was inserted successfully [" + id + "]");
	} else {
	    Log.d(TAG, "Contacted peer [" + contactedPeerID + "] already exist");
	}
    }

    private long insertContactedPeer(String contactedPeerID) {
	Log.d(TAG, "Is about to insert contacted peer ContactedPeerID [" + contactedPeerID + "]");

	// Populate the values to insert
	ContentValues valuesForContactedPeersTable = new ContentValues();
	valuesForContactedPeersTable.put(ContactedPeersTable.COLUMN_CONTACTED_PEER_ID,
		contactedPeerID);

	// Insert the new row to the Peers table
	return database.insert(ContactedPeersTable.TABLE_NAME, null, valuesForContactedPeersTable);
    }

    public ContactedPeerRowDB queryForContactedPeerByPeerID(String contactedPeerID) {
	ContactedPeerRowDB foundContactedPeerRowDB = null;

	Log.d(TAG, "Is about to query for contacted peer ID [" + contactedPeerID + "]");

	Cursor cursor = database.query(ContactedPeersTable.TABLE_NAME,
		ContactedPeersTable.allColumns, ContactedPeersTable.COLUMN_CONTACTED_PEER_ID
			+ " = ?", new String[] { contactedPeerID }, null, null, null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    foundContactedPeerRowDB = cursorToContactedPeer(cursor);
	}

	cursor.close();

	return foundContactedPeerRowDB;
    }

    public List<ContactedPeerRowDB> queryForContactedPeers() {
	List<ContactedPeerRowDB> contactedPeers = new ArrayList<ContactedPeerRowDB>();

	Cursor cursor = database.query(ContactedPeersTable.TABLE_NAME,
		ContactedPeersTable.allColumns, null, null, null, null, null);

	cursor.moveToFirst();
	while (!cursor.isAfterLast()) {
	    ContactedPeerRowDB contactedPeerRowDB = cursorToContactedPeer(cursor);
	    contactedPeers.add(contactedPeerRowDB);
	    cursor.moveToNext();
	}
	// Make sure to close the cursor
	cursor.close();
	return contactedPeers;
    }

    public void removeContactedPeer(String peerID) {
	Log.d(TAG, "Is about to remove contacted peer [" + peerID + "]");

	int numOfDeletedRows = database.delete(ContactedPeersTable.TABLE_NAME,
		ContactedPeersTable.COLUMN_CONTACTED_PEER_ID + " = ?", new String[] { peerID });

	Log.d(TAG, "[" + numOfDeletedRows + "] has(ve) been deleted from ["
		+ ContactedPeersTable.TABLE_NAME + "]");
    }

    /**
     * 
     * 
     * 'Coordinator' table Methods - this table includes only one single row!!!
     * 
     * 
     */
    public void setCoordinator(String coordinatorPeerID) {
	insertUpdateCoordinator(coordinatorPeerID);
    }

    public String queryForCoordinatorPeer() {
	String coordinatorPeerID = null;

	Log.d(TAG, "Is about to query for coordinator");

	Cursor cursor = database.query(CoordinatorTable.TABLE_NAME,
		new String[] { CoordinatorTable.COLUMN_COORDINATOR_PEER_ID }, null, null, null,
		null, null);

	cursor.moveToFirst();

	if (!cursor.isAfterLast()) {
	    coordinatorPeerID = cursor.getString(0);

	    Log.d(TAG, "Found coordinator [" + coordinatorPeerID + "]");
	}
	cursor.close();

	return coordinatorPeerID;
    }

    private void insertUpdateCoordinator(String coordinatorPeerID) {
	// Empty the table
	database.delete(CoordinatorTable.TABLE_NAME, null, null);

	// Insert a new row
	ContentValues valuesForCoordinatorTable = new ContentValues();
	valuesForCoordinatorTable.put(CoordinatorTable.COLUMN_COORDINATOR_PEER_ID,
		coordinatorPeerID);

	// Insert the new row to the Coordinator table
	database.insert(CoordinatorTable.TABLE_NAME, null, valuesForCoordinatorTable);
    }

    /**
     * 
     * 
     * 'Common methods' Methods
     * 
     * 
     */
    public void resetDB() {
	open();

	// Delete tables
	for (String tableToDelete : tablesToClearInReset) {
	    database.delete(tableToDelete, null, null);
	}

	close();
    }

    /**
     * @param keyValueMap
     * @return a string that represents the 'selection' part in the query
     *         statement
     */
    private static String populateSelectionForQuery(Map<String, Object> keyValueMap) {
	StringBuilder sb = new StringBuilder();
	for (String key : keyValueMap.keySet()) {
	    sb.append(key + " = " + keyValueMap.get(key).toString() + " AND ");
	}
	return sb.substring(0, sb.length() - " AND ".length());
    }

    private static PeerRowDB cursorToPeer(Cursor cursor) {
	PeerRowDB peer = new PeerRowDB();
	peer.setId(cursor.getLong(0));
	peer.setPeerID(cursor.getString(1));
	peer.setProtocol(cursor.getString(2));
	peer.setDiscovered(cursor.getString(3));
	peer.setReceivedBusses(cursor.getString(4));

	return peer;
    }

    private static LocalPeerInfoRowDB cursorToLocalPeerInfo(Cursor cursor) {
	LocalPeerInfoRowDB localPeerInfo = new LocalPeerInfoRowDB();
	localPeerInfo.setLocalPeerID(cursor.getString(0));

	return localPeerInfo;
    }

    private static LocalBusRowDB cursorToLocalBus(Cursor cursor) {
	LocalBusRowDB localBusRow = new LocalBusRowDB();
	localBusRow.setBusName(cursor.getString(0));
	localBusRow.setBusPackageName(cursor.getString(1));
	localBusRow.setBusClassName(cursor.getString(2));

	return localBusRow;
    }

    private ContactedPeerRowDB cursorToContactedPeer(Cursor cursor) {
	ContactedPeerRowDB contactedPeerRowDB = new ContactedPeerRowDB();
	contactedPeerRowDB.setContactedPeerID(cursor.getString(0));

	return contactedPeerRowDB;
    }
}
