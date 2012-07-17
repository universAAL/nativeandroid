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
package org.universAAL.middleware.android.upnp.plainjava.exporter;

import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.meta.LocalService;
import org.universAAL.middleware.acl.SodaPopPeer;

/**
 * Proxy service manager for exportingSodaPopPeerProxy
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> <a
 *          href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class ExportingSodaPopPeerProxyServiceManager extends
	DefaultServiceManager<ExportingSodaPopPeerProxy> {
    SodaPopPeer realPeer;

    public ExportingSodaPopPeerProxyServiceManager(LocalService<ExportingSodaPopPeerProxy> service,
	    SodaPopPeer realPeer) {
	super(service, ExportingSodaPopPeerProxy.class);
	this.realPeer = realPeer;
    }

    @Override
    protected ExportingSodaPopPeerProxy createServiceInstance() {
	ExportingSodaPopPeerProxy newProxy = null;
	try {
	    newProxy = super.createServiceInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	newProxy.setRealPeer(realPeer);
	return newProxy;
    }
}