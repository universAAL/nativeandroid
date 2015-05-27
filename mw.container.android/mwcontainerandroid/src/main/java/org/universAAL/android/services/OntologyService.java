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
package org.universAAL.android.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.ontology.location.LocationOntology;
import org.universAAL.ontology.measurement.MeasurementOntology;
import org.universAAL.ontology.phThing.PhThingOntology;
import org.universAAL.ontology.profile.ProfileOntology;
import org.universAAL.ontology.shape.ShapeOntology;
import org.universAAL.ontology.space.SpaceOntology;
import org.universAAL.ontology.unit.UnitOntology;
import org.universAAL.ontology.vcard.VCardOntology;

import dalvik.system.DexClassLoader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This service class is used when loading one or all (currently only all) the
 * ontologies into the middleware data representation.
 * 
 * @author alfiva
 * 
 */
public class OntologyService extends Service{
	private static final String TAG = "OntologyService";
//	public static final String ONT_FOLDER = "/data/felix/ontologies/";// this is just the default
	private static final String ONT_CACHE = "ontdex";
	private static final String ONT_ACTIVATOR_LIST_FILE = "activators.cfg";
	private static final String ONT_ONTOLOGY_LIST_FILE = "ontologies.cfg";

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		Log.v(TAG, "Start command: ");
		// Not in thread because currently is called by MW and should be
		// blocking until its finished loading. Thats fine for start, but other
		// intents may need a thread !!! It appears it is in a different thread
		if (intent != null) {
			String action = intent.getAction();
			Log.v(TAG, "Intent: " + action);
			if (action != null) {
				if (action.equals(AppConstants.ACTION_ONT_REG_ALL)) {
					Log.v(TAG, "Action is REGISTER ALL");
					registerOntologies(this);
				} else {
					Log.v(TAG, "Not the right action");
				}
			} else {// If (action=null) who?
				Log.v(TAG, "Action is none");
			}
		}
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null; // This service does not allow binding. Return null.
	}
	
	/**
	 * Load ALL the ontologies available. Called at startup. It is static so it
	 * can be called directly by MW and avoid race condition instead of calling
	 * this as a a service.
	 */
	public static void registerOntologies(Context ctxt) {
		// PATCH !!! The current way of registering ontologies in uAAL scans
		// them at an arbitrary order, so it may happen that some start before
		// their dependencies and therefore fail. A proposed solution would be
		// scanning their dependencies first and make sure they are registered
		// in order. This has to be solved in OSGi version first before I feel
		// like including such solution here. What I do for now is just scanning
		// first if there are some files where the onts
		// activators/ontologyClasses are listed in order, then the last step is
		// arbitrary order scan. Its fine because nothing happens if a
		// registration is attempted twice.

		// TODO In theory the OntologyManagement stays static in memory (that
		// was the problem with jslp after all) when the MW first invokes it
		// (indirectly through the buses)
		
		// FIRST SOURCE: ONTS INCLUDED IN APK
		registerOntologiesFromAPK();
	    
		// Scan all the JAR files in the ont folder, but dont register yet
		File ontFolder = new File(Environment.getExternalStorageDirectory(),
				PreferenceManager.getDefaultSharedPreferences(ctxt).getString(
						"setting_ofolder_key", AppConstants.Defaults.OFOLDER));
		if (!ontFolder.exists()) {
			Log.w(TAG, "The ontology folder does not exist."
					+ "Make sure the value is correct in the settings");
			return;
		}
		File[] files = ontFolder.listFiles(new ArchiveFilter());
		StringBuilder filenames = new StringBuilder();
		ArrayList<String> activators = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			//Add the file name to the formatted list of file names
			filenames.append(ontFolder.getAbsolutePath() + "/" + files[i].getName());
			if (i + 1 < files.length) {// avoid separator : in the end
				filenames.append(File.pathSeparator);
			}
			//Now scan the jar file
			JarFile jar = null;
			try {
				jar = new JarFile(files[i]);
				//Get all classes
				Enumeration<JarEntry> classes = jar.entries();
				while (classes.hasMoreElements()) {
					//For each class check if its the activator and add it to list
					JarEntry entry = (JarEntry) classes.nextElement();
					String name = entry.getName();//CAUTION!!! Activator classes must all have different names!!!!
					if (name.startsWith("org/universAAL/ontology/") && name.contains("Activator") && name.endsWith(".class")) {
						activators.add(name.substring(0, name.length()-6).replace("/","."));
						break;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "Error scanning an ontology jar package",e);
			}finally{
				try {
					if(jar!=null){
						jar.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "Could not close the ontology jar file properly",e);
				}
			}
		}
		
		// Prepare the classloader for all these JAR files
		final File optimizedDexOutputPath = ctxt.getDir(ONT_CACHE, Context.MODE_PRIVATE);
		DexClassLoader cl = new DexClassLoader(filenames.toString(),
				optimizedDexOutputPath.getAbsolutePath(), null,
				OntologyService.class.getClassLoader());
		// TODO check if I have to use other parent classloader (e.g. MW). Will ontologies still work after a while when this service dies?
		
		// SECOND SOURCE: ONTS IN THE ONT FOLDER, BY THEIR ACTIVATOR NAME IN CFG FILE
		registerOntologiesFromActList(ontFolder, cl);
		
		// THIRD SOURCE: ONTS IN THE ONT FOLDER, BY THEIR ONTOLOGY CLASS NAME IN CFG FILE
		registerOntologiesFromOntList(ontFolder, cl);
		
		if (activators.size() <= 0) {
			Log.w(TAG, "No activator files were found in the folder of ont jars. " +
					"If both previous attempts of finding ont list files failed, you may be in some trouble...");
		}

		// FOURTH SOURCE: ONTS IN THE ONT FOLDER, AS THEY WERE SCANNED SEQUENTIALLY BY THEIR JARS
		registerOntologiesFromJARS(activators, cl);
	}
	
	/**
	 * Inner method to load all ontologies included in the app libraries.
	 */
	private static void registerOntologiesFromAPK(){
		// First of all, register the phWorld ont, which is included in the apk.
		OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new LocationOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new ShapeOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new PhThingOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new SpaceOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new VCardOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new ProfileOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new UnitOntology());
	    OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, new MeasurementOntology());
	}
	
	/**
	 * Inner method to load all ontologies included in the ontologies folder,
	 * following the list of activators described in activators.cfg.
	 * 
	 * @param ontFolder
	 *            The folder where the dexed jar files of the ontologies are
	 *            found.
	 * @param cl
	 *            Class loader to use to access the jar files.
	 */
	private static void registerOntologiesFromActList(File ontFolder, ClassLoader cl){
		// Comma separated list of the names of the activator class, with or without org.universAAL.ontology pckg prefix
		File manualList=new File(ontFolder, ONT_ACTIVATOR_LIST_FILE);
		Scanner scan = null;
		try {
			scan = new Scanner(manualList);
			scan.useDelimiter(",");
			while(scan.hasNext()){
				String activator=scan.next();
				activator=activator.replace(",", "");
				if (!activator.trim().isEmpty()){//Avoid empty spaces in the end of the list
					if(!activator.startsWith("org.universAAL.ontology.")){
						activator="org.universAAL.ontology."+activator;
					}
					ModuleActivator modActivator;
					try {
						modActivator = (ModuleActivator) Class.forName(activator, true, cl).newInstance();
						modActivator.start(AndroidContext.THE_CONTEXT);
					} catch (Exception e) {
						Log.e(TAG, "Error loading and starting an ontology activator from teh list of activators",e);
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(TAG, "There is no file with the list of ontology activators. That is OK, will try other file...: "
							+ e.getMessage());
		}finally{
			if(scan!=null){
				scan.close();
			}
		}
	}
	
	/**
	 * Inner method to load all ontologies included in the ontologies folder,
	 * following the list described of ont classes in ontologies.cfg.
	 * 
	 * @param ontFolder
	 *            The folder where the dexed jar files of the ontologies are
	 *            found.
	 * @param cl
	 *            Class loader to use to access the jar files.
	 */
	private static void registerOntologiesFromOntList(File ontFolder, ClassLoader cl){
		// Comma separated list of the names of the ontology class, WITH the fully qualified package name
		File manualList = new File(ontFolder, ONT_ONTOLOGY_LIST_FILE);
		Scanner scan = null;
		try {
			scan = new Scanner(manualList);
			scan.useDelimiter(",");
			while(scan.hasNext()){
				String ontology=scan.next();
				ontology=ontology.replace(",", "");
				if (!ontology.trim().isEmpty()){//Avoid empty spaces in the end of the list
					try {
						Ontology ontologyclass = (Ontology) Class.forName(ontology, true, cl).newInstance();
						OntologyManagement.getInstance().register(AndroidContext.THE_CONTEXT, ontologyclass);
					} catch (Exception e) {
						Log.e(TAG, "Error loading and registering an ontology class from the list of classes",e);
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.w(TAG, "There is no file with the list of ontology classes. That is OK, will try scanning jars... :"
							+ e.getMessage());
		}finally{
			if(scan!=null){
				scan.close();
			}
		}
	}
	
	/**
	 * Inner method to load all ontologies included in the ontologies folder,
	 * automagically scanning their activators.
	 * 
	 * @param activators
	 *            List of the activators that were identified.
	 * @param cl
	 *            Class loader to use to access the jar files.
	 */
	private static void registerOntologiesFromJARS(ArrayList<String> activators, ClassLoader cl){
		for(String activator:activators){
			ModuleActivator modActivator;
			try {
				modActivator = (ModuleActivator) Class.forName(activator, true, cl).newInstance();
				modActivator.start(AndroidContext.THE_CONTEXT);
			} catch (Exception e) {
				Log.e(TAG, "Error loading and starting an ontology activator from a Jar file",e);
			}	
		}
	}
	
	/**
	 * File filter to identify jar files.
	 * 
	 * @author alfiva
	 * 
	 */
	static class ArchiveFilter implements FileFilter {
		public boolean accept(File file) {
			if (file.getName().toLowerCase(Locale.US).endsWith(".jar")) {
				return true;
			}
			return false;
		}
	}

}
