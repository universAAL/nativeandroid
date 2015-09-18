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
package org.universAAL.android.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

import android.content.Intent;
import android.util.Log;

/**
 * Helper class that provides static methods for variable substitution in the
 * metadata.
 * 
 * @author alfiva
 * 
 */
public class VariableSubstitution {
	private static final String TAG="VariableSubstitution";

	/**
	 * Puts any type of object as an intent extra with a given ID in the given
	 * intent, taking care of any possible variable substitution.
	 * 
	 * @param intent
	 *            The intent where to put the extra.
	 * @param extrakey
	 *            The ID of the extra to add.
	 * @param value
	 *            The value of the extra, which will be casted to the
	 *            appropriate object.
	 * @return The intent with the added extra.
	 */
	public static Intent putAnyExtra(Intent intent, String extrakey, Object value){
		if(value instanceof Resource){//Exceptions are handled by the calling methods
			if(extrakey.contains("{")){//TODO Check numbers again, just in case.
				//value URI= http://ont...#controlledLamp2instance             ->Put this "2" into  V
				//extra KEY= http://ont...#controlledLamp{lamp_number}instance                "lamp_number"
				// Take "lamp_number"
				int start=extrakey.indexOf("{");
				int end=extrakey.indexOf("}");
				String decodedKey=extrakey.substring(start+1,end);
				//Take "2"
				String valueUri=((Resource)value).getURI();
				int startUri=start;
				int endUri=valueUri.length()-(extrakey.length()-end);
				//Put "2" into "lamp_number"
				intent.putExtra(decodedKey, valueUri.substring(startUri, endUri+1)); //TODO put it in native type????
			}else{
				intent.putExtra(extrakey, ((Resource)value).getURI());
			}
		}else if(value instanceof String){
			intent.putExtra(extrakey, (String)value);
		}else if(value instanceof Boolean){
			intent.putExtra(extrakey, (Boolean)value);
		}else if(value instanceof Integer){
			intent.putExtra(extrakey, (Integer)value);
		}else if(value instanceof Long){
			intent.putExtra(extrakey, (Long)value);
		}else if(value instanceof Double){
			intent.putExtra(extrakey, (Double)value);
		}else if(value instanceof Float){
			intent.putExtra(extrakey, (Float)value);
		}else if(value instanceof Short){
			intent.putExtra(extrakey, (Short)value);
		}else if(value instanceof List){
			Object first=((List)value).get(0);
			if(first instanceof Resource){
				List<Resource> listinput=(List<Resource>)value;
				String[] vals=new String[listinput.size()];
				if(extrakey.contains("{")){
					int start=extrakey.indexOf("{");
					int end=extrakey.indexOf("}");
					for(int i=0; i<listinput.size(); i++){
						String valueUri = listinput.get(i).getURI();
						int startUri=start;
						int endUri=valueUri.length()-(extrakey.length()-end);
						vals[i]=valueUri.substring(startUri, endUri+1);
					}
					extrakey=extrakey.substring(start+1,end);
				}else{
					for(int i=0; i<listinput.size(); i++){
						vals[i]=listinput.get(i).getURI();
					}
				}
				intent.putExtra(extrakey, vals);
			}else if(first instanceof String){
				List<String> listinput=(List<String>)value;
				intent.putExtra(extrakey, listinput.toArray(new String[listinput.size()]));
			}else if(first instanceof Boolean){
				List<Boolean> listinput=(List<Boolean>)value;
				intent.putExtra(extrakey, listinput.toArray(new Boolean[listinput.size()]));
			}else if(first instanceof Integer){
				List<Integer> listinput=(List<Integer>)value;
				intent.putExtra(extrakey, listinput.toArray(new Integer[listinput.size()]));
			}else if(first instanceof Long){
				List<Long> listinput=(List<Long>)value;
				intent.putExtra(extrakey, listinput.toArray(new Long[listinput.size()]));
			}else if(first instanceof Double){
				List<Double> listinput=(List<Double>)value;
				intent.putExtra(extrakey, listinput.toArray(new Double[listinput.size()]));
			}else{
				Log.w(TAG, "Unrecognized value type of one item of an array value, for key: "
                				+ extrakey + ". Using its .toString()");
				intent.putExtra(extrakey, value.toString());
			}
		}else {
			Log.w(TAG,"Unrecognized value type of value, for key: "+extrakey+". Using its .toString()");
			intent.putExtra(extrakey, value.toString());
		}
		return intent;
	}

