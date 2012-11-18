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
package org.universAAL.middleware.android.buses.servicebus.servicecaller.xml;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public interface IServiceRequestGroundingXmlConstants {
    // Service Request
    public final static String SERVICE_REQUEST_GROUNDING_ELEMENT = "ServiceRequestGrounding";
    public final static String SERVICE_REQUEST_GROUNDING_ATTRIBUTE_VERSION = "version";

    // Actions
    public final static String ACTIONS_ELEMENT = "Actions";

    // Action
    public final static String ACTION_ELEMENT = "Action";
    public final static String ACTION_ATTRIBUTE_ANDROID_ACTION = "androidAction";
    public final static String ACTION_ATTRIBUTE_ANDROID_CATEGORY = "androidCategory";
    public final static String ACTION_ATTRIBUTE_SERVICE_JAVA_CLASS = "serviceJavaClass";
    public final static String ACTION_ATTRIBUTE_REPLY_TO_ACTION = "androidReplyToActionExtraParameter";
    public final static String ACTION_ATTRIBUTE_REPLY_TO_CATEGORY = "androidReplyToCategoryExtraParameter";

    // Value Filters
    public final static String VALUE_FILTERS_ELEMENT = "ValueFilters";

    // Value Filter
    public final static String VALUE_FILTER_ELEMENT = "ValueFilter";

    // Required Outputs
    public final static String REQUIRED_OUTPUTS_ELEMENT = "RequiredOutputs";

    // Required Output
    public final static String REQUIRED_OUTPUT_ELEMENT = "RequiredOutput";
    public final static String REQUIRED_OUTPUT_ATTRIBUTE_URI = "uri";

    // Common between value filters-required outputs
    public final static String PARAMETER_ATTRIBUTE_ANDROID_EXTRA_PARAMETER = "androidExtraParameter";
    public final static String PARAMETER_ATTRIBUTE_ANDROID_EXTRA_PARAMETER_JAVA_CLASS = "androidExtraParameterJavaClass";
    public final static String PARAMETER_ATTRIBUTE_JAVA_CLASS = "javaClass";
}
