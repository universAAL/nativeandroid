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
package org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.xml.objects.ContextSubscriberGroundingXmlObj;
import org.universAAL.middleware.android.common.CommonXmlParserUtils;
//import org.universAAL.middleware.android.common.xml.CommonXmlParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 15, 2012
 * 
 */
public class ContextSubscriberGroundingXmlMngr {

	public static ContextSubscriberGroundingXmlObj populateContextSubscriberGroundingXmlObjectFromXml(
			InputStream xmlIs) {
		ContextSubscriberGroundingXmlObj contextSubscriberXml = null;

		// Populate the document
		Document document = populateDocument(xmlIs);

		contextSubscriberXml = createContextSubscriberGroundingXmlObject(document);

		return contextSubscriberXml;
	}

	private static Document populateDocument(InputStream xmlIs) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			doc = db.parse(xmlIs);

		} catch (ParserConfigurationException e) { // TODO: throw exceptions
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	private static ContextSubscriberGroundingXmlObj createContextSubscriberGroundingXmlObject(
			Document document) {

		// Get the ContextSubscriber grounding node
		Node contextSubscriberGroundingNode = CommonXmlParserUtils
				.getSingleNode(
						document,
						IContextSubscriberGroundingXmlConstants.CONTEXT_SUBSCRIBER_GROUNDING_ELEMENT);

		// Initiate the ContextSubscriberGroundingXml object
		ContextSubscriberGroundingXmlObj contextSubscriberGroundingXml = new ContextSubscriberGroundingXmlObj(
				contextSubscriberGroundingNode);

		return contextSubscriberGroundingXml;
	}
}
