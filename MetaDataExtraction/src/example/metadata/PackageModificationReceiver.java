/*
	OCO Source Materials
            © Copyright IBM Corp. 2011

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
package example.metadata;

/**
 * The example of extracting meta-data of Android components within manifest.xml file on package install
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a>
 *
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageModificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("MetaDataExtract",
				"PackageModificationReceiver received Intent: "
						+ intent.toString()); // could specify install /
												// uninstall / update action
												// from this
		String packageName = intent.getDataString(); // extracting only package
														// name, but some salt
														// added
		packageName = packageName.replaceFirst("package:", ""); // removing the
																// "salt" to get
																// pure package
																// name

		Intent metaExtractorIntent = new Intent(); // constructing intent for
													// service
		metaExtractorIntent.setClassName("example.metadata",
				"example.metadata.MetaDataExtractorService"); // targeting
																// intent to
																// only our
																// extractor
																// class
		metaExtractorIntent.putExtra("packageName", packageName); // attaching
																	// package
																	// name to
																	// intent
																	// which
																	// will be
																	// passed to
																	// service

		context.startService(metaExtractorIntent); // starting out service for
													// extraction
	}
}