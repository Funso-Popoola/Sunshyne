package com.hoh.android.sunshyne;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    private ForecastFragment forecastFragment;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //restore the saved state of this instance
        super.onCreate(savedInstanceState);

        //set the view to the main activity layout
        setContentView(R.layout.activity_main);

        //add the forecast fragment only if there is no savedInstanceState
        if (savedInstanceState == null) {
            forecastFragment = new ForecastFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, forecastFragment)
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
//        else if (id == R.id.action_viewMap){
//            Log.i(MainActivity.class.getSimpleName(), "Clicked View Map Menu");
//            Intent mapIntent = new Intent();
//            mapIntent.setAction(Intent.ACTION_VIEW);
//            location = forecastFragment.getPreferredLocation();
//            mapIntent.setData(Uri.parse("geo:0,0?q=" + location));
//
//            if (mapIntent.resolveActivity(getPackageManager()) != null){
//                startActivity(mapIntent);
//            }
//
//        }

        return super.onOptionsItemSelected(item);
    }




}

