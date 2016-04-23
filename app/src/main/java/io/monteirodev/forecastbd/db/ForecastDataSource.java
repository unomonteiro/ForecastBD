package io.monteirodev.forecastbd.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import io.monteirodev.forecastbd.services.Forecast;

public class ForecastDataSource {
    public static final String TAG = ForecastDataSource.class.getSimpleName();

    private SQLiteDatabase mDatabase;
    private ForecastHelper mForecastHelper;
    private Context mContext;

    // Initialize ForeCastHelper on construct
    public ForecastDataSource(Context context){
        mContext = context;
        mForecastHelper = new ForecastHelper(mContext);
    }

    // open
    public void open() throws SQLException {
        Log.d(TAG, "Will open");
        mDatabase = mForecastHelper.getWritableDatabase();
        Log.d(TAG, "Will open");
    }

    // close
    public void close(){
        mDatabase.close();
    }

    // insert single line
    /*public void insertForecast(Forecast forecast) {
        // ContentValues is similar to key pair dictionary
        ContentValues values = new ContentValues();
        values.put(ForecastHelper.COLUMN_TEMPERATURE, 75.0);
        mDatabase.insert(ForecastHelper.TABLE_TEMPERATURES, null, values);
    }*/
    /** when inserting lots of data use ASYNC!!!
     * */
    public void insertForecast(Forecast forecast) {
        // use transaction to avoid one commit per line
        mDatabase .beginTransaction();

        try {
            for (Forecast.HourData hour :forecast.hourly.data) {
                // ContentValues is similar to key pair dictionary
                ContentValues values = new ContentValues();
                values.put(ForecastHelper.COLUMN_TEMPERATURE, hour.temperature);
                mDatabase.insert(ForecastHelper.TABLE_TEMPERATURES, null, values);
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            // ensure to close the Transaction
            mDatabase.endTransaction();
        }
    }

    // select
    /** function that returns a Cursor object */
    public Cursor selectAllTemperatures() {
        /** this cursor will be tight to the resultSet */
        Cursor cursor = mDatabase.query(
                ForecastHelper.TABLE_TEMPERATURES, // table
                new String[] { ForecastHelper.COLUMN_TEMPERATURE }, // array of column names
                null, // where clause
                null, // where params
                null, // group by
                null, // having
                null // order by
        );
        return cursor;
    }
    public Cursor selectTempsGreaterThan(String minTemp) {
        //String whereClause = "TEMPERATURE > minTemp";
        String whereClause = ForecastHelper.COLUMN_TEMPERATURE +" > ?";

        Cursor cursor = mDatabase.query(
                ForecastHelper.TABLE_TEMPERATURES, // table
                new String[] { ForecastHelper.COLUMN_TEMPERATURE }, // array of column names
                whereClause, // where clause
                new String[] { minTemp }, // where params ? : @
                null, // group by
                null, // having
                null // order by
        );
        return cursor;
    }
    public long selectHighTemp() {
        long highTemp = 0;
        try{
            //select max(temperatures) as maxTemp from Temperatures
            Cursor cursor = mDatabase.rawQuery("SELECT MAX(" + ForecastHelper.COLUMN_TEMPERATURE + ") " +
                    "FROM " + ForecastHelper.TABLE_TEMPERATURES, null);
            cursor.moveToFirst();
            highTemp = cursor.getLong(0);
        } catch (SQLException e){
            Log.e(TAG, e.toString());
        }

        return highTemp;
    }
    public long selectLowTemp() {
        long lowTemp = 0;
        try{
            //select max(temperatures) as maxTemp from Temperatures
            Cursor cursor = mDatabase.rawQuery("SELECT MIN(" + ForecastHelper.COLUMN_TEMPERATURE + ") " +
                    "FROM " + ForecastHelper.TABLE_TEMPERATURES, null);
            cursor.moveToFirst();
            lowTemp = cursor.getLong(0);
        } catch (SQLException e){
            Log.e(TAG, e.toString());
        }

        return lowTemp;
    }

    // update
    /** returns the number of rows updated */
    public int updateTemperature(double newTemp) {
        ContentValues values = new ContentValues();
        values.put(ForecastHelper.COLUMN_TEMPERATURE, newTemp);
        int rowsUpdated = mDatabase.update(
                ForecastHelper.TABLE_TEMPERATURES, // table
                values, // values
                null, // whereClause
                null // whereArgs
        );

        return rowsUpdated;
    }

    // delete
    public void deleteAll() {
        mDatabase.delete(
                ForecastHelper.TABLE_TEMPERATURES, // table
                null, // where clause
                null // where params
        );
    }
}
