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
public class FiltererContainerTable {

    // Provisions table
    public static final String TABLE_NAME = "FILTERER_CONTAINER";

    // Columns
    public static final String COLUMN_ID_PK = "_FILTERER_CONTAINER_ID_PK";
    public static final String COLUMN_CONTAINER_KEY = "CONTAINER_KEY";
    public static final String COLUMN_CONTAINER_TYPE = "CONTAINER_TYPE";

    // LocalPeerInfo table creation
    public static final String TABLE_CREATE = "create table if not exists " + TABLE_NAME + "( "
	    + COLUMN_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_CONTAINER_KEY
	    + " VARCHAR(50) UNIQUE " + ", " + COLUMN_CONTAINER_TYPE + " VARCHAR(20) "
	    + ISqlConstants.NOT_NULL_COLUMN + ");";

    public static final String[] allColumns = new String[] { COLUMN_ID_PK, COLUMN_CONTAINER_KEY,
	    COLUMN_CONTAINER_TYPE };
}
