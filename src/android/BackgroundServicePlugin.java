package net.kuama.backgroundservice;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dan on 30/03/17.
 */

public class BackgroundServicePlugin extends CordovaPlugin {

    private static final String TAG = BackgroundServicePlugin.class.getCanonicalName();
    private static final String START_ACTION = "BackgroundServicePlugin.start";
    private static final String SET_CONFIGURATION_ACTION = "BackgroundServicePlugin.setConfiguration";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (START_ACTION.equals(action)) {
            this.startService(callbackContext);
            return true;
        }

        if (SET_CONFIGURATION_ACTION.equals(action)) {
            String jsonConfString = args.getString(0);
            this.setConfiguration(callbackContext, jsonConfString);
            return true;
        }

        Log.d(TAG, "Received an action that I do not know how to handle: " + action);

        return false;
    }

    private void startService(final CallbackContext context) {

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    // Main Code goes here
                    BootBroadcastReceiver.scheduleJob(cordova.getActivity());
                    context.success();
                } catch (Exception ex) {
                    JSONObject error = new JSONObject();
                    try {
                        error.putOpt("error", ex.getMessage());
                        context.error(error);
                    } catch (JSONException jEx) {
                        Log.d(TAG, "Could not create error response");
                        Log.d(TAG, jEx.getMessage());
                        context.error(-1);
                    }
                }
            }
        });


    }

    private void setConfiguration(final CallbackContext context, final String configuration) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                // Main Code goes here
                JSONObject configObj = null;
                try {
                    if (configuration.length() > 0) {
                        configObj = new JSONObject(configuration);
                    } else {
                        configObj = new JSONObject();
                    }

                    new NotificationsFetcherService().storeConfiguration(configObj, cordova.getActivity());
                    context.success();
                } catch (JSONException ex) {
                    Log.d(TAG, "Could not create error response");
                    Log.d(TAG, ex.getMessage());
                    context.error(-1);
                }
            }
        });

    }

}
