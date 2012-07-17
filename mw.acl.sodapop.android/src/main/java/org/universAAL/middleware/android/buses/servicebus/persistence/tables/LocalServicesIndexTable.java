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
 *         Apr 22, 2012
 * 
 */
public class LocalServicesIndexTable {

    // LocalServicesIndex table
    public static final String TABLE_NAME = "LOCAL_SERVICES_INDEX";

    // Columns
    public static final String COLUMN_ID_PK = "_ID_PK";
    public static final String COLUMN_PROCESS_URI = "PROCESS_URI";
    public static final String COLUMN_SERVICE_SERVICE_REALIZATION = "SERVICE_SERVICE_REALIZATION";

    // LocalPeerInfo table creation
    public static final String TABLE_CREATE = "create table if not exists " + TABLE_NAME + "( "
	    + COLUMN_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_PROCESS_URI
	    + " VARCHAR(50) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_SERVICE_SERVICE_REALIZATION + " VARCHAR(200) " + ISqlConstants.NOT_NULL_COLUMN
	    + ");";

    public static final String[] allColumns = new String[] { COLUMN_PROCESS_URI,
	    COLUMN_SERVICE_SERVICE_REALIZATION };
}
