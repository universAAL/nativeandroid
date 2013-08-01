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
package org.universAAL.middleware.android.buses.servicebus.servicecallee;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.android.buses.common.AndroidNameParameterParser;
import org.universAAL.middleware.android.buses.servicebus.impl.AndroidServiceBusImpl;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.FilteringInputXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.OperationXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.OutputXmlObj;
import org.universAAL.middleware.android.buses.servicebus.servicecallee.xml.objects.ServiceGroundingXmlObj;
import org.universAAL.middleware.android.common.ReflectionsUtils;
import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.bus.member.BusMemberType;
import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 22, 2012
 * 
 */
public class AndroidServiceCalleeProxy extends ServiceCallee {

	private final static String TAG = AndroidServiceCalleeProxy.class
			.getCanonicalName();

	protected String packageName;
	protected String serviceGroundingID;
	protected String androidServiceName;
	protected ServiceGroundingXmlObj serviceGrounding;
	protected Context context;
	private String myID;

	public AndroidServiceCalleeProxy(ModuleContext mc/*
													 * AndroidServiceBusImpl
													 * androidServiceBus
													 */,
			ServiceProfile[] realizedServices, String packageName,
			String serviceGroundingID, String androidServiceName,
			ServiceGroundingXmlObj serviceGrounding, Context context) {
		super(mc/* androidServiceBus */, realizedServices);

		this.packageName = packageName;
		this.serviceGroundingID = serviceGroundingID;
		this.androidServiceName = androidServiceName;
		this.serviceGrounding = serviceGrounding;
		this.context = context;

		// PATCH In super(), it already gets registered in the bus, but because
		// the serviceGroundingID is not set yet, it is not properly registered
		// in android registry.
		// Therefore must deregister from the bus and register again, but this
		// time with serviceGroundingID set, so that it gets properly added to
		// the android registry
		theBus.unregister(this.busResourceURI, this);// Fails if DB not erased
														// at start!
		this.busResourceURI = theBus
				.register(mc, this, BusMemberType.responder);
		myID = this.busResourceURI;
		if (null != realizedServices) {
			// This was done at super, but failed because of patch
			addNewServiceProfiles(realizedServices);
			// myID = bus.register(this, realizedServices);
			// myID=this.busResourceURI;
			// populateLocalID(myID);
		}
	}

	public AndroidServiceCalleeProxy(ModuleContext mc/*
													 * AndroidServiceBus
													 * androidServiceBus
													 */, String packageName,
			String serviceGroundingID, String androidServiceName,
			ServiceGroundingXmlObj serviceGrounding, Context context,
			String memberID) {
		this(mc/* androidServiceBus */, null, packageName, serviceGroundingID,
				androidServiceName, serviceGrounding, context);

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
	public void handleRequest(BusMessage m) {
		if (null != m && m.getContent() instanceof ServiceCall) {
			// ServiceResponse sr = handleCall((ServiceCall) m.getContent());
			ServiceResponse sr = handleCall(m);
			if (null != sr) {
				BusMessage reply = m.createReply(sr);
				if (null != reply)
					((AndroidServiceBusImpl) theBus).brokerReply(myID, reply);
			}
		}
	}

	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		// This method is not in used, since the 'handleRequest' was overridden
		// and calls the other handleCall method
		throw new UnsupportedOperationException(
				"This method should NOT be called; use handleCall(Message message)");
	}

