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
package org.universAAL.middleware.android.buses.contextbus.messages.handlers.tests;

import org.universAAL.middleware.android.buses.common.AndroidOntologyManagement;
import org.universAAL.middleware.context.owl.ContextBusOntology;
import org.universAAL.middleware.datarep.SharedResources;
import org.universAAL.middleware.owl.ManagedIndividual;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jul 1, 2012
 * 
 */
public class TestRegistration {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// AndroidModuleContextFactory.createModuleContext();

	try {
	    SharedResources.loadReasoningEngine();
	} catch (ClassNotFoundException e) {
	    Log.e("", "Error when loading Reasoning Engine [" + e.getMessage() + "]");
	}

	try {

	    String[] onts = new String[] { "org.universAAL.ontology.location.LocationOntology",
		    "org.universAAL.ontology.shape.ShapeOntology",
		    "org.universAAL.ontology.phThing.PhThingOntology",
		    "org.universAAL.ontology.space.SpaceOntology",
		    "org.universAAL.ontology.lighting.LightingOntology" };

	    ContextBusOntology contextOntology = new ContextBusOntology();
	    AndroidOntologyManagement.registerOntology(contextOntology);

	    for (String str : onts) {
		AndroidOntologyManagement.registerOntology(str);
	    }

	} catch (Throwable t) {
	    t.printStackTrace();
	}

	System.out.println(ManagedIndividual
		.isRegisteredClassURI("http://ontology.universaal.org/Lighting.owl#LightSource"));
    }
}
