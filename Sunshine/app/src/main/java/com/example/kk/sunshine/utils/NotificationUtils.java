package com.example.kk.sunshine.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.example.kk.sunshine.DetailActivity;
import com.example.kk.sunshine.R;
import com.example.kk.sunshine.data.WeatherContract;
import com.example.kk.sunshine.data.WeatherPreferences;

/**
 * Created by kk on 2017/7/5.
 */

public class NotificationUtils {

    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    private static final int WEATHER_NOTIFICATION_ID = 3004;


    public static void notifyUserOfNewWeather(Context context) {

        /* Build the URI for today's weather in order to show up to date data in notification */
        Uri todaysWeatherUri = WeatherContract.WeatherEntry
                .buildWeatherUriWithDate(WeatherUtils.normalizeDate(System.currentTimeMillis()));

        Cursor todayWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);

        if(todayWeatherCursor.moveToFirst()){
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = WeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);

            String notificationTitle = "weather";// context.getString(R.string.app_name);

            String notificationText = getNotificationText(context, weatherId, high, low);

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = WeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            /*
             * NotificationCompat Builder is a very convenient way to build backward-compatible
             * notifications. In order to use it, we provide a context and specify a color for the
             * notification, a couple of different icons, the title for the notification, and
             * finally the text of the notification, which in our case in a summary of today's
             * forecast.
             */
//          COMPLETED (2) Use NotificationCompat.Builder to begin building the notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);

//          COMPLETED (3) Create an Intent with the proper URI to start the DetailActivity
            /*
             * This Intent will be triggered when the user clicks the notification. In our case,
             * we want to open Sunshine to the DetailActivity to display the newly updated weather.
             */
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

//          COMPLETED (4) Use TaskStackBuilder to create the proper PendingINtent
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//          COMPLETED (5) Set the content Intent of the NotificationBuilder
            notificationBuilder.setContentIntent(resultPendingIntent);

//          COMPLETED (6) Get a reference to the NotificationManager
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

//          COMPLETED (7) Notify the user with the ID WEATHER_NOTIFICATION_ID
            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

//          COMPLETED (8) Save the time at which the notification occurred using SunshinePreferences
            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            WeatherPreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }

        /* Always close your cursor when you're done with it to avoid wasting resources. */
        todayWeatherCursor.close();

    }
    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        String shortDescription = WeatherUtils
                .getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        /* Using String's format method, we create the forecast summary */
        String notificationText = String.format(notificationFormat,
                shortDescription,
                WeatherUtils.formatTemperature(context, high),
                WeatherUtils.formatTemperature(context, low));

        return notificationText;
    }


}
