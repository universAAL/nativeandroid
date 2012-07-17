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
 *         Apr 5, 2012
 * 
 */
public class LocalBusesTable extends AbstractBusesTable {

    // LocalBuses table
    public static final String TABLE_NAME = "LOCAL_BUSES";

    // Columns
    public static final String COLUMN_BUS_PACKAGE_NAME = "BUS_PACKAGE_NAME";
    public static final String COLUMN_BUS_CLASS_NAME = "BUS_CLASS_NAME";

    public static final String[] allColumns = { COLUMN_BUS_NAME, COLUMN_BUS_PACKAGE_NAME,
	    COLUMN_BUS_CLASS_NAME };

    // LocalBuses table creation
    public static final String TABLE_CREATE = "create table if not exists " + TABLE_NAME + "( "
	    + COLUMN_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_BUS_NAME
	    + " VARCHAR(20) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_BUS_PACKAGE_NAME + " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_BUS_CLASS_NAME + " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ");";
}
