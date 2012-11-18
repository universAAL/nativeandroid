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

import java.util.Vector;

import org.universAAL.middleware.android.buses.contextbus.IGroundingIDWrapper;
import org.universAAL.middleware.android.buses.contextbus.contextsubscriber.AndroidContextSubscriberProxy;
import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.ContextFiltererRowDB;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.impl.ContextStrategy.ContextFilterer;
import org.universAAL.serialization.turtle.TurtleParser;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 24, 2012
 * 
 */
public class AndroidFiltererContainerData extends AbstractAndroidContextBusPersistable implements
	IAndroidFiltererContainerData {

    private String containerKey;
    private String containerType = ContainerType.notIndexedContainer.toString();

    public AndroidFiltererContainerData(Context context, String containerKey) {
	super(context);

	this.containerKey = containerKey;
    }

    public String getContainerKey() {
	return containerKey;
    }

    public void setContainerType(String containerType) {
	this.containerType = containerType;
    }

    public String getContainerType() {
	return containerType;
    }

    public void addFilterer(ContextFilterer contextFilterer) {

	// Cast it to AndroidContextSubscriber
	AndroidContextSubscriberProxy androidSubscriber = (AndroidContextSubscriberProxy) contextFilterer.s;

	// Extract the grounding ID
	String groundingID = androidSubscriber.getGroundingID();

	// Serialize the context event
	String serializedContextEvent = serializeContextEventPattern(contextFilterer.f);

	sqliteMngr.open();
	try {
	    // Add the filterer
	    sqliteMngr.addContextFilterer(containerKey, containerType, groundingID,
		    serializedContextEvent);
	} finally {
	    sqliteMngr.close();
	}
    }

    public Vector getFilterers() {
	ContextFiltererRowDB[] contextFiltererRows = null;
	sqliteMngr.open();
	try {
	    // Query for the filterers
	    contextFiltererRows = sqliteMngr.queryForContextFilterers(containerKey, containerType);
	} finally {
	    sqliteMngr.close();
	}

	// Initiate the results
	Vector<ContextFilterer> filterers = new Vector<ContextFilterer>();

	for (ContextFiltererRowDB row : contextFiltererRows) {
	    filterers.add(createContextFilterer(row));
	}

	return filterers;
    }

    public void removeFilterers(ContextSubscriber subscriber) {
	// Cast to the IGroundingIDWrapper interface
	IGroundingIDWrapper groundingIDWrapper = (IGroundingIDWrapper) subscriber;

	sqliteMngr.open();
	try {
	    // Remove the filterers that are relevant to the given subscriber
	    sqliteMngr.removeContextFilterers(containerKey, containerType,
		    groundingIDWrapper.getGroundingID());
	} finally {
	    sqliteMngr.close();
	}
    }

    private String serializeContextEventPattern(ContextEventPattern contextEventPattern) {
	// Initiate a parser
	TurtleParser parser = new TurtleParser();

	return parser.serialize(contextEventPattern);
    }

    private ContextFilterer createContextFilterer(ContextFiltererRowDB contextFiltererRow) {
	ContextFilterer contextFilterer = new ContextFilterer();

	// Set the subscriber
	contextFilterer.s = new AndroidContextSubscriberWrapper(contextFiltererRow.getGroundingID());

	// Set the context event pattern
	contextFilterer.f = (ContextEventPattern) new TurtleParser().deserialize(contextFiltererRow
		.getContextEventAsString());

	return contextFilterer;
    }
}
