package org.universAAL.android.receivers.system;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.universAAL.middleware.context.ContextEvent;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GCMReceiver extends WakefulBroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("GCMReceiver", "%%%%%%%%%%%%% RECEIVED!!!!! %%%%%%%%%%%%%");
		//It is a wakefulBr because it will call the MWService, and device should not sleep when sending the intent there!
		String method=intent.getStringExtra("method");
		if (method.equals("SENDC")) {
			Log.d("GCMReceiver",
					"RECEIVED CONTEXT EVENT FROM SERVER WITH SUBJ: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_SUBJECT)
							+ " PRED: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_PREDICATE)
							+ " OBJ: "
							+ intent.getStringExtra(ContextEvent.PROP_RDF_OBJECT));
		} else if (method.equals("CALLS")) {
			Log.d("GCMReceiver",
					"RECEIVED SERVICE CALL FROM SERVER WITH INPUTS: "
							+ intent.getExtras().keySet().toString());
			
			//Send response
			String servResp = "status=http://ontology.universAAL.org/uAAL.owl#call_succeeded\n"
					+ "call="+intent.getStringExtra("call")+"\n"
					+ "TURTLE\n "
					+ "@prefix : <http://ontology.universAAL.org/uAAL.owl#> . "
					+ ":BN000000 a :ServiceResponse ; "
					+ "  :callStatus :call_succeeded . "
					+ ":call_succeeded a :CallStatus . ";
			URL url=null;
			try {
				url = new URL("http://158.42.167.41:8181/universaal?auth=yo&method=RESPONSES&param="+URLEncoder.encode(servResp));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return;
			}
			//Do it in Asynctask because otherwise it wont let us
			new AsyncTask<URL, Void, Void>() {
				@Override
				protected Void doInBackground(URL... url) {
					HttpURLConnection httpConnection = null;
					try {
						httpConnection = (HttpURLConnection) url[0]
								.openConnection();
						httpConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
						httpConnection.setRequestMethod("POST");
						httpConnection.connect();
						Log.d("GCMReceiver","SENT TO SERVER: "+url);
						if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
							Log.e("GCMReceiver","ERROR REACHING SERVER"+httpConnection.getResponseMessage());
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						// close the connection and set all objects to null
						if (httpConnection != null)
							httpConnection.disconnect();
					}
					return null;
				}
			}.execute(url, null, null);
		}else{
			Log.d("GCMReceiver","RECEIVED SOMETHING??? FROM SERVER");
		}
	}

}
