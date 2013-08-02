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

import org.universAAL.middleware.android.buses.common.xml.objects.OntologiesContainedXml;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.IServiceRequestGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.SerializeUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class ServiceRequestGroundingXmlObj extends OntologiesContainedXml {

	private static final long serialVersionUID = 1L;

	private String version;

	private List<ActionXmlObj> actions = new ArrayList<ActionXmlObj>();

	public ServiceRequestGroundingXmlObj(Node serviceRequestGroundingNode) {

		super(serviceRequestGroundingNode);

		// Get the version
		version = CommonXmlParserUtils
				.getAttributeValueAsString(
						serviceRequestGroundingNode,
						IServiceRequestGroundingXmlConstants.SERVICE_REQUEST_GROUNDING_ATTRIBUTE_VERSION);

		// Populate the actions
		populateActions(serviceRequestGroundingNode);
	}

	public String getVersion() {
		return version;
	}

	public List<ActionXmlObj> getActions() {
		return actions;
	}

	public ActionXmlObj getActionByName(String actionName) {
		ActionXmlObj foundAction = null;
		for (ActionXmlObj action : actions) {
			if (action.getAndroidAction().equals(actionName)) {
				foundAction = action;
				break;
			}
		}

		return foundAction;
	}

	public byte[] serialize() {
		return SerializeUtils.serializeObject(this);
	}

	public static ServiceRequestGroundingXmlObj deserialize(byte[] objAsBytesArr) {
		return (ServiceRequestGroundingXmlObj) SerializeUtils
				.deserializeObject(objAsBytesArr);
	}

	private void populateActions(Node serviceRequestGroundingNode) {
		// Get the actions element
		Node actionsNode = CommonXmlParserUtils.getSingleChildNode(
				serviceRequestGroundingNode,
				IServiceRequestGroundingXmlConstants.ACTIONS_ELEMENT);

		// Get the actions list
		List<Node> actionsChildNodes = CommonXmlParserUtils.getChildNodes(
				actionsNode,
				IServiceRequestGroundingXmlConstants.ACTION_ELEMENT);

		for (Node curActionNode : actionsChildNodes) {
			actions.add(new ActionXmlObj(curActionNode));
		}
	}
}
