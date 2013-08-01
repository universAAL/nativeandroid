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
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml.IContextPublisherGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.SerializeUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class ContextPublisherGroundingXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uri;
	private String version;

	private List<ActionXmlObj> actions = new ArrayList<ActionXmlObj>();

	public ContextPublisherGroundingXmlObj(Node contextPublisherNode) {

		// Get the uri
		uri = CommonXmlParserUtils
				.getAttributeValueAsString(
						contextPublisherNode,
						IContextPublisherGroundingXmlConstants.CONTEXT_PUBLISHER_GROUNDING_ATTRIBUTE_URI);

		// Get the version
		version = CommonXmlParserUtils
				.getAttributeValueAsString(
						contextPublisherNode,
						IContextPublisherGroundingXmlConstants.CONTEXT_PUBLISHER_GROUNDING_ATTRIBUTE_VERSION);

		// Populate the actions
		populateActions(contextPublisherNode);
	}

	public byte[] serialize() {
		return SerializeUtils.serializeObject(this);
	}

	public static ContextPublisherGroundingXmlObj deserialize(
			byte[] objAsBytesArr) {
		return (ContextPublisherGroundingXmlObj) SerializeUtils
				.deserializeObject(objAsBytesArr);
	}

	private void populateActions(Node contextPublisherNode) {
		// Get the actions element
		Node actionsNode = CommonXmlParserUtils.getSingleChildNode(
				contextPublisherNode,
				IContextPublisherGroundingXmlConstants.ACTIONS_ELEMENT);

		// Get the action list
		List<Node> actionsChileNodes = CommonXmlParserUtils.getChildNodes(
				actionsNode,
				IContextPublisherGroundingXmlConstants.ACTION_ELEMENT);

		for (Node curActionNode : actionsChileNodes) {
			actions.add(new ActionXmlObj(curActionNode));
		}
	}

	public String getUri() {
		return uri;
	}

	public String getVersion() {
		return version;
	}

	public List<ActionXmlObj> getActions() {
		return actions;
	}

	public ActionXmlObj getActionByName(String contextPublisherAction) {
		ActionXmlObj foundAction = null;
		for (ActionXmlObj action : actions) {
			if (action.getAndroidAction().equals(contextPublisherAction)) {
				foundAction = action;
				break;
			}
		}

		return foundAction;
	}
}
