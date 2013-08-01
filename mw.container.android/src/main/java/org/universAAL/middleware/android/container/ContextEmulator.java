package org.universAAL.middleware.android.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import org.universAAL.middleware.android.container.uAALBundleContainer;
import org.universAAL.middleware.android.modules.ModulesService;

import android.util.Log;

public class ContextEmulator {
	private static final String TAG = "ContextEmulator";

	public class ServiceEvent {
		public static final int MODIFIED = 0;
		public static final int REGISTERED = 1;
		public static final int UNREGISTERING = 2;
		private String service;
		private int type;

		public ServiceEvent(String serv, int typ) {
			service = serv;
			type = typ;
		}

		public int getType() {
			return type;
		}

		public ContextEmulator getContext() {
			return ContextEmulator.this;
		}

		public String getService() {
			return service;
		}
	}

	// TODO clean this
	private Hashtable<String, Object[]> services = new Hashtable<String, Object[]>();
	private Hashtable<String, Object> properties = new Hashtable<String, Object>();
	private List<uAALBundleContainer> listeners = Collections
			.synchronizedList(new ArrayList<uAALBundleContainer>());
	
	public Object getProperty(String name) {
		return properties.get(name);
	}

	
	
	public void registerService(String[] xface, Object obj, Dictionary props) {
		for (String xf : xface) {
			if (services.containsKey(xf)) {
				Object[] obs = services.remove(xf);
				List<Object> newobs = Arrays.asList(obs);
				newobs.add(obj);
				services.put(xf, newobs.toArray());
			} else {
				services.put(xf, new Object[] { obj });
			}
			warnService(xf);
		}
	}

	public void registerService(String xface, Object obj, Dictionary props) {
		if (services.containsKey(xface)) {
			Object[] obs = services.remove(xface);
			List<Object> newobs = Arrays.asList(obs);
			newobs.add(obj);
			services.put(xface, newobs.toArray());
		} else {
			services.put(xface, new Object[] { obj });
		}
		warnService(xface);
	}

	private void warnService(String xf) {
		ListIterator<uAALBundleContainer> iter = listeners.listIterator();
		while (iter.hasNext()) {
			iter.next().serviceChanged(
					new ServiceEvent(xf, ServiceEvent.REGISTERED));
		}
	}
	
//	public String getID() {
//		// TODO what is this for? the channel name??
//		return "mw.modules.aalspace.osgi";
//	}

	public Object[] getServices(String className, String filter) {
		return services.get(className);
	}

	public Object getService(String className) {
		Object[] obs = services.get(className);
		if (obs != null)
			return obs[0];
		else
			return null;
	}

	public void addServiceListener(uAALBundleContainer theContainer) {
		listeners.add(theContainer);
	}



	public static ContextEmulator createContextEmulator() {
		if(ModulesService.context==null){
			ContextEmulator ce=new ContextEmulator();
			ModulesService.context=ce;
		}
		return ModulesService.context;
	}

}
