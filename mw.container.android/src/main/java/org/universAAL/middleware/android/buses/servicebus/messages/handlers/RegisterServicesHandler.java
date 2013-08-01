/**
 * 
 *  OCO Source Materials 
 *      � Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.android.buses.servicebus.messages.handlers;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.common.AndroidOntologyManagement;
import org.universAAL.middleware.android.buses.common.xml.objects.ValueXmlObj;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
//import org.universAAL.middleware.android.buses.servicebus.AndroidServiceBus;
import org.universAAL.middleware.android.buses.servicebus.messages.RegisterServicesMessage;
import org.universAAL.middleware.android.buses.servicebus.messages.RegisterServicesMessage.AndroidServiceCalleeDataWrapper;
import org.universAAL.middleware.android.buses.servicebus.messages.RegisterServicesMessage.AndroidServiceCallerDataWrapper;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.AndroidServiceCalleeProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.FilteringInputXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.OperationXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.OutputXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.ServiceGroundingXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.AndroidServiceCallerProxy;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ServiceRequestGroundingXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ChangeEffectXmlObj;
import org.universAAL.middleware.android.common.ReflectionsUtils;
import org.universAAL.middleware.android.common.messages.IMessage;
import org.universAAL.middleware.android.common.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.android.modules.ModulesCommWrapper;
import org.universAAL.middleware.container.ModuleContext;
//import org.universAAL.middleware.android.common.modulecontext.AndroidModuleContextFactory;
//import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
//import org.universAAL.middleware.android.localsodapop.messages.handlers.AbstractMessagePersistableHandler;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.rdf.TypeMapper;
//import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owl.ServiceBusOntology;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 23, 2012
 * 
 */
public class RegisterServicesHandler extends AbstractMessagePersistableHandler {

	public RegisterServicesHandler(ModulesCommWrapper wrapper) {
		super(wrapper);
	}

	private static final String TAG = RegisterServicesHandler.class
			.getCanonicalName();

	@Override
	protected void privateHandleMessage(IMessage message/*
														 * ,
														 * AbstractSodaPopAndroidImpl
														 * sodaPop
														 */) {
		// Cast it to RegisterProfiles message
		RegisterServicesMessage registerServicesMessage = (RegisterServicesMessage) message;

		// Check if there is anything to handle (for example - it is an
		// application that was installed and doesn't contain ServiceGrouding /
		// ServiceCaller definition)
		if (registerServicesMessage.getAndroidServiceCalleesDataWrapperList()
				.isEmpty()
				&& registerServicesMessage
						.getAndroidServiceCallersDataWrapperList().isEmpty()) {
			Log.d(TAG, "Nothing was found to handle, therefore do nothing!");
			return;
		}

		// TODO Share the ModuleContext
		// AndroidModuleContextFactory.createModuleContext();

		// Get the context
		Context context = registerServicesMessage.getContext();

		// Create AndroidServiceBus
		AndroidServiceBusImpl androidServiceBus = createServiceBus();/*
																	 * new
																	 * AndroidServiceBus
																	 * (sodaPop,
																	 * context);
																	 */

		// Handle each ServiceCallee registration/unregistration separately
		for (AndroidServiceCalleeDataWrapper serviceCalleeRegistration : registerServicesMessage
				.getAndroidServiceCalleesDataWrapperList()) {
			handleServiceCallee(serviceCalleeRegistration, androidServiceBus,
					context);
		}

		// Handle each ServiceCallers registration/unregistration separately
		for (AndroidServiceCallerDataWrapper serviceCallerRegistration : registerServicesMessage
				.getAndroidServiceCallersDataWrapperList()) {
			handleServiceCaller(serviceCallerRegistration, androidServiceBus,
					context);
		}
	}

	protected void handleServiceCallee(
			AndroidServiceCalleeDataWrapper serviceCalleeRegistration,
			AndroidServiceBusImpl androidServiceBus, Context context) {

		ServiceGroundingXmlObj serviceGroundingXml = serviceCalleeRegistration
				.getServiceGroundingXmlObj();

		// Register ontologies
		registerOntologies(androidServiceBus.getModuleContext(),
				serviceGroundingXml);

		// Populate the Service Profiles
		ServiceProfile[] serviceProfiles = null;
		try {
			serviceProfiles = populateServiceProfiles(
					androidServiceBus.getModuleContext(), serviceGroundingXml);
		} catch (Exception e) {
			String errMsg = "Unable to register the service grounding ["
					+ e.getMessage() + "]";
			Log.e(TAG, errMsg);
			return;
		}

		// Create AndroidServiceCallee - the c'tor will do all the registration
		// stuff
		new AndroidServiceCalleeProxy(androidServiceBus.getModuleContext(),
				serviceProfiles, serviceCalleeRegistration.getPackageName(),
				serviceCalleeRegistration.getGroundingID(),
				serviceCalleeRegistration.getAndroidUniqueName(),
				serviceGroundingXml, context);
	}

	private static void registerOntologies(ModuleContext mc,
			ServiceGroundingXmlObj serviceXml) {
		registerOntologies(mc, serviceXml.getOntologies());
	}

