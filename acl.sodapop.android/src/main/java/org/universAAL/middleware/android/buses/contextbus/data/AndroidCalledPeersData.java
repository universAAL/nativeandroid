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
package org.universAAL.middleware.android.buses.contextbus.data;

import java.util.Arrays;
import java.util.List;

import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.CalledPeersRowDB;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.ContextEventPatternRowDB;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.data.ICalledPeers;
import org.universAAL.serialization.turtle.TurtleParser;

import android.content.Context;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 21, 2012
 * 
 */
public class AndroidCalledPeersData extends AbstractAndroidContextBusPersistable implements
	ICalledPeers {

    private String messageID;

    public AndroidCalledPeersData(Context context) {
	super(context);
    }

    public void setMessageID(String messageID) {
	this.messageID = messageID;
    }

    public int getNumOfCalledPeers() {
	sqliteMngr.open();
	try {
	    // Persist
	    CalledPeersRowDB row = queryForCalledPeer();
	    return row.getNumOfCalledPeers();
	} finally {
	    sqliteMngr.close();
	}
    }

    public void setNumOfCalledPeers(int numOfCalledPeers) {
	sqliteMngr.open();
	try {
	    sqliteMngr.updateCalledPeersWithNumOfCalledPeers(messageID, numOfCalledPeers);
	} finally {
	    sqliteMngr.close();
	}
    }

    public void reduceNumOfCalledPeers() {
	sqliteMngr.open();
	try {
	    // Persist
	    CalledPeersRowDB row = queryForCalledPeer();
	    sqliteMngr.updateCalledPeersWithNumOfCalledPeers(messageID,
		    row.getNumOfCalledPeers() - 1);
	} finally {
	    sqliteMngr.close();
	}
    }

    public void resetCalledPeers() {
	sqliteMngr.open();
	try {
	    // Persist
	    sqliteMngr.updateCalledPeersWithNumOfCalledPeers(messageID, 0);
	} finally {
	    sqliteMngr.close();
	}
    }

    public boolean gotResponsesFromAllPeers() {
	return getNumOfCalledPeers() <= 0;
    }

    public void addProvisions(List contextEventPatterns) {
	String[] serializedContextEventPatterns = serializeContextEventPatterns(contextEventPatterns);

	sqliteMngr.open();
	try {
	    // Persist
	    sqliteMngr.addContextEventPatterns(messageID, serializedContextEventPatterns);
	} finally {
	    sqliteMngr.close();
	}
    }

    public List getProvisions() {
	List provisions = null;
	sqliteMngr.open();
	try {
	    // Persist
	    ContextEventPatternRowDB[] rows = sqliteMngr.queryForContextEventPatterns(messageID);
	    return deserializeContextEventPatterns(rows);
	} finally {
	    sqliteMngr.close();
	}
    }

    private CalledPeersRowDB queryForCalledPeer() {
	return sqliteMngr.queryForCalledPeers(messageID);
    }

    private static String[] serializeContextEventPatterns(List contextEventPatterns) {
	String[] serializedContextEventPatterns = new String[contextEventPatterns.size()];

	// Initiate a parser
	TurtleParser parser = new TurtleParser();

	for (int i = 0; i < contextEventPatterns.size(); i++) {
	    ContextEventPattern contextEventPattern = (ContextEventPattern) contextEventPatterns
		    .get(i);

	    // Serialize current ContextEventPattern
	    serializedContextEventPatterns[i] = parser.serialize(contextEventPattern);
	}

	return serializedContextEventPatterns;
    }

    private static List deserializeContextEventPatterns(
	    ContextEventPatternRowDB[] contextEventPatternRows) {
	// Initiate a parser
	TurtleParser parser = new TurtleParser();

	// Set the context event patterns
	ContextEventPattern[] contextEventPatterns = new ContextEventPattern[contextEventPatternRows.length];
	for (int i = 0; i < contextEventPatternRows.length; i++) {
	    contextEventPatterns[i] = (ContextEventPattern) parser
		    .deserialize(contextEventPatternRows[i].getContextEventPattern());
	}

	// Add to the list
	return Arrays.asList(contextEventPatterns);
    }
}