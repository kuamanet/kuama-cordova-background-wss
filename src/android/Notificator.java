package net.kuama.backgroundservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Notificator {

    private final static String TAG = Notificator.class.getName();
    private final static String NOTIFICATION_IDS_INDEX_IN_SHARED_PREF = "kNotifications";
    private final static String[] ID_INDEXES = {"idVarco", "idTransito"};
    private static ArrayList<Integer> consumedNotificationsIdsCache = new ArrayList<Integer>();


    /**
     * {"notifiche":[{"idVarco":"varco3","idPista":"pista1","idTransito":"pista1-06","varcoNome":"Darsena Toscana","pistaDescrizione":"pista1","transitoTargaAnteriore":"EZZöõÖŌı"},{"idVarco":"varco4","idPista":"pista1","idTransito":"pista1-06","varcoNome":"Darsena Toscana","pistaDescrizione":"pista1","transitoTargaAnteriore":"EZZöõÖŌı"},{"idVarco":"varco1","idPista":"pista2","idTransito":"pista2-01","varcoNome":"Darsena Toscana","pistaDescrizione":"pista2","transitoTargaAnteriore":null},{"idVarco":"varco2","idPista":"pista2","idTransito":"pista2-01","varcoNome":"Darsena Toscana","pistaDescrizione":"pista2","transitoTargaAnteriore":null},{"idVarco":"varco3","idPista":"pista2","idTransito":"pista2-01","varcoNome":"Darsena Toscana","pistaDescrizione":"pista2","transitoTargaAnteriore":null},{"idVarco":"varco4","idPista":"pista2","idTransito":"pista2-01","varcoNome":"Darsena Toscana","pistaDescrizione":"pista2","transitoTargaAnteriore":null},{"idVarco":"varco1","idPista":"pista1","idTransito":"pista1-06","varcoNome":"Darsena Toscana","pistaDescrizione":"pista1","transitoTargaAnteriore":"EZZöõÖŌı"},{"idVarco":"varco2","idPista":"pista1","idTransito":"pista1-06","varcoNome":"Darsena Toscana","pistaDescrizione":"pista1","transitoTargaAnteriore":"EZZöõÖŌı"}]}
     *
     * @param remoteData a list of json object referring to notifications to be created
     */
    public static void process(JSONObject remoteData, Context ctx, Class<?> activityToLaunch) throws JSONException {

        if (remoteData == null) {
            Log.d(TAG, "Something went wrong while fetching data, remoteData is null");
            return;
        }



        JSONArray notifications = remoteData.getJSONArray("notifiche");

        Log.d(TAG, "received" + notifications.length() + " notifications to process");

        // rimuovi da notifications tutte le notifiche già tappate | rimosse in precedenti sessioni
        fetchConsumedNotifications(ctx);

        if (consumedNotificationsIdsCache.size() > 0) {

            for (int n = 0; n < notifications.length(); n++) {
                JSONObject notificationData = notifications.getJSONObject(n);
                Integer notificationId = getNotificationId(notificationData);
                if (consumedNotificationsIdsCache.contains(notificationId)) {
                    notifications.remove(n);
                    Log.d(TAG, "Removing" + notificationId+ " since was already tapped or removed from the status bar");
                }
            }

        } else {
            Log.d(TAG, "No consumed notifications in cache");
        }

        Log.d(TAG, "After consumed cache check, we have " + notifications.length() + " notifications to process");


        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (NotificationsWatcher.ready()) {
            // rimuovi da notifications tutte le notifiche già presenti nella status bar
            StatusBarNotification[] notificationsInStatusBar = NotificationsWatcher.notificationsInStatusBar();
            if (notificationsInStatusBar.length > 0) {

                for (StatusBarNotification aNotificationsInStatusBar : notificationsInStatusBar) {
                    Integer notificationIdInStatusBar = aNotificationsInStatusBar.getId();
                    boolean notificationIdInStatusBarIsContainedInRemoteNotifications = false;
                    for (int n = 0; n < notifications.length(); n++) {
                        JSONObject notificationData = notifications.getJSONObject(n);
                        Integer notificationId = getNotificationId(notificationData);
                        if (notificationId.equals(notificationIdInStatusBar)) {
                            notifications.remove(n);
                            notificationIdInStatusBarIsContainedInRemoteNotifications = true;
                            Log.d(TAG, "Removing" + notificationId+ " since is already in status bar");
                        }
                    }

                    //se la notifica è presente nella status bar ma non è stata ricevuta come da notificare
                    //rimuoverla
                    if(!notificationIdInStatusBarIsContainedInRemoteNotifications) {
                        nm.cancel(notificationIdInStatusBar);
                    }
                }

            } else {
                Log.d(TAG, "No notifications in status bar");
            }

            Log.d(TAG, "After status bar check, we have " + notifications.length() + " notifications to process");

        } else {
            Log.d(TAG, "Notification not ready yet");
            NotificationsWatcher.toggle(ctx);

        }




        // notifica tutte le notifiche rimaste in notifications
        for (int i = 0; i < notifications.length(); i++) {
            generateLocalNotification(notifications.getJSONObject(i), ctx, activityToLaunch, nm);
        }

    }

    static void addToConsumedNotifications(Context context, Integer notificationId) {
        fetchConsumedNotifications(context);
        consumedNotificationsIdsCache.add(notificationId);
        storeNotConsumedNotifications(context);
    }


    private static void fetchConsumedNotifications(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String serialized = sharedPref.getString(NOTIFICATION_IDS_INDEX_IN_SHARED_PREF, "");
        List<String> list = Arrays.asList(TextUtils.split(serialized, ","));
        consumedNotificationsIdsCache.clear();
        for (int i = 0; i < list.size(); i++) {
            consumedNotificationsIdsCache.add(Integer.parseInt(list.get(i)));
        }
    }

    private static void storeNotConsumedNotifications(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor;
        editor = sharedPref.edit();
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < consumedNotificationsIdsCache.size(); i++) {
            list.add(consumedNotificationsIdsCache.get(i).toString());
        }

        editor.putString(NOTIFICATION_IDS_INDEX_IN_SHARED_PREF, TextUtils.join(",", list));
        editor.commit();
    }

    private static void generateLocalNotification(JSONObject notificationData, Context ctx, Class<?> activityToLaunch, NotificationManagerCompat nm) {


        String title = titleForNotification(notificationData);
        String text = getTextForNotification(notificationData);

        Notification.Builder builder = new Notification.Builder(ctx);

        Intent notificationIntent = new Intent(ctx, activityToLaunch);
        //extra works only when we set data, really don't know why...
        notificationIntent.setData(Uri.parse(notificationData.toString()));
        notificationIntent.putExtra("kNotification", notificationData.toString());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //set
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        builder.setSmallIcon(ctx.getApplicationContext().getResources().getIdentifier("icon", "mipmap", ctx.getApplicationContext().getPackageName()));
        Notification notification = builder.build();
        Notification.InboxStyle inboxStyle =
                new Notification.InboxStyle();
        builder.setStyle(inboxStyle);

        try {
            int notificationId = getNotificationId(notificationData);
            nm.notify(notificationId, notification);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hascoding the json notification idVarco + idTransito to generate unique integer, used to assign
     * internal id to the notification
     *
     * @param remoteData the json remote notification description
     * @return idVarco + idTransito -> int
     * @throws JSONException Could throw if remoteData does not have one of the ID_INDEXES
     */
    private static int getNotificationId(JSONObject remoteData) throws JSONException {


        String id = "";
        for (String ID_INDEX : ID_INDEXES) id += remoteData.getString(ID_INDEX);

        return id.hashCode();

    }

    /**
     * Creates a sting like Nuovo veicolo [targato XXX] in varcoXXX pista pistaXXX given a json notification data
     *
     * @param notificationData the json remote notification description
     * @return the notification title
     */
    private static String titleForNotification(JSONObject notificationData) {

        String title = "Default push title";
        try {

            String targa = notificationData.getString("transitoTargaAnteriore");

            String pista = notificationData.getString("pistaDescrizione");

            title = "Veicolo" + (targa.isEmpty() || targa.equals("null") ? "" : " " + targa) + " in pista " + pista;

        } catch (JSONException ex) {
            Log.d(TAG, "Error while building notification title");
            Log.d(TAG, ex.getMessage());
        }

        return title;
    }

    private static String getTextForNotification(JSONObject notificationData) {
        String text = "";
        try {


            text = notificationData.getString("varcoNome");


        } catch (JSONException ex) {
            Log.d(TAG, "Error while building notification title");
            Log.d(TAG, ex.getMessage());
        }

        return text;
    }

}