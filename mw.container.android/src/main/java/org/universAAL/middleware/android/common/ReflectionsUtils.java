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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.universAAL.middleware.android.buses.common.AndroidOntologyManagement;
import org.universAAL.middleware.owl.ManagedIndividual;

import android.util.Log;

import dalvik.system.DexClassLoader;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 20, 2012
 * 
 */
public class ReflectionsUtils {
	
	private final static String TAG = ReflectionsUtils.class
			.getCanonicalName();

	public static String extractMYURIFieldFromClass(String ontologyClass)
			throws Exception {
		String myURI = null;

		try {
			Class superClassOnt = Class.forName(ontologyClass);
			Field field = superClassOnt.getField("MY_URI");
			myURI = (String) field.get(superClassOnt);
		} catch (ClassNotFoundException ex) {
			// Try with ontology jars
			try {
				DexClassLoader cl = AndroidOntologyManagement
						.getOntLoader(ontologyClass);
				// reflection from ont jar
				ManagedIndividual superClassOnt = (ManagedIndividual) Class.forName(ontologyClass, true, cl)
						.newInstance();
//				Field field = superClassOnt.getField("MY_URI");
//				myURI = (String) field.get(superClassOnt);
				myURI = superClassOnt.getClassURI();
			} catch (Exception e) {
				String errMsg = "Unable to extract MY_URI field from class ["
						+ ontologyClass + "] due to [" + e.getMessage() + "]";
				Log.e(TAG, errMsg);
				throw new Exception(errMsg); //TODO another exception
			}
		} catch (Throwable th) {
			String errMsg = "Unable to extract MY_URI field from class ["
					+ ontologyClass + "] due to [" + th.getMessage() + "]";
			throw new Exception(errMsg); //TODO another exception
		}

		return myURI;
	}

	public static Constructor createCtorThatReceiveStringParam(String javaClass)
			throws Exception {
		Constructor ctor = null;
		try {
			Class theClass = Class.forName(javaClass);
			ctor = theClass.getConstructor(new Class[] { String.class });
		} catch (ClassNotFoundException ex) {
			// Try with ontology jars
			try {
				DexClassLoader cl = AndroidOntologyManagement
						.getOntLoader(javaClass);
				// reflection from ont jar
				ManagedIndividual theClass = (ManagedIndividual) Class.forName(javaClass, true, cl)
						.newInstance();
				ctor = theClass.getClass().getConstructor(new Class[] { String.class });
			} catch (Exception e) {
				String errMsg = "Error when populating (again) output object ["
						+ javaClass + "] due to [" + e.getMessage() + "]";
				Log.e(TAG, errMsg);
				throw new Exception(errMsg); //TODO another exception
			}
		} catch (Throwable th) {
			String errMsg = "Error when populating output object [" + javaClass
					+ "] due to [" + th.getMessage() + "]";
			throw new Exception(errMsg); //TODO another exception
		}

		return ctor;
	}

	public static Object invokeCtorWithStringParam(Constructor ctor,
			String value) throws Exception {
		try {
			return ctor.newInstance(new Object[] { value });
		} catch (Throwable th) {
			throw new Exception(
					"Unable to create c'tor that gets string due to ["
							+ th.getMessage() + "]");
		}
	}
}
