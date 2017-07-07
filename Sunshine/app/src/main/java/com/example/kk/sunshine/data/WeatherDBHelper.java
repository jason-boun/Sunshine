package com.example.kk.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.kk.sunshine.data.WeatherContract.WeatherEntry;
/**
 * Created by kk on 2017/6/30.
 */

public class WeatherDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weather.db";

    private static final int DB_VERSION = 3;

    public WeatherDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE =
                "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                WeatherEntry.COLUMN_DATE       + " INTEGER NOT NULL, "                 +

                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "                 +

                WeatherEntry.COLUMN_MIN_TEMP   + " REAL NOT NULL, "                    +
                WeatherEntry.COLUMN_MAX_TEMP   + " REAL NOT NULL, "                    +

                WeatherEntry.COLUMN_HUMIDITY   + " REAL NOT NULL, "                    +
                WeatherEntry.COLUMN_PRESSURE   + " REAL NOT NULL, "                    +

                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "                    +
                WeatherEntry.COLUMN_DEGREES    + " REAL NOT NULL, "                    +

                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
