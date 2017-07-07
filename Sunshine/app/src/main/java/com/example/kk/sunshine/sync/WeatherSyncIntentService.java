package com.example.kk.sunshine.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by kk on 2017/7/3.
 */

public class WeatherSyncIntentService extends IntentService{

    public WeatherSyncIntentService() {
        super("WeatherSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WeatherSyncTask.syncWeather(this);
    }
}
