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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml;

import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.RestrictionType;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public interface IContextSubscriberGroundingXmlConstants {

    // Context Subscriber grounding
    public final static String CONTEXT_SUBSCRIBER_GROUNDING_ELEMENT = "ContextSubscriberGrounding";
    public final static String CONTEXT_SUBSCRIBER_GROUNDING_ATTRIBUTE_VERSION = "version";

    // Action
    public final static String ACTION_ELEMENT = "Action";
    public final static String ACTION_ATTRIBUTE_ANDROID_ACTION = "androidAction";
    public final static String ACTION_ATTRIBUTE_ANDROID_CATEGORY = "androidCategory";
    public final static String ACTION_ATTRIBUTE_PREDICATE_PROP_NAME = "predicatePropName";

    // Restrictions
    public final static String RESTRICTIONS_ELEMENT = "Restrictions";

    // Common Restrictions attributes
    public final static String RESTRICTIONS_COMMON_ATTRIBUTE_PROP_URI = "propertyUri";

    // AllValuesRestriction
    public final static String ALL_VALUES_RESTRICTIONS_ELEMENT = RestrictionType.AllValuesRestriction
	    .toString();
    public final static String ALL_VALUES_RESTRICTIONS_ATTRIBUTE_TYPE_URI = "typeUri";

    // FixedValuesRestriction
    public final static String FIXED_VALUES_RESTRICTIONS_ELEMENT = RestrictionType.FixedValueRestriction
	    .toString();

    // Value (will be inside 'FixedValuesRestrictions)
    public final static String VALUE_ELEMENT = "Value";
    public final static String VALUE_ATTRIBUTE_JAVA_CLASS = "javaClass";

    // SubjectFilters
    public final static String SUBJECT_FILTERS_ELEMENT = "SubjectFilters";

    // SubjectFilter
    public final static String SUBJECT_FILTER_ELEMENT = "SubjectFilter";
    public final static String SUBJECT_ATTRIBUTE_ANDROID_NAME = "androidName";

    // Object
    public final static String OBJECT_ELEMENT = "Object";
    public final static String OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM = "androidExtraParameter";
    public final static String OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM_JAVA_CLASS = "androidExtraParameterJavaClass";
}
