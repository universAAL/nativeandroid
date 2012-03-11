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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.meta.Device;

/**
 * Demonstration runner to create and publish SodaPop Peer client
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis
 *         Dalinkevicius</a>
 * 
 */

public class Runner implements Runnable {

	private UpnpService upnpService = null;

	public static void main(String[] args) throws Exception {
		// Starting thread that creates upnp service
		Thread serverThread = new Thread(new Runner());
		serverThread.setDaemon(false);
		serverThread.start();
	}

	/*
	 * Main thread method which starts new upnp service,
	 * creates new SodaPop Peer and publishes it and after that searches for all upnp devices
	 * to display them
	 */
	public void run() {
		try {
			this.upnpService = new UpnpServiceImpl();
			/*
			 * On exits end messages are send using hook. This way other network
			 * devices don't have to wait for expire time
			 */
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					upnpService.shutdown();
				}
			});

			// Getting resource xml file with upnp device description and creating input stream of it
			String deviceDescriptionXmlPath = this.getClass().getClassLoader()
					.getResource("META-INF/device_description.xml").getFile();
			InputStream descriptionInputStream = new FileInputStream(
					deviceDescriptionXmlPath);
			// creating and publishing new SodaPop peer
			upnpService.getRegistry()
					.addDevice(
							new DeviceFactory()
									.getParsedDevice(descriptionInputStream, "some string for UDN"));
		} catch (Exception ex) {
			System.err.println("Exception occured: " + ex);
			ex.printStackTrace(System.err);
			System.exit(1);
		}
		this.upnpService.getControlPoint().search(); //forcing refresh of device list after adding new SodaPop peer

		Collection<Device> devices = upnpService.getControlPoint()
				.getRegistry().getDevices();
		System.out.println("Searching for devices...");
		while (devices.isEmpty()) {
			/* This is just a primitive lock in case SodaPop peer is taking some time to
			 * load. In case if there are other upnp devices list of them will be also displayed
			 */
			devices = upnpService.getControlPoint().getRegistry().getDevices();
		}
		System.out.println("Something found!");
		for (Device device : upnpService.getControlPoint().getRegistry()
				.getDevices()) {
			System.out.println(device.toString());
		}
	}
}