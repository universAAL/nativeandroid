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
package org.universAAL.middleware.android.buses.common;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 18, 2012
 * 
 */
public class KeyWithTimestamp {
    private Object key;

    private Date expiredDate;

    public KeyWithTimestamp(Object key, int ageOldInSeconds) {
	this.key = key;
	this.expiredDate = addDate(new Date(), Calendar.SECOND, ageOldInSeconds);
    }

    public boolean isItemExpired() {
	return expiredDate.before(new Date());
    }

    public Object getKey() {
	return key;
    }

    private static Date addDate(Date date, int field, int amount) {
	// Initiate the Calendar object and add the given amount to the given
	// field
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	cal.add(field, amount);

	return cal.getTime();
    }
}
