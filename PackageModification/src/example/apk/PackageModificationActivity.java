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
package example.apk;

/**
 * The example of calling Android integrated services to install / uninstall *.apk packages
 * 
 * @author kestutis - <a href="mailto:kestutis@il.ibm.com">Kestutis Dalinkevicius</a>
 *
 */
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

public class PackageModificationActivity extends Activity {
	
	private String apkFileName = "Receiver1.apk";
	private String apkPackageName = "example.broadcast1";
	private String path = Environment.getExternalStorageDirectory()+ "/" + apkFileName;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void install(View V){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
        	Uri.fromFile(new File(path)),
        	"application/vnd.android.package-archive"
        );
        startActivity(intent);
    }
    
    public void remove(View V){
        Uri packageURI = Uri.parse("package:"+apkPackageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }    
}