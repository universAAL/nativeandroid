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

import java.io.File;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;

import android.util.Log;

/**
 * 
 * @author <a href="mailto:vadime@il.ibm.com"> vadime </a>
 * 
 */
public class AndroidModuleContext implements ModuleContext {
    Container container = new AndroidContainer();

    public boolean canBeStarted(ModuleContext arg0) {
	return false;
    }

    public boolean canBeStopped(ModuleContext arg0) {
	return false;
    }

    public boolean canBeUninstalled(ModuleContext arg0) {
	return false;
    }

    public Object getAttribute(String arg0) {
	return null;
    }

    public Container getContainer() {
	return container;
    }

    public String getID() {
	return null;
    }

    public File[] listConfigFiles(ModuleContext arg0) {
	return null;
    }

    public void logDebug(String tag, String message, Throwable t) {
	Log.d(tag, message, t);
    }

    public void logError(String tag, String message, Throwable t) {
	Log.e(tag, message, t);
    }

    public void logInfo(String tag, String message, Throwable t) {
	Log.i(tag, message, t);
    }

    public void logTrace(String tag, String message, Throwable t) {
	// there is no trace in Android log, using verbose instead
	Log.v(tag, message, t);
    }

    public void logWarn(String tag, String message, Throwable t) {
	Log.w(tag, message, t);
    }

    public void registerConfigFile(Object[] arg0) {
    }

    public void setAttribute(String arg0, Object arg1) {
    }

    public boolean start(ModuleContext arg0) {
	return false;
    }

    public boolean stop(ModuleContext arg0) {
	return false;
    }

    public boolean uninstall(ModuleContext arg0) {
	return false;
    }

	public Object getProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty(String name, Object def) {
		// TODO Auto-generated method stub
		return null;
	}
}