	/**
	 * Removes variables delimiters from a serialized context event. Used in
	 * CONTEXT PUBLISHER PROXY when registering.
	 * 
	 * @param turtleEvent
	 *            The serialized event.
	 * @return The serialized event without variable delimiters.
	 */
	public static String cleanContextEvent(String turtleEvent) {
		String replaced=turtleEvent;
		return replaced.replaceAll("&.*?;","");
	}
	
	/**
	 * Converts thanks to the metadata mappings the extras of an intent into
	 * Output objects for a Service Response in uAAL. Used in SERVICE CALLEE
	 * PROXY when when receiving intent.
	 * 
	 * @param intent
	 *            The intent containing the extras to transform.
	 * @param response
	 *            The Service Response object where the Outputs will be placed.
	 * @param outputTOextra
	 *            The mappings between Outputs and Extras.
	 */
	public static void putIntentExtrasAsResponseOutputs(Intent intent,
			ServiceResponse response, Hashtable<String, String> outputTOextra) {
		//table: URI of output in response -> substitutible string containing key of extra
		try{
			for(String outputURI:outputTOextra.keySet()){
				String encodedExtraKEY=outputTOextra.get(outputURI);
				if(encodedExtraKEY.contains("{")){
					// The encoded key is a URI with an {embedded} extra key and a type:
					// http://ont...#controlledLamp{lamp_number_array}@http://ont...#LightSource
					int start=encodedExtraKEY.indexOf("{");
					int end=encodedExtraKEY.indexOf("}");
					String[] parts=encodedExtraKEY.split("@");
					String decodedExtraKEY=parts[0].substring(start+1,end);
					String type=parts[1];
					Object extra=intent.getExtras().get(decodedExtraKEY);
					if(extra instanceof List){
						// Several values in this extra, in from of ArrayList
						Iterator iter=((List)extra).listIterator();
						ArrayList valueList=new ArrayList();
						while(iter.hasNext()){
							Object item=iter.next();
							String valueURI=parts[0].replaceAll("\\{.*\\}", item.toString());
							Resource value=Resource.getResource(type, valueURI);
							valueList.add(value);
						}
						response.addOutput(new ProcessOutput(outputURI,valueList));
					}else if(extra instanceof Object[]){
						// Several values in this extra, in from of Array
						Object[] array=(Object[])extra;
						ArrayList valueList=new ArrayList();
						for(Object item:array){
							String valueURI=parts[0].replaceAll("\\{.*\\}", item.toString());
							Resource value=Resource.getResource(type, valueURI);
							valueList.add(value);
						}
						response.addOutput(new ProcessOutput(outputURI,valueList));
					}else{
						// Just one value in this extra
						String valueURI=parts[0].replaceAll("\\{.*\\}", extra.toString());
						Resource value=Resource.getResource(type, valueURI);
						response.addOutput(new ProcessOutput(outputURI,value));
					}
				}else{
					// The encoded key is directly a key
					//TODO What happens if extras are an array and not an Arraylist?
					Object extra=intent.getExtras().get(encodedExtraKEY);
					// The value of the extra is a native type or an ArrayList of native types
					response.addOutput(new ProcessOutput(outputURI,extra));
				}

			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error placing values in outputs. Some or all outputs will not be added.", e);
		}
	}
	
	/**
	 * Converts thanks to metadata mappings the uAAL Service Request Inputs to
	 * intent extras. Used in SERVICE CALLEE PROXY when when receiving call.
	 * 
	 * @param call
	 *            The Service Call resulting from a Service Request, containing
	 *            the Inputs.
	 * @param intent
	 *            The intent where to put the extras.
	 * @param table
	 *            The mappings between Inputs and Extras.
	 */
	public static void putCallInputsAsIntentExtras(ServiceCall call, Intent intent, Hashtable<String,String> table){
		try{
			for(String inputURI:table.keySet()){
				putAnyExtra(intent, table.get(inputURI), call.getInputValue(inputURI));
			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error putting extras from inputs. Some or all extras will be empty or not present.", e);
		}
	}
	
	/**
	 * Converts thanks to the metadata mappings the Outputs of a Service
	 * Response into intent extras. Used in SERVICE CALLER PROXY when when
	 * receiving response.
	 * 
	 * @param response
	 *            The Service Response containing the Outputs.
	 * @param intent
	 *            The intent where to put the extras.
	 * @param table
	 *            The mappings between Extras and Outputs.
	 */
	public static void putResponseOutputsAsIntentExtras(ServiceResponse response,
			Intent intent, Hashtable<String,String> table) {
		try{
			for(String outputURI:table.keySet()){
				//TODO What happens if extras were an array?????
				List<Object> outs=response.getOutput(outputURI);
				if(outs==null || outs.size()<1){
					//TODO What????
				}else if(outs.size()==1){
					putAnyExtra(intent, table.get(outputURI), outs.get(0));
				}else{
					putAnyExtra(intent, table.get(outputURI), outs);
				}
			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error putting extras from outputs. Some or all extras will be empty or not present.", e);
		}
	}
	 
	/**
	 * Converts thanks to the metadata mappings the intent extras into Inputs
	 * for a Service Request. Used in SERVICE CALLER PROXY when when receiving
	 * intent.
	 * 
	 * @param intent
	 *            The intent where the extras are.
	 * @param turtleRequest
	 *            The serialized form of the Service Request where inputs must
	 *            be placed.
	 * @param table
	 *            The mappings between Extras and Inputs.
	 * @return The serialized from of the Service Request with the inputs added.
	 */
	public static String putIntentExtrasAsRequestInputs(Intent intent,
			String turtleRequest, Hashtable<String,String> table) {
		String replaced=turtleRequest;
		try{
			for(String val:table.keySet()){
				String encodedKey=table.get(val);
				if(encodedKey.contains("{")){
					// The encoded key is a whatever with an {embedded} extra key:
					// <http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp{lamp_number_array}>
					int start=encodedKey.indexOf("{");
					int end=encodedKey.indexOf("}");
					String decodedKey=encodedKey.substring(start+1,end);
					String decodedValue=intent.getExtras().get(decodedKey).toString();
					String encodedValue=encodedKey.replaceAll("\\{.*\\}",decodedValue);
					replaced=replaced.replaceAll("&"+val+";.*?&"+val+";", encodedValue);
				}else{
					replaced=replaced.replaceAll("&"+val+";.*?&"+val+";", intent.getExtras().get(table.get(val)).toString());
				}

			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error placing values in request. DO NOT use default values: Request will not be sent.", e);
			return null;
		}
		return replaced;
		//TODO its the same as putIntentExtrasAsEventValues?
	}
	
	/**
	 * Converts thanks to the metadata mappings the intent extras into actual
	 * values for a context event. Used in CONTEXT PUBLISHER PROXY when
	 * receiving intent
	 * 
	 * @param intent
	 *            The intent where the extras are.
	 * @param turtleEvent
	 *            The serialized form of the Context Event.
	 * @param table
	 *            The mappings between Extras and Values.
	 * @return The serialized form of the Context Event.
	 */
	public static String putIntentExtrasAsEventValues(Intent intent, String turtleEvent, Hashtable<String,String> table){
		String replaced=turtleEvent;//TODO Allow pre-variable uri substitution with { }
		try{
			for(String val:table.keySet()){
				replaced=replaced.replaceAll("&"+val+";.*?&"+val+";", intent.getExtras().get(table.get(val)).toString());
			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error placing values in event. DO NOT use default values: The event will not be sent.", e);
			return null;
		}
		return replaced;
	}
	
	/**
	 * Converts thanks to the metadata mappings the event values into intent
	 * extras. Used in CONTEXT SUBSCRIBER PROXY when receiving event.
	 * 
	 * @param event
	 *            The Context Event where the values are.
	 * @param intent
	 *            The intent where the extras must be placed.
	 * @param table
	 *            The mappings between Values and Extras.
	 */
	public static void putEventValuesAsIntentExtras(ContextEvent event,
			Intent intent, Hashtable<String, String> table) {
		try {
			for (String inputURI : table.keySet()) {// TODO Allow pre-variable uri substitution with { }
				Object obj=event;
				if (inputURI.contains(" ")) { // It is a ppath: "property property property"
					String[] steps = inputURI.split(" ");
					for (String step : steps) {
						try {
							obj = ((Resource) obj).getProperty(step);
						} catch (Exception e) {
							Log.e(TAG,
									"Invalid value following the property path defined in serialization: " + step
									+ "Some or all extras will be empty or not present.", e);
						}
					}
				}else{ // It is not a ppath, just one property (an event property)
					obj=((Resource) obj).getProperty(inputURI);
				}
				putAnyExtra(intent, table.get(inputURI), obj);
			}
		} catch (Exception e) {
			Log.e(TAG,"Unexpected error putting extras from event. Some or all extras will be empty or not present.", e);
		}
	}

}
