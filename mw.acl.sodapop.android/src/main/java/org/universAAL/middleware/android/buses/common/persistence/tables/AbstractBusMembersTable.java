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
package org.universAAL.middleware.android.buses.common.persistence.tables;

import org.universAAL.middleware.android.common.ISqlConstants;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public abstract class AbstractBusMembersTable {

    // Columns
    public static final String COLUMN_ID_PK = "_ID_PK";
    public static final String COLUMN_MEMBER_ID = "MEMBER_ID";
    public static final String COLUMN_PACKAGE_NAME = "PACKAGE_NAME";
    public static final String COLUMN_MEMBER_TYPE = "MEMBER_TYPE";
    public static final String COLUMN_GROUNDING_ID = "GROUNDING_ID";
    public static final String COLUMN_GROUNDING_XML = "GROUNDING_XML";
    public static final String COLUMN_ANDROID_UNIQUE_NAME = "ANDROID_UNIQUE_NAME";

    // BusMembers table creation
    private String TABLE_CREATE = "create table if not exists " + getTableName() + "( "
	    + COLUMN_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", " + COLUMN_MEMBER_ID
	    + " VARCHAR(50) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_PACKAGE_NAME
	    + " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_MEMBER_TYPE
	    + " VARCHAR(20) " + ISqlConstants.NOT_NULL_COLUMN + ", " + COLUMN_GROUNDING_ID
	    + " VARCHAR(50) UNIQUE " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_ANDROID_UNIQUE_NAME + " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_GROUNDING_XML + " BLOB " + ISqlConstants.NOT_NULL_COLUMN + ");";

    public static final String[] allColumns = new String[] { COLUMN_MEMBER_ID, COLUMN_PACKAGE_NAME,
	    COLUMN_MEMBER_TYPE, COLUMN_GROUNDING_ID, COLUMN_ANDROID_UNIQUE_NAME,
	    COLUMN_GROUNDING_XML };

    protected abstract String getTableName();

    public String getTableCreateCommand() {
	return TABLE_CREATE;
    }
}
