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
package org.universAAL.middleware.android.common;

import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class IntentUtils {

    private static final String TAG = IntentUtils.class.getCanonicalName();

    /**
     * 
     * @param keyName
     * @param javaClass
     *            - currently support only: java.lang.Integer Java.lang.String
     * @param value
     * 
     */
    public static void addToExtra(String keyName, String javaClass, Object value, Intent intent) {
	if (String.class.getCanonicalName().equals(javaClass)) {
	    intent.putExtra(keyName, (String) value);
	} else if (Integer.class.getCanonicalName().equals(javaClass)) {
	    intent.putExtra(keyName, (Integer) value);
	} else {
	    // For now just log it // TODO: consider to throw here exception
	    Log.e(TAG, "Unsupported javaClass for extras in the intent [" + javaClass + "]");
	}
    }
}
