package com.example.android.popuarmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.w3c.dom.Text;

public class FavouritesDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "favoritesDb.db";

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 26;

    // Constructor
    FavouritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the tasks database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tasks table (careful to follow SQL formatting rules)
        final String CREATE_TABLE = "CREATE TABLE " + FavouritesContract.FavouritesEntry.TABLE_NAME + " (" +
                FavouritesContract.FavouritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavouritesContract.FavouritesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavouritesContract.FavouritesEntry.COLUMN_FILM_ID + " INTEGER NOT NULL, " +
                FavouritesContract.FavouritesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavouritesContract.FavouritesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                FavouritesContract.FavouritesEntry.COLUMN_POSTER + " BLOB, " +
                FavouritesContract.FavouritesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                FavouritesContract.FavouritesEntry.COLUMN_USER_RATING + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouritesContract.FavouritesEntry.TABLE_NAME);
        onCreate(db);
    }
}
