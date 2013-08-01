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
package org.universAAL.middleware.android.buses.common.xml.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.common.CommonXmlParserUtils;
import org.w3c.dom.Node;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jul 2, 2012
 * 
 */
public class OntologiesContainedXml implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<OntologyXmlObj> ontologies = new ArrayList<OntologyXmlObj>();

	public OntologiesContainedXml(Node nodeContainsOntologies) {

		// Populate the ontologies
		populateOntologies(nodeContainsOntologies);
	}

	public String[] getOntologies() {
		String[] ontologiesAsStrings = new String[ontologies.size()];

		for (int i = 0; i < ontologies.size(); i++) {
			ontologiesAsStrings[i] = ontologies.get(i).getJavaClass();
		}

		return ontologiesAsStrings;
	}

	private void populateOntologies(Node nodeContainsOntologies) {
		// Get the ontologies element
		Node ontologiesNode = CommonXmlParserUtils.getSingleChildNode(
				nodeContainsOntologies, ICommonXmlConstants.ONTOLOGIES_ELEMENT);

		// Get the ontologies list
		List<Node> ontologiesChileNodes = CommonXmlParserUtils.getChildNodes(
				ontologiesNode, ICommonXmlConstants.ONTOLOGY_ELEMENT);

		for (Node curOntologyNode : ontologiesChileNodes) {
			ontologies.add(new OntologyXmlObj(curOntologyNode));
		}
	}

}
