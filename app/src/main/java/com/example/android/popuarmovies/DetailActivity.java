package com.example.android.popuarmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.popuarmovies.data.FavouritesContract;
import com.example.android.popuarmovies.databinding.MovieDetailBinding;
import com.example.android.popuarmovies.utilities.NetworkUtils;
import com.example.android.popuarmovies.utilities.OpenMovieJsonUtils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.android.popuarmovies.MainActivity.BUILD_DETAIL_URL;
import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.COLUMN_FILM_ID;
import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.CONTENT_URI;

public class DetailActivity extends AppCompatActivity implements LoaderCallbacks<DetailActivity.Wrapper> {

    MovieDetailBinding mBinding;
    static String IS_FAVOURITED = "is_favourited";
    private Boolean isFavourited = true;
    private String mFilmId;
    private Uri trailerUri;
    private String mMovieTitle;
    private Movie intentMovieInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_detail);

        getSupportLoaderManager().initLoader(0, null, DetailActivity.this);

        mBinding.favouriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.grey_star));
        mBinding.favouriteButton.setText(null);
        mBinding.favouriteButton.setTextOn(null);
        mBinding.favouriteButton.setTextOff(null);

        Intent intentThatStartedThisActivity = getIntent();

        //Retrieve the movie details passed from the MainActivity to populate the detail page
        try {
            if (intentThatStartedThisActivity != null) {
                if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                    intentMovieInstance = intentThatStartedThisActivity.getParcelableExtra(Intent.EXTRA_TEXT);
                    setPosterImage(intentMovieInstance.getMoviePosterThumbnail(), mBinding.detailPoster, intentMovieInstance.getPosterByte());
                    mMovieTitle = intentMovieInstance.getTitle();
                    mBinding.title.setText(mMovieTitle);
                    mBinding.overview.setText(intentMovieInstance.getOverview());
                    String formattedDate = formatDate(intentMovieInstance.getReleaseDate());
                    mBinding.releaseDate.setText(formattedDate);
                    String formattedRating = formatRating(intentMovieInstance.getRating());
                    mBinding.rating.setText(formattedRating + ("/10"));
                    //Set the Id in a member variable that will be used to check if the movie is favourited
                    mFilmId = intentMovieInstance.getid();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Sets a listener that will change the color of the favourite star when it is clicked.
        // The favourites database will also be updated as a result.
        mBinding.favouriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isPressed() && !isFavourited) {
                    setStarYellow();
                    addToFavorites();
                    isFavourited = true;
                } else if (buttonView.isPressed() && isFavourited) {
                    setStarGrey();
                    removeFromFavorites();
                    isFavourited = false;
                }
            }
        });

    }

    //Sets the main poster image
    private void setPosterImage(String imageUrl, ImageView moviePoster, byte[] posterByte) {
        //if the image if not in the favourites db and a poster image isn't passed in the form of a byte[],
        //then populate the poster using the url from TheMovieDb api using picasso. Else, convert byte[] to a bitmap
        //and set to imageView
        if (posterByte == null) {
            String moviePosterFinalUrl = "http://image.tmdb.org/t/p/w342" + imageUrl;
            Picasso.with(this)
                    .load(moviePosterFinalUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.poster_not_available)
                    .into(moviePoster);
        } else {
            Bitmap bmp = BitmapFactory.decodeByteArray(posterByte, 0, posterByte.length);
            moviePoster.setImageBitmap(bmp);
        }
    }

    private String formatDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            newDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern("dd MMM, yyyy");
        return sdf.format(newDate);
    }

    private String formatRating(String rating) {
        Double ratingDouble = Double.parseDouble(rating);
        DecimalFormat df = new DecimalFormat("#.0");
        return df.format(ratingDouble);
    }

    //A custom class to help pass 2 objects of different data types through the Loader
    public class Wrapper {
        public Movie movieInstance;
        public Cursor cursor;
    }


    @Override
    public Loader<Wrapper> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<Wrapper>(this) {

            private Wrapper resultWrapper;

            @Override
            protected void onStartLoading() {
                if (resultWrapper != null) {
                    //To skip loadInBackground call
                    deliverResult(resultWrapper);
                } else {
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(@Nullable Wrapper data) {
                resultWrapper = data;
                super.deliverResult(data);
            }

            @Override
            public Wrapper loadInBackground() {
                //Think of this as AsyncTask doInBackground() method, here you will actually initiate Network call, or any work that need to be done on background
                //Here we make a call to TheMovieDb api with this films id to get reviews and trailer details. We also make a query to the favourites db to see if this film id
                //is present. We save these values in the custom wrapper object.

                URL movieDataRequestUrl = NetworkUtils.buildUrl(null, BUILD_DETAIL_URL, mFilmId);

                Wrapper wrapper = new Wrapper();

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(movieDataRequestUrl);

                    wrapper.movieInstance = OpenMovieJsonUtils
                            .getDetailDataFromJson(jsonWeatherResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null; }

                try {
                    wrapper.cursor = getContentResolver().query(CONTENT_URI, null, COLUMN_FILM_ID + " = " + mFilmId, null, null);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                return wrapper; }
        };
    }

    @Override
    public void onLoadFinished(Loader<Wrapper> loader, Wrapper w) {
        if (!w.movieInstance.getmTrailerkeys().toString().equals("[]")) {
            mBinding.trailerTitle.setVisibility(View.VISIBLE);
        }

        try {
            w.cursor.moveToFirst();
            w.cursor.getString(w.cursor.getColumnIndex(COLUMN_FILM_ID));
            setStarYellow();
            isFavourited = true;

        } catch (Exception e) {
            setStarGrey();
            isFavourited = false;
            e.printStackTrace();
        }


        if (w.movieInstance != null) {
            ArrayList<String> trailerUrls = getTrailerUrls(w.movieInstance.getmTrailerkeys());
            try {
                String trailerUrl = trailerUrls.get(0);
                trailerUri = Uri.parse(trailerUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String formattedReviews = formatReviews(w.movieInstance.getmReviews().toString());

            if (formattedReviews.equals("")) {
                mBinding.noReviews.setVisibility(View.VISIBLE);
            } else {
                mBinding.noReviews.setVisibility(View.INVISIBLE);
                mBinding.reviews.setText(formattedReviews);
            }

            loadTrailerThumbnail(w.movieInstance.getmTrailerkeys());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Wrapper> loader) {
    }

    private ArrayList<String> getTrailerUrls(ArrayList<String> keys) {

        ArrayList<String> trailerUrls = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            String keyInstance = keys.get(i);
            String url = "http://www.youtube.com/watch?v=" + keyInstance;
            trailerUrls.add(url);
        }

        return trailerUrls;
    }

    public void launchYoutubeTrailerIntent(View view) {
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, trailerUri);
        startActivity(youtubeIntent);
    }

    public void loadTrailerThumbnail(ArrayList<String> keys) {
        try {
            String keyInstance = keys.get(0);
            Picasso.with(this)
                    .load(getThumbnailUrl(keyInstance))
                    .error(R.drawable.poster_not_available)
                    .into(mBinding.trailer1Button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getThumbnailUrl(String key) {
        StringBuilder builder = new StringBuilder()
                .append("https://img.youtube.com/vi/")
                .append(key)
                .append("/0.jpg");

        return builder.toString();
    }

    public void addToFavorites() {
        ContentValues values = new ContentValues();
        values.put(FavouritesContract.FavouritesEntry.COLUMN_FILM_ID, mFilmId);
        values.put(FavouritesContract.FavouritesEntry.COLUMN_TITLE, mMovieTitle);
        values.put(FavouritesContract.FavouritesEntry.COLUMN_OVERVIEW, intentMovieInstance.getOverview());
        values.put(FavouritesContract.FavouritesEntry.COLUMN_USER_RATING, intentMovieInstance.getRating());
        values.put(FavouritesContract.FavouritesEntry.COLUMN_RELEASE_DATE, intentMovieInstance.getReleaseDate());
        values.put(FavouritesContract.FavouritesEntry.COLUMN_POSTER_URL, intentMovieInstance.getMoviePosterThumbnail());

        BitmapDrawable drawable = (BitmapDrawable) mBinding.detailPoster.getDrawable();
        Bitmap bmp = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();
        values.put(FavouritesContract.FavouritesEntry.COLUMN_POSTER, image);

        getContentResolver().insert(FavouritesContract.FavouritesEntry.CONTENT_URI, values);

        Toast.makeText(this, "Added To Favourites", Toast.LENGTH_SHORT).show();
    }

    public void removeFromFavorites() {
        getContentResolver().delete(CONTENT_URI, COLUMN_FILM_ID + "=?", new String[]{mFilmId});
        Toast.makeText(this, "Removed From Favourites", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Saves the variable isFavourited so that the favourite star remains consistent through screen rotations
        outState.putBoolean(IS_FAVOURITED, isFavourited);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //
        isFavourited = savedInstanceState.getBoolean(IS_FAVOURITED);

        if (!isFavourited) {
            setStarGrey();
        } else {
            setStarYellow();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    private String formatReviews(String unformattedReview) {
        return unformattedReview.replace("[", "").replace("]", "");
    }

    private void setStarYellow() {
        mBinding.favouriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.yellow_star));
    }

    private void setStarGrey() {
        mBinding.favouriteButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.grey_star));
    }


}
