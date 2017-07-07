package com.example.kk.sunshine.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.net.URL;

/**
 * Created by kk on 2017/6/27.
 */

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    Activity activity;

    Handler handler;

    public FetchWeatherTask(Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    protected String[] doInBackground(String... strings) {
        if(strings.length == 0){
            return null;
        }
        String local = strings[0];
        URL weatherRequestUrl = NetworkUtils.buildUrl(local);
        try {

            String responseData = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            String[] weatherData = null;//OpenWeatherJsonUtils.getWeatherStringsFromJson(activity,responseData);

            return weatherData;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if(strings != null){

            Message message = new Message();
            Bundle bundle=new Bundle();
            bundle.putStringArray("weather_data", strings);
            message.setData(bundle);
            handler.sendMessage(message);
        }
        super.onPostExecute(strings);
    }
}
