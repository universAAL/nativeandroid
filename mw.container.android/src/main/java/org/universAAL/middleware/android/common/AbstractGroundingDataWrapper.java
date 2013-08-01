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
package org.universAAL.middleware.android.common;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public abstract class AbstractGroundingDataWrapper {

	private String packageName;
	private String groundingID; // android service name + service grounding uri
	private String androidUniqueName;

	public AbstractGroundingDataWrapper(String packageName, String groundingID,
			String androidUniqueName) {
		this.packageName = packageName;
		this.groundingID = groundingID;
		this.androidUniqueName = androidUniqueName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getGroundingID() {
		return groundingID;
	}

	public String getAndroidUniqueName() {
		return androidUniqueName;
	}
}