	private static ServiceProfile[] populateServiceProfiles(ModuleContext mc,
			ServiceGroundingXmlObj serviceXml) throws Exception {
		List<ServiceProfile> profiles = new ArrayList<ServiceProfile>();

		// Common Variables
		final String ontologyUri = serviceXml.getUri();

		class ServiceWrapper extends Service {

			public ServiceWrapper(String uri) {
				super(uri);
			}

			@Override
			public void addFilteringInput(String inParamURI, String typeURI,
					int minCardinality, int maxCardinality, String[] propPath) {
				super.addFilteringInput(inParamURI, typeURI, minCardinality,
						maxCardinality, propPath);
			}

			public ServiceProfile getServiceProfile() {
				return myProfile;
			}

			public String getClassURI() {
				return ontologyUri;
			}

			void addInputsToService(OperationXmlObj operationXml) {
				for (FilteringInputXmlObj inputXml : operationXml
						.getFilteringInputs()) {
					addFilteringInput(inputXml.getName(), inputXml
							.getFiltering().getClassURI(), inputXml
							.getFiltering().getMinCardinality(), inputXml
							.getFiltering().getMaxCardinality(),
							inputXml.getPropertiesAsStringArr());
				}
			}

			void addOutputsToService(OperationXmlObj operationXml)
					throws Exception {
				for (OutputXmlObj outputXml : operationXml.getOutputs()) {
					ProcessOutput out = new ProcessOutput(outputXml.getName());
					out.setParameterType(ReflectionsUtils
							.extractMYURIFieldFromClass(outputXml
									.getJavaClass()));
					out.setCardinality(outputXml.getMinCardinality(),
							outputXml.getMaxCardinality());
					getServiceProfile().addOutput(out);
					getServiceProfile().addSimpleOutputBinding(out,
							outputXml.getPropertiesAsStringArr());
				}
			}

			void addChangeEffectsToService(OperationXmlObj operationXml)
					throws Exception {
				for (ChangeEffectXmlObj changeEffectXml : operationXml
						.getChangeEffects()) {
					ValueXmlObj valueXml = changeEffectXml.getValue();
					String valueAsStr = valueXml.getValue();
					String valueCls = valueXml.getJavaClass();
					Object value = null;
					try {
						value = TypeMapper.getJavaInstance(valueAsStr,
								TypeMapper.getDatatypeURI(Class
										.forName(valueCls)));
					} catch (ClassNotFoundException e) {
						String errMsg = "Unable to populate change effect due to ["
								+ e.getMessage() + "]";
						throw new Exception(errMsg); // TODO: throw a better
						// exception
					}

					getServiceProfile().addChangeEffect(
							changeEffectXml.getPropertiesAsStringArr(), value);
				}
			}
		}

		// Extract the MY_URI field from the super class in ontology
		String superJavaClass = ReflectionsUtils
				.extractMYURIFieldFromClass(serviceXml.getSuperJavaClass());

		AndroidOntologyManagement.registerOntology(mc, new SimpleOntology(
				serviceXml.getUri(), superJavaClass, new ResourceFactory() {
					public Resource createInstance(String classURI,
							String instanceURI, int factoryIndex) {
						return new ServiceWrapper(instanceURI);
					}
				}));

		// Iterate over the operations - extract the variables related to a
		// specific operation
		for (OperationXmlObj operationXml : serviceXml.getOperations()) {
			String operationUri = operationXml.getUri();

			// Create anonymous class and overrides the 'getClassURI' method -
			// so this method will return the ontology uri
			ServiceWrapper service = new ServiceWrapper(operationUri);

			// Inputs
			service.addInputsToService(operationXml);

			// Outputs
			service.addOutputsToService(operationXml);

			// Change Effects
			service.addChangeEffectsToService(operationXml);

			// Add the ServiceProfile to the list
			profiles.add(service.getServiceProfile());
		}

		return profiles.toArray(new ServiceProfile[0]);
	}

	protected void handleServiceCaller(
			AndroidServiceCallerDataWrapper serviceCallerRegistration,
			AndroidServiceBusImpl androidServiceBus, Context context) {
		ServiceRequestGroundingXmlObj serviceGroundingXml = serviceCallerRegistration
				.getServiceRequestGroundingXmlObj();

		// Register ontologies
		registerOntologies(androidServiceBus.getModuleContext(),
				serviceGroundingXml);

		// Create AndroidServiceCallers - the c'tor will do all the registration
		// stuff
		boolean registerService = true;

		new AndroidServiceCallerProxy(androidServiceBus.getModuleContext(),
				registerService, serviceCallerRegistration.getPackageName(),
				serviceCallerRegistration.getGroundingID(),
				serviceCallerRegistration.getAndroidUniqueName(),
				serviceCallerRegistration.getServiceRequestGroundingXmlObj(),
				context);
	}

	private void registerOntologies(ModuleContext mc,
			ServiceRequestGroundingXmlObj serviceRequestGroundingXml) {
		registerOntologies(mc, serviceRequestGroundingXml.getOntologies());
	}

	private static void registerOntologies(ModuleContext mc, String[] ontologies) {

		ServiceBusOntology serviceOntology = new ServiceBusOntology();
		AndroidOntologyManagement.registerOntology(mc, serviceOntology);

		// Register the ontologies that the service grounding uses
		for (String ontologyClass : ontologies) {
			try {
				AndroidOntologyManagement.registerOntology(mc, ontologyClass);
			} catch (Throwable th) {
				String errMsg = "Error when registering ontology ["
						+ ontologyClass + "] due to [" + th.getMessage() + "]";
				Log.e(TAG, errMsg); // TODO: throw here an exception
			}
		}
	}
}
