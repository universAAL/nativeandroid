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
package org.universAAL.middleware.android.buses.servicebus.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.android.buses.common.persistence.AbstractCommonBusSQLiteMngr;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalServiceSearchResultsTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalServicesIndexTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.LocalWaitingCallerTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.RegistryServicesTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.ServiceProfileTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.WaitingCallTable;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalServiceIndexRowDB;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalServiceSearchResultRowDB;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.LocalWaitingCallerRowDB;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.ServiceProfileRowDB;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;
import org.universAAL.middleware.android.common.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import android.database.SQLException;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 */
public class ServiceBusSQLiteMngr extends AbstractCommonBusSQLiteMngr {

	private static final String TAG = ServiceBusSQLiteMngr.class.getName();

	// private static final Object sync = new Object();

	private static final String[] tablesToClearInReset = new String[] {
			RegistryServicesTable.TABLE_NAME,
			LocalServiceSearchResultsTable.TABLE_NAME,
			LocalServicesIndexTable.TABLE_NAME,
			LocalWaitingCallerTable.TABLE_NAME, ServiceProfileTable.TABLE_NAME,
			WaitingCallTable.TABLE_NAME };

	public ServiceBusSQLiteMngr(Context context) {
		super(context, new ServiceBusSQLiteHelper(context));
	}

	// public void open() throws SQLException {
	// synchronized (sync) {
	// System.out.println("vvvv     OPEN SQL SERV "+Thread.currentThread().getName());
	// database = dbHelper.getWritableDatabase(); // TODO: find another
	// // mechanism for
	// // open/close DB's
	// }
	// }
	//
	// public void close() {
	// System.out.println("    ^^^^ CLOSE SQL SERV "+Thread.currentThread().getName());
	// dbHelper.close();
	// }

	/**
	 * 
	 * 
	 * 'LocalServices' table Methods
	 * 
	 * 
	 */

	public void addLocalServiceIndex(String processURI,
			String serviceRealizationAsResource) {
		LocalServiceIndexRowDB localServiceIndex = queryLocalServiceIndexByProcessURI(processURI);

		if (null == localServiceIndex) {
			long insertedLocalServiceIndex = insertLocalServiceIndex(
					processURI, serviceRealizationAsResource);

			Log.d(TAG, "LocalServiceIndex was inserted successfully ["
					+ insertedLocalServiceIndex + "]");
		} else {
			Log.w(TAG,
					"No need to insert a new one, LocalServiceIndex already exists for ["
							+ processURI + "]");
		}
	}

	public LocalServiceIndexRowDB removeLocalServiceIndexByProcessURI(
			String processURI) {
		Log.d(TAG, "Is about to remove LocalServiceIndex [" + processURI + "]");

		LocalServiceIndexRowDB localServiceIndexRowDB = queryLocalServiceIndexByProcessURI(processURI);
		synchronized (sync) {
			try {
				open();
				int numOfDeletedRows = database.delete(
						LocalServicesIndexTable.TABLE_NAME,
						LocalServicesIndexTable.COLUMN_PROCESS_URI + " = ?",
						new String[] { processURI });

				Log.d(TAG, "[" + numOfDeletedRows
						+ "] has(ve) been deleted from ["
						+ LocalServicesIndexTable.TABLE_NAME + "]");
			} finally {
				close();
			}
		}
		return localServiceIndexRowDB;
	}

	public LocalServiceIndexRowDB queryLocalServiceIndexByProcessURI(
			String processURI) {
		LocalServiceIndexRowDB localServiceIndexRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						LocalServicesIndexTable.TABLE_NAME,
						LocalServicesIndexTable.allColumns,
						LocalServicesIndexTable.COLUMN_PROCESS_URI + " = ?",
						new String[] { processURI }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					localServiceIndexRowDB = cursorToLocalServiceIndex(cursor);
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return localServiceIndexRowDB;
	}

