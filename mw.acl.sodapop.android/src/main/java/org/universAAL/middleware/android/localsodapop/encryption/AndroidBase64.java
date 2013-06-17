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
package org.universAAL.middleware.android.localsodapop.encryption;

import org.universAAL.middleware.connectors.communication.jgroups.util.Codec;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 6, 2012
 * 
 */
public class AndroidBase64 implements Codec {

    public byte[] encode(byte[] binaryData) {
	return android.util.Base64.encode(binaryData, android.util.Base64.DEFAULT);
    }

    public byte[] decode(String base64String) {
	return android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
    }
}
