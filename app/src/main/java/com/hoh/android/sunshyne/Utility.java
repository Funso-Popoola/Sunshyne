package com.hoh.android.sunshyne;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.hoh.android.sunshyne.data.WeatherContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by funso on 5/9/15.
 */
public class Utility {

    public static String getReadableDateString(Long date){
//        date = WeatherContract.normalizeDate(date);
        String pattern = "E, MMM d";
        Date dateObj = new Date(date * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return dateFormat.format(dateObj);
    }

    public static String getFormattedTemp(Double minDayTemp, Double maxDayTemp){
        return Math.round(minDayTemp) + "/" + Math.round(maxDayTemp);
    }


    public static double convertTemp(double temp){
        return (temp + 32) * 9.0 / 5.0;
    }


    public static String getPreferredLocation(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.location_settings_key), context.getString(R.string.location_default_value));
    }
    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }


}
