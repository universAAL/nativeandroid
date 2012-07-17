//******************************************************************  
//   IBM Confidential                                                    
//
//   OCO Source Materials                                                
//
//   IBM Healthcare and Life Sciences Content Management Offering                                                
//
//   (C)Copyright IBM Corp. 2011                                  
//
//   The source code for this program is not published or otherwise      
//   divested of its trade secrets, irrespective of what has been        
//   deposited with the US Copyright Office.                             
//****************************************************************** 
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
