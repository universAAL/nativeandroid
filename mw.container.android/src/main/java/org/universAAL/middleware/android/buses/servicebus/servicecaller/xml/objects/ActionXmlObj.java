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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.IServiceRequestGroundingXmlConstants;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ChangeEffectXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ICommonServiceBusXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.SerializeUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class ActionXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String androidAction;
	private String androidCategory;
	private String serviceJavaClass;
	private String androidReplyToAction;
	private String androidReplyToCategory;

	private List<ValueFilterXmlObj> valueFilters = new ArrayList<ValueFilterXmlObj>();
	private List<RequiredOutputXmlObj> requiredOutputs = new ArrayList<RequiredOutputXmlObj>();
	private List<ChangeEffectXmlObj> changeEffects = new ArrayList<ChangeEffectXmlObj>();

	public ActionXmlObj(Node actionNode) {
		// Populate the attributes
		androidAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IServiceRequestGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_ACTION);
		androidCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IServiceRequestGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_CATEGORY);
		serviceJavaClass = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IServiceRequestGroundingXmlConstants.ACTION_ATTRIBUTE_SERVICE_JAVA_CLASS);
		androidReplyToAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IServiceRequestGroundingXmlConstants.ACTION_ATTRIBUTE_REPLY_TO_ACTION,
						false);
		androidReplyToCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IServiceRequestGroundingXmlConstants.ACTION_ATTRIBUTE_REPLY_TO_CATEGORY,
						false);

		// Populate the value filters
		populateValueFilters(actionNode);

		// Populate the required outputs
		populateRequiredOutputs(actionNode);

		// Populate the change effects
		populateChangeEffects(actionNode);
	}

	public String getAndroidAction() {
		return androidAction;
	}

	public String getAndroidCategory() {
		return androidCategory;
	}

	public String getServiceJavaClass() {
		return serviceJavaClass;
	}

	public String getAndroidReplyToAction() {
		return androidReplyToAction;
	}

	public String getAndroidReplyToCategory() {
		return androidReplyToCategory;
	}

	public List<ValueFilterXmlObj> getValueFilters() {
		return valueFilters;
	}

	public List<RequiredOutputXmlObj> getRequiredOutputs() {
		return requiredOutputs;
	}

	public List<ChangeEffectXmlObj> getChangeEffects() {
		return changeEffects;
	}

	public byte[] serialize() {
		return SerializeUtils.serializeObject(this);
	}

	public static ActionXmlObj deserialize(byte[] objAsBytesArr) {
		return (ActionXmlObj) SerializeUtils.deserializeObject(objAsBytesArr);
	}

	private void populateValueFilters(Node actionNode) {
		// Get the ValueFilters node
		Node valueFiltersNode = CommonXmlParserUtils.getSingleChildNode(
				actionNode,
				IServiceRequestGroundingXmlConstants.VALUE_FILTERS_ELEMENT);

		// The value filters node is not mandatory, therefore if null was
		// returned, this means that no value filters are available for the
		// action
		if (null == valueFiltersNode) {
			return;
		}

		// Get the Value Filter children
		List<Node> valueFilterChildrenList = CommonXmlParserUtils
				.getChildNodes(
						valueFiltersNode,
						IServiceRequestGroundingXmlConstants.VALUE_FILTER_ELEMENT);
		for (Node curValueFilterChild : valueFilterChildrenList) {
			valueFilters.add(new ValueFilterXmlObj(curValueFilterChild));
		}
	}

	private void populateRequiredOutputs(Node actionNode) {
		// Get the required outputs node
		Node requiredOutputsNode = CommonXmlParserUtils.getSingleChildNode(
				actionNode,
				IServiceRequestGroundingXmlConstants.REQUIRED_OUTPUTS_ELEMENT);

		// The required outputs node is not mandatory, therefore if null was
		// returned, this means that no required output are available for the
		// action
		if (null == requiredOutputsNode) {
			return;
		}

		// Get the required output children
		List<Node> requiredOutputChildrenList = CommonXmlParserUtils
				.getChildNodes(
						requiredOutputsNode,
						IServiceRequestGroundingXmlConstants.REQUIRED_OUTPUT_ELEMENT);
		for (Node curRequiredOutputChild : requiredOutputChildrenList) {
			requiredOutputs
					.add(new RequiredOutputXmlObj(curRequiredOutputChild));
		}
	}

	private void populateChangeEffects(Node operationNode) {
		Node changeEffectsNode = CommonXmlParserUtils.getSingleChildNode(
				operationNode,
				ICommonServiceBusXmlConstants.CHANGE_EFFECTS_ELEMENT);

		// The change effects node is not mandatory, therefore if null was
		// returned, this means that no change effect is available for the
		// operation
		if (null == changeEffectsNode) {
			return;
		}

		// Get the ChangeEffect children
		List<Node> changeEffectChildrenList = CommonXmlParserUtils
				.getChildNodes(changeEffectsNode,
						ICommonServiceBusXmlConstants.CHANGE_EFFECT_ELEMENT);
		for (Node curChangeEffectChild : changeEffectChildrenList) {
			changeEffects.add(new ChangeEffectXmlObj(curChangeEffectChild));
		}
	}
}
