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
package org.universAAL.middleware.android.buses.servicebus.servicecaller;

import java.lang.reflect.Constructor;
import java.util.List;

import org.universAAL.middleware.android.buses.common.AndroidNameParameterParser;
import org.universAAL.middleware.android.buses.common.AndroidOntologyManagement;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.android.buses.servicebus.persistence.tables.rows.WaitingCallRowDB;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ActionXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.RequiredOutputXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ServiceRequestGroundingXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecaller.xml.objects.ValueFilterXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.BundlePropertyXmlObj;
import org.universAAL.middleware.android.buses.servicebus.xml.objects.ChangeEffectXmlObj;
import org.universAAL.middleware.android.common.ReflectionsUtils;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owl.Service;

import dalvik.system.DexClassLoader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         May 18, 2012
 * 
 */
public class AndroidServiceCallerProxy extends ServiceCaller {

	private final static String TAG = AndroidServiceCallerProxy.class
			.getCanonicalName();

	private static final int NUM_OF_AMOUNTS_TO_GET_CALL = 3;
	private static final long INTERVAL_BTWEEN_GET_CALL_ATTEMPT_IN_MILLI_SECONDS = 1 * 1000;

	protected String packageName;
	protected String serviceRequestGroundingID;
	protected String androidUniqueName;
	protected ServiceRequestGroundingXmlObj serviceRequestGrounding;
	protected Context context;
	private String myID;

	public AndroidServiceCallerProxy(ModuleContext mc,
			boolean registerService, String packageName,
			String serviceRequestGroundingID, String androidUniqueName,
			ServiceRequestGroundingXmlObj serviceRequestGrounding,
			Context context) {
		super(mc);

		this.packageName = packageName;
		this.serviceRequestGroundingID = serviceRequestGroundingID;
		this.androidUniqueName = androidUniqueName;
		this.serviceRequestGrounding = serviceRequestGrounding;
		this.context = context;
		// PATCH In super(), it already gets registered in the bus, but because
		// the serviceGroundingID is not set yet, it is not properly registered
		// in android registry.
		// Therefore must deregister from the bus and register again, but this
		// time with serviceGroundingID set, so that it gets properly added to
		// the android registry
		theBus.unregister(this.busResourceURI, this);
		this.busResourceURI = theBus
				.register(mc, this, BusMemberType.requester);
		myID = this.busResourceURI;
		if (registerService) {
			// myID = androidServiceBus.register(this);
			// populateLocalID(myID);
			// myID=this.busResourceURI;
			// Register a broadcast receiver for each action and store it in
			// some container, in uninstall this should be unregistered!!
			// Once an action is invoked the broadcast receiver will be
			// triggered
			ServiceRequestsMngr.reigsterToServiceRequestGroundingActions(myID,
					serviceRequestGrounding, context);
		}
	}

	public AndroidServiceCallerProxy(ModuleContext mc, String packageName,
			String serviceRequestGroundingID, String androidUniqueName,
			ServiceRequestGroundingXmlObj serviceRequestGrounding,
			Context context, String memberID) {
		this(mc/* androidServiceBus */, false, packageName,
				serviceRequestGroundingID, androidUniqueName,
				serviceRequestGrounding, context);

		// In this c'tor, no registration is performed, therefore populate the
		// ID's
		// .busResourceURI remains the same as the first time it gets (failed)
		// registered!!! In theory it is not used anywhere, what matters for
		// android port is myID, but take into account.
		// myID =
		// this.busResourceURI;//Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX +
		// memberID;
		// populateLocalID(memberID);
	}

	@Override
	public void communicationChannelBroken() {
		// This is called when the bus is being stopped.
		// Do nothing...
	}

	@Override
	public void handleResponse(String reqID, ServiceResponse response) {
		// Get and remove call from the DB
		WaitingCallRowDB waitingCall = getWaitingCall(reqID);

		if (null == waitingCall) {
			Log.d(TAG,
					"No waiting call ["
							+ reqID
							+ "] was found! Since the requestor doesn't wait for response!");
			return;
		}

		// Get the action itself
		ActionXmlObj action = serviceRequestGrounding
				.getActionByName(waitingCall.getActionName());

		// Handle the response only if there are required outputs
		if (CallStatus.succeeded.equals(response.getCallStatus())
				&& !action.getRequiredOutputs().isEmpty()) {
			// The action + category for the response intent
			String replyToAction = waitingCall.getReplyToAction();
			String replyToCategory = waitingCall.getReplyToCategory();

			Intent responseIntent = new Intent(replyToAction);
			responseIntent.addCategory(replyToCategory);

			populateIntentWithOutputs(response, action, responseIntent);

			// Send this as a broadcast message
			context.sendBroadcast(responseIntent);
		}
	}

