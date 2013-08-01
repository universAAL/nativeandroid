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
package org.universAAL.middleware.android.buses.contextbus.contextpublisher.xml;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 19, 2012
 * 
 */
public interface IContextPublisherGroundingXmlConstants {

	// Context Publisher grounding
	public final static String CONTEXT_PUBLISHER_GROUNDING_ELEMENT = "ContextPublisherGrounding";
	public final static String CONTEXT_PUBLISHER_GROUNDING_ATTRIBUTE_URI = "uri";
	public final static String CONTEXT_PUBLISHER_GROUNDING_ATTRIBUTE_VERSION = "version";

	// Actions
	public final static String ACTIONS_ELEMENT = "Actions";

	// Action
	public final static String ACTION_ELEMENT = "Action";
	public final static String ACTION_ATTRIBUTE_ANDROID_ACTION = "androidAction";
	public final static String ACTION_ATTRIBUTE_ANDROID_CATEGORY = "androidCategory";

	// Subject
	public final static String SUBJECT_ELEMENT = "Subject";
	public final static String SUBJECT_ATTRIBUTE_URI = "uri";
	public final static String SUBJECT_ATTRIBUTE_ANDROID_NAME = "androidName";

	// Predicate
	public final static String PREDICATE_ELEMENT = "Predicate";
	public final static String PREDICATE_ATTRIBUTE_URI = "uri";

	// Object
	public final static String OBJECT_ELEMENT = "Object";
	public final static String OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM = "androidExtraParameter";
	public final static String OBJECT_ATTRIBUTE_ANDROID_EXTRA_PARAM_JAVA_CLASS = "androidExtraParameterJavaClass";

}
