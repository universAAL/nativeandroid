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
package org.universAAL.middleware.android.buses.servicebus.servicecallee.xml;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public interface IServiceGroundingXmlConstants {
	// Service
	public final static String SERVICE_GROUNDING_ELEMENT = "ServiceGrounding";
	public final static String SERVICE_GROUNDING_ATTRIBUTE_URI = "uri";
	public final static String SERVICE_GROUNDING_ATTRIBUTE_SUPER_JAVA_CLASS = "superJavaClass";
	public final static String SERVICE_GROUNDING_ATTRIBUTE_VERSION = "version";

	// Operations
	public final static String OPERATIONS_ELEMENT = "Operations";

	// Operation
	public final static String OPERATION_ELEMENT = "Operation";
	public final static String OPERATION_ATTRIBUTE_URI = "uri";
	public final static String OPERATION_ATTRIBUTE_ANDROID_ACTION = "androidAction";
	public final static String OPERATION_ATTRIBUTE_ANDROID_CATEGORY = "androidCategory";
	public final static String OPERATION_ATTRIBUTE_REPLY_TO_ACTION = "androidReplyToActionExtraParameter";
	public final static String OPERATION_ATTRIBUTE_REPLY_TO_CATEGORY = "androidReplyToCategoryExtraParameter";

	// IO (common between filtering inputs-outputs)
	public final static String IO_ATTRIBUTE_URI = "uri";
	public final static String IO_ATTRIBUTE_ANDROID_NAME = "androidName";

	// Filtering Inputs
	public final static String FILTERING_INPUTS_ELEMENT = "FilteringInputs";

	// Filtering Input
	public final static String FILTERING_INPUT_ELEMENT = "FilteringInput";

	// Outputs
	public final static String OUTPUTS_ELEMENT = "Outputs";

	// Output
	public final static String OUTPUT_ELEMENT = "Output";
	public final static String OUTPUT_ATTRIBUTE_JAVA_CLASS = "javaClass";
	public final static String OUTPUT_ATTRIBUTE_MIN_CARDINALITY = "minCardinality";
	public final static String OUTPUT_ATTRIBUTE_MAX_CARDINALITY = "maxCardinality";

	// Filtering
	public final static String FILTERING_ELEMENT = "Filtering";
	public final static String FILTERING_ATTRIBUTE_CLASS_URI = "classUri";
	public final static String FILTERING_ATTRIBUTE_MIN_CARDINALITY = "minCardinality";
	public final static String FILTERING_ATTRIBUTE_MAX_CARDINALITY = "maxCardinality";
}
