package com.example.kk.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.kk.sunshine.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 2017/7/3.
 */

public class WeatherSyncUtils {

    private static boolean sInitialized;

    private static final String JOB_TAG = "WeatherSync";

    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

     static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context){

        Driver driver = new GooglePlayDriver(context);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job job = dispatcher.newJobBuilder()
                .setService(WeatherJobServer.class)
                .setTag(JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(job);
    }

    synchronized public static void initialize(@NonNull final Context context) {

        if (sInitialized) return;

        sInitialized = true;

        scheduleFirebaseJobDispatcherSync(context);


        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground( Void... voids ) {

                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry
                        .getSqlSelectForTodayOnwards();

                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null);

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                /* Make sure to close the Cursor to avoid memory leaks! */
                cursor.close();
                return null;
            }
        }.execute();
    }

    public static void startImmediateSync(@NonNull final Context context){
        Intent i = new Intent(context,WeatherSyncIntentService.class);
        context.startService(i);
    }
}
