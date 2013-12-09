package org.universAAL.android.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

import android.content.Intent;

public class VariableSubstitution {
//TODO Clean this code
	public static Intent putAnyExtra(Intent intent, String extrakey, Object value){
		if(value instanceof Resource){
			if(extrakey.contains("{")){//TODO Check numbers again, just in case. HANDLE EXCEPTIONS!!!
				//value URI= http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp2instance             ->Put this "2" into  V
				//extra KEY= http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp{lamp_number}instance                "lamp_number"
				
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
		}else if(value instanceof List){
			Object first=((List)value).get(0);
			if(first instanceof Resource){
				List<Resource> listinput=(List<Resource>)value;
				String[] uris=new String[listinput.size()];
				for(int i=0; i<listinput.size(); i++){
					uris[i]=listinput.get(i).getURI();
				}
				intent.putExtra(extrakey, uris);
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
				System.out.println("NOT VALID VALUE");
			}
		}else {
			System.out.println("NOT VALID VALUE");
		}
		return intent;
	}

	// Used in CONTEXT PUBLISHER PROXY when registering
	public static String cleanContextEvent(String turtleEvent) {
		String replaced=turtleEvent;
		return replaced.replaceAll("&.*?;","");
	}
	
	// Used in SERVICE CALLEE PROXY when when receiving intent
	public static void putIntentExtrasAsResponseOutputs(Intent intent, ServiceResponse response, Hashtable<String,String> outputTOextra){
		//table: URI of output in response -> substitutible string containing key of extra
		for(String outputURI:outputTOextra.keySet()){
			String encodedExtraKEY=outputTOextra.get(outputURI);
			if(encodedExtraKEY.contains("{")){
				// The encoded key is a URI with an {embedded} extra key and a type:
				// http://ontology.igd.fhg.de/LightingServer.owl#controlledLamp{lamp_number_array}@http://ontology.universaal.org/Lighting.owl#LightSource
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
					String valueURI=encodedExtraKEY.replaceAll("\\{.*\\}", extra.toString());
					Resource value=Resource.getResource(parts[1], parts[0]);
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
	}
	
	// Used in SERVICE CALLEE PROXY when when receiving call
	public static void putCallInputsAsIntentExtras(ServiceCall call, Intent intent, Hashtable<String,String> table){
		for(String inputURI:table.keySet()){
			putAnyExtra(intent, table.get(inputURI), call.getInputValue(inputURI));
		}
	}
	
	// Used in SERVICE CALLER PROXY when when receiving response
	public static void putResponseOutputsAsIntentExtras(ServiceResponse response,
			Intent intent, Hashtable<String,String> table) {
		for(String outputURI:table.keySet()){
			//TODO What happens if extras were an array?????
			List<Object> outs=response.getOutput(outputURI, true);
			if(outs==null || outs.size()<1){
				//TODO What????
			}else if(outs.size()==1){
				putAnyExtra(intent, table.get(outputURI), outs.get(0));
			}else{
				putAnyExtra(intent, table.get(outputURI), outs);
			}
		}
	}
	
	// Used in SERVICE CALLER PROXY when when receiving intent
	public static String putIntentExtrasAsRequestInputs(Intent intent,
			String turtleRequest, Hashtable<String,String> table) {
		String replaced=turtleRequest;
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
		return replaced;
		//TODO its the same as putIntentExtrasAsEventValues
	}
	
	// Used in CONTEXT PUBLISHER PROXY when receiving intent
	public static String putIntentExtrasAsEventValues(Intent intent, String turtleEvent, Hashtable<String,String> table){
		String replaced=turtleEvent;//TODO Allow pre-variable uri substitution with { }
		for(String val:table.keySet()){
			replaced=replaced.replaceAll("&"+val+";.*?&"+val+";", intent.getExtras().get(table.get(val)).toString());
		}
		return replaced;
	}
	
	// Used in CONTEXT SUBSCRIBER PROXY when receiving event
	public static void putEventValuesAsIntentExtras(ContextEvent event, Intent intent, Hashtable<String,String> table){
		for(String inputURI:table.keySet()){//TODO Allow pre-variable uri substitution with { }
			putAnyExtra(intent, table.get(inputURI), event.getProperty(inputURI));//TODO Allow ppaths into event?
		}
	}

}
