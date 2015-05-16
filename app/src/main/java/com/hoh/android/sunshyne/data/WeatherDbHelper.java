package com.hoh.android.sunshyne.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.hoh.android.sunshyne.data.WeatherContract.LocationEntry;
import com.hoh.android.sunshyne.data.WeatherContract.WeatherEntry;

/**
 * Created by funso on 3/14/15.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    public static final int DATABASE_VERSION = 1;

    public WeatherDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME +
                "( "
                + LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, "
                + LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, "
                + "UNIQUE (" + LocationEntry.COLUMN_COORD_LAT + ", "
                + LocationEntry.COLUMN_COORD_LONG + ") ON CONFLICT REPLACE "
                + " );";

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME +
                " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherEntry.COLUMN_LOC_ID + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_ID + ") REFERENCES " +
                LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + "), " +
                "UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " + WeatherEntry.COLUMN_LOC_ID +
                ") ON CONFLICT REPLACE " +
                ");";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