	public LocalServiceIndexRowDB[] queryForAllLocalServiceIndexes() {
		List<LocalServiceIndexRowDB> localServiceIndexes = new ArrayList<LocalServiceIndexRowDB>();
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						LocalServicesIndexTable.TABLE_NAME,
						LocalServicesIndexTable.allColumns, null, null, null,
						null, null);

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					LocalServiceIndexRowDB localServiceIndexRowDB = cursorToLocalServiceIndex(cursor);
					localServiceIndexes.add(localServiceIndexRowDB);
					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			} finally {
				close();
			}
		}
		return localServiceIndexes.toArray(new LocalServiceIndexRowDB[0]);
	}

	private long insertLocalServiceIndex(String processURI,
			String serviceRealizationAsResource) {
		Log.d(TAG, "Is about to insert LocalServiceIndex processURI ["
				+ processURI + "]; ServiceRealizationAsResource ["
				+ serviceRealizationAsResource + "]");

		// Populate the values to insert
		ContentValues valuesForLocalServicesIndexTable = new ContentValues();
		valuesForLocalServicesIndexTable.put(
				LocalServicesIndexTable.COLUMN_PROCESS_URI, processURI);
		valuesForLocalServicesIndexTable.put(
				LocalServicesIndexTable.COLUMN_SERVICE_SERVICE_REALIZATION,
				serviceRealizationAsResource);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the LocalServicesIndex table
				return database.insert(LocalServicesIndexTable.TABLE_NAME,
						null, valuesForLocalServicesIndexTable);
			} finally {
				close();
			}
		}
	}

	/**
	 * 
	 * 
	 * 'LocalServicesSearchResults' table Methods
	 * 
	 * 
	 */

	public void addLocalServicesSearchResultsWithProfiles(
			String serviceRealizationID, String[] serializedProfiles) {
		long insertedLocalServicesSearchResults = insertLocalServicesSearchResults(serviceRealizationID);

		Log.d(TAG, "LocalServicesSearchResults was inserted successfully ["
				+ insertedLocalServicesSearchResults + "]");

		// For each profile, insert it to the ServicePorfiles table
		for (String serializedProfile : serializedProfiles) {
			long insertedServiceProfile = insertServiceProfile(
					serializedProfile, insertedLocalServicesSearchResults);

			Log.d(TAG, "ServiceProfile was inserted successfully ["
					+ insertedServiceProfile + "] with ["
					+ insertedLocalServicesSearchResults + "] reference");
		}
	}

	public LocalServiceSearchResultRowDB queryForLocalServiceSearchResults(
			String serviceRealizationID) {
		LocalServiceSearchResultRowDB localServiceSearchResultRowDB = null;

		String query = ServiceBusSQLiteHelper.SERVICE_SEARCH_RESULTS_SERVICE_PROFILES_JOIN_QUERY
				+ serviceRealizationID;
		synchronized (sync) {
			try {
				open();
				Log.d(TAG, "Is about to perform a query [" + query + "]");
				Cursor cursor = database.rawQuery(query, null);

				Collection<LocalServiceSearchResultRowDB> services = getLocalServiceSearchResultsFromCursor(cursor);

				cursor.close();

				if (!services.isEmpty()) {
					localServiceSearchResultRowDB = services.iterator().next();
				}
			} finally {
				close();
			}
		}
		return localServiceSearchResultRowDB;
	}

	private long insertLocalServicesSearchResults(String serviceRealizationID) {
		Log.d(TAG,
				"Is about to insert LocalServicesSearchResults serviceRealizationID ["
						+ serviceRealizationID + "]");

		// Populate the values to insert
		ContentValues valuesLocalServicesSearchResultsTable = new ContentValues();
		valuesLocalServicesSearchResultsTable.put(
				LocalServiceSearchResultsTable.COLUMN_SERVICE_REALIZATION_ID,
				serviceRealizationID);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the LocalServicesIndex table
				return database.insert(
						LocalServiceSearchResultsTable.TABLE_NAME, null,
						valuesLocalServicesSearchResultsTable);
			} finally {
				close();
			}
		}
	}

	/**
	 * 
	 * 
	 * 'ServiceProfiles' table Methods
	 * 
	 * 
	 */
	private long insertServiceProfile(String serializedProfile,
			long localServicesSearchResultsID) {
		Log.d(TAG, "Is about to insert ServiceProfile SerializedProfile ["
				+ serializedProfile + "]; LocalServicesSearchResultsID ["
				+ localServicesSearchResultsID + "]");

		// Populate the values to insert
		ContentValues valuesServiceProfileTable = new ContentValues();
		valuesServiceProfileTable.put(ServiceProfileTable.COLUMN_CONTENT,
				serializedProfile);
		valuesServiceProfileTable.put(
				ServiceProfileTable.COLUMN_SERVICES_ID_FK,
				localServicesSearchResultsID);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the ServiceProfile table
				return database.insert(ServiceProfileTable.TABLE_NAME, null,
						valuesServiceProfileTable);
			} finally {
				close();
			}
		}
	}

	/**
	 * 
	 * 
	 * 'Waiting Calls' table Methods
	 * 
	 * 
	 */

	public void addLocalWaitingCaller(String msgID, String callerID) {
		Log.d(TAG, "Is about to insert LocalWaiter MsgID [" + msgID
				+ "]; CallerID [" + callerID + "]");

		// Populate the values to insert
		ContentValues valuesLocalWaitingCallerTable = new ContentValues();
		valuesLocalWaitingCallerTable.put(
				LocalWaitingCallerTable.COLUMN_MESSAGE_ID, msgID);
		valuesLocalWaitingCallerTable.put(
				LocalWaitingCallerTable.COLUMN_CALLER_ID, callerID);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the LocalWaitingCaller table
				long id = database.insert(LocalWaitingCallerTable.TABLE_NAME,
						null, valuesLocalWaitingCallerTable);

				Log.d(TAG, "LocalWaitingCaller insert status [" + id + "]");
			} finally {
				close();
			}
		}
	}

	public LocalWaitingCallerRowDB removeLocalWaitingCaller(String msgID) {
		Log.d(TAG, "Is about to remove LocalWaitingCaller [" + msgID + "]");

		LocalWaitingCallerRowDB localWaitingCaller = queryForLocalWaitingCallerByMessageID(msgID);

		if (null != localWaitingCaller) {
			synchronized (sync) {
				try {
					open();
					int numOfDeletedRows = database.delete(
							LocalWaitingCallerTable.TABLE_NAME,
							LocalWaitingCallerTable.COLUMN_MESSAGE_ID + " = ?",
							new String[] { msgID });

					Log.d(TAG, "[" + numOfDeletedRows
							+ "] has(ve) been deleted from ["
							+ LocalWaitingCallerTable.TABLE_NAME + "]");
				} finally {
					close();
				}
			}
		}

		return localWaitingCaller;
	}

	private LocalWaitingCallerRowDB queryForLocalWaitingCallerByMessageID(
			String msgID) {
		Log.d(TAG, "Is about to query for LocalWaitingCaller MessageID ["
				+ msgID + "]");

		LocalWaitingCallerRowDB localWaitingCallerRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(
						LocalWaitingCallerTable.TABLE_NAME,
						LocalWaitingCallerTable.allColumns,
						LocalWaitingCallerTable.COLUMN_MESSAGE_ID + " = ?",
						new String[] { msgID }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					localWaitingCallerRowDB = cursorToLocalWaitingCaller(cursor);
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return localWaitingCallerRowDB;
	}

	/**
	 * 
	 * 
	 * 'Waiting Calls' table Methods
	 * 
	 * 
	 */

	public void addWaitingCall(String callID, String actionName,
			String replyToAction, String replyToCategory) {
		Log.d(TAG, "Is about to insert WaitingCall CallID [" + callID
				+ "]; ActionName [" + actionName + "]; ReplyToAction ["
				+ replyToAction + "]; ReplyToCategory [" + replyToCategory
				+ "]");

		// Populate the values to insert
		ContentValues valuesWaitingCallsTable = new ContentValues();
		valuesWaitingCallsTable.put(WaitingCallTable.COLUMN_CALL_ID, callID);
		valuesWaitingCallsTable.put(WaitingCallTable.COLUMN_ACTION_NAME,
				actionName);
		valuesWaitingCallsTable.put(WaitingCallTable.COLUMN_REPLY_TO_ACTION,
				replyToAction);
		valuesWaitingCallsTable.put(WaitingCallTable.COLUMN_REPLY_TO_CATEGORY,
				replyToCategory);
		synchronized (sync) {
			try {
				open();
				// Insert the new row to the WaitingCalls table
				long id = database.insert(WaitingCallTable.TABLE_NAME, null,
						valuesWaitingCallsTable);

				Log.d(TAG, "WaitingCall insert status [" + id + "]");
			} finally {
				close();
			}
		}
	}

	public WaitingCallRowDB removeWaitingCallByCallID(String callID) {
		Log.d(TAG, "Is about to remove WaitingCall [" + callID + "]");

		WaitingCallRowDB waitingCallRowDB = queryForWaitingCallByCallID(callID);

		if (null != waitingCallRowDB) {
			synchronized (sync) {
				try {
					open();
					int numOfDeletedRows = database.delete(
							WaitingCallTable.TABLE_NAME,
							WaitingCallTable.COLUMN_CALL_ID + " = ?",
							new String[] { callID });

					Log.d(TAG, "[" + numOfDeletedRows
							+ "] has(ve) been deleted from ["
							+ WaitingCallTable.TABLE_NAME + "]");
				} finally {
					close();
				}
			}
		}

		return waitingCallRowDB;
	}

	private WaitingCallRowDB queryForWaitingCallByCallID(String callID) {
		Log.d(TAG, "Is about to query for WaitingCall [" + callID + "]");

		WaitingCallRowDB waitingCallRowDB = null;
		synchronized (sync) {
			try {
				open();
				Cursor cursor = database.query(WaitingCallTable.TABLE_NAME,
						WaitingCallTable.allColumns,
						WaitingCallTable.COLUMN_CALL_ID + " = ?",
						new String[] { callID }, null, null, null);

				cursor.moveToFirst();

				if (!cursor.isAfterLast()) {
					waitingCallRowDB = cursorToWaitingCall(cursor);
				}

				cursor.close();
			} finally {
				close();
			}
		}
		return waitingCallRowDB;
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

	private LocalServiceIndexRowDB cursorToLocalServiceIndex(Cursor cursor) {
		LocalServiceIndexRowDB localServiceIndexRowDB = new LocalServiceIndexRowDB();
		localServiceIndexRowDB.setProcessURI(cursor.getString(0));
		localServiceIndexRowDB.setServiceRealization(cursor.getString(1));

		return localServiceIndexRowDB;
	}

	private Collection<LocalServiceSearchResultRowDB> getLocalServiceSearchResultsFromCursor(
			Cursor cursor) {
		Map<String, LocalServiceSearchResultRowDB> localServiceSearchResultRowDBMap = new HashMap<String, LocalServiceSearchResultRowDB>();

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocalServiceSearchResultRowDB localServiceSearchResultRowDB = null;
			String id = cursor.getString(0);
			if (localServiceSearchResultRowDBMap.containsKey(id)) {
				localServiceSearchResultRowDB = localServiceSearchResultRowDBMap
						.get(id);
			} else {
				localServiceSearchResultRowDB = cursorLocalServiceSearchResult(cursor);
				localServiceSearchResultRowDBMap.put(id,
						localServiceSearchResultRowDB);
			}
			// Add Service Profile
			String serviceProfileAsStr = cursor.getString(1);
			if (!StringUtils.isEmpty(serviceProfileAsStr)) {
				ServiceProfileRowDB serviceProfile = new ServiceProfileRowDB();
				serviceProfile.setContent(serviceProfileAsStr);
				localServiceSearchResultRowDB.addProfile(serviceProfile);
			}
			cursor.moveToNext();
		}

		return localServiceSearchResultRowDBMap.values();
	}

	private LocalServiceSearchResultRowDB cursorLocalServiceSearchResult(
			Cursor cursor) {
		LocalServiceSearchResultRowDB localServiceSearchResultRowDB = new LocalServiceSearchResultRowDB();
		localServiceSearchResultRowDB.setServiceRealizationID(cursor
				.getString(0));

		return localServiceSearchResultRowDB;
	}

	private LocalWaitingCallerRowDB cursorToLocalWaitingCaller(Cursor cursor) {
		LocalWaitingCallerRowDB localWaitingCallerRowDB = new LocalWaitingCallerRowDB();
		localWaitingCallerRowDB.setMessageID(cursor.getString(0));
		localWaitingCallerRowDB.setCallerID(cursor.getString(1));

		return localWaitingCallerRowDB;
	}

	private WaitingCallRowDB cursorToWaitingCall(Cursor cursor) {
		WaitingCallRowDB waitingCallRowDB = new WaitingCallRowDB();
		waitingCallRowDB.setCallID(cursor.getString(0));
		waitingCallRowDB.setActionName(cursor.getString(1));
		waitingCallRowDB.setReplyToAction(cursor.getString(2));
		waitingCallRowDB.setReplyToCategory(cursor.getString(3));

		return waitingCallRowDB;
	}

	@Override
	protected String getRegistryTableName() {
		return RegistryServicesTable.TABLE_NAME;
	}
}
