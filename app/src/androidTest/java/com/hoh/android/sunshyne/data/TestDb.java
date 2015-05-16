package com.hoh.android.sunshyne.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

import com.hoh.android.sunshyne.utils.PollingCheck;

import java.sql.SQLOutput;
import java.util.HashSet;

/**
 * Created by funso on 3/14/15.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteDb(){
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp(){
        deleteDb();
    }

    public void testCreateDb() throws Throwable{

        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable(){
        insertLocation();
    }

    public void testWeatherTable(){
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        long locId = insertLocation();

        // First step: Get reference to writable database

        SQLiteDatabase database = new WeatherDbHelper(mContext).getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues contentValues = TestUtilities.createWeatherValues(locId);

        // Insert ContentValues into database and get a row ID back
        database.insert(WeatherContract.WeatherEntry.TABLE_NAME,
               null,
               contentValues);

        // Query the database and receive a Cursor back
        String [] projection = new String[]{

        };
        Cursor cursor = database.query(
               WeatherContract.WeatherEntry.TABLE_NAME,
               projection,
                null,
                null,
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE
       );

        // Move the cursor to a valid database row
        cursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        TestUtilities.validateCurrentRecord("Error retrieving record", cursor, contentValues);

        // Finally, close the cursor and database
        if (!cursor.isClosed())
            cursor.close();
        if (database.isOpen())
            database.close();
    }

    public long insertLocation(){
        // First step: Get reference to writable database
        SQLiteDatabase database = new WeatherDbHelper(mContext).getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)

        ContentValues records = TestUtilities.createNorthPoleLocationValues();

        // Insert ContentValues into database and get a row ID back
        long rowId = database.insert(WeatherContract.LocationEntry.TABLE_NAME,
                null,
                records);

        assertTrue(rowId != -1L);

        // Query the database and receive a Cursor back
        String [] projection = new String []{
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT
        };

        Cursor cursor = database.query(WeatherContract.LocationEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                WeatherContract.LocationEntry._ID);

        ContentValues expectedResult = new ContentValues();
        expectedResult.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        expectedResult.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TestUtilities.TEST_LOCATION);
        expectedResult.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        expectedResult.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);



        // Move the cursor to a valid database row
        cursor.moveToFirst();

        TestUtilities.validateCurrentRecord("No Record", cursor, expectedResult);
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
        if (!cursor.isClosed())
            cursor.close();
        if (database.isOpen())
            database.close();

        return rowId;
    }


}
