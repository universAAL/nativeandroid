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
package org.universAAL.middleware.android.common.modulecontext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;

/**
 * 
 * @author <a href="mailto:vadime@il.ibm.com"> vadime </a>
 * 
 */
public class AndroidContainer implements Container {
    Map sharedObjects = new HashMap();

    public Object fetchSharedObject(ModuleContext context, Object[] parameters) {
	if (parameters.length != 1) {
	    throw new IllegalArgumentException("parameters size not equal 0"
		    + " for DefaultContainer.fetchSharedObject()");
	}
	return sharedObjects.get(parameters[0]);
    }

    public Object[] fetchSharedObject(ModuleContext arg0, Object[] arg1, SharedObjectListener arg2) {
	return null;
    }

    public ModuleContext installModule(ModuleContext arg0, Object[] arg1) {
	return null;
    }

    public Iterator logListeners() {
	return Collections.emptyList().iterator();
    }

    public ModuleContext registerModule(Object[] arg0) {
	return null;
    }

    public void shareObject(ModuleContext context, Object sharedObject, Object[] parameters) {
	if (parameters.length != 1) {
	    throw new IllegalArgumentException("parameters size not equal 0"
		    + " for DefaultContainer.fetchSharedObject()");
	}
	sharedObjects.put(parameters[0], sharedObject);
    }

	public void removeSharedObjectListener(SharedObjectListener listener) {
		// TODO Auto-generated method stub
		
	}
}
