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

import java.util.List;
import java.util.Vector;

import org.universAAL.middleware.android.buses.contextbus.persistence.AbstractAndroidContextBusPersistable;
import org.universAAL.middleware.android.buses.contextbus.persistence.tables.rows.AllProvisionsRowDB;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.data.IAllProvisionData;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;


/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Jun 24, 2012
 * 
 */
public class AndroidAllProvisionsData extends
		AbstractAndroidContextBusPersistable implements IAllProvisionData {

	public void addContextEventPatterns(List contextEventPatterns) {
		// Serialize the context evnet patterns
		String[] contextEventPatternsAsStrings = serializeContextEventPatterns(contextEventPatterns);

		// Persist
		for (String contextEventPatternAsString : contextEventPatternsAsStrings) {
			sqliteMngr.addAllProvision(contextEventPatternAsString);
		}
	}

	public Vector getContextEventPatterns() {
		Vector<ContextEventPattern> contextEventPatterns = new Vector<ContextEventPattern>();

		AllProvisionsRowDB[] allProvisionsRows = null;
		// Query
		allProvisionsRows = sqliteMngr.queryForAllProvision();

		TurtleSerializer parser = new TurtleSerializer();

		for (AllProvisionsRowDB row : allProvisionsRows) {
			contextEventPatterns.add(createContextEventPattern(row, parser));
		}

		return contextEventPatterns;
	}

	private String[] serializeContextEventPatterns(List contextEventPatterns) {
		String[] contextEventPatternsStrings = new String[contextEventPatterns
				.size()];

		TurtleSerializer parser = new TurtleSerializer();

		for (int i = 0; i < contextEventPatterns.size(); i++) {
			contextEventPatternsStrings[i] = parser
					.serialize(contextEventPatterns.get(i));
		}

		return contextEventPatternsStrings;
	}

	private ContextEventPattern createContextEventPattern(
			AllProvisionsRowDB row, TurtleSerializer parser) {
		return (ContextEventPattern) parser.deserialize(row
				.getContextEventPatternAsString());
	}
}
