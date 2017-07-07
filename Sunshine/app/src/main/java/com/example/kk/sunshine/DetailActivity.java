package com.example.kk.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kk.sunshine.data.WeatherContract;
import com.example.kk.sunshine.databinding.ActivityDetailBinding;
import com.example.kk.sunshine.utils.WeatherUtils;

/**
 * Created by kk on 2017/6/28.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private String mForecastSummary;

    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;

    private static final int ID_DETAIL_LOADER = 353;

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("detail", "onCreate: " );
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();

        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + " #WeatherApp")
                .getIntent();
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        //当点击菜单项没有被MenuItemClickListener
        // 或Activity的onOptionsItemSelected处理函数处理的话，
        //指定给菜单项的Intent就会被触发。触发时，
        // Android会执行startActivity，
        // 传入一个指定的Intent。下面的代码片段给出了如何指定一个Intent给菜单项：

        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int choice = item.getItemId();
        if(choice == R.id.action_setting){
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e("detail", "onCreateLoader: " );
        switch (id){
            case ID_DETAIL_LOADER:
                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

//      COMPLETED (7) Display the weather icon using mDetailBinding
        /****************
         * Weather Icon *
         ****************/
        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        /* Set the resource ID on the icon to display the art */
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
        /*
         * Read the date from the cursor. It is important to note that the date from the cursor
         * is the same date from the weather SQL table. The date that is stored is a GMT
         * representation at midnight of the date when the weather information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = WeatherUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

//      COMPLETED (8) Use mDetailBinding to display the date
        mDetailBinding.primaryInfo.date.setText(dateText);

        /***********************
         * Weather Description *
         ***********************/
        /* Use the weatherId to obtain the proper description */
        String description = WeatherUtils.getStringForWeatherCondition(this, weatherId);

//      COMPLETED (15) Create the content description for the description for a11y
        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = getString(R.string.a11y_forecast, description);

//      COMPLETED (9) Use mDetailBinding to display the description and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);

//      COMPLETED (16) Set the content description of the icon to the same as the weather description a11y text
        /* Set the content description on the weather image (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = WeatherUtils.formatTemperature(this, highInCelsius);

//      COMPLETED (17) Create the content description for the high temperature for a11y
        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = getString(R.string.a11y_high_temp, highString);

//      COMPLETED (10) Use mDetailBinding to display the high temperature and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = WeatherUtils.formatTemperature(this, lowInCelsius);

//      COMPLETED (18) Create the content description for the low temperature for a11y
        String lowA11y = getString(R.string.a11y_low_temp, lowString);

//      COMPLETED (11) Use mDetailBinding to display the low temperature and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

//      COMPLETED (20) Create the content description for the humidity for a11y
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

//      COMPLETED (12) Use mDetailBinding to display the humidity and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

//      COMPLETED (19) Set the content description of the humidity label to the humidity a11y String
        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = WeatherUtils.getFormattedWind(this, windSpeed, windDirection);

//      COMPLETED (21) Create the content description for the wind for a11y
        String windA11y = getString(R.string.a11y_wind, windString);

//      COMPLETED (13) Use mDetailBinding to display the wind and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);

//      COMPLETED (22) Set the content description of the wind label to the wind a11y String
        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        String pressureString = getString(R.string.format_pressure, pressure);

//      COMPLETED (23) Create the content description for the pressure for a11y
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

//      COMPLETED (14) Use mDetailBinding to display the pressure and set the content description
        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

//      COMPLETED (24) Set the content description of the pressure label to the pressure a11y String
        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
