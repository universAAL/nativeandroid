/*
        Copyright 2011-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
        Fraunhofer-Gesellschaft - Institute for Computer Graphics Research

        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.android.container;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;

import android.util.Log;

//TODO change name to uAALAndroidContext
/**
 * An implementation of the concept of {@link ModuleContext} for OSGi.
 * 
 * @author mtazari
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 * 
 */
public class uAALBundleContext implements ModuleContext {
	private ContextEmulator bundle;
	private Hashtable extension = new Hashtable();
	private static BundleConfigHome servicesConfHome = new BundleConfigHome(
			"services");
	private ArrayList confFiles = new ArrayList(2);
	private String bundleId="mw.modules.aalspace.osgi";//A default just in case?...

	uAALBundleContext(ContextEmulator bc, String id) {
		bundle = bc;
		bundleId=id;
	}
	
	uAALBundleContext(ContextEmulator bc) {
		bundle = bc;
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#canBeStarted(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean canBeStarted(ModuleContext requester) {
		// TODO check permissions
		return true; //TODO Disabled for now
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#canBeStopped(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean canBeStopped(ModuleContext requester) {
		// TODO check permissions
		return true; //TODO Disabled for now
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#canBeUninstalled(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean canBeUninstalled(ModuleContext requester) {
		// TODO check permissions
		return true; //TODO Disabled for now
	}

	public Object fetchObject(String className) {
		return (className == null) ? null : bundle.getService(className);
	}

	public Object[] fetchObject(String className, String filter) {
		return (className == null) ? null : bundle.getServices(className, filter);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attrName) {
		return (attrName == null) ? null : extension.get(attrName);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#getContainer()
	 */
	public Container getContainer() {
		return uAALBundleContainer.THE_CONTAINER;
	}

	public String getID() {
		return bundleId;
	}

	public uAALBundleContext installBundle(String location) {
		return null; //TODO Deploy management disabled for now
	}

	public uAALBundleContext installBundle(String location, InputStream is) {
		return null; //TODO Disabled for now
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#listConfigFiles(org.universAAL.middleware.container.ModuleContext)
	 */
	public File[] listConfigFiles(ModuleContext requester) {
		// TODO check permissions
		int n = confFiles.size();
		File[] files = new File[n];
		for (int i = 0; i < n; i++)
			files[i] = (File) ((Object[]) confFiles.get(i))[0];
		return files;
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logDebug(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logDebug(String tag, String message, Throwable t) {
		Log.d(tag, message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logError(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logError(String tag, String message, Throwable t) {
		Log.e(tag, message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logInfo(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logInfo(String tag, String message, Throwable t) {
		Log.i(tag, message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logWarn(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logWarn(String tag, String message, Throwable t) {
		Log.w(tag, message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#logTrace(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void logTrace(String tag, String message, Throwable t) {
		Log.v(tag, message, t);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#registerConfigFile(java.lang.Object[])
	 */
	public void registerConfigFile(Object[] configFileParams) {
		// TODO define a convention for the array param
		// current assumption: 1st param @ index 0 is the
		// org.osgi.framework.Constants.SERVICE_PID
		// chosen for a org.osgi.service.cm.ManagedService (type = String)
		// possible extensions:
		// 2nd param @ index 1: help string describing the role of the property
		// file indirectly specified by the first first param @ index 0
		// 3rd param @ index 2: a hash-table with allowed properties as keys and
		// a help string about each property as value
		if (configFileParams != null && configFileParams.length > 0) {
			configFileParams[0] = servicesConfHome
					.getPropFile(configFileParams[0].toString());
			confFiles.add(configFileParams);
		}
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(String attrName, Object attrValue) {
		if (attrName != null && attrValue != null)
			extension.put(attrName, attrValue);
	}

	public void shareObject(String xface, Object obj, Dictionary props) {
		bundle.registerService(xface, obj, props);
	}

	public void shareObject(String[] xface, Object obj, Dictionary props) {
		bundle.registerService(xface, obj, props);
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#start(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean start(ModuleContext requester) {
		return false; //TODO Deploy management disabled for now
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#stop(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean stop(ModuleContext requester) {
		return false; //TODO Deploy management disabled for now
	}

	/**
	 * @see org.universAAL.middleware.container.ModuleContext#uninstall(org.universAAL.middleware.container.ModuleContext)
	 */
	public boolean uninstall(ModuleContext requester) {
		return false; //TODO Deploy management disabled for now
	}

	public Object getProperty(String name) {

		Object value = getAttribute(name);
		if (value != null)
			return value;

		value = bundle.getProperty(name);
		if (value != null)
			return value;

		value = System.getProperty(name);
		if (value != null)
			return value;

		value = System.getenv(name);
		if (value != null)
			return value;

		return null;
	}

	public Object getProperty(String name, Object def) {
		Object value = getProperty(name);
		if (value == null)
			return def;
		return value;
	}

}
