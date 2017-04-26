package net.kuama.backgroundservice;

import android.app.job.JobParameters;
import android.os.AsyncTask;
import android.util.Log;


public class JobService extends android.app.job.JobService {
    private static final String TAG = JobService.class.getCanonicalName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "on start job: " + params.getJobId());
        new ConnectTask().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "on end job: " + params.getJobId());
        BootBroadcastReceiver.scheduleJob(this);
        return true;
    }

    private class ConnectTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... arg0) {
            new NotificationsFetcherService().doWork(JobService.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            BootBroadcastReceiver.scheduleJob(JobService.this);
        }
    }

}
