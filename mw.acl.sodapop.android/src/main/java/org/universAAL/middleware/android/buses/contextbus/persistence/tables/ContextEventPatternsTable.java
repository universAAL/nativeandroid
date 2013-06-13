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
package org.universAAL.middleware.android.buses.contextbus.persistence.tables;

import org.universAAL.middleware.android.common.ISqlConstants;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 21, 2012
 * 
 */
public class ContextEventPatternsTable {

    // ContextEventPatterns table
    public static final String TABLE_NAME = "CONTEXT_EVENT_PATTERNS";

    // Columns
    public static final String COLUMN_CONTEXT_EVENT_PAT_ID_PK = "_CONTEXT_EVENT_PAT_ID_PK";
    public static final String COLUMN_CALLED_PEERS_FK = "_ID_CALLED_PEERS_FK";
    public static final String COLUMN_CONTEXT_EVENT_CONTENT = "CONTEXT_EVENT_CONTENT";

    public static String[] allColumns = { COLUMN_CONTEXT_EVENT_CONTENT };

    // ContextEventPatterns table creation
    public static final String TABLE_CREATE = "create table if not exists " + TABLE_NAME + "( "
	    + COLUMN_CONTEXT_EVENT_PAT_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", "
	    + COLUMN_CONTEXT_EVENT_CONTENT + " VARCHAR(200) " + ISqlConstants.NOT_NULL_COLUMN
	    + ", " + COLUMN_CALLED_PEERS_FK + " integer, " + "FOREIGN KEY ("
	    + COLUMN_CALLED_PEERS_FK + ") REFERENCES " + CalledPeersTable.TABLE_NAME + " ("
	    + CalledPeersTable.COLUMN_ID_PK + ") ON DELETE CASCADE);";
}
