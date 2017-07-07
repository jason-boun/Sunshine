package com.example.kk.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.kk.sunshine.data.WeatherContract;
import com.example.kk.sunshine.data.WeatherPreferences;
import com.example.kk.sunshine.utils.NetworkUtils;
import com.example.kk.sunshine.utils.NotificationUtils;
import com.example.kk.sunshine.utils.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by kk on 2017/7/3.
 */

public class WeatherSyncTask {

    synchronized public static void syncWeather(Context context){

        try{

            URL requestUrl = NetworkUtils.getUrl(context);

            String jsonStr = NetworkUtils.getResponseFromHttpUrl(requestUrl);

            ContentValues[] weatherData = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonStr);

            if(weatherData != null && weatherData.length != 0){

                ContentResolver weatherContentResolver = context.getContentResolver();

                weatherContentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);

                weatherContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,weatherData);

                boolean notificationsEnabled = WeatherPreferences.areNotificationsEnabled(context);

                /*
                 * If the last notification was shown was more than 1 day ago, we want to send
                 * another notification to the user that the weather has been updated. Remember,
                 * it's important that you shouldn't spam your users with notifications.
                 */
                long timeSinceLastNotification = WeatherPreferences
                        .getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

//              COMPLETED (14) Check if a day has passed since the last notification
                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                /*
                 * We only want to show the notification if the user wants them shown and we
                 * haven't shown a notification in the past day.
                 */
//              COMPLETED (15) If more than a day have passed and notifications are enabled, notify the user
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    Log.e("notification", "syncWeather: in" );
                    NotificationUtils.notifyUserOfNewWeather(context);
                }
                Log.e("notification", "syncWeather: "+notificationsEnabled+"oneday"+oneDayPassedSinceLastNotification );

            /* If the code reaches this point, we have successfully performed our sync */

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