	private void populateIntentWithOutputs(ServiceResponse response,
			ActionXmlObj action, Intent responseIntent) {
		for (RequiredOutputXmlObj requiredOutput : action.getRequiredOutputs()) {
			List resultList = response.getOutput(requiredOutput.getUri(), true);

			String androidParamJavaClass = requiredOutput
					.getAndroidExtraParameterJavaClass();
			String androidParameterName = requiredOutput
					.getAndroidExtraParameter();
			if (List.class.getCanonicalName().equals(androidParamJavaClass)) {
				// This is a list
				populateIntentWithAListOutput(resultList, responseIntent,
						androidParameterName);
			} else if (Bundle.class.getCanonicalName().equals(
					androidParamJavaClass)) {
				// This suppose to be mapped into android bundle
				// TODO: validate that the resultList has object, if not throw
				// an exception
				populateIntentWithABundleOutput((Resource) resultList.get(0),
						responseIntent, androidParameterName,
						requiredOutput.getBundleProperties());
			} else {
				// Treat this as a string
				populateIntentWithAStringOutput(resultList.get(0),
						responseIntent, androidParameterName);
			}
		}
	}

	private void populateIntentWithAListOutput(List resultList,
			Intent responseIntent, String androidExtraParameterName) {
		String[] resultAsStringArr = new String[resultList.size()];
		for (int i = 0; i < resultList.size(); i++) {
			resultAsStringArr[i] = extractStringFromOutputObject(resultList
					.get(i));
		}

		// Add the value to the intent
		responseIntent.putExtra(androidExtraParameterName, resultAsStringArr);
	}

	private void populateIntentWithABundleOutput(Resource resource,
			Intent responseIntent, String androidParameterName,
			List<BundlePropertyXmlObj> bundleProperties) {
		// Build the bundle
		Bundle outputBundle = new Bundle();

		for (BundlePropertyXmlObj bundleProperty : bundleProperties) {
			// Extract the property from the resource
			String propertyUri = bundleProperty.getUri();
			String propertyValue = String.valueOf(resource
					.getProperty(propertyUri));

			// Extract the android name
			String androidName = bundleProperty.getAndroidName();

			// Parse it
			AndroidNameParameterParser parser = AndroidNameParameterParser
					.parseAndroidName(androidName);
			String androidParameter = parser.getAndroidParameter();

			if (null != parser) {
				if (propertyValue.contains(parser
						.getAndroidNameWithoutParameter())) {
					String androidValue = propertyValue.substring(parser
							.getAndroidNameWithoutParameter().length(),
							propertyValue.length());

					// Add this to the output bundle
					outputBundle.putString(androidParameter, androidValue);
				}
			}
		}

		// Add the bundle to the intent
		responseIntent.putExtra(androidParameterName, outputBundle);
	}

	private void populateIntentWithAStringOutput(Object object,
			Intent responseIntent, String androidParameterName) {
		String value = extractStringFromOutputObject(object);

		responseIntent.putExtra(androidParameterName, value);
	}

	private String extractStringFromOutputObject(Object object) {
		String value = "";
		if (object instanceof Resource) {
			value = ((Resource) object).getURI();
		} else {
			value = object.toString();
		}

		return value;
	}

