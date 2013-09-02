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
package org.universAAL.middleware.android.buses.common;

import java.io.File;

import org.universAAL.middleware.android.modules.ModulesService;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;

import dalvik.system.DexClassLoader;

import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jul 1, 2012
 * 
 */
public class AndroidOntologyManagement {

	private final static String TAG = AndroidOntologyManagement.class
			.getCanonicalName();

	private final static OntologyManagement ontologyManagement = OntologyManagement
			.getInstance();
	
	//TODO: specify folder another way?
	private final static File ontFolder = new File(Environment.getExternalStorageDirectory()
			.getPath(), "/data/felix/ontologies/");

	synchronized public static void registerOntology(ModuleContext context,
			Ontology ontology) {
		ontologyManagement.register(context, ontology);

		logRegisteredOntologies(ontology.getClass().getCanonicalName());
	}

	public static void registerOntology(ModuleContext context,
			String ontologyClass) {
		Ontology ontology;
		try {
			// ontology = (Ontology) Class.forName(ontologyClass).newInstance();
			// reflection from external ont jar instead
			DexClassLoader cl=getOntLoader(ontologyClass);
			ontology = (Ontology) Class.forName(ontologyClass, true, cl)
					.newInstance();
		} catch (Throwable th) {
			Log.e(TAG, "Unable to instantiate ontology class [" + ontologyClass
					+ "] due to :" + th,th);
			return;
		}
		registerOntology(context, ontology);
	}

	private static void logRegisteredOntologies(String lastRegisteredOntology) {
		StringBuffer sb = new StringBuffer();

		// Print the last registered ontology
		sb.append("Last registered ontology [" + lastRegisteredOntology + "]\n");

		// Extract all registered ontologies
		int i = 1;
		for (String currentRegisteredOntology : ontologyManagement
				.getOntoloyURIs()) {
			sb.append("\t Registered ontology no." + (i++) + ":"
					+ currentRegisteredOntology + "\n");
		}

		Log.w(TAG, sb.toString());
	}
	
	//TODO Move this method to another class? ReflectionUtils?
	public static DexClassLoader getOntLoader(String ontClass) {
		// With this new classloading strategy we can reflect ontology
		// classes from a dexed jar in a folder in sdcard
		File[] files = ontFolder.listFiles(); // folder where onts are
		StringBuilder names = new StringBuilder();
		for (int i = 0; i < files.length; i++) {
			names.append(ontFolder.getAbsolutePath() + "/" + files[i].getName());
			if (i + 1 < files.length) {
				names.append(File.pathSeparator);
			}// avoid : in the end
		}
		// cache folder in internal storage
		final File optimizedDexOutputPath = ModulesService.ontCacheFile;
		// class loader that can load ont jars
		DexClassLoader cl = new DexClassLoader(names.toString(),
				optimizedDexOutputPath.getAbsolutePath(), null,
				AndroidOntologyManagement.class.getClassLoader());
		return cl;
	}
}
