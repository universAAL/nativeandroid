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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.IServiceGroundingXmlConstants;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ChangeEffectXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ICommonServiceBusXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public class OperationXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uri;
	private String androidAction;
	private String androidCategory;
	private String androidReplyToAction;
	private String androidReplyToCategory;
	private List<FilteringInputXmlObj> filteringInputs = new ArrayList<FilteringInputXmlObj>();
	private List<OutputXmlObj> outputs = new ArrayList<OutputXmlObj>();
	private List<ChangeEffectXmlObj> changeEffects = new ArrayList<ChangeEffectXmlObj>();

	public OperationXmlObj(Node operationNode) {
		// Populate the attributes
		uri = CommonXmlParserUtils.getAttributeValueAsString(operationNode,
				IServiceGroundingXmlConstants.OPERATION_ATTRIBUTE_URI);
		androidAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						operationNode,
						IServiceGroundingXmlConstants.OPERATION_ATTRIBUTE_ANDROID_ACTION);
		androidCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						operationNode,
						IServiceGroundingXmlConstants.OPERATION_ATTRIBUTE_ANDROID_CATEGORY);
		androidReplyToAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						operationNode,
						IServiceGroundingXmlConstants.OPERATION_ATTRIBUTE_REPLY_TO_ACTION,
						false);
		androidReplyToCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						operationNode,
						IServiceGroundingXmlConstants.OPERATION_ATTRIBUTE_REPLY_TO_CATEGORY,
						false);

		// Populate the filtering inputs
		populateFilteringInputs(operationNode);

		// Populate the outputs
		populateOutputs(operationNode);

		// Populate the change effects
		populateChangeEffects(operationNode);
	}

	public String getUri() {
		return uri;
	}

	public String getAndroidAction() {
		return androidAction;
	}

	public String getAndroidCategory() {
		return androidCategory;
	}

	public String getAndroidReplyToAction() {
		return androidReplyToAction;
	}

	public String getAndroidReplyToCategory() {
		return androidReplyToCategory;
	}

	public List<FilteringInputXmlObj> getFilteringInputs() {
		return filteringInputs;
	}

	public List<OutputXmlObj> getOutputs() {
		return outputs;
	}

	public List<ChangeEffectXmlObj> getChangeEffects() {
		return changeEffects;
	}

	private void populateFilteringInputs(Node operationNode) {
		// Get the inputs node
		Node inputsNode = CommonXmlParserUtils.getSingleChildNode(
				operationNode,
				IServiceGroundingXmlConstants.FILTERING_INPUTS_ELEMENT);

		// The inputs node is not mandatory, therefore if null was returned,
		// this means that no input is available for the operation
		if (null == inputsNode) {
			return;
		}

		// Get the input children
		List<Node> inputChildrenList = CommonXmlParserUtils.getChildNodes(
				inputsNode,
				IServiceGroundingXmlConstants.FILTERING_INPUT_ELEMENT);
		for (Node curInputChild : inputChildrenList) {
			filteringInputs.add(new FilteringInputXmlObj(curInputChild));
		}
	}

	private void populateOutputs(Node operationNode) {
		// Get the outputs node
		Node outputsNode = CommonXmlParserUtils.getSingleChildNode(
				operationNode, IServiceGroundingXmlConstants.OUTPUTS_ELEMENT);

		// The outputs node is not mandatory, therefore if null was returned,
		// this means that no output is available for the operation
		if (null == outputsNode) {
			return;
		}

		// Get the output children
		List<Node> outputChildrenList = CommonXmlParserUtils.getChildNodes(
				outputsNode, IServiceGroundingXmlConstants.OUTPUT_ELEMENT);
		for (Node curOutputChild : outputChildrenList) {
			outputs.add(new OutputXmlObj(curOutputChild));
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
