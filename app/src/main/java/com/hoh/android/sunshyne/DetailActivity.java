package com.hoh.android.sunshyne;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoh.android.sunshyne.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    private final static String LOG_TAG = DetailActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private final String[] PROJECTION = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
        } ;

        private Uri uri;

        private final String SHARE_HASH_TAG = "#SunShineApp";
        private final int DETAIL_LOADER = 1;

        private TextView detailTextView;
        private ShareActionProvider shareActionProvider;
        private String shareString;

        public DetailFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            detailTextView = (TextView) rootView.findViewById(R.id.detail_textView);
            String detailText = getActivity().getIntent().getDataString();
            uri = Uri.parse(detailText);
//            detailTextView.setText(detailText);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getActivity().getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        public void setUpShareActivity(){

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            // disallow this app used in sharing from getting to application back-stack
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            // set the data mime type as plain text
            shareIntent.setType("text/plain");

            // put the string to share as an EXTRA_TEXT of the intent
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);

            // launch the share intent
            shareActionProvider.setShareIntent(shareIntent);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            setUpShareActivity();
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Log.i(DetailActivity.LOG_TAG, "Option Clicked");
            switch(item.getItemId()){
                case R.id.action_share:
                    Log.i(DetailActivity.LOG_TAG, "Clicked Share");
                    setUpShareActivity();
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getActivity(),
                    uri,
                    PROJECTION,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToLast();
            String detail = ForecastAdapter.convertCursorRowToUXFormat(data);
            detailTextView.setText(detail);
            shareString = detail + " " + SHARE_HASH_TAG;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            detailTextView.setText("");
        }
    }
}
