package net.kuama.backgroundservice;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

class NullHostNameVerifier implements HostnameVerifier {
    private final static String TAG = NullHostNameVerifier.class.getCanonicalName();
    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(TAG, "Approving certificate for " + hostname);
        return true;
    }

}