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

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteMngr;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.AllProvisionsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.CalledPeersTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ContextEventPatternsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ContextFiltererTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.FiltererContainerTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.ProvisionsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.RegistryContextsTable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.AllProvisionsRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.CalledPeersRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.ContextEventPatternRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.ContextFiltererRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.FiltererContainerRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.ProvisionRowDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 */
public class ContextBusSQLiteMngr extends AbstractCommonBusSQLiteMngr {

	private static final String TAG = ContextBusSQLiteMngr.class.getName();

	private static final String[] tablesToClearInReset = new String[] {
			RegistryContextsTable.TABLE_NAME, AllProvisionsTable.TABLE_NAME,
			CalledPeersTable.TABLE_NAME, ContextEventPatternsTable.TABLE_NAME,
			ContextFiltererTable.TABLE_NAME, FiltererContainerTable.TABLE_NAME,
			ProvisionsTable.TABLE_NAME };

	public ContextBusSQLiteMngr(Context context) {
		super(context, new ContextBusSQLiteHelper(context));
	}

	/**
	 * 
	 * 
	 * 'Provisions' table Methods
	 * 
	 * 
	 */
	public void addProvision(String provisionID) {
		insertProvision(provisionID);
	}

	public boolean existProvision(String provisionID) {
		return null != queryForProvision(provisionID);
	}

