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
package org.universAAL.middleware.android.buses.contextbus.data;

import org.universAAL.middleware.android.buses.contextbus.IGroundingIDWrapper;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;

/**
 * 
 * This class is just used to wrap the grounding ID (unique ID to get the later
 * on the bus memeber from the DB) This class doesn't contain the subscriber
 * itself.
 * 
 * Merely a grounding ID that will be used to extract the REAL subscriber from
 * the DB
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 20, 2012
 * 
 */
public class AndroidContextSubscriberWrapper extends ContextSubscriber implements
	IGroundingIDWrapper {

    private String groundingID;

    public AndroidContextSubscriberWrapper(String groundingID) {
	super(null, null, false);

	this.groundingID = groundingID;
    }

    @Override
    public void communicationChannelBroken() {
	// Do nothing
    }

    @Override
    public void handleContextEvent(ContextEvent event) {
	// Do nothing
    }

    public String getGroundingID() {
	return groundingID;
    }
}
