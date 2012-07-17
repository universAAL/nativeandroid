/* 
        OCO Source Materials 
        © Copyright IBM Corp. 2011 

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
package org.universAAL.middleware.android.upnp.service;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.registry.Registry;

import android.util.Log;

/**
 * Android binder class for UPnPAndroidService. It is returned when binding to
 * service {@link UPnPAndroidService#}
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> <a
 *          href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class UPnPAndroidServiceBinder extends android.os.Binder implements AndroidUpnpService {
    protected UpnpService service = null;

    /*
     * IBinder target service setter. Used to set service after it's
     * initialization. Target setvice might be null only in the beginning. Once
     * set, it can not be reset to null again
     */
    public void setService(UpnpService service) {
	Log.i("acl.upnp.android", "Setter: " + (null != service ? service.toString() : "null"));
	if (service == null) {
	    // Rejecting attempts to null the service which was set before
	    return;
	}
	/*
	 * There might be tries (from other threads) to access the service
	 * before it is fully created (using a returned binder). On such access
	 * attempt thread is put to wait until all initialization is completed.
	 * Once service is ready, this setter is used to set the ready service
	 * as the one to use and notification to all blocked threads is sent
	 */
	synchronized (this) {
	    this.service = service;
	    notifyAll();
	}
    }

    /*
     * Method used to access the binded service. It contains build in waiting
     * mechanism in case service is being accessed before fully initialized, so
     * calls to the service must be done in the new thread.
     * 
     * @see org.teleal.cling.android.AndroidUpnpService#get()
     */
    public UpnpService get() {
	while (service == null) { // Cycle needed because wait might be
				  // interrupted, so we need to check for the
				  // main condition
	    synchronized (this) {
		// double check if the service wasn't set by other simultaneous
		// thread
		if (service == null) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	// Once we are sure service is fully initialized we can return it
	return service;
    }

    /*
     * Binder delegates method call to the targeted service
     * 
     * @see org.teleal.cling.android.AndroidUpnpService#getConfiguration()
     */
    public UpnpServiceConfiguration getConfiguration() {
	return get().getConfiguration();
    }

    /*
     * Binder delegates method call to the targeted service
     * 
     * @see org.teleal.cling.android.AndroidUpnpService#getRegistry()
     */
    public Registry getRegistry() {
	return get().getRegistry();
    }

    /*
     * Binder delegates method call to the targeted service
     * 
     * @see org.teleal.cling.android.AndroidUpnpService#getControlPoint()
     */
    public ControlPoint getControlPoint() {
	return get().getControlPoint();
    }
}