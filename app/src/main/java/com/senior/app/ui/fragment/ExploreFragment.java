package com.senior.app.ui.fragment;

import android.util.Log;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import model.Restaurant;
import utils.ZomatoNetworkUtils;

public class ExploreFragment extends BaseFragment {

    private static final String TAG = ExploreFragment.class.getSimpleName();

    // Return the Restaurants to be explored in selected city
    @Override
    Query getQuery(DatabaseReference reference) {
        return reference.child("/new-york-popular");
    }

    private void makeApiCall() {
        // Zomato API Call
        ZomatoNetworkUtils.get(ZomatoNetworkUtils.buildTrendingRestaurantsURL("280"),
                null, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            // Root of the Response
                            JSONArray restaurants = response.getJSONArray("restaurants");
                            // Iterate through items
                            for (int i = 0; i < restaurants.length(); i++) {
                                JSONObject firstRestaurant = restaurants.getJSONObject(i);
                                JSONObject restaurantDetails = firstRestaurant.getJSONObject("restaurant");
                                String name = restaurantDetails.getString("name");
                                long id = restaurantDetails.getLong("id");
                                int priceRange = restaurantDetails.getInt("price_range");
                                String imageUrl = restaurantDetails.getString("thumb");
                                JSONObject userRating = restaurantDetails.getJSONObject("user_rating");
                                double rating = Double.parseDouble(userRating.getString("aggregate_rating"));
                                // Add the data into Firebase
                                mRootReference.child("restaurants").
                                        child("new-york-popular").
                                        child(String.valueOf(id)).
                                        setValue(new Restaurant(id, name, imageUrl, rating, priceRange));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // @Override
                    // public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    //     Log.d(TAG, "onFailure: errorCode: " + errorResponse.toString());
                    // }

                });

        mProgressBar.setVisibility(View.INVISIBLE);

    }

}
