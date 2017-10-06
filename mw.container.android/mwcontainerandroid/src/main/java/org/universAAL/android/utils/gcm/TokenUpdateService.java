package org.universAAL.android.utils.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.Config;
import org.universAAL.android.utils.RAPIManager;
import org.universAAL.android.utils.RESTManager;

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

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        //Intent intent = new Intent(this, RegistrationService.class);
        //startService(intent);
        if(Config.getRemoteType() == AppConstants.REMOTE_TYPE_RAPI){
            RAPIManager.performRegistrationInThread(getApplicationContext(), null);
        }else if(Config.getRemoteType() == AppConstants.REMOTE_TYPE_RESTAPI){
            RESTManager.performRegistrationInThread(getApplicationContext(), null);
        }
    }

}
