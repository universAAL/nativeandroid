/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.android.container;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;

import android.util.Log;

/**
 * Android-specific implementation of the Container interface for universAAL.
 * Its prime function is to hold the references to all running modules of the
 * middleware, and allow access to them.
 * 
 * @author alfiva
 * 
 */
public class AndroidContainer implements Container {
	private static final String TAG = "AndroidContainer";
	// Events for listeners
	private static final int EVENT_SERV_MODIFIED = 0;
	private static final int EVENT_SERV_REGISTERED = 1;
	private static final int EVENT_SERV_UNREGISTER = 2;
	// Singleton
	public static final AndroidContainer THE_CONTAINER = new AndroidContainer();
	// This is where we store in memory all the references to modules (OSGI
	// services)
	private Hashtable<String, WeakReference<Object>> services = new Hashtable<String, WeakReference<Object>>();
	// This is a list of all modules listening to something (OSGI listeners)
	private List<SharedObjectListener> listeners = Collections
			.synchronizedList(new ArrayList<SharedObjectListener>());

	public Object fetchSharedObject(ModuleContext requester,
			Object[] fetchParams) {
		if (requester instanceof AndroidContext && fetchParams != null
				&& fetchParams.length > 0) {
			if (fetchParams.length == 1) {
				if (fetchParams[0] instanceof String) {
					return getObject((String) fetchParams[0]);
				}
			} else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
					&& (fetchParams[1] == null || fetchParams[1] instanceof String)) {
				Object[] result = getObjects((String) fetchParams[0],
						(String) fetchParams[1]);
				if (result != null)
					return result[0];
			}
		} // else{ problems with parameters
		return null;
	}

	public Object[] fetchSharedObject(ModuleContext requester,
			Object[] fetchParams, SharedObjectListener listener) {
		if (requester instanceof AndroidContext && fetchParams != null
				&& fetchParams.length > 0)
			if (fetchParams.length == 1) {
				if (fetchParams[0] instanceof String) {
					if (listener != null && !listeners.contains(listener)) {
						synchronized (listeners) {
							listeners.add(listener);
						}
					}
					return getObjects((String) fetchParams[0], null);
				}
			} else if ((fetchParams[0] == null || fetchParams[0] instanceof String)
					&& (fetchParams[1] == null || fetchParams[1] instanceof String)) {
				if (listener != null && !listeners.contains(listener))
					synchronized (listeners) {
						listeners.add(listener);
					}
				return getObjects((String) fetchParams[0],
						(String) fetchParams[1]);
			} // else{ problems with parameters => do not add the listener
		return null;
	}

	public void removeSharedObjectListener(SharedObjectListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	public ModuleContext installModule(ModuleContext requester,
			Object[] installParams) {
		// TODO This is in theory only used by Deploy Manager therefore we dont implement it yet.
		return null;
	}

	public Iterator logListeners() {
		// Looks like this is just for logging
		ArrayList empty = new ArrayList();
		return empty.iterator();
	}

	public ModuleContext registerModule(Object[] regParams) {
		// Actually we dont need to register anything because the only thing
		// registered was the listener in OSGI context, but here listeners are
		// handled directly by this container.
		return AndroidContext.THE_CONTEXT;
	}

	public void shareObject(ModuleContext requester, Object objToShare,
			Object[] shareParams) {
		if (!(requester instanceof AndroidContext) || objToShare == null
				|| shareParams == null || shareParams.length == 0) {
			Log.w(TAG, "Parameters passed to 'shareObject' are null or not the right type");
			return;
		}
		int n = shareParams.length - 1;
		if (n == 0) {
			if (shareParams[0] instanceof String) {
				shareObject((String) shareParams[0], objToShare, null);
			} else if (shareParams[0] instanceof Dictionary) {
				shareObject((String) null, objToShare,
						(Dictionary) shareParams[0]);
			} else {
				Log.w(TAG, "'shareParams' passed to 'shareObject' are not Strings or Dicitionary");
			}
		} else {
			for (int i = 0; i < n; i++) {
				if (!(shareParams[i] instanceof String)) {
					Log.w(TAG, "one of 'shareParams' passed to 'shareObject' is not a String");
					return;
				}
			}
			if (shareParams[n] instanceof String) {
				shareObject((String[]) shareParams, objToShare, null);
			} else if (shareParams[n] instanceof Dictionary) {
				if (n == 1) {
					shareObject((String) shareParams[0], objToShare,
							(Dictionary) shareParams[1]);
				} else {
					String[] xfaces = new String[n];
					for (int i = 0; i < n; i++)
						xfaces[i] = (String) shareParams[i];
					shareObject(xfaces, objToShare, (Dictionary) shareParams[n]);
				}
			} else
				Log.w(TAG, "one of 'shareParams' passed to 'shareObject' is not String or Dictionary");
		}
	}
	
	public void removeSharedObject(ModuleContext requester, Object objToRemove,
			Object[] shareParams) {
		if (!(requester instanceof AndroidContext) || objToRemove == null
				|| shareParams == null || shareParams.length == 0) {
			Log.w(TAG, "Parameters passed to 'removeSharedObject' are null or not the right type");
			return;
		}

		int n = shareParams.length - 1;
		if (n == 0)
			if (shareParams[0] instanceof String)
				unshareObject((String) shareParams[0], objToRemove);
			else if (shareParams[0] instanceof Dictionary)
				unshareObject((String) null, objToRemove);
			else
				Log.w(TAG, "'shareParams' passed to 'removeSharedObject' is not String or Dictionary");
		else {
			for (int i = 0; i < n; i++)
				if (!(shareParams[i] instanceof String)) {
					Log.w(TAG, "'shareParams' passed to 'removeSharedObject' is not String or Dictionary");
					return;
				}
			if (shareParams[n] instanceof String)
				unshareObject((String[]) shareParams, objToRemove);
			else if (shareParams[n] instanceof Dictionary)
				if (n == 1)
					unshareObject((String) shareParams[0], objToRemove);
				else {
					String[] xfaces = new String[n];
					for (int i = 0; i < n; i++)
						xfaces[i] = (String) shareParams[i];
					unshareObject(xfaces, objToRemove);
				}
			else
				Log.w(TAG, "'shareParams' passed to 'removeSharedObject' is not String or Dictionary");
		}
	}

	// END OF INTERFACE

	/**
	 * Auxiliary method to store objects in the container.
	 * 
	 * @param xface
	 *            Interface implemented by the object to store.
	 * @param obj
	 *            Object to store.
	 * @param object
	 *            Properties of the object (not used).
	 */
	public void shareObject(String xface, Object obj, Dictionary object) {
		WeakReference<Object> weak = new WeakReference<Object>(obj);
		if (services.containsKey(xface)) { // looks like this works
			notifyListeners(xface, services.remove(xface).get(),
					EVENT_SERV_UNREGISTER);
		}
		services.put(xface, weak); // TODO Only one at each point in time...
		notifyListeners(xface, weak.get(), EVENT_SERV_REGISTERED);
	}

	/**
	 * Auxiliary method to store objects in the container.
	 * 
	 * @param xface
	 *            Interfaces implemented by the object to store.
	 * @param obj
	 *            Object to store.
	 * @param props
	 *            Properties of the object (not used).
	 */
	public void shareObject(String[] xface, Object obj, Dictionary props) {
		for (String xf : xface) {
			shareObject(xf, obj, props);
		}
	}

	/**
	 * Auxiliary method to get object from the container.
	 * 
	 * @param className
	 *            Name of the class of the interface implemented by the object
	 *            to retrieve.
	 * @return The Object that implements it.
	 */
	public Object getObject(String className) {
		if (className == null) {
			return null;
		}
		WeakReference<Object> weak = services.get(className);
		if (weak != null) {
			return weak.get();
		}
		return null;
	}

	/**
	 * Auxiliary method to get objects from the container.
	 * 
	 * @param className
	 *            Name of the class of the interface implemented by the object
	 *            to retrieve.
	 * @param filter
	 *            Filter to get objects (not used).
	 * @return The Objects that implement the interface.
	 */
	public Object[] getObjects(String className, String filter) {
		// TODO Right now I dont care about the filter
		if (className == null) {
			return null;
		}
		WeakReference<Object> weak = services.get(className);
		if (weak != null) {
			return new Object[] { weak.get() };
		}
		return null;
	}

	/**
	 * Auxiliary method to remove object from the container.
	 * The referenced instance is set to null.
	 * 
	 * @param xface
	 *            Interface implemented by the object to remove.
	 * @param obj
	 *            Object to remove - not really used
	 */
	public void unshareObject(String xface, Object obj) {
		WeakReference<Object> weak = services.remove(xface);
		if (weak != null) {
			// TODO I can do this because there is just 1 reference per xface.
			// Otherwise notify on obj directly
			notifyListeners(xface, weak.get(), EVENT_SERV_UNREGISTER);
			weak.clear();
		}
	}

	/**
	 * Auxiliary method to remove object from the container.
	 * 
	 * @param xface
	 *            Interfaces implemented by the object to remove.
	 * @param obj
	 *            Object to remove.
	 */
	public void unshareObject(String[] xface, Object obj) {
		for (String xf : xface) {
			unshareObject(xf, obj);
		}
	}

	/**
	 * Method to use when an object is added/removed/changed in the container
	 * and there are listeners for such situation.
	 * 
	 * @param xf
	 *            Interface of the object being added/removed/changed.
	 * @param obj
	 *            Object being added/removed/changed.
	 * @param event
	 *            Type of situation happening.
	 */
	private void notifyListeners(String xf, Object obj, int event) {
		// TODO Not handling ServiceEvent.MODIFIED event
		if (event == EVENT_SERV_MODIFIED) {
			return;
		}
		final ArrayList<SharedObjectListener> listenersLocalCopy;
		// Make a copy of the listeners so it does not get modified
		synchronized (listeners) {
			listenersLocalCopy = new ArrayList<SharedObjectListener>(listeners);
		}
		switch (event) {
		case EVENT_SERV_REGISTERED:
			for (Iterator<SharedObjectListener> i = listenersLocalCopy
					.iterator(); i.hasNext();) {
				SharedObjectListener sol = (SharedObjectListener) i.next();
				if (sol != null) {
					sol.sharedObjectAdded(obj, obj); // TODO passing obj as removeHook ?
				}
			}
			break;
		case EVENT_SERV_UNREGISTER:
			for (Iterator<SharedObjectListener> i = listenersLocalCopy
					.iterator(); i.hasNext();) {
				SharedObjectListener sol = (SharedObjectListener) i.next();
				if (sol != null) {
					sol.sharedObjectRemoved(obj); // TODO passing obj as removeHook ?
				}
			}
			break;
		}
	}
}
