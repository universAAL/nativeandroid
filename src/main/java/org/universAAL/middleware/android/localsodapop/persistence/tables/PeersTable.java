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
package org.universAAL.middleware.android.localsodapop.persistence.tables;

import org.universAAL.middleware.android.common.ISqlConstants;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class PeersTable {

    // Peers table
    public static final String TABLE_NAME = "PEERS";

    // Columns
    public static final String COLUMN_ID_PK = "_PEERS_ID_PK";
    public static final String COLUMN_PEER_ID = "PEER_ID";
    public static final String COLUMN_PEER_PROTOCOL = "PEER_PROTOCOL";
    public static final String COLUMN_DISCOVERED = "DISCOVERED";
    public static final String COLUMN_BUSSES_RECEIVED = "BUSSES_RECEIVED";

    // Peers table creation
    public static final String TABLE_CREATE = "create table if not exists " + PeersTable.TABLE_NAME
	    + "( " + COLUMN_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_PEER_ID
	    + " VARCHAR(50) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_PEER_PROTOCOL
	    + " VARCHAR(20) " + ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_DISCOVERED
	    + " VARCHAR(1) DEFAULT 0, " + COLUMN_BUSSES_RECEIVED + " VARCHAR(1) DEFAULT 0);";
}
