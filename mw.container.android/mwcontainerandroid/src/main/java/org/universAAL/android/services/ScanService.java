/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research 
	
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
package org.universAAL.android.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.universAAL.android.utils.GroundingParcel;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.middleware.interfaces.mpa.model.AalMpa;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * This service class is used when scanning all installed apps looking for
 * universAAL metadata. The groundings found in the metadata will be sent to the
 * Middleware service.
 * 
 * @author alfiva
 * 
 */
public class ScanService extends Service{
	
	private static final String TAG = "ScanServiceXML";
	private static final String START_THREAD_TAG = "Scan Service XML Start";
	private static final String METADATA = "org.universAAL.android.metadata";

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		// This will be called when packages change. Trigger evaluation of installed apps and call (bind) to MW when needed.
		Log.v(TAG, "Start command: ");
		new Thread(new Runnable() {
			public void run() {
				if (intent != null) {
					String action = intent.getAction();
					Log.v(TAG, "Intent: " + action);
					if (action != null) {
						// Populate the "pure" package name
						if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
							String packageName = intent.getDataString().replaceFirst("package:", "");
							Log.v(TAG, "Action is ADDED");
							registerPackage(packageName);
						}else if(action.equals(Intent.ACTION_PACKAGE_CHANGED)){
							String packageName = intent.getDataString().replaceFirst("package:", "");
							Log.v(TAG, "Action is CHANGED");
							unregisterPackage(packageName);
							registerPackage(packageName);
						}else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
							String packageName = intent.getDataString().replaceFirst("package:", "");
							Log.v(TAG, "Action is REMOVED");
							unregisterPackage(packageName);
						}else if(action.equals(AppConstants.ACTION_PCK_REG_ALL)){ // This is called by MW when started
							Log.v(TAG, "Action is REGISTER ALL");
							try {
								scanAllApps(true);
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "Something wrong when registering apps. " +
										"An app in particular may not be registered, " +
										"but the MW is started without it. " + e.getMessage());
							} finally {
								MiddlewareService.mStatus = AppConstants.STATUS_STARTED; // MW started and apps registered
								Intent notifStarted = new Intent(AppConstants.ACTION_NOTIF_STARTED);
								sendBroadcast(notifStarted);
							}
						}else if(action.equals(AppConstants.ACTION_PCK_UNREG_ALL)){ // This is called by MW when stopped
							Log.v(TAG, "Action is UNREGISTER ALL");
							scanAllApps(false);
						}else{
							Log.v(TAG, "Not the right action");
						}
					}else{ // If (action=null) who?
						Log.v(TAG, "Action is none");
					}
				}
			}
		},START_THREAD_TAG).start();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null; // This service does not allow binding. Return null.
	}
	
	/**
	 * Starts the scan for all installed apps, to register or unregister what it
	 * finds.
	 * 
	 * @param register
	 *            True if this is for registering groundings, at startup. False
	 *            to unregister, when shutdown.
	 */
	private void scanAllApps(boolean register) {
		Log.d(TAG, "Is about to scan uAAL packages");
		PackageManager pm = getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0); 
		if (register) { // register all packages
			for (PackageInfo curPackage : packages) {
				registerPackage(curPackage.packageName);
			}
		} else { // unregister all packages
			for (PackageInfo curPackage : packages) {
				unregisterPackage(curPackage.packageName);
			}
		}
	}
	
	/**
	 * Scan a manifest to register the groundings found.
	 * 
	 * @param packageName
	 *            The package ID of the App.
	 */
	private synchronized void registerPackage(final String packageName) {
		scanManifest(packageName, true);
	}

	/**
	 * Scan a manifest to unregister the groundings found.
	 * 
	 * @param packageName
	 *            The package ID of the App.
	 */
	private synchronized void unregisterPackage(String packageName) {
		scanManifest(packageName, false);
	}

	/**
	 * Scan a manifest to register/unregister the groundings found.
	 * 
	 * @param packageName
	 *            The package ID of the App.
	 * @param register
	 *            True for register, false for unregister.
	 */
	private synchronized void scanManifest(final String packageName, boolean register){
		if(packageName.matches("(com\\.android|com\\.sec\\.android|com\\.google\\.android).*")){
			Log.d(TAG, "Ignore system package ["+ packageName + "]");
			return; //Ignore system packages com.android com.sec.android and com.google.android
		}
		Log.d(TAG, "Is about to search for uAAL metadata in package ["+ packageName + "]");
		String componentNameString;
		ComponentName componentName;
		PackageManager pm = getPackageManager();
		// Scan the manifest of the package
		try {
			// Scan Application section
			ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			componentNameString = packageName;
			scanMetadata(packageName, appInfo.metaData, componentNameString, appInfo, register);
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Error when scanning package [" +packageName+ "]; Error [" + e.getMessage() + "]");
		}
	}

	/**
	 * Analyze the metadata to extract the groundings and register/unregister
	 * them.
	 * 
	 * @param packageName
	 *            The package ID of the App.
	 * @param metadata
	 *            Android metadata of the App manifest.
	 * @param componentNameString
	 *            Name of the component holding the metadata.
	 * @param appInfo
	 *            Android App Info.
	 * @param register
	 *            True for register, false for unregister.
	 * @throws NameNotFoundException
	 *             If the universAAL metadat file was not found were specified.
	 */
	private void scanMetadata(String packageName, Bundle metadata,
			String componentNameString, ApplicationInfo appInfo, boolean register) throws NameNotFoundException {
		// Find uAAL metadata file declared in this element. MAX ONE PER ELEMENT (app) !!!!!!
		int groundingID = extractResourceFromMetadata(metadata,METADATA);
		PackageManager pm = getPackageManager();
		Resources resources = pm.getResourcesForApplication(appInfo);
		// For each found uAAL wrapper, create an id and a grounding reference and send to register
		if (0 != groundingID) {
			// Read and analyze the metadata file. This is from org.universAAL.support.maven.manifest.ManifestReader
			Log.d(TAG, "Found Metadata for ["+ componentNameString + "]");
			InputStream is = resources.openRawResource(groundingID);
			XPath xpath = XPathFactory.newInstance().newXPath();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			Document manifest = null;
			Node root = null;
			try {
				builder = factory.newDocumentBuilder();
				manifest = builder.parse(is);
				root = (Node) xpath.evaluate("application/permissions", manifest, XPathConstants.NODE);
			} catch (Exception e) {
				Log.e(TAG, "Error parsing metadata",e);
			}
			if (root == null){
				Log.e(TAG, "No root found in the metadata, make sure it follows the Functional Manifest format");
				return;
			}
			NodeList busNodes = root.getChildNodes();
			boolean something=false;
			for (int i = 0; i < busNodes.getLength(); i++) {
				Node busNode = busNodes.item(i);
				if (busNode.getNodeType() == Node.ELEMENT_NODE) {
					String busName = busNode.getNodeName();
					busName = busName.replace("-", "_");
					busName = busName.replace(":", "_");
					busName = busName.replace(".", "_");
					NodeList adORreqNodes = busNode.getChildNodes();
					for (int j = 0; j < adORreqNodes.getLength(); j++) {
						Node adORreqNode = adORreqNodes.item(j);
						if (adORreqNode.getNodeType() == Node.ELEMENT_NODE) {
							String adORreqName = adORreqNode.getNodeName();
							adORreqName = adORreqName.replace("-", "_");
							adORreqName = adORreqName.replace(":", "_");
							adORreqName = adORreqName.replace(".", "_");
							int typeInt=parseTypeName(busName, adORreqName);
							if(register){
								//Scan inputs and outputs variable replacements
								List<String> listKeysIN=new ArrayList<String>();
								List<String> listValuesIN=new ArrayList<String>();
								List<String> listKeysOUT=new ArrayList<String>();
								List<String> listValuesOUT=new ArrayList<String>();
								NodeList descNodes = adORreqNode.getChildNodes();
								for (int k = 0; k < descNodes.getLength(); k++) {
									Node descNode = descNodes.item(k);
									if (descNode.getNodeType() == Node.ELEMENT_NODE) {
										//This is an input or output node
										String descNodeName = descNode.getNodeName();
										if(descNodeName.equals("input")){
											listKeysIN.add(extractPermissionProperty("key", descNode, xpath));
											listValuesIN.add(extractPermissionProperty("value", descNode, xpath));
										}else if(descNodeName.equals("output")){
											listKeysOUT.add(extractPermissionProperty("key", descNode, xpath));
											listValuesOUT.add(extractPermissionProperty("value", descNode, xpath));
										}
									}
								}
								//Build the grounding and send to register
								GroundingParcel grounding=new GroundingParcel(
										extractPermissionProperty("action", adORreqNode, xpath), 
										extractPermissionProperty("category", adORreqNode, xpath), 
										extractPermissionProperty("serialization", adORreqNode, xpath), 
										extractPermissionProperty("replyAction", adORreqNode, xpath),
										extractPermissionProperty("replyCategory", adORreqNode, xpath),
										extractPermissionProperty("remote", adORreqNode, xpath),
										listKeysIN,listValuesIN,listKeysOUT,listValuesOUT);
								sendRegisterToBus(packageName+":"+groundingID+":"+i+":"+j, grounding, typeInt);
							}else{
								//No need to scan anything else, send to unregister
								sendUnRegisterToBus(packageName+":"+groundingID+":"+i+":"+j, typeInt);
							}
							something=true;
						}
					}
				}
			}
			if(!something){
				Log.e(TAG, "No items found in the metadata, make sure it follows the Functional Manifest format");
			}
		}//TODO check that the ID works ok built like that
	}
	
	/**
	 * Extract the type constant from the metadata.
	 * 
	 * @param busName
	 *            Bus section name.
	 * @param typeName
	 *            Advertisement or requirement.
	 * @return The constant identifying the type of uAAL wrapper.
	 */
    private int parseTypeName(String busName, String typeName) {
    	if(busName.equals("mw_bus_context")){
    		if(typeName.equals("advertisement")){
        		return AppConstants.TYPE_CPUBLISHER;
        	}else if(typeName.equals("requirement")){
        		return AppConstants.TYPE_CSUBSCRIBER;
        	}
    	}else if(busName.equals("mw_bus_service")){
    		if(typeName.equals("advertisement")){
    			return AppConstants.TYPE_SCALLEE;
        	}else if(typeName.equals("requirement")){
        		return AppConstants.TYPE_SCALLER;
        	}
    	}
		return 0;
	}
	
	/**
	 * Helper method to extract nodes from the metadata file.
	 * 
	 * @param property
	 *            What to extract.
	 * @param from
	 *            From which Node.
	 * @param xpath
	 *            How it is reached.
	 * @return The value.
	 */
	private String extractPermissionProperty(String property, Node from, XPath xpath) {
		try {
			return xpath.evaluate("normalize-space(" + property + ")", from);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Once the grounding is ready, send it to the Service Middleware to be
	 * registered.
	 * 
	 * @param id
	 *            ID for the Registry.
	 * @param registration
	 *            The Grounding data.
	 * @param type
	 *            Type of uAAL wrapper.
	 */
	private void sendRegisterToBus(String id, GroundingParcel registration, int type) {
		Log.d(TAG, "Attempt to register component [id: " + id + ", \n act: "
				+ registration.getAction() + ", \n cat: " + registration.getCategory()
				+ ", \n turtle: " + registration.getGrounding() + ", \n type: "
				+ type + "]");
		Intent start = new Intent(AppConstants.ACTION_PCK_REG);
		start.setClass(this, MiddlewareService.class);
		start.putExtra(AppConstants.ACTION_PCK_REG_X_ID, id);
		start.putExtra(AppConstants.ACTION_PCK_REG_X_PARCEL, registration);
		start.putExtra(AppConstants.ACTION_PCK_REG_X_TYPE, type);
		startService(start);
	}
	
	/**
	 * Once the grounding is ready, send it to the Service Middleware to be
	 * unregistered.
	 * 
	 * @param id
	 *            ID for the Registry.
	 * @param type
	 *            Type of uAAL wrapper.
	 */
	private void sendUnRegisterToBus(String id, int type) {
		Log.d(TAG, "Attempt to unregister component [id: " + id + ", \n type: "
				+ type + "]");
		Intent start = new Intent(AppConstants.ACTION_PCK_UNREG);
		start.setClass(this, MiddlewareService.class);
		start.putExtra(AppConstants.ACTION_PCK_REG_X_ID, id);
		start.putExtra(AppConstants.ACTION_PCK_REG_X_TYPE, type);
		startService(start);
	}

	/**
	 * Extract the universAAL metadata file as specified by the manifest
	 * metadata declaration.
	 * 
	 * @param metaData
	 *            All available metadata.
	 * @param metdataName
	 *            The uAAL metadata file.
	 * @return ID of the file.
	 */
	protected int extractResourceFromMetadata(Bundle metaData,
			String metdataName) {
		int resourceID = 0;
		if (null == metaData) {
			return resourceID;
		}
		Iterator<String> metaTags = metaData.keySet().iterator();
		while (metaTags.hasNext()) {
			String tag = metaTags.next();
			if (metdataName.equals(tag)) {
				String resourceIDAsStr = metaData.get(tag).toString();
				resourceID = Integer.parseInt(resourceIDAsStr);
			}
		}
		return resourceID;
	}

}