	public ServiceResponse handleCall(BusMessage message) {
		ServiceResponse response = null;

		ServiceCall serviceCall = (ServiceCall) message.getContent();

		// Get UAAL Operation
		String uAALOperation = serviceCall.getProcessURI();
		OperationXmlObj foundOperation = serviceGrounding
				.getOperationByUri(uAALOperation);

		if (null == foundOperation) {
			String errMsg = "No operations was found for [" + uAALOperation
					+ "]";
			Log.e(TAG, errMsg);

			response = new ServiceResponse(CallStatus.serviceSpecificFailure);
			response.addOutput(new ProcessOutput(
					ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, errMsg));
		} else {

			// Create intent
			Intent intent = new Intent();

			// Extract the android action
			intent.setAction(foundOperation.getAndroidAction());

			// Extract the android category
			intent.addCategory(foundOperation.getAndroidCategory());

			// Handle inputs
			boolean doesItFitToCallee = populateIntentWithInputs(serviceCall,
					foundOperation, intent);

			// Check if the operation supports a reply back
			if (StringUtils.isEmpty(foundOperation.getAndroidReplyToAction())
					|| StringUtils.isEmpty(foundOperation
							.getAndroidReplyToCategory())) {
				// In that case no need to wait for a response for the request,
				// merely assume that the request was handled successfully
				response = new ServiceResponse(CallStatus.succeeded);
			} else if (!doesItFitToCallee) {
				response = new ServiceResponse(
						CallStatus.serviceSpecificFailure);
			} else {
				// In that case a broadcast receiver should be registered to
				// listen to the response
				// NOTE also that the response will be null in that case, this
				// means we didn't send a response back!
				populateIntentWithOutputsAndRegisterRecevier(message,
						foundOperation, intent);
			}

			if (doesItFitToCallee) {
				// Send it to the callee only if fits to the callee
				// Accordingly start the Activity / Service
				switch (serviceGrounding.getAndroidServiceType()) {
				case ACTIVITY:
					context.startActivity(intent);
					break;
				case SERVICE:
					context.startService(intent);
					break;
				}
			} else {
				Log.i(TAG, "The request [" + message.getID()
						+ "] doesn't fit to the callee, therefore ignore it");
			}
		}

		return response;
	}

