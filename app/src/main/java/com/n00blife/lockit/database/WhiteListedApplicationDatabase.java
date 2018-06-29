package com.n00blife.lockit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.n00blife.lockit.model.Profile;

import java.util.ArrayList;

public class WhiteListedApplicationDatabase extends SQLiteOpenHelper {

    // Increment this if you've performed any changes
    // to the database (for eg. Changing column info)
    // Incrementing it would drop older version's DB
    // See onUpgrade() for implementation
    private static final int DATABASE_VERSION = 1;
    // Give the DB a good name
    private static final String DATABASE_NAME = "WhiteListedApps";
    // Static Instance of Database Helper
    private static WhiteListedApplicationDatabase whiteListedApplicationDatabase;
    private final String TAG = getClass().getSimpleName();
    // Table name
    private final String WHITELISTED_APP_LIST_TABLE = "apps";

    // Table columns
    private final String ID_KEY = "id";
    private final String PROFILE_NAME = "profile";
    private final String PACKAGE_LIST = "packages";

    private WhiteListedApplicationDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Make sure that we get a single AppDB Instance throughout
    // the Application (Singleton Instance they said)
    public static synchronized WhiteListedApplicationDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (whiteListedApplicationDatabase == null) {
            whiteListedApplicationDatabase = new WhiteListedApplicationDatabase(
                    context.getApplicationContext(),
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
        }
        return whiteListedApplicationDatabase;
    }

    // Creates a DB only if it does not exist in the Storage
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_DB = "CREATE TABLE " + WHITELISTED_APP_LIST_TABLE +
                "("
                + PROFILE_NAME + " TEXT, "
                + PACKAGE_LIST + " TEXT" +
                ")";

        // Execute above SQL Query
        sqLiteDatabase.execSQL(CREATE_DB);
        Log.i(TAG, "Database " + WHITELISTED_APP_LIST_TABLE + "created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // i -> oldVersion
        // i1 -> newVersion
        if (i != i1) {
            // Simplest implementation is to drop all old tables and recreate them
            onCreate(sqLiteDatabase);
        }
    }

    public void createProfile(String profileName, ArrayList<String> packageList) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues quoteContentValues = new ContentValues();
        // Inserting values into respective fields

        quoteContentValues.put(PACKAGE_LIST, new Gson().toJson(packageList));
        quoteContentValues.put(PROFILE_NAME, profileName);
        sqLiteDatabase.beginTransaction();
        try {
            // Perform Insert Operation with the above Values
            // Throws an exception if it occurs
            sqLiteDatabase.insertOrThrow(WHITELISTED_APP_LIST_TABLE, null, quoteContentValues);
            // Set current transaction as successful
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    // Required arg -> ID of the quote
    public void deleteProfile(String profileName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            // Delete a row by its ID (Primary Key)
            // args -> TableName, Clause (condition), Arguments (I've put nothing)
            sqLiteDatabase.delete(WHITELISTED_APP_LIST_TABLE, PROFILE_NAME + "=" + profileName, null);
            Log.d(TAG, "Removed App " + profileName + " successfully");
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public ArrayList<String> getPackageListForProfile(String profileName) {
        // ArrayList to hold the results
        ArrayList<String> packageList;
        String QUERY = "SELECT * FROM " + WHITELISTED_APP_LIST_TABLE + " WHERE " + PROFILE_NAME + "='" + profileName + "'";
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor appDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        appDataCursor.moveToFirst();
        // Returns true if cursor is moved to the first row successfully
        packageList = new Gson().fromJson(appDataCursor.getString(appDataCursor.getColumnIndex(PACKAGE_LIST)), new TypeToken<ArrayList<String>>() {
        }.getType());
        // Prevents any other Query to be executed after this
        appDataCursor.close();
        sqLiteDatabase.endTransaction();
        return packageList;
    }

    public ArrayList<Profile> getProfiles() {
        ArrayList<Profile> profileList = new ArrayList<>();
        String QUERY = "SELECT * FROM " + WHITELISTED_APP_LIST_TABLE;
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor appDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        if (appDataCursor.moveToFirst()) {
            do {
                ArrayList<String> list = new Gson().fromJson(appDataCursor.getString(appDataCursor.getColumnIndex(PACKAGE_LIST)), new TypeToken<ArrayList<String>>() {
                }.getType());
                profileList.add(new Profile(
                        appDataCursor.getString(appDataCursor.getColumnIndex(PROFILE_NAME)),
                        list
                ));
            } while (appDataCursor.moveToNext());
        }
        // Returns true if cursor is moved to the first row successfully
        // Prevents any other Query to be executed after this
        appDataCursor.close();
        sqLiteDatabase.endTransaction();
        return profileList;
    }

    public long getRowCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), WHITELISTED_APP_LIST_TABLE);
    }

}
