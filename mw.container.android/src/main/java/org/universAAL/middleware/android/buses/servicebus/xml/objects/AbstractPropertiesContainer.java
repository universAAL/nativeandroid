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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 23, 2012
 * 
 */
public class AbstractPropertiesContainer implements Serializable {
	private static final long serialVersionUID = 1L;

	protected List<PropertyXmlObj> properties = new ArrayList<PropertyXmlObj>();

	public AbstractPropertiesContainer(Node nodeContainsListOfProperties) {
		populatePropertiesElements(nodeContainsListOfProperties);
	}

	public List<PropertyXmlObj> getProperties() {
		return properties;
	}

	public String[] getPropertiesAsStringArr() {
		int propertiesSize = getProperties().size();
		String[] propertyPaths = new String[propertiesSize];
		for (int i = 0; i < propertiesSize; i++) {
			propertyPaths[i] = getProperties().get(i).getUri();
		}
		return propertyPaths;
	}

	private void populatePropertiesElements(Node input) {
		// Get the property path node
		Node propertyPath = CommonXmlParserUtils.getSingleChildNode(input,
				ICommonServiceBusXmlConstants.PROPERTY_PATH_ELEMENT);

		// Get the properties list
		List<Node> propertiesNodes = CommonXmlParserUtils.getChildNodes(
				propertyPath, ICommonServiceBusXmlConstants.PROPERTY_ELEMENT);

		// Initiate each one of the properties
		for (Node curProp : propertiesNodes) {
			properties.add(new PropertyXmlObj(curProp));
		}
	}
}
