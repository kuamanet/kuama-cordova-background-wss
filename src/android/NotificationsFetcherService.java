package net.kuama.backgroundservice;

import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationsFetcherService {

    private final static String TAG = NotificationsFetcherService.class.getSimpleName();

    private String certFilePath = "";
    private String certFilePassword = "";
    private String url = "";
    private String mainPackageName = "";
    private String mainClassName = "";

    JSONObject doWork(Context context) {
        JSONObject result = new JSONObject();

        try {
            loadConfiguration(context);

            if (requiredParamsAreMissing(context)) {
                String REQUIRED_PARAMS_MISSING_MESSAGE = "Not all required params to connect to the remote service are set";
                Log.d(TAG, REQUIRED_PARAMS_MISSING_MESSAGE);
                Log.d(TAG, "certFilePassword: " + certFilePassword);
                Log.d(TAG, "certFilePath: " + certFilePath);
                Log.d(TAG, "url: " + url);
                Log.d(TAG, "mainPackageName: " + mainPackageName);
                Log.d(TAG, "mainClassName: " + mainClassName);

                result.put("error", REQUIRED_PARAMS_MISSING_MESSAGE);
                return result;

            }

            if (!NetworkUtils.isOnline(context)) {
                String NO_CONNECTIVITY_MESSAGE = "The device is not connected to internet";
                Log.d(TAG, NO_CONNECTIVITY_MESSAGE);
                result.put("error", NO_CONNECTIVITY_MESSAGE);
                return result;
            }


            Notificator.process(SSLUtils.getRemoteData(new CertificateData(certFilePath, certFilePassword), url), context, ReflectionHelper.findClassByName(mainClassName, mainPackageName));


        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


    /**
     * @return true if some params are missing from the configuration
     */
    private boolean requiredParamsAreMissing(Context context) {
        loadConfiguration(context);
        return certFilePassword.isEmpty() || url.isEmpty() || certFilePath.isEmpty()
                || mainPackageName.isEmpty() || mainClassName.isEmpty();
    }

    private void loadConfiguration(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        url = sharedPref.getString("url", "");
        certFilePath = sharedPref.getString("certFilePath", "");
        certFilePassword = sharedPref.getString("certFilePassword", "");
        mainClassName = sharedPref.getString("mainClassName", "");
        mainPackageName = sharedPref.getString("mainPackageName", "");

    }

    void storeConfiguration(JSONObject config, Context context) throws JSONException {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPref.edit();


        if (config.has("url")) {
            editor.putString("url", config.getString("url"));
        }

        if (config.has("certFilePath")) {
            editor.putString("certFilePath", config.getString("certFilePath"));
        }

        if (config.has("certFilePassword")) {
            editor.putString("certFilePassword", config.getString("certFilePassword"));
        }

        if (config.has("mainPackageName")) {
            editor.putString("mainPackageName", config.getString("mainPackageName"));
        }

        if (config.has("mainClassName")) {
            editor.putString("mainClassName", config.getString("mainClassName"));
        }

        if (config.has("notificationEventsName")) {
            editor.putString("notificationEventsName", config.getJSONArray("notificationEventsName").toString());
        }


        if (config.has("idIndexes")) {
            editor.putString("idIndexes", config.getJSONArray("idIndexes").toString());
        }

        if (config.has("titleIndex")) {
            editor.putString("titleIndex", config.getString("titleIndex"));
        }

        editor.commit();
    }
}
