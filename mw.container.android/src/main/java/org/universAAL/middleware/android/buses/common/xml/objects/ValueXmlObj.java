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
package org.universAAL.middleware.android.buses.common.xml.objects;

import java.io.Serializable;

import org.universAAL.middleware.android.common.CommonXmlParserUtils;
//import org.universAAL.middleware.android.buses.common.xml.ICommonXmlConstants;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public class ValueXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String javaClass;
	private String value;

	public ValueXmlObj(Node valueNode) {
		// Populate the attributes
		javaClass = CommonXmlParserUtils.getAttributeValueAsString(valueNode,
				ICommonXmlConstants.VALUE_ATTRIBUTE_JAVA_CLASS);

		// Get the node value itself
		value = CommonXmlParserUtils.getNodeContent(valueNode);
	}

	public String getJavaClass() {
		return javaClass;
	}

	public String getValue() {
		return value;
	}
}
