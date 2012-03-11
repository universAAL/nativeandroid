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
package org.universAAL.middleware.acl.upnp.plainjava;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.binding.xml.DescriptorBindingException;
import org.teleal.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.universAAL.middleware.acl.SodaPopPeer;

/**
 * Example upnp device creator using Cling library
 * 
 * @author kestutis -<a href="mailto:kestutis@il.ibm.com">Kestutis
 *         Dalinkevicius</a>
 * 
 */
public class DeviceFactory {

	/*
	 * Example method creates and return local upnp device based on hard-coded
	 * description
	 */
	public LocalDevice getDevice() throws ValidationException,
			LocalServiceBindingException, IOException {
		DeviceIdentity identity = new DeviceIdentity(
				UDN.uniqueSystemIdentifier("Some String to make unique UDN"));

		DeviceType type = new UDADeviceType("SodaPopPeer", 1);

		DeviceDetails details = new DeviceDetails("SodaPop Peer",
				new ManufacturerDetails("ISTI-CNR (Persona Project)"),
				new ModelDetails("SodaPop ACL UPnP Connector",
						"A Sodapop Peer Proxy", "1.0"));

		// Device might have icon associated with it
		// Icon icon = new Icon("image/png", 48, 48, 8,
		// getClass().getResource("icon.png"));

		LocalService<SodaPopPeer> sodaPopPeerService = new AnnotationLocalServiceBinder()
				.read(ExportingSodaPopPeerProxy.class);

		sodaPopPeerService.setManager(new DefaultServiceManager(
				sodaPopPeerService, ExportingSodaPopPeerProxy.class));
		return new LocalDevice(identity, type, details, sodaPopPeerService); // Different
																				// LocalDevice
																				// constructors
																				// allow
																				// creating
																				// various
																				// devices
	}

	/*
	 * Method creates and returns local upnp device based on description
	 * provided by input stream
	 */
	public LocalDevice getParsedDevice(InputStream descriptionInputStream,
			String udnString) throws ValidationException,
			LocalServiceBindingException, IOException {

		String xml = readInputStream(descriptionInputStream);

		UDN templateUDN = new UDN(udnString);
		DeviceIdentity templateDeviceIdentity = new DeviceIdentity(templateUDN);
		LocalDevice templateDevice = new LocalDevice(templateDeviceIdentity);

		LocalDevice parcedDevice = null;

		try {
			parcedDevice = new UDA10DeviceDescriptorBinderSAXImpl().describe(
					templateDevice, xml);
		} catch (DescriptorBindingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LocalService<SodaPopPeer> sodaPopPeerService = new AnnotationLocalServiceBinder()
				.read(ExportingSodaPopPeerProxy.class);

		sodaPopPeerService.setManager(new DefaultServiceManager(
				sodaPopPeerService, ExportingSodaPopPeerProxy.class));
		return new LocalDevice(templateDeviceIdentity, parcedDevice.getType(),
				parcedDevice.getDetails(), sodaPopPeerService);
	}

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
}