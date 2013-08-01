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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects;

import org.universAAL.middleware.android.buses.common.xml.objects.OntologiesContainedXml;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.IContextSubscriberGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.SerializeUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class ContextSubscriberGroundingXmlObj extends OntologiesContainedXml {

	private static final long serialVersionUID = 1L;

	private String version;

	private ActionXmlObj action;

	public ContextSubscriberGroundingXmlObj(Node contextSubscriberNode) {

		super(contextSubscriberNode);

		// Get the version
		version = CommonXmlParserUtils
				.getAttributeValueAsString(
						contextSubscriberNode,
						IContextSubscriberGroundingXmlConstants.CONTEXT_SUBSCRIBER_GROUNDING_ATTRIBUTE_VERSION);

		// Populate the actions
		populateAction(contextSubscriberNode);
	}

	public byte[] serialize() {
		return SerializeUtils.serializeObject(this);
	}

	public static ContextSubscriberGroundingXmlObj deserialize(
			byte[] objAsBytesArr) {
		return (ContextSubscriberGroundingXmlObj) SerializeUtils
				.deserializeObject(objAsBytesArr);
	}

	public String getVersion() {
		return version;
	}

	public ActionXmlObj getAction() {
		return action;
	}

	private void populateAction(Node contextSubscriberNode) {
		// Get the actions element
		Node actionNode = CommonXmlParserUtils.getSingleChildNode(
				contextSubscriberNode,
				IContextSubscriberGroundingXmlConstants.ACTION_ELEMENT);

		action = new ActionXmlObj(actionNode);
	}
}
