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
package org.universAAL.middleware.android.buses.common;

import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jul 1, 2012
 * 
 */
public class AndroidOntologyManagement {

    private final static String TAG = AndroidOntologyManagement.class.getCanonicalName();

    private final static OntologyManagement ontologyManagement = OntologyManagement.getInstance();

    synchronized public static void registerOntology(Ontology ontology) {
	ontologyManagement.register(ontology);

	logRegisteredOntologies(ontology.getClass().getCanonicalName());
    }

    public static void registerOntology(String ontologyClass) {
	Ontology ontology;
	try {
	    ontology = (Ontology) Class.forName(ontologyClass).newInstance();
	} catch (Throwable th) {
	    Log.e(TAG,
		    "Unable to instantiate ontology class [" + ontologyClass + "] due to ["
			    + th.getMessage() + "]");
	    return;
	}
	registerOntology(ontology);
    }
    
    public static void testRegisterOntology(String ontologyClass) throws Exception {
	Ontology ontology;
	    ontology = (Ontology) Class.forName(ontologyClass).newInstance();
	registerOntology(ontology);
    }

    @SuppressWarnings("unused")
    private static void logRegisteredOntologies(String lastRegisteredOntology) {
	StringBuffer sb = new StringBuffer();

	// Print the last registered ontology
	sb.append("Last registered ontology [" + lastRegisteredOntology + "]\n");

	// Extract all registered ontologies
	int i = 1;
	for (String currentRegisteredOntology : ontologyManagement.getOntoloyURIs()) {
	    sb.append("\t Registered ontology no." + (i++) + ":" + currentRegisteredOntology + "\n");
	}

	Log.w(TAG, sb.toString());
    }
}