	private long insertProvision(String provisionID) {
		Log.d(TAG, "Is about to insert Provision provisionID [" + provisionID
				+ "]");
		long insertedID = 0;
		// Populate the values to insert
		ContentValues valuesForProvisionTable = new ContentValues();
		valuesForProvisionTable.put(ProvisionsTable.COLUMN_PROVISION_ID,
				provisionID);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the Provisions table
				insertedID = database.insert(ProvisionsTable.TABLE_NAME, null,
						valuesForProvisionTable);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ ProvisionsTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	private ProvisionRowDB queryForProvision(String provisionID) {
		Log.d(TAG, "Is about to query for Provision provisionID ["
				+ provisionID + "]");

		ProvisionRowDB provisionRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(ProvisionsTable.TABLE_NAME,
						ProvisionsTable.allColumns,
						ProvisionsTable.COLUMN_PROVISION_ID + " = ?",
						new String[] { provisionID }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					provisionRowDB = cursorToProvision(cursor);

					Log.d(TAG, "Found provision [" + provisionID + "]");
				} else {
					Log.d(TAG, "No provision was found provision with ID ["
							+ provisionID + "]");
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return provisionRowDB;
	}

	/**
	 * 
	 * 
	 * 'CalledPeers' table Methods
	 * 
	 * 
	 */
	public void addCalledPeers(String messageID, int numOfCalledPeers) {
		insertCalledPeers(messageID, numOfCalledPeers);
	}

	public CalledPeersRowDB queryForCalledPeers(String messageID) {
		Log.d(TAG, "Is about to query for CalledPeers MessageID [" + messageID
				+ "]");

		CalledPeersRowDB calledPeersRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(CalledPeersTable.TABLE_NAME,
						CalledPeersTable.allColumns,
						CalledPeersTable.COLUMN_MESSAGE_ID + " = ?",
						new String[] { messageID }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					calledPeersRowDB = cursorToCalledPeers(cursor);
					Log.d(TAG, "Found CalledPeers [" + messageID + "]");
				} else {
					Log.d(TAG, "No CalledPeers was found for messageID ["
							+ messageID + "]");
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return calledPeersRowDB;
	}

	public void updateCalledPeersWithNumOfCalledPeers(String messageID,
			int numOfCalledPeers) {
		ContentValues valuesForCalledPeers = new ContentValues();
		valuesForCalledPeers.put(CalledPeersTable.COLUMN_NUM_OF_CALLED_PEERS,
				numOfCalledPeers);
		synchronized (sync) {
			try {
				open();
				int updatedRowsCount = database.update(
						CalledPeersTable.TABLE_NAME, valuesForCalledPeers,
						CalledPeersTable.COLUMN_MESSAGE_ID + "= ?",
						new String[] { messageID });
				if (1 != updatedRowsCount) {
					Log.e(TAG,
							"Only one row should be found for the messageID ["
									+ messageID + "]; were found ["
									+ updatedRowsCount + "] rows");
				} else {
					Log.d(TAG, "CalledPeers with MessageID [" + messageID
							+ "] has been updated with numOfCalledPeers ["
							+ numOfCalledPeers + "]");
				}
			} finally {
				close();
			}
		}
	}

	public void removeCalledPeers(String messageID) {
		Log.d(TAG, "Is about to remove CalledPeers with messageID ["
				+ messageID + "]");
		synchronized (sync) {
			try {
				open();
				int numOfDeletedRows = database.delete(
						CalledPeersTable.TABLE_NAME,
						CalledPeersTable.COLUMN_MESSAGE_ID + "= ?",
						new String[] { messageID });

				Log.d(TAG, "[" + numOfDeletedRows
						+ "] has(ve) been deleted from ["
						+ CalledPeersTable.TABLE_NAME + "]");
			} finally {
				close();
			}
		}
	}

	private long insertCalledPeers(String messageID, int numOfCalledPeers) {
		Log.d(TAG, "Is about to insert CalledPeers MessageID [" + messageID
				+ "]; NumOfCalledPeers [" + numOfCalledPeers + "]");

		// Populate the values to insert
		ContentValues valuesForCalledPeers = new ContentValues();
		valuesForCalledPeers.put(CalledPeersTable.COLUMN_MESSAGE_ID, messageID);
		valuesForCalledPeers.put(CalledPeersTable.COLUMN_NUM_OF_CALLED_PEERS,
				numOfCalledPeers);
		long insertedID = 0;
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the CalledPeers table
				insertedID = database.insert(CalledPeersTable.TABLE_NAME, null,
						valuesForCalledPeers);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ CalledPeersTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	/**
	 * 
	 * 
	 * 'ContextEventPatterns' table Methods
	 * 
	 * 
	 */
	public void addContextEventPatterns(String messageID,
			String[] serializedContextEventPatterns) {
		// Query for the CalledPeers to get the ID_PK
		CalledPeersRowDB calledPeersRow = queryForCalledPeers(messageID);

		// Add the context event patterns
		for (String contextEventPattern : serializedContextEventPatterns) {
			insertContextEventPattern(calledPeersRow.getCalledPeerID(),
					contextEventPattern);
		}
	}

	public ContextEventPatternRowDB[] queryForContextEventPatterns(
			String messageID) {
		Log.d(TAG, "Is about to query for ContextEventPatterns for MessageID ["
				+ messageID + "]");

		// Query for the CalledPeers to get the ID_PK
		CalledPeersRowDB calledPeersRow = queryForCalledPeers(messageID);

		return queryForContextEventPatternsByCalledPeersID(calledPeersRow
				.getCalledPeerID());
	}

	private long insertContextEventPattern(long calledPeerID,
			String contextEventPattern) {
		Log.d(TAG, "Is about to insert ContextEvent for CalledPeersID ["
				+ calledPeerID + "]; ContextEvent [" + contextEventPattern
				+ "]");

		// Populate the values to insert
		ContentValues valuesForContextEventPattern = new ContentValues();
		valuesForContextEventPattern.put(
				ContextEventPatternsTable.COLUMN_CONTEXT_EVENT_CONTENT,
				contextEventPattern);
		valuesForContextEventPattern.put(
				ContextEventPatternsTable.COLUMN_CALLED_PEERS_FK, calledPeerID);
		long insertedID;
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the ContextEventPatterns table
				insertedID = database.insert(
						ContextEventPatternsTable.TABLE_NAME, null,
						valuesForContextEventPattern);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ ContextEventPatternsTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	private ContextEventPatternRowDB[] queryForContextEventPatternsByCalledPeersID(
			long calledPeerID) {
		Log.d(TAG, "Is about to query for CalledPeers CalledPeerID ["
				+ calledPeerID + "]");

		List<ContextEventPatternRowDB> contextEventPatternRows = new ArrayList<ContextEventPatternRowDB>();
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						ContextEventPatternsTable.TABLE_NAME,
						ContextEventPatternsTable.allColumns,
						ContextEventPatternsTable.COLUMN_CALLED_PEERS_FK
								+ " = ?",
						new String[] { String.valueOf(calledPeerID) }, null,
						null, null);

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					ContextEventPatternRowDB contextEventPatternRowDB = cursorToContextEventPattern(cursor);
					contextEventPatternRows.add(contextEventPatternRowDB);
					cursor.moveToNext();
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return contextEventPatternRows.toArray(new ContextEventPatternRowDB[0]);
	}

	/**
	 * 
	 * 
	 * 'FiltererContainer' table Methods
	 * 
	 * 
	 */
	public FiltererContainerRowDB addFiltererContainer(String containerKey,
			String containerType) {
		FiltererContainerRowDB row = null;

		long insertedFiltererContainer = insertFiltererContainer(containerKey,
				containerType);

		if (-1 != insertedFiltererContainer) {
			row = queryForFiltererContainer(containerKey, containerType);
		}

		return row;
	}

	public FiltererContainerRowDB queryForFiltererContainer(
			String containerKey, String containerType) {
		Log.d(TAG, "Is about to query for FiltererContainer ContainerKey ["
				+ containerKey + "]; ContainerType [" + containerType + "]");

		FiltererContainerRowDB filtererContainerRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						FiltererContainerTable.TABLE_NAME,
						FiltererContainerTable.allColumns,
						FiltererContainerTable.COLUMN_CONTAINER_KEY
								+ " = ? AND "
								+ FiltererContainerTable.COLUMN_CONTAINER_TYPE
								+ " = ?", new String[] { containerKey,
								containerType }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					filtererContainerRowDB = cursorToFiltererContainer(cursor);
					Log.d(TAG, "Found FiltererContainer [" + containerKey + ";"
							+ containerType + "]");
				} else {
					Log.d(TAG, "No FiltererContainer was found ["
							+ containerKey + ";" + containerType + "]");
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return filtererContainerRowDB;
	}

	public FiltererContainerRowDB[] queryForFiltererContainersByContainerType(
			String containerType) {
		Log.d(TAG, "Is about to query for FiltererContainers ContainerType ["
				+ containerType + "]");

		List<FiltererContainerRowDB> filtererContainerRows = new ArrayList<FiltererContainerRowDB>();
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						FiltererContainerTable.TABLE_NAME,
						FiltererContainerTable.allColumns,
						FiltererContainerTable.COLUMN_CONTAINER_TYPE + " = ?",
						new String[] { containerType }, null, null, null);

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					FiltererContainerRowDB filtererContainerRowDB = cursorToFiltererContainer(cursor);
					filtererContainerRows.add(filtererContainerRowDB);
					cursor.moveToNext();
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return filtererContainerRows.toArray(new FiltererContainerRowDB[0]);
	}

	private long insertFiltererContainer(String containerKey,
			String containerType) {
		Log.d(TAG, "Is about to insert FiltererContainer for ContainerKey ["
				+ containerKey + "]; ContainerType [" + containerType + "]");

		// Populate the values to insert
		ContentValues valuesForFiltererContainer = new ContentValues();
		valuesForFiltererContainer.put(
				FiltererContainerTable.COLUMN_CONTAINER_KEY, containerKey);
		valuesForFiltererContainer.put(
				FiltererContainerTable.COLUMN_CONTAINER_TYPE, containerType);
		long insertedID = 0;
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the FiltererContainer table
				insertedID = database.insert(FiltererContainerTable.TABLE_NAME,
						null, valuesForFiltererContainer);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ FiltererContainerTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	private long queryForFiltererContainerID(String containerKey,
			String containerType) {
		// Query for the FiltererContainer
		FiltererContainerRowDB filtererContainer = queryForFiltererContainer(
				containerKey, containerType);

		// Extract the ID
		return filtererContainer.getFiltererContainerID();
	}

	/**
	 * 
	 * 
	 * 'ContextFilterer' table Methods
	 * 
	 * 
	 */
	public void addContextFilterer(String containerKey, String containerType,
			String groundingID, String serializedContextEvent) {
		// Query
		long filtererContainerID = queryForFiltererContainerID(containerKey,
				containerType);

		// Add the filterer
		insertContextFilterer(filtererContainerID, groundingID,
				serializedContextEvent);
	}

	public ContextFiltererRowDB[] queryForContextFilterers(String containerKey,
			String containerType) {
		// Query for the container ID
		long filtererContainerID = queryForFiltererContainerID(containerKey,
				containerType);

		// Query for the ContextFilterers
		return queryForContextFilterersByFiltererContainerID(filtererContainerID);
	}

	public void removeContextFilterers(String containerKey,
			String containerType, String contextSubscriberGroundingID) {
		// Query for the FiltererContainer
		FiltererContainerRowDB filtererContainer = queryForFiltererContainer(
				containerKey, containerType);

		// Extract the ID
		long filtererContainerID = filtererContainer.getFiltererContainerID();

		// Remove the filterer
		removeContextFilterers(filtererContainerID,
				contextSubscriberGroundingID);
	}

	private long insertContextFilterer(long filtererContainerID,
			String groundingID, String serializedContextEvent) {
		Log.d(TAG,
				"Is about to insert ContextFilterer for FiltererContainerID ["
						+ filtererContainerID + "]; GroundingID ["
						+ groundingID + "]");

		// Populate the values to insert
		ContentValues valuesForContextFilterer = new ContentValues();
		valuesForContextFilterer.put(
				ContextFiltererTable.COLUMN_FILTERER_CONTAINER_FK,
				filtererContainerID);
		valuesForContextFilterer.put(ContextFiltererTable.COLUMN_GROUNDING_ID,
				groundingID);
		valuesForContextFilterer.put(
				ContextFiltererTable.COLUMN_CONTEXT_EVENT_CONTENT,
				serializedContextEvent);
		long insertedID = 0;
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the ContextFilterer table
				insertedID = database.insert(ContextFiltererTable.TABLE_NAME,
						null, valuesForContextFilterer);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ ContextFiltererTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	private ContextFiltererRowDB[] queryForContextFilterersByFiltererContainerID(
			long filtererContainerID) {
		Log.d(TAG,
				"Is about to query for ContextFilterers FiltererContainerID ["
						+ filtererContainerID + "]");

		List<ContextFiltererRowDB> contextFiltererRows = new ArrayList<ContextFiltererRowDB>();
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						ContextFiltererTable.TABLE_NAME,
						ContextFiltererTable.allColumns, // TODO: make sure that
															// the
						// search by ID as string will
						// work here...
						ContextFiltererTable.COLUMN_FILTERER_CONTAINER_FK
								+ " = " + filtererContainerID, null, null,
						null, null);

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					ContextFiltererRowDB contextFiltererRowDB = cursorToContextFilterer(cursor);
					contextFiltererRows.add(contextFiltererRowDB);
					cursor.moveToNext();
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return contextFiltererRows.toArray(new ContextFiltererRowDB[0]);
	}

	private void removeContextFilterers(long filtererContainerID,
			String contextSubscriberGroundingID) {
		Log.d(TAG,
				"Is about to remove ContextFilrerers with FiltererContainerID ["
						+ filtererContainerID + "]; SubscriberID ["
						+ contextSubscriberGroundingID + "]");
		synchronized (sync) {
			try {
				open();
				int numOfDeletedRows = database.delete(
						ContextFiltererTable.TABLE_NAME,
						ContextFiltererTable.COLUMN_CONTEXT_FILTERER_ID_PK
								+ "= ? AND "
								+ ContextFiltererTable.COLUMN_GROUNDING_ID
								+ "= ?",
						new String[] { String.valueOf(filtererContainerID),
								contextSubscriberGroundingID });

				Log.d(TAG, "[" + numOfDeletedRows
						+ "] has(ve) been deleted from ["
						+ ContextFiltererTable.TABLE_NAME + "]");
			} finally {
				close();
			}
		}
	}

	/**
	 * 
	 * 
	 * 'AllProvisions' table Methods
	 * 
	 * 
	 */

	public void addAllProvision(String contextEventPatternAsString) {
		insertAllProvision(contextEventPatternAsString);
	}

	public AllProvisionsRowDB[] queryForAllProvision() {
		Log.d(TAG, "Is about to query for AllProvisions FiltererContainerID");

		List<AllProvisionsRowDB> allProvisionsRows = new ArrayList<AllProvisionsRowDB>();
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(AllProvisionsTable.TABLE_NAME,
						AllProvisionsTable.allColumns, null, null, null, null,
						null);

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					AllProvisionsRowDB allProvisionsRowDB = cursorToAllProvision(cursor);
					allProvisionsRows.add(allProvisionsRowDB);
					cursor.moveToNext();
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return allProvisionsRows.toArray(new AllProvisionsRowDB[0]);
	}

	private long insertAllProvision(String contextEventPatternAsString) {
		Log.d(TAG,
				"Is about to insert AllProvision ContextEventPatternAsString ["
						+ contextEventPatternAsString + "]");

		// Populate the values to insert
		ContentValues valuesForAllProvisions = new ContentValues();
		valuesForAllProvisions.put(
				AllProvisionsTable.COLUMN_CONTEXT_EVENT_PATTERN_CONTENT,
				contextEventPatternAsString);
		long insertedID = 0;
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the AllProvisions table
				insertedID = database.insert(AllProvisionsTable.TABLE_NAME,
						null, valuesForAllProvisions);
				Log.d(TAG, "Inserted status [" + insertedID + "] to ["
						+ AllProvisionsTable.TABLE_NAME + "] table");
			} finally {
				close();
			}
		}
		return insertedID;
	}

	/**
	 * 
	 * 
	 * Common Methods
	 * 
	 * 
	 */

	public void resetDB() {
		synchronized (sync) {
			try {
				open();
				// Table may not have been initialized correctly, therefore try
				// catch
				// Delete tables
				for (String tableToDelete : tablesToClearInReset) {
					database.delete(tableToDelete, null, null);
				}
				// }catch (Exception e) {
				// Log.e(TAG, "!!! Error reseting DB !!!",e);
			} finally {
				close();
			}
		}
	}

	private ProvisionRowDB cursorToProvision(Cursor cursor) {
		ProvisionRowDB provisionRowDB = new ProvisionRowDB();
		provisionRowDB.setProvisionID(cursor.getString(0));

		return provisionRowDB;
	}

	private CalledPeersRowDB cursorToCalledPeers(Cursor cursor) {
		CalledPeersRowDB calledPeersRowDB = new CalledPeersRowDB();
		calledPeersRowDB.setCalledPeerID(cursor.getLong(0));
		calledPeersRowDB.setMessageID(cursor.getString(1));
		calledPeersRowDB.setNumOfCalledPeers(cursor.getInt(2));

		return calledPeersRowDB;
	}

	private ContextEventPatternRowDB cursorToContextEventPattern(Cursor cursor) {
		ContextEventPatternRowDB contextEventPatternRowDB = new ContextEventPatternRowDB();
		contextEventPatternRowDB.setContextEventPattern(cursor.getString(0));

		return contextEventPatternRowDB;
	}

	private FiltererContainerRowDB cursorToFiltererContainer(Cursor cursor) {
		FiltererContainerRowDB filtererContainerRowDB = new FiltererContainerRowDB();
		filtererContainerRowDB.setFiltererContainerID(cursor.getLong(0));
		filtererContainerRowDB.setContainerKey(cursor.getString(1));
		filtererContainerRowDB.setContainerType(cursor.getString(2));

		return filtererContainerRowDB;
	}

	private ContextFiltererRowDB cursorToContextFilterer(Cursor cursor) {
		ContextFiltererRowDB contextFiltererRowDB = new ContextFiltererRowDB();
		contextFiltererRowDB.setGroundingID(cursor.getString(0));
		contextFiltererRowDB.setContextEventAsString(cursor.getString(1));

		return contextFiltererRowDB;
	}

	private AllProvisionsRowDB cursorToAllProvision(Cursor cursor) {
		AllProvisionsRowDB allProvisionsRowDB = new AllProvisionsRowDB();
		allProvisionsRowDB.setContextEventPatternAsString(cursor.getString(0));

		return allProvisionsRowDB;
	}

	@Override
	protected String getRegistryTableName() {
		return RegistryContextsTable.TABLE_NAME;
	}
}
