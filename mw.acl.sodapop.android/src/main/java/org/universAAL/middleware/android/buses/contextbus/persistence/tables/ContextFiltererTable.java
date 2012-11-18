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
public class ContextFiltererTable {

    // ContextFilterer table
    public static final String TABLE_NAME = "CONTEXT_FILTERER";

    // Columns
    public static final String COLUMN_CONTEXT_FILTERER_ID_PK = "_CONTEXT_FILTERER_ID_PK";
    public static final String COLUMN_FILTERER_CONTAINER_FK = "_ID_FILTERER_CONTAINER_FK";
    public static final String COLUMN_GROUNDING_ID = "GROUNDING_ID";
    public static final String COLUMN_CONTEXT_EVENT_CONTENT = "CONTEXT_EVENT_CONTENT";

    public static String[] allColumns = { COLUMN_GROUNDING_ID, COLUMN_CONTEXT_EVENT_CONTENT };

    // ContextFilterer table creation
    public static final String TABLE_CREATE = "create table if not exists " + TABLE_NAME + "( "
	    + COLUMN_CONTEXT_FILTERER_ID_PK + " " + ISqlConstants.AUTO_INCREMENT_PK + ", "
	    + COLUMN_GROUNDING_ID + " VARCHAR(50) " + ISqlConstants.NOT_NULL_COLUMN + ", "
	    + COLUMN_CONTEXT_EVENT_CONTENT + " VARCHAR(200) " + ISqlConstants.NOT_NULL_COLUMN
	    + ", " + COLUMN_FILTERER_CONTAINER_FK + " integer, " + "FOREIGN KEY ("
	    + COLUMN_FILTERER_CONTAINER_FK + ") REFERENCES " + FiltererContainerTable.TABLE_NAME
	    + " (" + FiltererContainerTable.COLUMN_ID_PK + ") ON DELETE CASCADE);";
}
