package com.example.android.popuarmovies.utilities;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.popuarmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Scott on 21/04/2018.
 */

public class OpenMovieJsonUtils {

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * <p/>
     *
     * @return Array of Movie objects
     * @throws JSONException If JSON data cannot be properly parsed
     */


    public static ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        ArrayList<Movie> parsedMovieArray;

        if (TextUtils.isEmpty(movieJsonStr)) {
            return null;
        }

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray("results");
        parsedMovieArray = new ArrayList<>();

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject currentMovie = movieArray.getJSONObject(i);
            String title = currentMovie.getString("title");
            String posterPath = currentMovie.getString("poster_path");
            String overview = currentMovie.getString("overview");
            String rating = currentMovie.getString("vote_average");
            String releaseDate = currentMovie.getString("release_date");
            String id = currentMovie.getString("id");

            Movie movieInstance = new Movie(title, posterPath, overview, rating, releaseDate, id);
            parsedMovieArray.add(movieInstance);

        }
        return parsedMovieArray;
    }

    public static Movie getDetailDataFromJson(String movieJsonStr)
            throws JSONException {

        if (TextUtils.isEmpty(movieJsonStr)) {
            Log.e("getDetailDataFromJson", "Empty Json String, returning null");
            return null;
        }

        JSONObject movieJson = new JSONObject(movieJsonStr);

        JSONObject reviews = movieJson.getJSONObject("reviews");
        JSONArray reviewResults = reviews.getJSONArray("results");

        JSONObject videos = movieJson.getJSONObject("videos");
        JSONArray videoResults = videos.getJSONArray("results");

        String id = movieJson.getString("id");

        ArrayList<String> trailerKeys = new ArrayList<String>();
        ArrayList<String> reviewContents = new ArrayList<String>();

        for (int i = 0; i < videoResults.length(); i++) {
            JSONObject currentTrailerObject = videoResults.getJSONObject(i);
            String videoYoutubeKey = currentTrailerObject.getString("key");
            trailerKeys.add(videoYoutubeKey);
        }

        for (int i = 0; i < reviewResults.length(); i++) {
            JSONObject currentReviewObject = reviewResults.getJSONObject(i);
            String reviewContent = currentReviewObject.getString("content");

            reviewContents.add(reviewContent);
        }
        return new Movie(trailerKeys, reviewContents, id);
    }


}
