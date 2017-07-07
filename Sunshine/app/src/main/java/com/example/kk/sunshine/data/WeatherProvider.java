package com.example.kk.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.kk.sunshine.utils.WeatherUtils;

/**
 * Created by kk on 2017/6/30.
 */

public class WeatherProvider extends ContentProvider {

    private static final String WEATHER_AUTHOR = "com.example.kk.sunshine";

    private static final String WEATHER_TABLE_NAME = "weather";

    private static final int TABLE_CODE = 100;

    private static final int CODE_WEATHER_WITH_DATE = 200;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private WeatherDBHelper mDBHelper;

    static {

        /* This URI is content://com.example.kk.sqlitetest/weather/ */
        uriMatcher.addURI(WEATHER_AUTHOR, WeatherContract.PATH_WEATHER, TABLE_CODE);

        /* This URI is content://com.example.kk.sqlitetest/weather/ #/    */
        uriMatcher.addURI(WEATHER_AUTHOR, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);
    }
    @Override
    public boolean onCreate() {
        mDBHelper = new WeatherDBHelper(getContext());
        return true;
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int code = uriMatcher.match(uri);
        Cursor cursor;
        switch (code){
            case CODE_WEATHER_WITH_DATE:
                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};
                cursor = mDBHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        strings,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        s1);
                break;
            case TABLE_CODE: {
                cursor = mDBHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        strings,
                        s,
                        strings1,
                        null,
                        null,
                        s1);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //设置监听的url   自动更新。。。
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //insert multiple data
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case TABLE_CODE:
                db.beginTransaction();
                int rowInserted = 0;
                try {
                    for(ContentValues value : values){
                        long weatherDate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if(!WeatherUtils.isDateNormalized(weatherDate)){
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }
                        long _id = db.insert(WEATHER_TABLE_NAME, null, value);
                        if(_id != -1){
                            rowInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                if (rowInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        int numRowDeleted;
        if (null == s) s = "1";
        switch (uriMatcher.match(uri)){
            case TABLE_CODE:
                numRowDeleted = mDBHelper.getWritableDatabase().delete(
                        WEATHER_TABLE_NAME,
                        s,
                        strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public void shutdown() {
        mDBHelper.close();
        super.shutdown();
    }
}
