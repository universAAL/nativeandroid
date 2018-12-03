/*
	Copyright 2017 ITACA-SABIEN, http://www.sabien.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.universAAL.middleware.xsd.util.Base64;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class RESTManager {
    //R API methods
    public static final int REGISTER = 0;
    public static final int UNREGISTER = 1;
    public static final int SENDC = 2;
    public static final int SUBSCRIBEC = 3;
    public static final int CALLS = 4;
    public static final int PROVIDES = 5;
    public static final int RESPONSES = 6;
    private static final String TAG = "RAPIManager";

    // --------------Util strings to build json bodies--------------
    private static final String REPLACE_PARAM = "$%p";
    private static final String REPLACE_ID = "$%i";
    private static final String POST_SPACES = "{\r\n"
            + "   \"space\": {\r\n"
            + "     \"@id\": \"" + REPLACE_ID + "\",\r\n"
            + "     \"callback\": \"" + REPLACE_PARAM + "\"\r\n"
            + "   }\r\n"
            + " }";
    private static final String POST_SPACES_S_SERVICE_CALLERS = "{\r\n"
            + "  \"caller\": {\r\n"
            + "    \"@id\": \"" + REPLACE_ID + "\"\r\n"
            + "  }\r\n"
            + " }";
    private static final String POST_SPACES_S_CONTEXT_PUBLISHERS = "{\r\n"
            + "  \"publisher\": {\r\n"
            + "    \"@id\": \"" + REPLACE_ID + "\",\r\n"
            + "    \"providerinfo\": \"" + REPLACE_PARAM + "\"\r\n"
            + "  } \r\n"
            + " }";
    private static final String POST_SPACES_S_SERVICE_CALLEES = "{\r\n"
            + "  \"callee\": {\r\n"
            + "    \"@id\": \"" + REPLACE_ID + "\",\r\n"
            + "    \"profile\": \"" + REPLACE_PARAM + "\"\r\n"
            + "  }\r\n"
            + " }";
    private static final String POST_SPACES_S_CONTEXT_SUBSCRIBERS = "{\r\n"
            + "  \"subscriber\": {\r\n"
            + "    \"@id\": \"" + REPLACE_ID + "\",\r\n"
            + "    \"pattern\": \"" + REPLACE_PARAM + "\"\r\n"
            + "  }\r\n"
            + " }";
    private static final String BODY_DEFAULT_PUBLISHER = "@prefix owl: <http://www.w3.org/2002/07/owl#> .\\r\\n" +
            "@prefix : <http://ontology.universAAL.org/Context.owl#> .\\r\\n" +
            "<http://ontology.universAAL.org/uAAL.owl#DefaultContextPublisher"+REPLACE_ID+"> :myClassesOfEvents (\\r\\n" +
            "    [\\r\\n" +
            "      a :ContextEventPattern \\r\\n" +
            "    ]\\r\\n" +
            "  ) ;\\r\\n" +
            "  a :ContextProvider ;\\r\\n" +
            "  :hasType :controller .\\r\\n" +
            ":controller a :ContextProviderType .";

    // ----------The following is for connecting to GCM to receive callbacks from R API---------

    public static void performRegistrationInThread(final Context context, final String newserverId) {
        new Thread() {
            @Override
            public void run() {
                performRegistration(context, newserverId);
            }
        }.start();
    }

    public static void performRegistration(Context context, String newserverId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(context);
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from
            // google-services.json. = project ID = project number
            // See https://developers.google.com/cloud-messaging/android/start for details on this.
            String serverId;
            if (newserverId != null) {
                serverId = newserverId;
            } else {
                serverId = PreferenceManager
                        .getDefaultSharedPreferences(context).getString(
                                AppConstants.Keys.CONNGCM, AppConstants.Defaults.CONNGCM);
            }
            String token;
            if(serverId.isEmpty() || serverId.equals("null")){
                token=""; // This allows for "push-only" interaction with REST API
            }else{
                token = instanceID.getToken(serverId,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            }

            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // Send any registration to your app's servers.
            invoke(RESTManager.REGISTER, token, null, null);

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
                if (context instanceof Activity) {
                    apiAvailability.getErrorDialog((Activity) context, resultCode,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
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

    public static void invokeInThread(final int method, final String param, final String cee, final String originCall) {
        new Thread() {
            @Override
            public void run() {
                invoke(method, param, cee, originCall);
            }
        }.start();
    }

    public static synchronized String invoke(int method, String param, String cee, String originCall) {
        if (Config.getServerUSR().equals(AppConstants.Defaults.CONNUSR)) {
            Log.e(TAG, "Still using default user. Calls to REST API are disabled until a correct " +
                    "user is set up and may lead to error messages.");
            return null;
        }
        String json;
        switch (method) { //TODO Allow setting tenant id instead of reusing user name
            case REGISTER:
                Log.d(TAG, "Sending REST API request to uAAL server: REGISTER");
                json = POST_SPACES
                        .replace(REPLACE_ID, Config.getServerUSR())
                        .replace(REPLACE_PARAM, param);
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/",
                        json, false, null, false);
                json = POST_SPACES_S_SERVICE_CALLERS
                        .replace(REPLACE_ID, "default");
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/service/callers",
                        json, false, null, false);
                json = POST_SPACES_S_CONTEXT_PUBLISHERS
                        .replace(REPLACE_ID, "default")//TODO Check if generic can send any event
                        .replace(REPLACE_PARAM, BODY_DEFAULT_PUBLISHER.replace(REPLACE_ID,Config.getServerUSR()));
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/context/publishers",
                        json, false, null, false);
                break;
            case UNREGISTER:
                Log.d(TAG, "Sending REST API request to uAAL server: UNREGISTER");
                sendHTTP("DELETE",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR(),
                        null, false, null, false);
                break;
            case SENDC:
                Log.d(TAG, "Sending REST API request to uAAL server: SENDC");
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/context/publishers/default",
                        param, false, null, true);
                break;
            case SUBSCRIBEC:
                Log.d(TAG, "Sending REST API request to uAAL server: SUBSCRIBEC");
                json = POST_SPACES_S_CONTEXT_SUBSCRIBERS
                        .replace(REPLACE_ID, Integer.toString(param.hashCode()))//TODO Check if that works
                        .replace(REPLACE_PARAM, param);
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/context/subscribers",
                        json, false, null, false);
                break;
            case CALLS:
                Log.d(TAG, "Sending REST API request to uAAL server: CALLS");
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/context/publishers/default",
                        param, true, null, true);
                break;
            case PROVIDES:
                Log.d(TAG, "Sending REST API request to uAAL server: PROVIDES");
                json = POST_SPACES_S_SERVICE_CALLEES
                        .replace(REPLACE_ID, Integer.toString(param.hashCode()))//TODO Check if that works
                        .replace(REPLACE_PARAM, param);
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/service/callees/",
                        json, false, null, false);
                break;
            case RESPONSES:
                Log.d(TAG, "Sending REST API request to uAAL server: RESPONSES");
                sendHTTP("POST",
                        Config.getServerURL() + "/spaces/" + Config.getServerUSR() + "/service/callees/" + cee,
                        param, false, originCall, true);
                break;
            default:
                Log.w(TAG, "Unknown REST API call: " + method);
                return null;
        }

        return null;
    }

    private static String sendHTTP(String method, String restURL, String body, boolean expectResponse, String originCall, boolean text) {
        HttpURLConnection conn = null;
        try {
            byte[] data = null;
            if (body != null) data = body.getBytes(Charset.forName("UTF-8"));
            String auth = "Basic " + Base64.encodeBytes((Config.getServerUSR() + ":" + Config.getServerPWD()).getBytes());
            URL url;
            if (originCall != null) {
                url = new URL(restURL + "?o=" + originCall);
            } else {
                url = new URL(restURL);
            }

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", text?"text/plain":"application/json");
            conn.setRequestProperty("charset", "utf-8");
            if (body != null)
                conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length));
            conn.setRequestProperty("Authorization", auth);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            if (body != null) wr.write(data);
            wr.flush();
            wr.close();

            Log.d(TAG, "SENT TO SERVER: " + url);
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                Log.e(TAG, "ERROR REACHING SERVER " + conn.getResponseCode() + " : " + conn.getResponseMessage());
                //TODO Error handling
                return null;
            }

            if (expectResponse) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = rd.readLine();
                StringBuilder strb = new StringBuilder();
                while (line != null) {
                    line = rd.readLine();
                    if (line != null) {
                        strb.append(line);
                    }
                }
                String serialized = strb.toString();
                rd.close();
                if (serialized.length() > 1) {
                    return serialized;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close the connection and set all objects to null
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }
}
