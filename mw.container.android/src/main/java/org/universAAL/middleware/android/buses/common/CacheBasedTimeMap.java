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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 * 
 */
public class CacheBasedTimeMap<K, V> extends HashMap<K, V> {
	private int ageInSeconds = 600; // default = 10 minutes

	private int delayCleanerInSeconds = 600; // default = 10 minutes

	/**
	 * Used for the synchronizations
	 */
	private Object syncObj = new Object();

	/**
	 * This list is sorted by timestamp (the first item in the list is oldest
	 * one)
	 * 
	 * This list enables us to remove the oldest items and not to pass the whole
	 * list. Each key with timestamp here contains the key that is stored in the
	 * map, in that way in O(1) we will be able to remove the item from the map
	 */
	private List<KeyWithTimestamp> keyWithTimestamps;

	/**
	 * This map enables us to figure out in O(1) if the item was expired or not.
	 * 
	 * When an item is requested from the 'this' (the global map) then we have a
	 * key, using the key we will access this local map to figure out if the
	 * item was not expired
	 */
	private Map<K, KeyWithTimestamp> keyToExpiredTime;

	private boolean stop;

	private static final long serialVersionUID = 1L;

	public CacheBasedTimeMap() {
		keyToExpiredTime = new HashMap<K, KeyWithTimestamp>();
		keyWithTimestamps = new ArrayList<KeyWithTimestamp>();

		startTimerForTheCleaner();
	}

	public CacheBasedTimeMap(int ageInSeconds, int delayInSeconds) {
		this.ageInSeconds = ageInSeconds;
		this.delayCleanerInSeconds = delayInSeconds;

		keyToExpiredTime = new HashMap<K, KeyWithTimestamp>();
		keyWithTimestamps = new ArrayList<KeyWithTimestamp>();

		startTimerForTheCleaner();
	}

	@Override
	public V put(K key, V value) {
		synchronized (syncObj) {
			KeyWithTimestamp keyWithTimestamp = new KeyWithTimestamp(key,
					ageInSeconds);

			// Add to the list with the expired items
			keyWithTimestamps.add(keyWithTimestamp);

			// Add to the local map
			keyToExpiredTime.put(key, keyWithTimestamp);

			// Add to the global map
			return super.put(key, value);
		}
	}

	@Override
	public V get(Object key) {
		synchronized (syncObj) {
			boolean valid = isItemValid(key);

			if (valid) {
				return super.get(key);
			} else {
				return null;
			}
		}
	}

	private boolean isItemValid(Object key) {
		boolean valid = false;

		// Get the key with timestamp from the local map
		if (keyToExpiredTime.containsKey(key)) {
			KeyWithTimestamp keyWithTimestamp = keyToExpiredTime.get(key);

			// Check if the item was expired
			valid = !keyWithTimestamp.isItemExpired();
		}

		return valid;
	}

	@SuppressWarnings("unchecked")
	private void removeExpiredItems() {
		int maxIndexToRemoveItems = -1;
		for (int i = 0; i < keyWithTimestamps.size(); i++) {
			if (keyWithTimestamps.get(i).isItemExpired()) {
				maxIndexToRemoveItems = i;
			} else {
				// The list is sorted by times, therefore if this item was not
				// expired
				// no need to continue searching
				break;
			}
		}

		if (maxIndexToRemoveItems > -1) {
			while (maxIndexToRemoveItems > -1) {
				// Remove from the list
				KeyWithTimestamp keyWithTimestamp = keyWithTimestamps
						.remove(maxIndexToRemoveItems--);

				// Get the key
				K key = (K) keyWithTimestamp.getKey();

				// Remove from the local map
				keyToExpiredTime.remove(key);

				// Remove from the global
				remove(key);
			}
		}
	}

	private void startTimerForTheCleaner() {
		if (delayCleanerInSeconds <= 0) {
			return;
		}

		runTimerInStandAloneMode();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			stop = true;
		} finally {
			super.finalize();
		}
	}

	private void runTimerInStandAloneMode() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				while (!stop) {
					synchronized (syncObj) {
						removeExpiredItems();
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Do nothing... continue to the next work
					}
				}
			}

		});

		t.start();
	}
}
