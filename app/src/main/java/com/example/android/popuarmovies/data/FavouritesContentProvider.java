package com.example.android.popuarmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.TABLE_NAME;

public class FavouritesContentProvider extends ContentProvider {

    private FavouritesDbHelper mfavouritesDbHelper;

    @Override
    public boolean onCreate() {
        // initialize a FavouritesDbhelper on startup
        Context context = getContext();
        mfavouritesDbHelper = new FavouritesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mfavouritesDbHelper.getWritableDatabase();

        Cursor retCursor;

        retCursor =  db.query(TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Insert to handle requests to insert a single new row of data
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Get access to the favourites database (to write new data to)
        final SQLiteDatabase db = mfavouritesDbHelper.getWritableDatabase();

        Uri returnUri; // URI to be returned

        // Insert new values into the database
        // Inserting values into favourites table
        Long id = db.insert(TABLE_NAME, null, values);
        if (id > 0) {
            // Set the value for the returnedUri
            returnUri = ContentUris.withAppendedId(FavouritesContract.FavouritesEntry.CONTENT_URI, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mfavouritesDbHelper.getWritableDatabase();
        // Keep track of the number of deleted tasks
        int tasksDeleted;

        // Use selections/selectionArgs to filter for this ID
        tasksDeleted = db.delete(TABLE_NAME, selection, selectionArgs);

        // Notify the resolver of a change and return the number of items deleted
        if (tasksDeleted != 0) {
            // A favourite was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return tasksDeleted;
        }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
