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
package org.universAAL.middleware.android.upnp.plainjava;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.binding.xml.DescriptorBindingException;
import org.teleal.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.types.UDN;
import org.universAAL.middleware.android.upnp.UPnPAndroidSodaPopPeer;
import org.universAAL.middleware.android.upnp.plainjava.exporter.ExportingSodaPopPeerProxy;
import org.universAAL.middleware.android.upnp.plainjava.exporter.ExportingSodaPopPeerProxyServiceManager;

import android.content.Context;

/**
 * Example upnp device creator using Cling library
 * 
 * @authors <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a> <a
 *          href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 */
public class DeviceFactory {

    /* Reads given input stream and concatenates all content into single string */
    private String readInputStream(InputStream stream) throws IOException {
	DataInputStream in = new DataInputStream(stream);
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	String singleLineString;
	String resultString = new String();
	while ((singleLineString = br.readLine()) != null) {
	    resultString = resultString.concat(singleLineString);
	}
	in.close();
	return resultString;
    }

    public LocalDevice createDevice(InputStream descriptionInputStream, String peerID,
	    Context context) throws ValidationException, IOException {
	String xml = readInputStream(descriptionInputStream);

	UDN templateUDN = new UDN(peerID);
	DeviceIdentity templateDeviceIdentity = new DeviceIdentity(templateUDN);
	LocalDevice templateDevice = new LocalDevice(templateDeviceIdentity);

	LocalDevice parsedDevice = null;

	try {
	    parsedDevice = new UDA10DeviceDescriptorBinderSAXImpl().describe(templateDevice, xml);
	} catch (DescriptorBindingException e) {
	    e.printStackTrace();
	}
	LocalService<ExportingSodaPopPeerProxy> sodaPopPeerService = new AnnotationLocalServiceBinder()
		.read(ExportingSodaPopPeerProxy.class);

	sodaPopPeerService.setManager(new ExportingSodaPopPeerProxyServiceManager(
		sodaPopPeerService, new UPnPAndroidSodaPopPeer(peerID, context)));
	return new LocalDevice(templateDeviceIdentity, parsedDevice.getType(),
		parsedDevice.getDetails(), sodaPopPeerService);
    }
}