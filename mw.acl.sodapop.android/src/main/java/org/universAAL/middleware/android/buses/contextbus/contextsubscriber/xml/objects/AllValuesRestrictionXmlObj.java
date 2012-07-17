/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
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

import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.IContextSubscriberGroundingXmlConstants;
import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.universAAL.middleware.owl.MergedRestriction;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public class AllValuesRestrictionXmlObj extends AbstractRestrictionXmlObj {

    private static final long serialVersionUID = 1L;

    private String typeUri;

    public AllValuesRestrictionXmlObj(Node restrictionNode) {
	super(restrictionNode);

	// Populate the attributes
	typeUri = CommonXmlParserUtils.getAttributeValueAsString(restrictionNode,
		IContextSubscriberGroundingXmlConstants.ALL_VALUES_RESTRICTIONS_ATTRIBUTE_TYPE_URI);
    }

    public String getTypeUri() {
	return typeUri;
    }

    public MergedRestriction populateRestriction(MergedRestriction mergedRestriction) {
	MergedRestriction localMergedRestriction = MergedRestriction.getAllValuesRestriction(
		propertyUri, typeUri);
	return (null == mergedRestriction) ? localMergedRestriction : mergedRestriction
		.addRestriction(localMergedRestriction);
    }
}
