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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.IContextSubscriberGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
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
	private String predicatePropName;

	private List<IRestrictionXmlObj> restrictions = new ArrayList<IRestrictionXmlObj>();
	private List<SubjectFilterXmlObj> subjectFilters = new ArrayList<SubjectFilterXmlObj>();
	private ObjectXmlObj object;

	public ActionXmlObj(Node actionNode) {
		// Populate the attributes
		androidAction = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IContextSubscriberGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_ACTION);
		androidCategory = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IContextSubscriberGroundingXmlConstants.ACTION_ATTRIBUTE_ANDROID_CATEGORY);
		predicatePropName = CommonXmlParserUtils
				.getAttributeValueAsString(
						actionNode,
						IContextSubscriberGroundingXmlConstants.ACTION_ATTRIBUTE_PREDICATE_PROP_NAME);

		// Populate the Restrictions
		populateRestrictions(actionNode);

		// Populate the subject
		populateSubjectFilters(actionNode);

		// Populate the object
		populateObject(actionNode);
	}

	private void populateRestrictions(Node actionNode) {
		// Get the restrictions element
		Node restrictionsNode = CommonXmlParserUtils.getSingleChildNode(
				actionNode,
				IContextSubscriberGroundingXmlConstants.RESTRICTIONS_ELEMENT);

		for (RestrictionType restrictionType : RestrictionType.values()) {
			List<Node> restrictionNodes = CommonXmlParserUtils.getChildNodes(
					restrictionsNode, restrictionType.toString());

			for (Node curRestrictionNode : restrictionNodes) {
				restrictions.add(RestrictionFactory.createRestriction(
						restrictionType, curRestrictionNode));
			}
		}
	}

	private void populateSubjectFilters(Node actionNode) {
		// Get the subject filters element
		Node subjectFiltersNode = CommonXmlParserUtils
				.getSingleChildNode(
						actionNode,
						IContextSubscriberGroundingXmlConstants.SUBJECT_FILTERS_ELEMENT);

		List<Node> subjectFilterNodes = CommonXmlParserUtils.getChildNodes(
				subjectFiltersNode,
				IContextSubscriberGroundingXmlConstants.SUBJECT_FILTER_ELEMENT);

		for (Node subjectFilterNode : subjectFilterNodes) {
			subjectFilters.add(new SubjectFilterXmlObj(subjectFilterNode));
		}
	}

	private void populateObject(Node actionNode) {
		// Get the Object node
		Node objectNode = CommonXmlParserUtils.getSingleChildNode(actionNode,
				IContextSubscriberGroundingXmlConstants.OBJECT_ELEMENT);

		object = new ObjectXmlObj(objectNode);
	}

	public String getAndroidAction() {
		return androidAction;
	}

	public String getAndroidCategory() {
		return androidCategory;
	}

	public String getPredicatePropName() {
		return predicatePropName;
	}

	public List<IRestrictionXmlObj> getRestrictions() {
		return restrictions;
	}

	public List<SubjectFilterXmlObj> getSubjectFilters() {
		return subjectFilters;
	}

	public ObjectXmlObj getObject() {
		return object;
	}
}
