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
package org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class FiltererContainerRowDB {

    private long filtererContainerID;
    private String containerKey;
    private String containerType;

    public String getContainerKey() {
	return containerKey;
    }

    public long getFiltererContainerID() {
	return filtererContainerID;
    }

    public void setFiltererContainerID(long filtererContainerID) {
	this.filtererContainerID = filtererContainerID;
    }

    public void setContainerKey(String containerKey) {
	this.containerKey = containerKey;
    }

    public String getContainerType() {
	return containerType;
    }

    public void setContainerType(String containerType) {
	this.containerType = containerType;
    }
}
