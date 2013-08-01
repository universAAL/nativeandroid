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
package org.universAAL.middleware.android.buses.common;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class AndroidNameParameterParser {

	private static final String TAG = AndroidNameParameterParser.class
			.getCanonicalName();

	private String androidNameWithoutParameter;
	private String androidParameter;

	protected AndroidNameParameterParser(String androidNameWithoutParameter,
			String androidParameter) {
		this.androidNameWithoutParameter = androidNameWithoutParameter;
		this.androidParameter = androidParameter;
	}

	public static AndroidNameParameterParser parseAndroidName(String androidName) {
		AndroidNameParameterParser parser = null;

		String startDelim = "{";
		String endDelim = "}";

		Log.d(TAG, "Is about to parse name [" + androidName + "] delims ["
				+ startDelim + endDelim + "]");

		int startIndex = androidName.indexOf(startDelim);
		int endIndex = androidName.lastIndexOf(endDelim);

		if (startIndex > -1 && endIndex > -1 && endIndex > startIndex) {
			String androidNameWithoutParameter = androidName.substring(0,
					startIndex);
			String androidParameter = androidName.substring(startIndex
					+ startDelim.length(), endIndex);

			parser = new AndroidNameParameterParser(
					androidNameWithoutParameter, androidParameter);
		}

		return parser;
	}

	public String getAndroidNameWithoutParameter() {
		return androidNameWithoutParameter;
	}

	public String getAndroidParameter() {
		return androidParameter;
	}
}
