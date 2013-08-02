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
package org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.IServiceRequestGroundingXmlConstants;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.BundlePropertyXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ICommonServiceBusXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.w3c.dom.Node;

import android.os.Bundle;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class RequiredOutputXmlObj extends AbstractParameterXmlObj {

	private static final long serialVersionUID = 1L;

	private String uri;

	private List<BundlePropertyXmlObj> bundleProperties = new ArrayList<BundlePropertyXmlObj>();

	public RequiredOutputXmlObj(Node requiredOutputNode) {
		super(requiredOutputNode);

		uri = CommonXmlParserUtils
				.getAttributeValueAsString(
						requiredOutputNode,
						IServiceRequestGroundingXmlConstants.REQUIRED_OUTPUT_ATTRIBUTE_URI);

		if (Bundle.class.getCanonicalName().equals(
				androidExtraParameterJavaClass)) {
			populateBundleProperties(requiredOutputNode);
		}
	}

	public String getUri() {
		return uri;
	}

	public List<BundlePropertyXmlObj> getBundleProperties() {
		return bundleProperties;
	}

	private void populateBundleProperties(Node requiredOutputNode) {
		// Get the BundleProperties node
		Node bundlePropertiesNode = CommonXmlParserUtils.getSingleChildNode(
				requiredOutputNode,
				ICommonServiceBusXmlConstants.BUNDLE_PROPERTIES_ELEMENT);

		// Get the BundlePeroperty children
		List<Node> bundlePropertyChildrenList = CommonXmlParserUtils
				.getChildNodes(bundlePropertiesNode,
						ICommonServiceBusXmlConstants.BUNDLE_PROPERTY_ELEMENT);
		for (Node curBundlePropertyChild : bundlePropertyChildrenList) {
			bundleProperties.add(new BundlePropertyXmlObj(
					curBundlePropertyChild));
		}
	}
}
