package org.universAAL.android.container;

import java.io.File;
import java.util.Dictionary;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;

import ch.ethz.iks.slp.impl.PlatformAbstraction;
import ch.ethz.iks.slp.impl.filter.Filter;

import android.util.Log;

public class AndroidContext implements ModuleContext, PlatformAbstraction{
	// In android we do not have bundles so lets use the same context for all.
	// In the end it is just a facade for container-specific methods which in
	// android we are never going to use.
	public static final AndroidContext THE_CONTEXT = new AndroidContext();
	// TODO allow enable/disable the log of jslp
	private static boolean JSLP_LOG=false;
	private static final String DEFAULT_TAG = "Default Tag";
	private String id="mw.modules.aalspace.osgi";
	
	public AndroidContext(String id){
		this.id=id;
	}
	
	public AndroidContext(){
	}
	
	public Container getContainer() {
		return AndroidContainer.THE_CONTAINER;
	}

	public String getID() {
		// TODO This is (!?) used in jgroups to describe channels, so it must have a specific value per instance...
		return id;
//		return "mw.container.android";//TODO this was in theory not used anymore.. but it is
	}

	public boolean canBeStarted(ModuleContext requester) {
		return true;
	}

	public boolean canBeStopped(ModuleContext requester) {
		return true;
	}

	public boolean canBeUninstalled(ModuleContext requester) {
		return true;
	}
	
	public boolean start(ModuleContext requester) {
		return false;
	}

	public boolean stop(ModuleContext requester) {
		return false;
	}

	public boolean uninstall(ModuleContext requester) {
		return false;
	}

	public Object getAttribute(String attrName) {
		// TODO Doesnt seem that anybody uses this
		return null;
	}
	
	public void setAttribute(String attrName, Object attrValue) {
		// TODO Doesnt seem that anybody uses this
	}

	public Object getProperty(String name) {
		// TODO Doesnt seem that anybody uses this
		return System.getProperty(name);
	}

	public Object getProperty(String name, Object def) {
		// TODO Doesnt seem that anybody uses this
		Object value = getProperty(name);
		if (value == null)
			return def;
		return value;
	}

	public void registerConfigFile(Object[] configFileParams) {
		// TODO This is never going to be used in android
	}

	public File[] listConfigFiles(ModuleContext requester) {
		// TODO This is never going to be used in android
		return new File[0];
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

	public void logWarn(String tag, String message, Throwable t) {
		Log.w(tag, message, t);
	}

	public void logTrace(String tag, String message, Throwable t) {
		Log.v(tag, message, t);
	}

	public void logDebug(String message) {
		if(JSLP_LOG)logDebug(DEFAULT_TAG,message,null);
	}

	public void logDebug(String message, Throwable exception) {
		if(JSLP_LOG)logDebug(DEFAULT_TAG,message,exception);
	}

	public void logTraceMessage(String string) {
		if(JSLP_LOG)logTrace(DEFAULT_TAG, string, null);
	}

	public void logTraceReg(String string) {
		if(JSLP_LOG)logTrace(DEFAULT_TAG, string, null);
	}

	public void logTraceDrop(String string) {
		if(JSLP_LOG)logTrace(DEFAULT_TAG, string, null);
	}

	public void logWarning(String message) {
		if(JSLP_LOG)logWarn(DEFAULT_TAG, message, null);
	}

	public void logWarning(String message, Throwable exception) {
		if(JSLP_LOG)logWarn(DEFAULT_TAG, message, exception);
	}

	public void logError(String message) {
		if(JSLP_LOG)logError(DEFAULT_TAG, message, null);
	}

	public void logError(String message, Throwable exception) {
		if(JSLP_LOG)logError(DEFAULT_TAG, message, exception);
	}

	public Filter createFilter(String filterString) {
		return new Filter() {
			public boolean match(Dictionary values) {
				// TODO This is used by jslp, dont know for what...
				return true;
			}
		};
	}

	public String getManifestEntry(String name) {
		// TODO Not used yet, but I guess will have to do something with the metadata...
		return null;
	}

	public String getManifestEntry(String manifest, String name) {
		// TODO Not used yet, but I guess will have to do something with the metadata...
		return null;
	}

}