	private WaitingCallRowDB getWaitingCall(String reqID) {
		WaitingCallRowDB waitingCall = null;
		int i = 0;
		while (i++ < NUM_OF_AMOUNTS_TO_GET_CALL) {
			waitingCall = ((AndroidServiceBusImpl) theBus)
					.getWaitingCall(reqID);
			if (null != waitingCall) {
				break;
			}
			try {
				Thread.sleep(INTERVAL_BTWEEN_GET_CALL_ATTEMPT_IN_MILLI_SECONDS);
			} catch (InterruptedException e) {
				// Do nothing, just keep trying
			}
		}
		return waitingCall;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getServiceRequestGroundingID() {
		return serviceRequestGroundingID;
	}

	public String getAndroidUniqueName() {
		return androidUniqueName;
	}

	public ServiceRequestGroundingXmlObj getServiceRequestGrounding() {
		return serviceRequestGrounding;
	}

	public WaitingCall sendServiceRequest(String actionName,
			Bundle extrasSectionOfOriginalServiceCallerRequest) {
		// Get the action itself by the action name
		ActionXmlObj actionXml = serviceRequestGrounding
				.getActionByName(actionName);

		// Building the service request
		ServiceRequest serviceRequest;
		try {
			serviceRequest = createServiceRequest(actionXml,
					extrasSectionOfOriginalServiceCallerRequest);
		} catch (Exception e) {
			Log.e(TAG, "Error when creating service request for action ["
					+ actionXml.getAndroidAction() + "]: " + e.getMessage());
			return null;
		}

		// Send the service call, NOTE: we don't call the 'call' method, since
		// this method is waiting for the response
		// We want a different mechanism

		String callID = sendRequest(serviceRequest);

		WaitingCall waitingCall = null;
		if (null != callID) {
			// Extract the replyToAction
			String replyToActionParameterName = actionXml
					.getAndroidReplyToAction();
			String replyToActionParameterValue = extrasSectionOfOriginalServiceCallerRequest
					.getString(replyToActionParameterName);

			// Extract the replyToCategory
			String replyToCategoryParameterName = actionXml
					.getAndroidReplyToCategory();
			String replyToCategoryParameterValue = extrasSectionOfOriginalServiceCallerRequest
					.getString(replyToCategoryParameterName);

			waitingCall = new WaitingCall(callID, replyToActionParameterValue,
					replyToCategoryParameterValue);
		}

		return waitingCall;
	}

	@Override
	public ServiceResponse call(ServiceRequest request) {
		throw new UnsupportedOperationException(
				"This method should not be called for ["
						+ AndroidServiceCallerProxy.class.getCanonicalName());
	}

	private ServiceRequest createServiceRequest(ActionXmlObj actionXml,
			Bundle extrasSectionOfOriginalServiceCallerRequest)
			throws Exception {
		Service service = null;
		try {
			// Create the service class
			Class serviceJavaClass = Class.forName(actionXml
					.getServiceJavaClass());
			service = (Service) serviceJavaClass.newInstance();
		} catch (ClassNotFoundException ex) {
			// Try with ontology jars
			try {
				DexClassLoader cl = AndroidOntologyManagement
						.getOntLoader(actionXml
								.getServiceJavaClass());
				// reflection from ont jar
				service = (Service) Class.forName(actionXml
						.getServiceJavaClass(), true, cl)
						.newInstance();
			} catch (Exception e) {
				String errMsg = "Unable to create java class (again) ["
						+ actionXml.getServiceJavaClass() + "] due to ["
						+ e.getMessage() + "]";
				Log.e(TAG, errMsg);
				throw new Exception(errMsg);
			}
		} catch (Throwable th) {
			String errMsg = "Unable to create java class ["
					+ actionXml.getServiceJavaClass() + "] due to ["
					+ th.getMessage() + "]";
			throw new Exception(errMsg);
		}

		// Create the Service Request
		ServiceRequest serviceRequest = new ServiceRequest(service, null);

		// Add value filters
		populateValueFilters(serviceRequest, actionXml.getValueFilters(),
				extrasSectionOfOriginalServiceCallerRequest);

		// Add required outputs
		populateRequiredOutputs(serviceRequest, actionXml.getRequiredOutputs());

		// Add change effects
		populateChangeEffects(serviceRequest, actionXml.getChangeEffects());

		return serviceRequest;
	}

	private void populateValueFilters(ServiceRequest serviceRequest,
			List<ValueFilterXmlObj> valueFilters,
			Bundle extrasSectionOfOriginalServiceCallerRequest)
			throws Exception {
		for (ValueFilterXmlObj valueFilter : valueFilters) {
			String[] refPath = valueFilter.getPropertiesAsStringArr();

			// Extract the value from the extras
			String parameterValue = extrasSectionOfOriginalServiceCallerRequest
					.getString(valueFilter.getAndroidExtraParameter());

			// Create the object that receive the uri
			Constructor ctor = ReflectionsUtils
					.createCtorThatReceiveStringParam(valueFilter
							.getJavaClass());
			Object object = ReflectionsUtils.invokeCtorWithStringParam(ctor,
					parameterValue);

			// Add the value filter to the service
			serviceRequest.addValueFilter(refPath, object);
		}
	}

	private void populateRequiredOutputs(ServiceRequest serviceRequest,
			List<RequiredOutputXmlObj> requiredOutputs) {
		for (RequiredOutputXmlObj requiredOutput : requiredOutputs) {
			String[] fromProp = requiredOutput.getPropertiesAsStringArr();

			serviceRequest.addRequiredOutput(requiredOutput.getUri(), fromProp);
		}
	}

	private void populateChangeEffects(ServiceRequest serviceRequest,
			List<ChangeEffectXmlObj> changeEffects) throws Exception {
		for (ChangeEffectXmlObj changeFilter : changeEffects) {
			String[] ppath = changeFilter.getPropertiesAsStringArr();

			// Create the value
			Object value;
			try {
				value = TypeMapper.getJavaInstance(changeFilter.getValue()
						.getValue(), TypeMapper.getDatatypeURI(Class
						.forName(changeFilter.getValue().getJavaClass())));
			} catch (ClassNotFoundException e) {
				String errMsg = "Unable to create java class ["
						+ changeFilter.getValue().getJavaClass() + "] due to ["
						+ e.getMessage() + "]";
				Log.e(TAG, errMsg);
				throw new Exception(errMsg);
			}

			// Add the change effect
			serviceRequest.addChangeEffect(ppath, value);
		}
	}

	public static class WaitingCall {
		private String callID;
		private String replyToActionParameterValue;;
		private String replyToCategoryParameterValue;

		public WaitingCall(String callID, String replyToActionParameterValue,
				String replyToCategoryParameterValue) {
			this.callID = callID;
			this.replyToActionParameterValue = replyToActionParameterValue;
			this.replyToCategoryParameterValue = replyToCategoryParameterValue;
		}

		public String getCallID() {
			return callID;
		}

		public String getReplyToActionParameterValue() {
			return replyToActionParameterValue;
		}

		public String getReplyToCategoryParameterValue() {
			return replyToCategoryParameterValue;
		}
	}
}