	public void handleResponse(String messageIDInReplyTo,
			String operationNameToRespondTo, String replyTo, Bundle extras) {

		try {
			// Get the operation name
			OperationXmlObj foundOperation = serviceGrounding
					.getOperationByUri(operationNameToRespondTo);

			// TODO: check that the operation was found

			// Initiate the ServiceResponse with a success status
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);

			// Iterate over the outputs
			for (OutputXmlObj output : foundOperation.getOutputs()) {
				// Get the output android name
				String outputAndroidName = output.getAndroidName();

				// The output URI
				String outputURI = output.getName();

				// Parse the android name
				AndroidNameParameterParser parser = AndroidNameParameterParser
						.parseAndroidName(outputAndroidName);

				// Check the output cardinality
				CardinalityType cardinality = CardinalityType.getCardinality(
						output.getMinCardinality(), output.getMaxCardinality());

				switch (cardinality) {
				case Single:
					String value = extras.getString(parser
							.getAndroidParameter());
					List<Object> objectsExpectedSingleOne = populateOutputObjects(
							output, new String[] { value }, parser);
					sr.addOutput(new ProcessOutput(outputURI,
							objectsExpectedSingleOne.get(0)));
					break;
				case Multi: // TODO: here you should take the type from the xml
					// and by reflection initate it
					String[] values = extras.getStringArray(parser
							.getAndroidParameter());
					List<Object> objectsExpecteMulti = populateOutputObjects(
							output, values, parser);
					sr.addOutput(new ProcessOutput(outputURI,
							objectsExpecteMulti));
					break;
				}
			}

			// Create the reply
			BusMessage reply = BusMessage.createP2PReply(messageIDInReplyTo,
					new PeerCard(replyTo), sr, theBus);

			// Send the response
			((AndroidServiceBusImpl) theBus).brokerReply(myID, reply);
		} catch (Exception e) {
			Log.e(TAG, "Error when building the response [" + e.getMessage()
					+ "]");
		}
	}

	private List<Object> populateOutputObjects(OutputXmlObj output,
			String[] values, AndroidNameParameterParser parser)
			throws Exception {
		List<Object> objects = new ArrayList<Object>();

		Constructor constructor = ReflectionsUtils
				.createCtorThatReceiveStringParam(output.getJavaClass());

		for (String value : values) {
			Object obj = ReflectionsUtils.invokeCtorWithStringParam(
					constructor, parser.getAndroidNameWithoutParameter()
							+ value);
			objects.add(obj);
		}

		return objects;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getServiceGroundingID() {
		return serviceGroundingID;
	}

	public String getAndroidServiceName() {
		return androidServiceName;
	}

	public ServiceGroundingXmlObj getServiceGrounding() {
		return serviceGrounding;
	}

	public void populateServiceCalleeID(String memberID) {
		myID = memberID;

		// populateLocalID(myID);
	}

	private void populateIntentWithOutputsAndRegisterRecevier(
			BusMessage message, OperationXmlObj operation, Intent intent) {
		// The action that is set in the IntentFilter of the broadcast receiver
		// is the message ID
		String messageIDAsAction = message.getID();

		// Extract the peer to reply to
		PeerCard replyTo = message.getSender();// .getSource();

		// Category
		String category = Intent.CATEGORY_DEFAULT;

		// Prepare the broadcast receiver
		registerRequestToBroadcastReceiver(messageIDAsAction,
				operation.getUri(), category, replyTo.toString());

		// Update the intent
		intent.putExtra(operation.getAndroidReplyToAction(), messageIDAsAction);
		intent.putExtra(operation.getAndroidReplyToCategory(), category);
	}

	private void registerRequestToBroadcastReceiver(String messageIDAsAction,
			String operationName, String category, String replyTo) {

		// Initiate the receiver
		BroadcastReceiver receiver = new ServiceCalleeResponsesBroadcastReceiver(
				myID, operationName, replyTo);

		// Intent filter
		IntentFilter filter = new IntentFilter(messageIDAsAction);
		filter.addCategory(category);

		// Register the receiver
		context.registerReceiver(receiver, filter);

		Log.d(TAG, "A registration to receiver has been performed: Action ["
				+ messageIDAsAction + "]; Category [" + category
				+ "]; ReplyTo [" + replyTo + "]");
	}

	private static boolean populateIntentWithInputs(ServiceCall call,
			OperationXmlObj operation, Intent intent) {
		// If not inputs - then for sure it will be fit to the callee
		boolean fitToCallee = operation.getFilteringInputs().size() > 0 ? false
				: true;

		for (FilteringInputXmlObj input : operation.getFilteringInputs()) {
			// Get the input name
			Object inputValue = call.getInputValue(input.getName());
			String inputValueAsString = "";
			if (inputValue instanceof Resource) {
				inputValueAsString = ((Resource) inputValue).getURI();
			} else {
				inputValueAsString = inputValue.toString();
			}

			// Extract the android parameter
			String inputAndroidName = input.getAndroidName();

			// Parse the android name
			AndroidNameParameterParser parser = AndroidNameParameterParser
					.parseAndroidName(inputAndroidName);

			// Make sure if the input is relevant to the callee
			if (null != parser) {
				if (inputValueAsString.contains(parser
						.getAndroidNameWithoutParameter())) {
					String inputAndroidValue = inputValueAsString.substring(
							parser.getAndroidNameWithoutParameter().length(),
							inputValueAsString.length());

					// Add the input value to the intent
					intent.putExtra(parser.getAndroidParameter(),
							inputAndroidValue);

					// If found one fit input than it fits to the callee
					fitToCallee = true;
				}
			}
		}

		return fitToCallee;
	}

	private static enum CardinalityType {
		Single, Multi;

		public static CardinalityType getCardinality(int minCardinality,
				int maxCardinality) {
			if (1 == minCardinality && 1 == maxCardinality) {
				return Single;
			} else {
				return Multi; // TODO: need to perform some validations here
			}
		}
	}
}
