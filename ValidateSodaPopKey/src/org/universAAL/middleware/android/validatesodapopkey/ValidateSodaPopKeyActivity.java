package org.universAAL.middleware.android.validatesodapopkey;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class ValidateSodaPopKeyActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onButtonCheckSodaPopKeyClicked(View v){
		final String fileName = "/mnt/sdcard/Android/data/org.universAAL.middleware/files";
		
		String tmpMsg = "File [" + fileName + "] ";
		File file = new File(fileName);
		if (file.exists()) {
			tmpMsg += "exists !!!";
		} else {
			tmpMsg += "does NOT exist !!!";
		}
		final String formattedMsg = tmpMsg;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(formattedMsg)
		       .setCancelable(false).
		       setNegativeButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
}