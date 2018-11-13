package com.example.android.popuarmovies;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by Scott on 22/04/2018.
 */

@SuppressLint("ParcelCreator")
public class Movie implements Parcelable {

    private final String mTitle;
    private String mMoviePosterThumbnail;
    private byte[] mMoviePosterByte;
    private final String mOverview;
    private final String mRating;
    private final String mReleaseDate;
    private final String mId;
    private ArrayList<String> mTrailerUrl;
    private ArrayList<String> mReviews;

    public Movie(String title, String moviePosterThumbnail, String overview, String rating, String releaseDate, String id) {
        mTitle = title;
        mMoviePosterThumbnail = moviePosterThumbnail;
        mOverview = overview;
        mRating = rating;
        mReleaseDate = releaseDate;
        mId = id;
    }

    public Movie(String title, byte[] poster, String moviePosterThumbnail, String overview, String rating, String releaseDate, String id) {
        mTitle = title;
        mMoviePosterByte = poster;
        mOverview = overview;
        mMoviePosterThumbnail = moviePosterThumbnail;
        mRating = rating;
        mReleaseDate = releaseDate;
        mId = id;
    }

    public Movie(ArrayList<String> trailerUrl, ArrayList<String> reviews, String id) {
        mReviews = reviews;
        mTrailerUrl = trailerUrl;
        mId = id;
        mTitle = null;
        mMoviePosterThumbnail = null;
        mOverview = null;
        mRating = null;
        mReleaseDate = null;
    }


    private Movie(Parcel in) {
        mTitle = in.readString();
        mMoviePosterThumbnail = in.readString();
        mOverview = in.readString();
        mRating = in.readString();
        mReleaseDate = in.readString();
        mId = in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getTitle() {
        return mTitle;
    }

    public String getMoviePosterThumbnail() {
        return mMoviePosterThumbnail;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getRating() {
        return mRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getid() {
        return mId;
    }

    public ArrayList<String> getmTrailerkeys() {
        return mTrailerUrl;
    }

    public ArrayList<String> getmReviews() {
        return mReviews;
    }

    public byte[] getPosterByte() { return mMoviePosterByte;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mMoviePosterThumbnail);
        parcel.writeString(mOverview);
        parcel.writeString(mRating);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mId);
    }
}
