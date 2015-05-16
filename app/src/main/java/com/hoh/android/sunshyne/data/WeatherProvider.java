package com.hoh.android.sunshyne.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.hoh.android.sunshyne.data.WeatherContract.LocationEntry;
import com.hoh.android.sunshyne.data.WeatherContract.WeatherEntry;

/**
 * Created by funso on 3/14/15.
 */
public class WeatherProvider extends ContentProvider{

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private WeatherDbHelper weatherDbHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sqliteQueryBuilder;

    static {
        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(WeatherEntry.TABLE_NAME
                + " INNER JOIN " + LocationEntry.TABLE_NAME
                + " ON " + WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_LOC_ID + " = "
                + LocationEntry.TABLE_NAME + "." + LocationEntry._ID
        );
    }

    // selections
    private static final String locationSettingSelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING
            + " = ?";

    private static final String locationSettingWithStartDateSelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING
            + "= ? AND "
            + WeatherEntry.TABLE_NAME + "."
            + WeatherEntry.COLUMN_DATE + " >= ?";

    private static final String locationSettingAndDaySelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING
                    + "= ? AND " + WeatherEntry.COLUMN_DATE + " = ?";

    @Override
    public boolean onCreate() {
        weatherDbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (uriMatcher.match(uri)){
            case WEATHER:
                cursor = weatherDbHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case WEATHER_WITH_LOCATION:
                cursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                cursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            case LOCATION:
                cursor = weatherDbHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    public Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder){

        Long startDate = WeatherEntry.getStartDateFromUri(uri);
        String selection;
        String []selectionArgs;
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);

        if (startDate == 0){
            selection = locationSettingSelection;

            selectionArgs = new String[]{
                    locationSetting
            };
        }
        else {
            selection = locationSettingWithStartDateSelection;
            selectionArgs = new String []{
                    locationSetting,
                    Long.toString(startDate)
            };
        }

        return sqliteQueryBuilder.query(
                weatherDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder){

        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherEntry.getDateFromUri(uri) + "";

        String selection = locationSettingAndDaySelection;

        String [] selectionArgs = new String[]{
                locationSetting,
                date
        };

        return sqliteQueryBuilder.query(
                weatherDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        switch (match){
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION:
                long _id = db.insert(LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationEntry.buildLocationUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        db.close();
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase database = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted = 0;

        if (null == selection) selection = "1";

        switch (match){
            case WEATHER:
                rowsDeleted = database.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = database.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Student: return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase database = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated = 0;

        if (null == selection) selection = "1";

        switch (match){
            case WEATHER:
                rowsUpdated = database.update(WeatherEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = database.update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = weatherDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                Log.i("URI matcher", "Matched WEATHER");
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                Log.i("URI matcher", "DOES NOT Matched WEATHER");
                return super.bulkInsert(uri, values);
        }
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = Long.parseLong(values.getAsString(WeatherContract.WeatherEntry.COLUMN_DATE));
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION, LOCATION);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);


        // 3) Return the new matcher!
        return matcher;
    }


    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        weatherDbHelper.close();
        super.shutdown();
    }

}
