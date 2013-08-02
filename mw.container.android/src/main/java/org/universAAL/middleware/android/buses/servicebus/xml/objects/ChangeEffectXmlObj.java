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
package org.universAAL.middleware.android.buses.servicebus.xml.objects;

import org.universAAL.middleware.android.buses.common.xml.objects.ICommonXmlConstants;
import org.universAAL.middleware.android.buses.common.xml.objects.ValueXmlObj;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public class ChangeEffectXmlObj extends AbstractPropertiesContainer {

	private static final long serialVersionUID = 1L;

	private ValueXmlObj value;

	public ChangeEffectXmlObj(Node changeEffectNode) {
		super(changeEffectNode);

		populateValueElement(changeEffectNode);
	}

	public ValueXmlObj getValue() {
		return value;
	}

	private void populateValueElement(Node changeEffectNode) {
		// Get the value node
		Node valueNode = CommonXmlParserUtils.getSingleChildNode(
				changeEffectNode, ICommonXmlConstants.VALUE_ELEMENT);

		// Initiate the value xml object
		value = new ValueXmlObj(valueNode);
	}
}
