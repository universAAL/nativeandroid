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
package org.universAAL.middleware.android.common;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 15, 2012
 * 
 */
public class CommonXmlParserUtils {

	public static Node getSingleNode(Document document, String elementName) {
		NodeList nodes = document.getElementsByTagName(elementName);

		verifySingleItemInNodesList(nodes);

		return nodes.item(0);
	}

	public static Node getSingleChildNode(Node node, String elementName) {
		return getSingleChildNode(node, elementName, false);
	}

	public static Node getSingleChildNode(Node node, String elementName,
			boolean isMandatory) {
		Node foundNode = null;

		List<Node> foundNodes = getChildNodes(node, elementName);

		if (null != foundNodes && !foundNodes.isEmpty()) {
			foundNode = foundNodes.get(0);
		}
		if (isMandatory || foundNodes.size() != 1) {
			// TODO: throw exception if the nodes list doesn't contain a single
			// item
		}
		return foundNode;
	}

	public static List<Node> getChildNodes(Node node, String elementName) {
		NodeList nodes = node.getChildNodes();
		List<Node> foundNodes = new ArrayList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node curNode = nodes.item(i);
			if (elementName.equals(curNode.getNodeName())) {
				foundNodes.add(curNode);
			}
		}

		return foundNodes;
	}

	public static String getAttributeValueAsString(Node node,
			String attributeName) {
		return getAttributeValueAsString(node, attributeName, true);
	}

	public static String getAttributeValueAsString(Node node,
			String attributeName, boolean isMandatory) {
		if (isMandatory) {
			verifyAttributeExistence(node, attributeName);
		}
		Node foundAttribute = node.getAttributes().getNamedItem(attributeName);
		String attributeValue = foundAttribute != null ? foundAttribute
				.getNodeValue() : null;

		return attributeValue;
	}

	public static int getAttributeValueAsInt(Node node, String attributeName) {
		String attributeValue = getAttributeValueAsString(node, attributeName);

		return Integer.parseInt(attributeValue);
	}

	public static String getNodeContent(Node node) {
		String nodeContent = "";
		Node child = node.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			nodeContent = cd.getData();
		}
		return nodeContent;
	}

	private static void verifySingleItemInNodesList(NodeList nodes) {
		if (null == nodes || nodes.getLength() != 0) {
			// TODO: throw exception if the nodes list doesn't contain a single
			// item
		}
	}

	private static void verifyAttributeExistence(Node node, String attributeName) {
		if (null == node.getAttributes()
				|| null == node.getAttributes().getNamedItem(attributeName)) {
			// TODO: throw exception if the nodes list doesn't contain a single
			// item
		}
	}
}
