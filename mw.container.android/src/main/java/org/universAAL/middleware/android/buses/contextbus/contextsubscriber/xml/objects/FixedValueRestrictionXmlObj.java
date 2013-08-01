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

import java.lang.reflect.Constructor;

import org.universAAL.middleware.android.buses.common.xml.objects.ValueXmlObj;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.IContextSubscriberGroundingXmlConstants;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.universAAL.middleware.android.common.IllegalGroundingFormat;
import org.universAAL.middleware.android.common.ReflectionsUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.universAAL.middleware.owl.MergedRestriction;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class FixedValueRestrictionXmlObj extends AbstractRestrictionXmlObj {

	private static final long serialVersionUID = 1L;

	private ValueXmlObj value;

	public FixedValueRestrictionXmlObj(Node restrictionNode) {
		super(restrictionNode);

		// Populate the value
		populateValue(restrictionNode);
	}

	private void populateValue(Node restrictionNode) {
		// Get the Value node
		Node valueNode = CommonXmlParserUtils.getSingleChildNode(
				restrictionNode,
				IContextSubscriberGroundingXmlConstants.VALUE_ELEMENT);

		value = new ValueXmlObj(valueNode);
	}

	public ValueXmlObj getValue() {
		return value;
	}

	public MergedRestriction populateRestriction(
			MergedRestriction mergedRestriction) throws IllegalGroundingFormat {

		Constructor ctor = null;
		Object obj = null;

		try {
			ctor = ReflectionsUtils.createCtorThatReceiveStringParam(value
					.getJavaClass());
			obj = ReflectionsUtils.invokeCtorWithStringParam(ctor,
					value.getValue());

			MergedRestriction localMergedRestriction = MergedRestriction
					.getFixedValueRestriction(propertyUri, obj);

			return (null == mergedRestriction) ? localMergedRestriction
					: mergedRestriction.addRestriction(localMergedRestriction);
		} catch (Throwable th) {
			throw new IllegalGroundingFormat(
					"Unable to add restrictions due to [" + th.getMessage()
							+ "]");
		}
	}
}
