package net.kuama.backgroundservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


class NetworkUtils {

    static boolean isOnline(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
