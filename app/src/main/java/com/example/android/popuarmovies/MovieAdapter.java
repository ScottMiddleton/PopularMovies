package com.example.android.popuarmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.sql.Blob;
import java.util.ArrayList;

import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.COLUMN_FILM_ID;
import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.COLUMN_POSTER;
import static com.example.android.popuarmovies.data.FavouritesContract.FavouritesEntry.CONTENT_URI;

/**
 * Created by Scott on 18/04/2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.movieAdapterViewHolder> {


    private ArrayList<Movie> mMovieDataArray;

    private final MoviePosterOnClickHandler mClickHandler;

    private String mSortByParam;

    private ImageView mMoviePosterImageView;

    private final Context mContext;


    public MovieAdapter(Context context, MoviePosterOnClickHandler clickHandler, String sortByParam) {
        mClickHandler = clickHandler;
        mSortByParam = sortByParam;
        this.mContext = context;
    }

    public class movieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public movieAdapterViewHolder(View view) {
            super(view);
            mMoviePosterImageView = view.findViewById(R.id.movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie movieInstance = mMovieDataArray.get(adapterPosition);
            if (mMovieDataArray != null) {
                mClickHandler.onClick(movieInstance);
            }
        }

        public void bind(String posterUrl, Movie movieInstance, Bitmap bmp) {

            if(bmp != null){mMoviePosterImageView.setImageBitmap(bmp);}
            else{
            // Canceling the older request
            Picasso.with(mContext).cancelRequest(mMoviePosterImageView);
            // Creating a new request.

            if (movieInstance.getMoviePosterThumbnail() != null) {
                Picasso.with(mContext)
                        .load(posterUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.poster_not_available)
                        .into(mMoviePosterImageView);}
            }
        }
    }

    @NonNull
    @Override
    public movieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new movieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull movieAdapterViewHolder holder, int position) {
        Log.e("positions", Integer.toString(position));
        Movie movieInstance = mMovieDataArray.get(position);
        if (mSortByParam.equals("favourites")) {
            byte[] poster = movieInstance.getPosterByte();
            Bitmap bmp = BitmapFactory.decodeByteArray(poster, 0, poster.length);
            holder.bind(null, movieInstance, bmp);
        } else {
            String movieInstancePosterUrl = movieInstance.getMoviePosterThumbnail();
            String moviePosterFinalUrl = "http://image.tmdb.org/t/p/w500/" + movieInstancePosterUrl;
            holder.bind(moviePosterFinalUrl, movieInstance, null);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMovieDataArray == null){return 0;}else{
            Log.e("Count", Integer.toString(mMovieDataArray.size()));
        return mMovieDataArray.size();}
    }

    public interface MoviePosterOnClickHandler {
        void onClick(Movie moviePosterImageUrl);
    }

    public void setMovieData(ArrayList<Movie> MoviePosterData) {
        mMovieDataArray = MoviePosterData;
        notifyDataSetChanged();
    }
}

