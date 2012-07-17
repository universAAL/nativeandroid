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
package org.universAAL.middleware.android.buses.common.persistence.tables.rows;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class BusMemberRowDB {

    private String memberID;
    private String packageName;
    private String memberType;
    private String groundingID;
    private String androidUniqueName;
    private byte[] groundingXml;

    public String getMemberID() {
	return memberID;
    }

    public void setMemberID(String memberID) {
	this.memberID = memberID;
    }

    public String getPackageName() {
	return packageName;
    }

    public void setPackageName(String packageName) {
	this.packageName = packageName;
    }

    public String getMemberType() {
	return memberType;
    }

    public void setMemberType(String memberType) {
	this.memberType = memberType;
    }

    public String getGroundingID() {
	return groundingID;
    }

    public void setGroundingID(String groundingID) {
	this.groundingID = groundingID;
    }

    public String getAndroidUniqueName() {
	return androidUniqueName;
    }

    public void setAndroidUniqueName(String androidUniqueName) {
	this.androidUniqueName = androidUniqueName;
    }

    public byte[] getGroundingXml() {
	return groundingXml;
    }

    public void setGroundingXml(byte[] groundingXml) {
	this.groundingXml = groundingXml;
    }
}
