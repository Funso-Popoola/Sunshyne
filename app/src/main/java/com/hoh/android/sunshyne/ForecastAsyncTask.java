package com.hoh.android.sunshyne;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.hoh.android.sunshyne.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by funso on 5/16/15.
 */
class ForecastAsyncTask extends AsyncTask<String, Void, String[]> {

    // the log tag
    private final String LOG_TAG = ForecastAsyncTask.class.getSimpleName();

    private final Context context;

    private String locationSetting;
    // define constants
    private final String POSTAL_CODE_KEY = "q";
    private final String MODE_KEY = "mode";
    private final String COUNT_KEY = "cnt";
    private final String UNIT_KEY = "units";

    private String mode = "json";
    private String unit = "metric";
    private int count = 7;

    private String preferredUnit;


    public ForecastAsyncTask(Context context){
        this.context = context;
//        mForecastAdapter = forecastAdapter;
        preferredUnit = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.units_settings_key),
                        context.getString(R.string.units_default_value));
    }

    @Override
    protected String[] doInBackground(String ... params) {
        //gather the input params
        String postalCode = params[0];
        this.locationSetting = postalCode;

        //initialize components
        String forecastData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;

        // build the uri with Uri Builder with the param key-value built into it
        String baseUri = "http://api.openweathermap.org/data/2.5/forecast/daily";
        Uri uri = Uri.parse(baseUri);
        uri = uri.buildUpon()
                .appendQueryParameter(POSTAL_CODE_KEY, postalCode)
                .appendQueryParameter(MODE_KEY, mode)
                .appendQueryParameter(UNIT_KEY, unit)
                .appendQueryParameter(COUNT_KEY, ((Integer)count).toString())
                .build();

        //extract url from the built uri
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //help cross-check the url returned
        Log.i(LOG_TAG + " URL", url.toString());


        try{

            //create connection to OpenMapWeather and connect
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // check if there is any input gotten
            inputStream = urlConnection.getInputStream();
            if (inputStream == null){
                forecastData = null;
                return null;
            }

            //create a reader to read the incoming stream of data
            Reader reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            StringBuilder buffer = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if (buffer.length() != 0)
                forecastData = buffer.toString();


        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {

            if (urlConnection != null)  urlConnection.disconnect();

            if (inputStream != null){
                try{
                    inputStream.close();
                }
                catch (final IOException ex){
                    Log.e(LOG_TAG, "Error closing InputStream!");
                }
            }
        }
        String [] weatherList = null;
        try{
            parseAndStoreForecastData(forecastData);
        }
        catch (JSONException jEx){
            jEx.printStackTrace();
        }

        return null;
    }

    public void parseAndStoreForecastData(String forecastStr) throws JSONException{

        //declare useful constants to access json objects, arrays and object properties
        final String FORECAST_LIST_KEY = "list";
        final String FORECAST_CITY_KEY = "city";
        final String CITY_NAME_KEY = "name";
        final String COORD_KEY = "coord";
        final int WEATHER_LIST_INDEX = 0;
        final String TEMP_KEY = "temp";
        final String MIN_TEMP_KEY = "min";
        final String MAX_TEMP_KEY = "max";
        final String WEATHER_KEY = "weather";
        final String MAIN_KEY = "main";
        final String LAT_KEY = "lat";
        final String LNG_KEY = "lon";
        final String PRESSURE_KEY = "pressure";
        final String HUMIDITY_KEY = "humidity";
        final String WIND_SPEED_KEY = "speed";
        final String DEGREE = "deg";
        final String WEATHER_ID = "id";

        //initialize the array of forecast data Strings to return
        if (null == forecastStr)
            return;

        JSONObject forecastJSONObject = new JSONObject(forecastStr);
        JSONArray forecastListArr = forecastJSONObject.getJSONArray(FORECAST_LIST_KEY);
        JSONObject cityObject = forecastJSONObject.getJSONObject(FORECAST_CITY_KEY);
        String cityName = cityObject.getString(CITY_NAME_KEY);
        JSONObject coordObject = cityObject.getJSONObject(COORD_KEY);
        double lat = coordObject.getDouble(LAT_KEY);
        double lng = coordObject.getDouble(LNG_KEY);

        long locationId = addLocation(locationSetting, cityName, lat, lng);
        JSONObject dayForecastObject, dayTempJSONObject, dayWeatherJSONObject;
        JSONArray dayWeatherJSONArr;

        Long dateTime;
        String mainWeatherDesc;
        double minDayTemp, maxDayTemp;
        double humidity, pressure, windSpeed, windDirection;
        long weatherId;

        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(count);

        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new Time();

        for (int i = 0; i < forecastListArr.length(); i++){
            dayForecastObject = forecastListArr.getJSONObject(i);
            dayTempJSONObject = dayForecastObject.getJSONObject(TEMP_KEY);

            humidity = dayForecastObject.getDouble(HUMIDITY_KEY);
            pressure = dayForecastObject.getDouble(PRESSURE_KEY);
            windSpeed = dayForecastObject.getDouble(WIND_SPEED_KEY);
            windDirection = dayForecastObject.getDouble(DEGREE);

            dayWeatherJSONArr = dayForecastObject.getJSONArray(WEATHER_KEY);
            dayWeatherJSONObject = dayWeatherJSONArr.getJSONObject(WEATHER_LIST_INDEX);

            dateTime = dayTime.setJulianDay(julianStartDay + i);
            minDayTemp = dayTempJSONObject.getDouble(MIN_TEMP_KEY);
            maxDayTemp = dayTempJSONObject.getDouble(MAX_TEMP_KEY);

            if ( !preferredUnit.equalsIgnoreCase(context.getString(R.string.units_default_value))){
                minDayTemp = Utility.convertTemp(minDayTemp);
                maxDayTemp = Utility.convertTemp(maxDayTemp);
            }

            weatherId = dayWeatherJSONObject.getLong(WEATHER_ID);
            mainWeatherDesc = dayWeatherJSONObject.getString(MAIN_KEY);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_ID, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, maxDayTemp);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, minDayTemp);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, mainWeatherDesc);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            contentValuesVector.add(weatherValues);
        }

        int numOfRowsInserted = 0;

        // add to database

        if ( contentValuesVector.size() > 0 ) {
            ContentValues [] contentValuesArr = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArr);
            numOfRowsInserted = context.getContentResolver().bulkInsert(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    contentValuesArr
            );
        }

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = context.getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        contentValuesVector = new Vector<>(cur.getCount());
        if ( cur.moveToFirst() ) {
            do {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                contentValuesVector.add(cv);
            } while (cur.moveToNext());
        }

        Log.d(LOG_TAG, "FetchWeatherTask Complete. " + numOfRowsInserted + " Inserted");

    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        Uri locationUri = WeatherContract.LocationEntry.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(
                locationUri,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String[]{locationSetting},
                null
        );
        // If it exists, return the current ID
        if (null != cursor && cursor.moveToFirst()){
            long id = cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
            cursor.close();
            return id;
        }
        // Otherwise, insert it using the content resolver and the base URI
        else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri resultUri = context.getContentResolver().insert(locationUri, contentValues);
            return Long.parseLong(resultUri.getPathSegments().get(1));
        }

    }



}
