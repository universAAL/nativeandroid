package org.universAAL.android.utils.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.container.AndroidRegistry;
import org.universAAL.android.proxies.ServiceCalleeProxy;
import org.universAAL.android.utils.AppConstants;
import org.universAAL.android.utils.Config;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.ServiceCall;

public class MessageReceptionService extends GcmListenerService {

    private static final String TAG = "MessageReceptionService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (Config.getRemoteType() != AppConstants.REMOTE_TYPE_RAPI) {
            return;// For now, double check. TODO make sure not needed
        }
        String method = data.getString("method");
        if(method==null){
            // This may happen if trying to connect to bad R-API server
            return;
        }
        MessageContentSerializerEx parser = (MessageContentSerializerEx) AndroidContainer.THE_CONTAINER
                .fetchSharedObject(AndroidContext.THE_CONTEXT,
                        new Object[] { MessageContentSerializerEx.class
                                .getName() });
        if (method.equals("SENDC")) {
            if (parser != null) {
                String serial = data.getString("param");
                if (serial != null) {
                    ContextEvent cev = (ContextEvent) parser
                            .deserialize(serial);
                    if (cev != null) {
                        ContextProvider cprov = cev.getProvider();
                        if (cprov.getProvidedEvents()==null){
                            cprov.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
                        }
                        DefaultContextPublisher cp = new DefaultContextPublisher(
                                AndroidContext.THE_CONTEXT, cprov);
                        cp.publish(cev); // Cheat: Deliver the context event as an impostor. The receiver will get it.
                        cp.close();
                        cp = null;
                        cprov = null;
                        // TODO check performance of creating a publisher per call (it eases the reuse of providerinfo)
                    }
                }
            }

        } else if (method.equals("CALLS")) {
            if (parser != null) {
                String serial = data.getString("param");
                String spuri = data.getString("to");
                String origincall = data.getString("call");
                if (serial != null && spuri != null) {
                    ServiceCall scall = (ServiceCall) parser
                            .deserialize(serial);
                    if (scall != null) { //TODO Use wakeful service calling?
                        ServiceCalleeProxy calleeProxy = AndroidRegistry
                                .getCallee(spuri);
                        if (calleeProxy != null) {
                            // I cannot use a caller impostor here because I receive a ServiceCall, not a ServiceRequest
                            calleeProxy.handleCallFromGCM(scall, origincall);
                        }
                    }
                }
            }
        } else {
            // TODO Received something unexpected from server. What to do?
        }
    }
}