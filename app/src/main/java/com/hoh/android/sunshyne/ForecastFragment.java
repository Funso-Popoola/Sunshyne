package com.hoh.android.sunshyne;

import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hoh.android.sunshyne.data.WeatherContract;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private final int FORECAST_LOADER = 0;
    private ForecastAsyncTask forecastAsyncTask;
    private ForecastAdapter adapter;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // declare that this fragment has custom options menu
        setHasOptionsMenu(true);
    }

    public void onLocationChanged(){
        updateWeather();
        getActivity().getSupportLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the fragment components from the layout file
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapter = new ForecastAdapter(getActivity(), null, 0);
        ListView forecastListView = (ListView)rootView.findViewById(R.id.listView_forecast);

        forecastListView.setAdapter(adapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = "Not a cursor";
                Cursor cursor = (Cursor) adapter.getItem(position);

                if (cursor != null) {
                    message = ForecastAdapter.convertCursorRowToUXFormat(cursor);
                }
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                toast.show();

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        Utility.getPreferredLocation(getActivity()),
                        cursor.getLong(COL_WEATHER_DATE)
                ));
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
            String location = Utility.getPreferredLocation(getActivity());
            mapIntent.setData(Uri.parse("geo:0,0?q=")
                    .buildUpon()
                    .appendQueryParameter("q", location)
                    .build());

            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                Log.i(LOG_TAG, "Starting the Map activity");
                startActivity(mapIntent);
            }
            else{
                Log.i(LOG_TAG, "Unable to start the Map Activity");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){
        forecastAsyncTask = new ForecastAsyncTask(getActivity());
        forecastAsyncTask.execute(Utility.getPreferredLocation(getActivity()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}





