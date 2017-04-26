package net.kuama.backgroundservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by dan on 29/03/17.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String TAG = BootBroadcastReceiver.class.getCanonicalName();
    static private int kJobId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                NotificationsWatcher.toggle(context);
                scheduleJob(context);
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    public static void scheduleJob(Context context) {

        cancelAllJobs(context);
        ComponentName mServiceComponent = new ComponentName(context, JobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        builder.setMinimumLatency(20000); // wait at least
        builder.setOverrideDeadline(25000); // maximum delay
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    private static void cancelAllJobs(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
    }
}
