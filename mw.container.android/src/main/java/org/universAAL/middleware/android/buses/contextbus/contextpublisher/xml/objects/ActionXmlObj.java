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
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class ActionXmlObj implements Serializable {

	private static final long serialVersionUID = 1L;

	private String androidAction;
	private String androidCategory;
	private SubjectXmlObj subject;
	private PredicateXmlObj predicate;
	private ObjectXmlObj object;

	public ActionXmlObj(Node actionNode) {
		// Populate the attributes
		androidAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IContextPublisherGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_ACTION);
		androidCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IContextPublisherGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_CATEGORY);

		// Populate the Subject
		populateSubject(actionNode);

		// Populate the Predicate
		populatePredicate(actionNode);

		// Populate the Object
		populateObject(actionNode);
	}

	private void populateSubject(Node actionNode) {
		// Get the Subject node
		Node subjectNode = CommonXmlParserUtils.getSingleChildNode(actionNode,
				IContextPublisherGroundingXmlConstants.SUBJECT_ELEMENT);

		subject = new SubjectXmlObj(subjectNode);
	}

	private void populatePredicate(Node actionNode) {
		// Get the Predicate node
		Node predicateNode = CommonXmlParserUtils.getSingleChildNode(
				actionNode,
				IContextPublisherGroundingXmlConstants.PREDICATE_ELEMENT);

		predicate = new PredicateXmlObj(predicateNode);
	}

	private void populateObject(Node actionNode) {
		// Get the Object node
		Node objectNode = CommonXmlParserUtils.getSingleChildNode(actionNode,
				IContextPublisherGroundingXmlConstants.OBJECT_ELEMENT);

		object = new ObjectXmlObj(objectNode);
	}

	public String getAndroidAction() {
		return androidAction;
	}

	public String getAndroidCategory() {
		return androidCategory;
	}

	public SubjectXmlObj getSubject() {
		return subject;
	}

	public PredicateXmlObj getPredicate() {
		return predicate;
	}

	public ObjectXmlObj getObject() {
		return object;
	}
}
