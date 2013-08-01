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

import org.universAAL.middleware.android.buses.common.CacheBasedTimeMap;
import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.CalledPeersRowDB;
import org.universAAL.middleware.context.data.ICalledPeers;
import org.universAAL.middleware.context.data.INumCalledPeersData;
import org.universAAL.middleware.context.data.factory.IContextStrategyDataFactory;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 * 
 */
public class AndroidNumCalledPeersData extends
		AbstractAndroidContextBusPersistable implements INumCalledPeersData {

	private static final int recordAgeInSeconds = 2 * 60; // 2 minutes
	private static final int intervalToCleanOldRecordsInSeconds = 5 * 60; // 5
	// minutes

	private IContextStrategyDataFactory factory;

	// Store a caching - this cache will be cleared once the application is
	// unloaded / after X minutes
	// We want that the returned CalledPeers will be same in between callers
	private CacheBasedTimeMap<String, ICalledPeers> cache = new CacheBasedTimeMap<String, ICalledPeers>(
			recordAgeInSeconds, intervalToCleanOldRecordsInSeconds);

	public AndroidNumCalledPeersData(Context context) {
		super(context);

		factory = new AndroidContextStrategyDataFactory(context);
	}

	public void addCalledPeers(String messageID, ICalledPeers calledPeers) {

		// Persist
		sqliteMngr.addCalledPeers(messageID, calledPeers.getNumOfCalledPeers());

		// Keep in the cache
		cache.put(messageID, calledPeers);
	}

	public ICalledPeers getCalledPeers(String messageID) {
		ICalledPeers calledPeers = null;

		// First - check in cache
		if (cache.containsKey(messageID)) {
			calledPeers = cache.get(messageID);
		} else {
			// If doesn't exist - query in DB and store in cache
			// Query from DB
			CalledPeersRowDB calledPeersRow = sqliteMngr
					.queryForCalledPeers(messageID);
			calledPeers = createCalledPeersFromRow(calledPeersRow);

			// Store in cache
			cache.put(messageID, calledPeers);
		}

		return calledPeers;
	}

	public void removeCalledPeers(String messageID) {
		// Clear from DB
		sqliteMngr.removeCalledPeers(messageID);

		// Clear from cache
		cache.remove(messageID);
	}

	private ICalledPeers createCalledPeersFromRow(
			CalledPeersRowDB calledPeersRowDB) {
		ICalledPeers calledPeers = factory.createCalledPeers();

		calledPeers.setMessageID(calledPeersRowDB.getMessageID());
		calledPeers.setNumOfCalledPeers(calledPeersRowDB.getNumOfCalledPeers());

		return calledPeers;
	}
}
