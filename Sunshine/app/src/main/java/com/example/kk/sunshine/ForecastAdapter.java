package com.example.kk.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kk.sunshine.utils.WeatherUtils;

/**
 * Created by kk on 2017/6/28.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {


    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    private final Context context;

    private String[] mWeatherData;

    private Cursor mCursor;

    public ForecastAdapter(Context context) {
        this.context = context;
        mUseTodayLayout = this.context.getResources().getBoolean(R.bool.use_today_layout);
    }

    public ForecastAdapterOnClickHandler mClickHandler;

    public interface ForecastAdapterOnClickHandler {
        void onClick(long date);
    }

    public void setForecastAdapterOnClickHandler(ForecastAdapterOnClickHandler handler){
        this.mClickHandler = handler;
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId;
        switch (viewType){
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.forecast_list_item;
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setFocusable(true);
        return new ForecastAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;
        int viewType = getItemViewType(position);
        switch (viewType) {
//          COMPLETED (15) If the view type of the layout is today, display a large icon
            case VIEW_TYPE_TODAY:
                weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
                break;

//          COMPLETED (16) If the view type of the layout is today, display a small icon
            case VIEW_TYPE_FUTURE_DAY:
                weatherImageId = WeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);
                break;

//          COMPLETED (17) Otherwise, throw an IllegalArgumentException
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        holder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
         /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
         /* Get human readable string using our utility method */
        String dateString = WeatherUtils.getFriendlyDateString(context, dateInMillis, false);

         /* Display friendly date string */
        holder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = WeatherUtils.getStringForWeatherCondition(context, weatherId);
         /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = context.getString(R.string.a11y_forecast, description);

         /* Set the text and content description (for accessibility purposes) */
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
         /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String highString = WeatherUtils.formatTemperature(context, highInCelsius);
         /* Create the accessibility (a11y) String from the weather description */
        String highA11y = context.getString(R.string.a11y_high_temp, highString);

         /* Set the text and content description (for accessibility purposes) */
        holder.highTempView.setText(highString);

        holder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
         /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String lowString = WeatherUtils.formatTemperature(context, lowInCelsius);
        String lowA11y = context.getString(R.string.a11y_low_temp, lowString);

         /* Set the text and content description (for accessibility purposes) */
        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);
    }

    @Override
    public int getItemCount() {
        if(mCursor != null){
            return mCursor.getCount();
        }
        return 0;
    }

    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        final ImageView iconView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
}
