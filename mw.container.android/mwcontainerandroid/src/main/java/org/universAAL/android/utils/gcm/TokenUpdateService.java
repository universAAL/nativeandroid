package org.universAAL.android.utils.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import org.universAAL.android.utils.RAPIManager;

/**
 * Created by alfiva on 03/11/2015.
 */
public class TokenUpdateService extends InstanceIDListenerService {

    private static final String TAG = "TokenUpdateService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        //Intent intent = new Intent(this, RegistrationService.class);
        //startService(intent);
        RAPIManager.performRegistration(getApplicationContext(), null);
    }
    // [END refresh_token]
}
