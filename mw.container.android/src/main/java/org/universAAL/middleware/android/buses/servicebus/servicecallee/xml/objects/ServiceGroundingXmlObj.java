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
package org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.common.xml.objects.OntologiesContainedXml;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.IServiceGroundingXmlConstants;
import org.universAAL.middleware.android.common.AndroidServiceType;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.SerializeUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public class ServiceGroundingXmlObj extends OntologiesContainedXml {

	private static final long serialVersionUID = 1L;

	private String uri;
	private String superJavaClass;
	private String version;
	private AndroidServiceType androidServiceType;
	private List<OperationXmlObj> operations = new ArrayList<OperationXmlObj>();

	public ServiceGroundingXmlObj(Node serviceNode,
			AndroidServiceType androidServiceType) {

		super(serviceNode);

		// Get the name
		uri = CommonXmlParserUtils.getAttributeValueAsString(serviceNode,
				IServiceGroundingXmlConstants.SERVICE_GROUNDING_ATTRIBUTE_URI);

		// Get the super class in ontology
		superJavaClass = CommonXmlParserUtils
				.getAttributeValueAsString(
						serviceNode,
						IServiceGroundingXmlConstants.SERVICE_GROUNDING_ATTRIBUTE_SUPER_JAVA_CLASS);

		// Get the version
		version = CommonXmlParserUtils
				.getAttributeValueAsString(
						serviceNode,
						IServiceGroundingXmlConstants.SERVICE_GROUNDING_ATTRIBUTE_VERSION);

		// Set the android service type
		this.androidServiceType = androidServiceType;

		// Populate the operations
		populateOperations(serviceNode);
	}

	public String getUri() {
		return uri;
	}

	public String getSuperJavaClass() {
		return superJavaClass;
	}

	public String getVersion() {
		return version;
	}

	public AndroidServiceType getAndroidServiceType() {
		return androidServiceType;
	}

	public List<OperationXmlObj> getOperations() {
		return operations;
	}

	public byte[] serialize() {
		return SerializeUtils.serializeObject(this);
	}

	public static ServiceGroundingXmlObj deserialize(byte[] objAsBytesArr) {
		return (ServiceGroundingXmlObj) SerializeUtils
				.deserializeObject(objAsBytesArr);
	}

	public OperationXmlObj getOperationByUri(String uri) {
		OperationXmlObj foundOperation = null;
		for (OperationXmlObj operation : operations) {
			if (uri.startsWith(operation.getUri())) {
				foundOperation = operation;
				break;
			}
		}

		return foundOperation;
	}

	private void populateOperations(Node serviceNode) {
		// Get the operations element
		Node operationsNode = CommonXmlParserUtils.getSingleChildNode(
				serviceNode, IServiceGroundingXmlConstants.OPERATIONS_ELEMENT);

		// Get the operation list
		List<Node> operationsChileNodes = CommonXmlParserUtils
				.getChildNodes(operationsNode,
						IServiceGroundingXmlConstants.OPERATION_ELEMENT);

		for (Node curOperationNode : operationsChileNodes) {
			operations.add(new OperationXmlObj(curOperationNode));
		}
	}
}
