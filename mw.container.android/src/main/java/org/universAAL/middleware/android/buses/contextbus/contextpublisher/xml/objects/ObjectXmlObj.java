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
package org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.objects;

import java.io.Serializable;

import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.IContextPublisherGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class ObjectXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String androidExtraParameter;
	private String androidExtraParameterJavaClass;

	public ObjectXmlObj(Node objectNode) {
		// Populate the attributes
		androidExtraParameter = CommonXmlParserUtils
				.getAttributeValueAsString(
						objectNode,
						IContextPublisherGroundingXmlConstants.OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM);
		androidExtraParameterJavaClass = CommonXmlParserUtils
				.getAttributeValueAsString(
						objectNode,
						IContextPublisherGroundingXmlConstants.OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM_JAVA_CLASS);
	}

	public String getAndroidExtraParameter() {
		return androidExtraParameter;
	}

	public String getAndroidExtraParameterJavaClass() {
		return androidExtraParameterJavaClass;
	}
}
