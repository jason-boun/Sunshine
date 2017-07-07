package com.example.kk.sunshine.sync;

import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobService;

/**
 * Created by kk on 2017/7/4.
 */

public class WeatherJobServer extends JobService {

    private AsyncTask<Void, Void, Void> asyncTask;

    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                WeatherSyncTask.syncWeather(getApplicationContext());
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        };
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if(asyncTask != null){
            asyncTask.cancel(true);
        }
        return true;
    }
}
