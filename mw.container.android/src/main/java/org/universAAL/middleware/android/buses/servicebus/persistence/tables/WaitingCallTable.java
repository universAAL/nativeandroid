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
package org.universAAL.middleware.android.buses.servicebus.persistence.tables;

import org.universAAL.middleware.android.common.ISqlConstants;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 21, 2012
 * 
 */
public class WaitingCallTable {
	// WaitingCall table
	public static final String TABLE_NAME = "WAITING_CALLS";

	// Columns
	public static final String COLUMN_ID_PK = "_ID_PK";
	public static final String COLUMN_CALL_ID = "CALL_ID";
	public static final String COLUMN_ACTION_NAME = "ACTION_NAME";
	public static final String COLUMN_REPLY_TO_ACTION = "REPLY_TO_ACTION";
	public static final String COLUMN_REPLY_TO_CATEGORY = "REPLY_TO_CATEGORY";

	// WaitingCalls table creation
	public static final String TABLE_CREATE = "create table if not exists "
			+ TABLE_NAME + "( " + COLUMN_ID_PK + " "
			+ ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_CALL_ID
			+ " VARCHAR(50) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", "
			+ COLUMN_ACTION_NAME + " VARCHAR(50) "
			+ ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_REPLY_TO_ACTION
			+ " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ", "
			+ COLUMN_REPLY_TO_CATEGORY + " VARCHAR(50) "
			+ ISqlConstants.NOT_NULL_COLUMN + ");";

	public static final String[] allColumns = new String[] { COLUMN_CALL_ID,
			COLUMN_ACTION_NAME, COLUMN_REPLY_TO_ACTION,
			COLUMN_REPLY_TO_CATEGORY };
}
