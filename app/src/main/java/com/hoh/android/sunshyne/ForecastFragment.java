package com.hoh.android.sunshyne;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ForecastFragment extends Fragment {

    private ForecastAsyncTask forecastAsyncTask;
    private ArrayAdapter adapter;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // declare that this fragment has custom options menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the fragment components from the layout file
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        // the fake data to start the app with
        String [] fakeData = {
                "Today-Sunny-88/66",
                "Tomorrow-Foggy-70/46",
                "Weds-Cloudy-72/63",
                "Thurs-Rainy-64/51",
                "Fri-Foggy-70/46",
                "Sat-Sunny-76/68"
        };

        // create the array list for array adapter
        //ArrayList<String> dataArr = new ArrayList<>(fakeData.length);
        ArrayList<String> dataArr = new ArrayList<>();

//        for (int i = 0, l = fakeData.length; i < l; i++){
//            dataArr.add(fakeData[i]);
//        }

        // create the ArrayAdapter to be attached to the list View
        adapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, dataArr);

        ListView forecastListView = (ListView)rootView.findViewById(R.id.listView_forecast);

        // attach the string array to the
        forecastListView.setAdapter(adapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = adapter.getItem(position).toString();
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                toast.show();

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("dayForecast", message);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            updateWeather();
        }
        else if (itemId == R.id.action_viewMap){
            Log.i(MainActivity.class.getSimpleName(), "Clicked View Map Menu");
            Intent mapIntent = new Intent();
            mapIntent.setAction(Intent.ACTION_VIEW);
            String location = getPreferredLocation();
            mapIntent.setData(Uri.parse("geo:0,0?q=" + location));

            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(mapIntent);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){
        forecastAsyncTask = new ForecastAsyncTask();
        forecastAsyncTask.execute(getPreferredLocation());
    }

    public String getPreferredLocation(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences.getString(getString(R.string.location_settings_key), getString(R.string.location_default_value));
    }


    private class ForecastAsyncTask extends AsyncTask<String, Void, String[]> {

        // the log tag
        private final String LOG_TAG = ForecastAsyncTask.class.getSimpleName();

        // define constants
        private final String POSTAL_CODE_KEY = "q";
        private final String MODE_KEY = "mode";
        private final String COUNT_KEY = "cnt";
        private final String UNIT_KEY = "units";

        private String mode = "json";
        private String unit = "metric";
        private int count = 7;

        private String preferredUnit = PreferenceManager.getDefaultSharedPreferences(getActivity())
                                        .getString(getString(R.string.units_settings_key),
                                                getString(R.string.units_default_value));

        @Override
        protected String[] doInBackground(String ... params) {
            //gather the input params
            String postalCode = params[0];

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
                }

                //create a reader to read the incoming stream of data
                Reader reader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(reader);
                StringBuffer buffer = new StringBuffer();
                String line = null;

                while ((line = bufferedReader.readLine()) != null){
                    buffer.append(line + "\n");
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
                weatherList = parseForecastData(forecastData);
            }
            catch (JSONException jEx){
                jEx.printStackTrace();
            }

            return weatherList;
        }

        @Override
        protected void onPostExecute(String [] realData) {
            if ( realData != null){

                Log.i(LOG_TAG, Arrays.toString(realData));

                adapter.clear();
                for (String item : realData)
                    adapter.add(item);
                }

        }

        public String[] parseForecastData(String forecastStr) throws JSONException{

            //declare useful constants to access json objects, arrays and object properties
            final String FORECAST_LIST_KEY = "list";
            final int WEATHER_LIST_INDEX = 0;
            final String DATE_KEY = "dt";
            final String TEMP_KEY = "temp";
            final String MIN_TEMP_KEY = "min";
            final String MAX_TEMP_KEY = "max";
            final String WEATHER_KEY = "weather";
            final String MAIN_KEY = "main";

            //initialize the array of forecast data Strings to return
            String [] forecastArr = new String[count];

            JSONObject forecastJSONObject = new JSONObject(forecastStr);
            JSONArray forecastListArr = forecastJSONObject.getJSONArray(FORECAST_LIST_KEY);
            JSONObject dayForecastObject, dayTempJSONObject, dayWeatherJSONObject;
            JSONArray dayWeatherJSONArr;

            Long date;
            String dateStr, mainWeatherDesc;
            double minDayTemp, maxDayTemp;


            for (int i = 0; i < count; i++){
                dayForecastObject = forecastListArr.getJSONObject(i);
                dayTempJSONObject = dayForecastObject.getJSONObject(TEMP_KEY);
                dayWeatherJSONArr = dayForecastObject.getJSONArray(WEATHER_KEY);
                dayWeatherJSONObject = dayWeatherJSONArr.getJSONObject(WEATHER_LIST_INDEX);

                date = dayForecastObject.getLong(DATE_KEY);
                minDayTemp = dayTempJSONObject.getDouble(MIN_TEMP_KEY);
                maxDayTemp = dayTempJSONObject.getDouble(MAX_TEMP_KEY);

                if ( !preferredUnit.equalsIgnoreCase(getString(R.string.units_default_value))){
                    minDayTemp = convertTemp(minDayTemp);
                    maxDayTemp = convertTemp(maxDayTemp);
                }

                mainWeatherDesc = dayWeatherJSONObject.getString(MAIN_KEY);

                dateStr = getReadableDateString(date);

                forecastArr[i] = dateStr + " - " + mainWeatherDesc + " - " + getFormattedTemp(minDayTemp, maxDayTemp);
            }


            return forecastArr;
        }

        public String getReadableDateString(Long date){
            String pattern = "E, MMM d";
            Date dateObj = new Date(date * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(dateObj).toString();
        }

        public String getFormattedTemp(Double minDayTemp, Double maxDayTemp){
            return Math.round(minDayTemp) + "/" + Math.round(maxDayTemp);

        }

        public double convertTemp(double temp){
            return (temp + 32) * 9.0 / 5.0;
        }


    }


}



