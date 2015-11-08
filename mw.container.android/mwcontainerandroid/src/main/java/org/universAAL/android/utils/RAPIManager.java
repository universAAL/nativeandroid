/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.android.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.universAAL.middleware.xsd.util.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class RAPIManager {
	private static final String TAG = "RAPIManager";
    //R API methods
    public static final int REGISTER=0;
    public static final int UNREGISTER=1;
    public static final int SENDC=2;
    public static final int SUBSCRIBEC=3;
    public static final int CALLS=4;
    public static final int PROVIDES=5;
    public static final int RESPONSES=6;
    //GCM Connection details
//    private static final String PROPERTY_REG_ID = "registration_id";
//    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static int STATIC_APP_VERSION = Integer.MIN_VALUE;
    
    // --------------The following is for connecting to GCM to receive callbacks from R API--------------
    //(from http://developer.android.com/google/gcm/client.html)
	/**

	 */
//	// No need to use AsyncTask because that only works for UI thread, and we
//	// call also from Service, and when calling from UI thread we dont need
//	// return value (only register)
//	/** Registers the application with GCM servers asynchronously.
//	 * <p>
//	 * Stores the registration ID and app versionCode in the application's
//	 * shared preferences.
//	 * @param ctxt Android context
//	 * @param serverId Server GCM ID to register with - set to null to get it from preferences
//	 */
//	public static void registerInThread(final Context ctxt, final String serverId) {
//		Log.d(TAG, "Registering GCM key in GCM server, then in uAAL server");
//	    new Thread() {
//			@Override
//			public void run() {
//	            try {
//	            	GoogleCloudMessaging mGCM = GoogleCloudMessaging.getInstance(ctxt);
//	            	String currentServerId;
//	            	if(serverId!=null){
//	            	    currentServerId = serverId;
//	            	}else{
//	            	    currentServerId = PreferenceManager
//	            		    .getDefaultSharedPreferences(ctxt).getString(
//	            			    AppConstants.Keys.CONNGCM, AppConstants.Defaults.CONNGCM);
//	            	}
//	            	String mRegID = mGCM.register(currentServerId);
//	            	// Persist the regID - no need to register again.
//	                storeRegistrationId(ctxt, mRegID);
//	                // You should send the registration ID to your server over HTTP,
//	                // so it can use GCM/HTTP or CCS to send messages to your app.
//	                // The request to your server should be authenticated if your app
//	                // is using accounts.
//	                RAPIManager.invoke(RAPIManager.REGISTER, mRegID);
//	            } catch (IOException ex) {
//	                // TODO If there is an error, don't just keep trying to register.
//	                // Require the user to click a button again, or perform
//	                // exponential back-off.
//	            }
//			}
//	    }.start();
//	}
	
//	/**
//	 * Gets the current registration ID for application on GCM service.
//	 * <p>
//	 * If result is empty, the app needs to register.
//	 *
//	 * @return registration ID, or empty string if there is no existing
//	 *         registration ID.
//	 */
//	public static String getRegistrationId(Context context) {
//	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
//	    if (registrationId.isEmpty()) {
//	        Log.i(TAG, "Google Play Services GCM Registration not found.");
//	        return "";
//	    }
//	    // Check if app was updated; if so, it must clear the registration ID
//	    // since the existing regID is not guaranteed to work with the new
//	    // app version.
//	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//	    int currentVersion = getAppVersion(context);
//	    if (registeredVersion != currentVersion) {
//	        Log.i(TAG, "App version changed. GCM Registration must be renewed.");
//	        return "";
//	    }
//	    return registrationId;
//	}
	
//	/**
//	 * @return Application's version code from the {@code PackageManager}.
//	 */
//	private static int getAppVersion(Context context) {
//		if (STATIC_APP_VERSION == Integer.MIN_VALUE) { // If still default
//			try {
//				PackageInfo packageInfo = context.getPackageManager()
//						.getPackageInfo(context.getPackageName(), 0);
//				STATIC_APP_VERSION = packageInfo.versionCode; // This way we need to call the manager only once
//			} catch (NameNotFoundException e) { // should never happen
//				throw new RuntimeException("Could not get package name: " + e);
//			}
//		}
//		return STATIC_APP_VERSION;
//	}
	
//	/**
//	 * Stores the registration ID and app versionCode in the application's
//	 * {@code SharedPreferences}.
//	 *
//	 * @param context application's context.
//	 * @param regId registration ID
//	 */
//	private static void storeRegistrationId(Context context, String regId) {
//	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//	    int appVersion = getAppVersion(context);
//	    Log.i(TAG, "Saving regId "+regId+" on app version " + appVersion);
//	    SharedPreferences.Editor editor = prefs.edit();
//	    editor.putString(PROPERTY_REG_ID, regId);
//	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
//	    editor.commit();
//	}
	
//	/**
//	 * Check the device to make sure it has the Google Play Services APK. If
//	 * it doesn't, display a dialog that allows users to download the APK from
//	 * the Google Play Store or enable it in the device's system settings.
//	 * @param context
//	 */
//	public static boolean checkPlayServices(Context context) {
//	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
//	    if (resultCode != ConnectionResult.SUCCESS) {
//	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//	        	if(context instanceof Activity){
//	        		 GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)context,
//	            		AppConstants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
//	        	}
//
//	        } else {
//	            Log.i(TAG, "This device does not support Google Play Services");
////	            finish(); // Do not close app if it does not have Play Services
//	        }
//	        return false;
//	    }
//	    return true;
//	}

	public static void performRegistration(Context context, String newserverId){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			// [START register_for_gcm]
			// Initially this call goes out to the network to retrieve the token, subsequent calls
			// are local.
			// [START get_token]
			InstanceID instanceID = InstanceID.getInstance(context);
			// R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json. = project ID = project number
			// See https://developers.google.com/cloud-messaging/android/start for details on this file.
			String serverId;
			if(newserverId!=null){
				serverId=newserverId;
			}else{
				serverId = PreferenceManager
						.getDefaultSharedPreferences(context).getString(
								AppConstants.Keys.CONNGCM, AppConstants.Defaults.CONNGCM);
			}
			String token = instanceID.getToken(serverId,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			// [END get_token]
			Log.i(TAG, "GCM Registration Token: " + token);

			// Send any registration to your app's servers.
			invoke(RAPIManager.REGISTER, token);

			// You should store a boolean that indicates whether the generated token has been
			// sent to your server. If the boolean is false, send the token to your server,
			// otherwise your server should have already received the token.
			sharedPreferences.edit().putBoolean(AppConstants.SENT_TOKEN_TO_SERVER, true).apply();
			// [END register_for_gcm]
		} catch (Exception e) {
			Log.d(TAG, "Failed to complete token refresh", e);
			// If an exception happens while fetching the new token or updating our registration data
			// on a third-party server, this ensures that we'll attempt the update at a later time.
			sharedPreferences.edit().putBoolean(AppConstants.SENT_TOKEN_TO_SERVER, false).apply();
		}
	}
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	public static boolean checkPlayServices(Context context) {
		int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;//Is this a fixed value?
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				if(context instanceof Activity) {
					apiAvailability.getErrorDialog((Activity) context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
							.show();
				}
			} else {
				Log.i(TAG, "This device does not support Google Play Services");
				// finish(); // Do not close app if it does not have Play Services
			}
			return false;
		}
		return true;
	}
	
	// --------------The following is for calling the R API--------------
	
	public static void invokeInThread(final int method, final String param){
		new Thread(){
			@Override
			public void run() {
				invoke(method, param);
			}
		}.start();
	}
	
	public static synchronized String invoke(int method, String param){
		if (Config.getServerUSR().equals(AppConstants.Defaults.CONNUSR)) {
			Log.w(TAG, "Still using default user. " +
					"Calls to R API are disabled until a correct user is set up and may lead to error messages.");
			return null;
		}
		Log.d(TAG, "Sending R API request to uAAL server: "+getStringMethod(method));	
		StringBuilder strb = new StringBuilder();
		strb.append("method=").append(getStringMethod(method))
				.append("&param=").append(param).append("&v=").append("AND-")
				.append(STATIC_APP_VERSION); //Add app version for managing purposes
		final String str = strb.toString();

		HttpURLConnection conn = null;
		String result="";
		try {
			byte[] data = str.getBytes(Charset.forName("UTF-8"));
			String auth="Basic "+Base64.encodeBytes((Config.getServerUSR()+":"+Config.getServerPWD()).getBytes());
			URL url = new URL(Config.getServerURL());

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setInstanceFollowRedirects(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setReadTimeout(30000);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length));
			conn.setRequestProperty("Authorization", auth);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();

			Log.d(TAG,"SENT TO SERVER: "+url);
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){
				Log.e(TAG,"ERROR REACHING SERVER "+conn.getResponseCode()+" : "+conn.getResponseMessage());
			}

			if (method == RAPIManager.CALLS) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
				String line = rd.readLine();
				// Do nothing (keep reading) until we reach TURTLE
				while (line != null && !line.equals("TURTLE")) {
					result = result + line + "\n";
					line = rd.readLine();
				}
				// At this point we reached TURTLE or the end of out (if no TURTLE was there)
				strb = new StringBuilder();
				while (line != null) {
					// We only get here if there was something after TURTLE (and there was TURTLE)
					line = rd.readLine();
					if (line != null){
						strb.append(line);
					}
				}
				String serialized = strb.toString();
				if (serialized.length() > 1) {
					return serialized;
				}
				rd.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close the connection and set all objects to null
			if (conn != null){
				conn.disconnect();
			}
		}
		return null;
	}
	
	private static String getStringMethod(int method) {
		switch (method) {
		case REGISTER:
			return "REGISTER";
		case UNREGISTER:
			return "UNREGISTER";
		case SENDC:
			return "SENDC";
		case SUBSCRIBEC:
			return "SUBSCRIBEC";
		case CALLS:
			return "CALLS";
		case PROVIDES:
			return "PROVIDES";
		case RESPONSES:
			return "RESPONSES";
		default:
			return "REGISTER";
		}
	}

}
