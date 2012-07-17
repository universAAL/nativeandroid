/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
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
package org.universAAL.middleware.android.buses.contextbus;

import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.data.factory.AndroidContextStrategyDataFactory;
import org.universAAL.middleware.android.localsodapop.AbstractSodaPopAndroidImpl;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.data.factory.IContextStrategyDataFactory;
import org.universAAL.middleware.context.impl.ContextStrategy;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.msg.Message;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 17, 2012
 * 
 */
public class AndroidContextStrategy extends ContextStrategy {

    public AndroidContextStrategy(SodaPop sodapop) {
	super(sodapop);
    }

    @Override
    protected IContextStrategyDataFactory createContextStrategyDataFactory() {
	// TODO: consider how get context in other way (not via the sodapop)
	Context context = ((AbstractSodaPopAndroidImpl) sodapop).getContext();

	return new AndroidContextStrategyDataFactory(context);
    }

    @Override
    protected void handleEvent(ContextSubscriber contextSubscriber, Message msg) {
	// Cast to bus member
	BusMember busMember = (BusMember) contextSubscriber;

	// Get the bus member ID
	String busMemberID = bus.getBusMemberID(busMember);

	// Query for the bus member
	AndroidContextSubscriberProxy androidContextSubscriber = (AndroidContextSubscriberProxy) bus
		.getBusMember(busMemberID);

	androidContextSubscriber.handleEvent(msg);
    }
}
