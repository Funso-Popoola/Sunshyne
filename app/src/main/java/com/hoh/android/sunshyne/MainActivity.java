package com.hoh.android.sunshyne;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    private String location;
    private String FORECAST_FRAGMENT_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //restore the saved state of this instance
        super.onCreate(savedInstanceState);

        //set the view to the main activity layout
        setContentView(R.layout.activity_main);
        location = Utility.getPreferredLocation(this);

        // add the forecast fragment only if there is no savedInstanceState
        // no savedInstanceState means the app is just been created, not re-created
        // hence, no fragment has been created yet

        if (savedInstanceState == null) {
            ForecastFragment forecastFragment = new ForecastFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, forecastFragment, FORECAST_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        String preferredLocation = Utility.getPreferredLocation(this);
        if (!preferredLocation.equalsIgnoreCase(location)){
            ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECAST_FRAGMENT_TAG);
            fragment.onLocationChanged();
            location = preferredLocation;
        }
        super.onResume();
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }



}

