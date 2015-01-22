package com.hoh.android.sunshyne;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ForecastFragment extends Fragment {

        public ForecastFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            String [] fakeData = {
                    "Today-Sunny-88/66",
                    "Tomorrow-Foggy-70/46",
                    "Weds-Cloudy-72/63",
                    "Thurs-Rainy-64/51",
                    "Fri-Foggy-70/46",
                    "Sat-Sunny-76/68"
            };
            ArrayList<String> dataArr = new ArrayList<>(fakeData.length);
            for (int i = 0, l = fakeData.length; i < l; i++){
                dataArr.add(fakeData[i]);
            }

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, dataArr);
            ListView forecastListView = (ListView)rootView.findViewById(R.id.listView_forecast);
            forecastListView.setAdapter(adapter);
            return rootView;
        }

        public String getForecastData(String postalCode, String mode, int count){
            final String POSTAL_CODE_KEY = "q";
            final String MODE_KEY = "mode";
            final String COUNT_KEY = "days";
            String forecastData = null;
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String baseUri = "http://";
            Uri uri = Uri.parse(baseUri);
            uri.buildUpon()
                    .appendQueryParameter(POSTAL_CODE_KEY, postalCode)
                    .appendQueryParameter(MODE_KEY, mode)
                    .appendQueryParameter(COUNT_KEY, ((Integer)count).toString())
                    .build();

            return forecastData;
        }
    }
}
