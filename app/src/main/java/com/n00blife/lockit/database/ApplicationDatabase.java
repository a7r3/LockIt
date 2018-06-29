package com.n00blife.lockit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;

import java.util.ArrayList;

public class ApplicationDatabase extends SQLiteOpenHelper {

    // Increment this if you've performed any changes
    // to the database (for eg. Changing column info)
    // Incrementing it would drop older version's DB
    // See onUpgrade() for implementation
    private static final int DATABASE_VERSION = 1;
    // Give the DB a good name
    private static final String DATABASE_NAME = "Apps";
    // Static Instance of Database Helper
    private static ApplicationDatabase applicationDatabaseInstance;
    private final String TAG = getClass().getSimpleName();
    // Table name
    private final String APP_LIST_TABLE = "apps";

    // Table columns
    private final String ID_KEY = "id";
    private final String PACKAGE_NAME_KEY = "package_name";
    private final String APPLICATION_NAME = "app_label";
    private final String APPLICATION_VERSION = "app_version";
    private final String APPLICATION_ICON = "app_icon";

    private ApplicationDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Make sure that we get a single AppDB Instance throughout
    // the Application (Singleton Instance they said)
    public static synchronized ApplicationDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (applicationDatabaseInstance == null) {
            applicationDatabaseInstance = new ApplicationDatabase(
                    context.getApplicationContext(),
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
        }
        return applicationDatabaseInstance;
    }

    // Creates a DB only if it does not exist in the Storage
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_DB = "CREATE TABLE " + APP_LIST_TABLE +
                "("
                + ID_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PACKAGE_NAME_KEY + " TEXT, "
                + APPLICATION_NAME + " TEXT, "
                + APPLICATION_VERSION + " TEXT,"
                + APPLICATION_ICON + " TEXT" +
                ")";

        // Execute above SQL Query
        sqLiteDatabase.execSQL(CREATE_DB);
        Log.i(TAG, "Database " + APP_LIST_TABLE + "created");
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

    public void addApplication(Application application) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues quoteContentValues = new ContentValues();
        // Inserting values into respective fields
        quoteContentValues.put(APPLICATION_NAME, application.getApplicationName());
        quoteContentValues.put(APPLICATION_VERSION, application.getApplicationVersion());
        quoteContentValues.put(PACKAGE_NAME_KEY, application.getApplicationPackageName());
        quoteContentValues.put(APPLICATION_ICON, ImageUtils.encodeBitmapToBase64(application.getApplicationIcon()));
        sqLiteDatabase.beginTransaction();
        try {
            // Perform Insert Operation with the above Values
            // Throws an exception if it occurs
            sqLiteDatabase.insertOrThrow(APP_LIST_TABLE, null, quoteContentValues);
            // Set current transaction as successful
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    // Required arg -> ID of the quote
    public void removeApplication(Application application) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            // Delete a row by its ID (Primary Key)
            // args -> TableName, Clause (condition), Arguments (I've put nothing)
            sqLiteDatabase.delete(APP_LIST_TABLE, PACKAGE_NAME_KEY + "=" + application.getApplicationPackageName(), null);
            Log.d(TAG, "Removed App " + application.getApplicationPackageName() + " successfully");
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public ArrayList<Application> getAllApplications() {

        // ArrayList to hold the results
        ArrayList<Application> applicationArrayList = new ArrayList<>();

        String QUERY = "SELECT * FROM " + APP_LIST_TABLE;

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor appDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        // Returns true if cursor is moved to the first row successfully
        if (appDataCursor.moveToFirst()) {
            do {
                applicationArrayList.add(new Application(
                        appDataCursor.getString(appDataCursor.getColumnIndex(APPLICATION_NAME)),
                        appDataCursor.getString(appDataCursor.getColumnIndex(PACKAGE_NAME_KEY)),
                        appDataCursor.getString(appDataCursor.getColumnIndex(APPLICATION_VERSION)),
                        ImageUtils.decodeBase64ToBitmap(appDataCursor.getString(appDataCursor.getColumnIndex(APPLICATION_ICON)))
                ));
            } while (appDataCursor.moveToNext());
        }

        // Prevents any other Query to be executed after this
        appDataCursor.close();
        sqLiteDatabase.endTransaction();

        return applicationArrayList;
    }

    public ArrayList<String> getAllPackages() {

        // ArrayList to hold the results
        ArrayList<String> applicationArrayList = new ArrayList<>();

        String QUERY = "SELECT * FROM " + APP_LIST_TABLE;

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();
        // Runs the Query and returns the result to this Cursor
        // Cursor <-> Result Iterator
        Cursor appDataCursor = sqLiteDatabase.rawQuery(QUERY, null);
        // Returns true if cursor is moved to the first row successfully
        if (appDataCursor.moveToFirst()) {
            do {
                applicationArrayList.add(appDataCursor.getString(appDataCursor.getColumnIndex(PACKAGE_NAME_KEY)));
            } while (appDataCursor.moveToNext());
        }

        // Prevents any other Query to be executed after this
        appDataCursor.close();
        sqLiteDatabase.endTransaction();

        return applicationArrayList;
    }

    public long getRowCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), APP_LIST_TABLE);
    }

}
