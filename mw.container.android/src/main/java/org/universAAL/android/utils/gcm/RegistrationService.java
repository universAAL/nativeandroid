package org.universAAL.android.utils.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.RAPIManager;

public class RegistrationService extends IntentService {

    private static final String TAG = "RegistrationService";

    public RegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json. = project ID = project number
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            String serverId=intent.getStringExtra(AppConstants.GCM_PROJECT_ID);
            if(serverId==null){
                serverId = PreferenceManager
                        .getDefaultSharedPreferences(this).getString(
                                AppConstants.Keys.CONNGCM, AppConstants.Defaults.CONNGCM);
            }
            String token = instanceID.getToken(serverId,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // Send any registration to your app's servers.
            RAPIManager.invoke(RAPIManager.REGISTER, token);

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

}