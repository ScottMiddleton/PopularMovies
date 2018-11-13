package com.example.android.popuarmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.BundleCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.popuarmovies.data.FavouritesContract;
import com.example.android.popuarmovies.utilities.NetworkUtils;
import com.example.android.popuarmovies.utilities.OpenMovieJsonUtils;

import java.net.URL;
import java.util.ArrayList;

import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MoviePosterOnClickHandler {

    public final static int BUILD_MAIN_URL = 111;
    public final static int BUILD_DETAIL_URL = 112;
    public final static int BUILD_FAVOURITES_URL = 113;

    public final static String LIST_STATE_KEY = "list_state";
    private Parcelable mListState;
    private RecyclerView.LayoutManager mLayoutManager;


    public MovieAdapter mMovieAdapter;

    public String sortByParam;

    ProgressBar mLoadingIndicator;

    FetchMovieDataTask mMovieDataTask;

    RecyclerView recyclerViewGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = findViewById(R.id.indeterminateBar);

        recyclerViewGrid = findViewById(R.id.recycler_view);
        recyclerViewGrid.setLayoutManager(new GridLayoutManager(this, numberOfColumns()));
        mLayoutManager = recyclerViewGrid.getLayoutManager();

        recyclerViewGrid.setHasFixedSize(false);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sortByParam = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        mMovieDataTask = new FetchMovieDataTask();
        mMovieDataTask.execute(sortByParam);

        mMovieAdapter = new MovieAdapter(this, this, sortByParam);
        recyclerViewGrid.setAdapter(mMovieAdapter);
    }

    @Override
    public void onClick(Movie movieInstance) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, movieInstance);
        startActivity(intentToStartDetailActivity);
    }

    public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            ArrayList<Movie> favouritesMovieArray = new ArrayList<>();
            ArrayList<Movie> movieInstances = new ArrayList<>();
            /* If there's no zip code, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            if (sortByParam.equals("favourites")) {
                Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, null, "_id");

                try {
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_FILM_ID));
                        byte[] poster = cursor.getBlob(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_POSTER));
                        String title = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_TITLE));
                        String overview = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_OVERVIEW));
                        String rating = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_USER_RATING));
                        String releaseDate = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_RELEASE_DATE));
                        String posterUrl = cursor.getString(cursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_POSTER_URL));

                        Movie movieInstance = new Movie(title, poster, posterUrl, overview, rating, releaseDate, id);
                        favouritesMovieArray.add(movieInstance);
                    }
                } finally {
                    cursor.close();
                }
            } else {
                URL movieDataRequestUrl = NetworkUtils.buildUrl(sortByParam, BUILD_MAIN_URL, null);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(movieDataRequestUrl);

                    return OpenMovieJsonUtils
                            .getMovieDataFromJson(jsonWeatherResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (sortByParam.equals("favourites")) {
                return favouritesMovieArray;
            } else {
                return movieInstances;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieInstances) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieInstances != null) {
                mMovieAdapter.setMovieData(movieInstances);
            }
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    @Override
    protected void onResume() {
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);}
        mMovieAdapter = new MovieAdapter(this, this, sortByParam);
        recyclerViewGrid.setAdapter(mMovieAdapter);
        mMovieDataTask = new FetchMovieDataTask();
        mMovieDataTask.execute(sortByParam);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save list state
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        if(state != null)
            mListState = state.getParcelable(LIST_STATE_KEY);
    }
}

