package net.kuama.backgroundservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationsWatcher extends NotificationListenerService {

    private final static String TAG = NotificationsWatcher.class.getCanonicalName();
    private static NotificationsWatcher me = null;


    @Override
    public void onCreate() {
        Log.d(TAG, "NotificationsWatcher is watching notifications");
        me = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Implement what you want here
        int removedNotificationId = sbn.getId();
        Notificator.addToConsumedNotifications(this, removedNotificationId);
    }

    public static StatusBarNotification[] notificationsInStatusBar() {
        if (me != null) {
            return me.getActiveNotifications();
        }

        return new StatusBarNotification[0];
    }

    public static boolean ready() {
        return me != null;
    }

    public static void toggle(Context context) {
        //android is a little buggy, in the emulator at least. Toggling notification watcher state.
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationsWatcher.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationsWatcher.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

}
