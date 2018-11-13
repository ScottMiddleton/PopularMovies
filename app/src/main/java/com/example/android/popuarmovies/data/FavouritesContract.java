package com.example.android.popuarmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class FavouritesContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FavouritesContract() {}

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.android.popuarmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "favourites" directory
    public static final String PATH_FAVOURITES = "favourites";



    /* Inner class that defines the table contents */
    public static class FavouritesEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        // Task table and column names
        public static final String TABLE_NAME = "favorites";

        // Since FavouritesEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_TITLE = "film_title";
        public static final String COLUMN_FILM_ID = "film_id";
        public static final String COLUMN_OVERVIEW = "synopsis";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster_byte";
        public static final String COLUMN_POSTER_URL = "poster_url";




        /*
        The above table structure looks something like the sample table below.
        With the name of the table and columns on top, and potential contents in rows

        Note: Because this implements BaseColumns, the _id column is generated automatically

        tasks
         - - - - - - - - - - - - - - - - - - - - - -
        | _id  |    title              |  film id   |
         - - - - - - - - - - - - - - - - - - - - - -
        |  1   |  Interstellar         |  434292    |
         - - - - - - - - - - - - - - - - - - - - - -
        |  2   |  Mission Impossible   |  399334    |
         - - - - - - - - - - - - - - - - - - - - - -
        .
        .
        .
         - - - - - - - - - - - - - - - - - - - - - -
        | 43   |  Toy Story           |   294979    |
         - - - - - - - - - - - - - - - - - - - - - -

         */

    }
}
