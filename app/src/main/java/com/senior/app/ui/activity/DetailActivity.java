package com.senior.app.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.senior.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import model.Review;
import model.Restaurant;
import utils.GooglePlacesNetworkUtils;

public class DetailActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    // Static Fields EXTRAS ...
    public static final String NAME_EXTRA = "NAME_EXTRA";
    public static final String RES_ID_EXTRA = "RES_ID_EXTRA";
    public static String DATABASE_ROOT_EXTRA = "DATABASE_ROOT_EXTRA";
    public static String PHOTOS_EXTRA = "PHOTOS_EXTRA";
    public static String CUISINES_EXTRA = "CUISINES_EXTRA";
    public static String PHONE_NUMBER_EXTRA = "PHONE_NUMBER_EXTRA";
    public static String ADDRESS_EXTRA = "ADDRESS_EXTRA";
    public static String RESTAURANT_EXTRA = "RESTAURANT_EXTRA";

    // Firebase Database
    private DatabaseReference mOrigin;
    private DatabaseReference mZomato;
    private DatabaseReference mZomatoReviews;
    private DatabaseReference mTripAdvisorReviews;
    private DatabaseReference mInternalReviews;

    // UI Components
    private SliderLayout mSliderLayout;
    private Button mFavButton;
    private Button mCommentButton;
    private Button mCallButton;
    private Button mSeeReviewsButton;
    private Button mZomatoButton;
    private Button mPhotoButton;

    private TextView mRestaurantTitle;
    private TextView mRestaurantAddress;
    private TextView mPhoneNumberTextView;
    private TextView mCuisinesTextView;
    private TextView mAveragePriceTextView;
    private TextView mSpecsTextView;
    private TextView mOpenNowTextView;
    private TextView mOpeningHoursTextView;

    private CardView mOpeningHoursCard;

    // Google Map
    private GoogleMap mGoogleMap;
    private CameraUpdate mCameraUpdate;

    private String mRestaurantName;
    private String resId;
    private boolean userOnline;
    private boolean userLiked;

    private Restaurant mRestaurant;
    private List<Review> mReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeView();

        mRestaurantName = getIntent().getStringExtra(NAME_EXTRA).replace("\n", "").replace("\r", "");
        resId = getIntent().getStringExtra(RES_ID_EXTRA);
        String databaseRoot = getIntent().getStringExtra(DATABASE_ROOT_EXTRA);
        String phoneNumber = getIntent().getStringExtra(PHONE_NUMBER_EXTRA);
        ArrayList<String> photos = getIntent().getStringArrayListExtra(PHOTOS_EXTRA);
        ArrayList<String> cuisines = getIntent().getStringArrayListExtra(CUISINES_EXTRA);

        mRestaurantTitle.setText(mRestaurantName);

        // Google Places API Call
        makeGoogleAPICall();

        /**
         * Cuisines
         */
        if (cuisines != null) {
            if (cuisines.isEmpty()) {
                mCuisinesTextView.setText(R.string.not_available);
            } else {
                for (String cuisine : cuisines) {
                    mCuisinesTextView.append(cuisine + ", ");
                }
                String currentText = mCuisinesTextView.getText().toString();
                String beautifiedText = currentText.substring(0, currentText.length() - 2);
                mCuisinesTextView.setText(beautifiedText);
            }
        }

        /**
         * Phone number
         */
        mPhoneNumberTextView.setText(phoneNumber);

        /**
         * Add photo urls to slider
         */
        for (String url : photos) {
            DefaultSliderView sliderView = new DefaultSliderView(this);
            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);
            mSliderLayout.addSlider(sliderView);
        }

        getSupportActionBar().setTitle(mRestaurantName);

        /**
         * Google Maps
         */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Firebase References
        mOrigin = FirebaseDatabase.getInstance().getReference(databaseRoot).child(resId);
        mZomato = FirebaseDatabase.getInstance().getReference("nyc-zomato-external").child(resId);
        mZomatoReviews = FirebaseDatabase.getInstance().getReference("nyc-zomato-reviews").child(resId);
        mTripAdvisorReviews = FirebaseDatabase.getInstance().getReference("reviews").child(resId);
        mInternalReviews = mRootReference.child("internal-reviews");

        mRestaurant = (Restaurant) getIntent().getSerializableExtra(RESTAURANT_EXTRA);
        if (getCurrentUser() != null) {
            userOnline = true;
            if (mRestaurant.getStars().containsKey(getCurrentUser().getUid())) {
                updateLikeButton(true);
            } else {
                updateLikeButton(false);
            }
        } else {
            userOnline = false;
        }

        mReviews = new ArrayList<>();

        mZomatoReviews.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Store the review information
                        Map reviewMap = (Map) snapshot.getValue();
                        String text = (String) reviewMap.get("review_text");
                        String timeDescription = (String) reviewMap.get("review_time_friendly");
                        Number rating = (Number) reviewMap.get("rating");
                        Map userMap = (Map) reviewMap.get("user");
                        String username = (String) userMap.get("name");
                        Review review = new Review("Zomato", username, rating.doubleValue(), timeDescription, text);
                        mReviews.add(review);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mTripAdvisorReviews.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Map reviewMap = (Map) snapshot.getValue();
                        String text = (String) reviewMap.get("entry");
                        String provider = (String) reviewMap.get("provider");
                        String username = (String) reviewMap.get("user_name");
                        Review review = new Review(provider, username, -1, "Unknown", text);
                        mReviews.add(review);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (getCurrentUser() != null) {
            mInternalReviews.child(getCurrentUser().getUid()).child(resId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Review review = snapshot.getValue(Review.class);
                            mReviews.add(review);
                        }
                    } else {
                        Log.w(TAG, "onDataChange: Snapshot does not exists");
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void updateLikeButton(boolean liked) {
        if (liked) {
            mFavButton.setText(R.string.dislike);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_cancel_white_24dp);
            mFavButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            userLiked = true;
        } else {
            mFavButton.setText(R.string.add_to_fav);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_favorite_white_24dp);
            mFavButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            userLiked = false;
        }
    }

    private void initializeView() {
        mSliderLayout = (SliderLayout) findViewById(R.id.slider_layout);
        mFavButton = (Button) findViewById(R.id.detail_activity_add_fav_button);
        mCommentButton = (Button) findViewById(R.id.detail_activity_post_comment_button);
        mCallButton = (Button) findViewById(R.id.detail_activity_call_button);
        mSeeReviewsButton = (Button) findViewById(R.id.detail_activity_see_reviews_button);
        mZomatoButton = (Button) findViewById(R.id.detail_activity_menu_button);
        mPhotoButton = (Button) findViewById(R.id.detail_activity_photos_button);
        mPhoneNumberTextView = (TextView) findViewById(R.id.detail_activity_call_main_text);
        mCuisinesTextView = (TextView) findViewById(R.id.detail_activity_cuisines_main_text);
        mAveragePriceTextView = (TextView) findViewById(R.id.detail_activity_average_price_main_text);
        mSpecsTextView = (TextView) findViewById(R.id.detail_activity_specs_main_text);
        mRestaurantTitle = (TextView) findViewById(R.id.detail_activity_restaurant_title);
        mRestaurantAddress = (TextView) findViewById(R.id.detail_activity_restaurant_address);
        mOpenNowTextView = (TextView) findViewById(R.id.detail_activity_open_now);
        mOpeningHoursTextView = (TextView) findViewById(R.id.detail_activity_opening_hours);
        mOpeningHoursCard = (CardView) findViewById(R.id.detail_activity_fifth_card);

        // Set OnClickListener
        mCallButton.setOnClickListener(this);
        mFavButton.setOnClickListener(this);
        mSeeReviewsButton.setOnClickListener(this);
        mCommentButton.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        /**
         * Listen Zomato data here, when the map is ready to use
         */
        mZomato.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map restaurantMap = (Map) dataSnapshot.getValue();
                    Map locationMap = (Map) restaurantMap.get("location");
                    double lat = Double.parseDouble((String) locationMap.get("latitude"));
                    double lon = Double.parseDouble((String) locationMap.get("longitude"));
                    LatLng coordinates = new LatLng(lat, lon);
                    mCameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 15);
                    mGoogleMap.addMarker(new MarkerOptions().position(coordinates).title(mRestaurantName));
                    mGoogleMap.moveCamera(mCameraUpdate);
                    // Info
                    long averageCost = (long) restaurantMap.get("average_cost_for_two");
                    mAveragePriceTextView.setText(getString(R.string.average_cost, averageCost));
                    String address = (String) locationMap.get("address");
                    mRestaurantAddress.setText(address);
                    String thumb = (String) restaurantMap.get("thumb");
                    DefaultSliderView sliderView = new DefaultSliderView(getBaseContext());
                    if (thumb.length() > 1) {
                        sliderView
                                .image(thumb)
                                .setScaleType(BaseSliderView.ScaleType.CenterCrop);
                        mSliderLayout.addSlider(sliderView);
                    }
                } else {
                    mAveragePriceTextView.setText(R.string.not_available);
                    String address = getIntent().getStringExtra(ADDRESS_EXTRA);
                    mRestaurantAddress.setText(address);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateOpenNowText(boolean open) {
        if (open) {
            mOpenNowTextView.setText(getResources().getString(R.string.open_now));
            mOpenNowTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            mOpenNowTextView.setText(getResources().getString(R.string.closed));
            mOpenNowTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    /**
     * Handle click events here
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.detail_activity_add_fav_button) {
            String res_id = getIntent().getStringExtra(RES_ID_EXTRA);
            // add the current res to favs, if the user is authenticated
            if (!userOnline) {
                // dialog
                popDialog();
                return;
            }
            if (!userLiked) {
                // like the restaurant
                mRestaurant.getStars().put(getCurrentUser().getUid(), true);
                mOrigin.setValue(mRestaurant);
                getFavoritesReference().child(res_id).setValue(mRestaurant);
                updateLikeButton(true);
            } else {
                // remove from favorites
                mRestaurant.getStars().remove(getCurrentUser().getUid());
                getFavoritesReference().child(res_id).removeValue();
                mOrigin.setValue(mRestaurant);
                updateLikeButton(false);
            }
        } else if (id == R.id.detail_activity_post_comment_button) {
            // pop a dialog for the comment
            buildCommentDialog();
        }else if (id == R.id.detail_activity_see_reviews_button) {
            Intent reviewIntent = new Intent(this, ReviewActivity.class);
            reviewIntent.putExtra(ReviewActivity.REVIEWS_EXTRA, (Serializable) mReviews);
            reviewIntent.putExtra(ReviewActivity.THUMB_EXTRA, mRestaurant.getPhotos().get(0));
            startActivity(reviewIntent);
        } else if (id == R.id.detail_activity_call_button) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + mPhoneNumberTextView.getText()));
            startActivity(callIntent);
        }
    }

    private void popDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Authentication required to like the restaurants !")
                .setTitle("Authenticate !");
        builder.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onSignUpButtonClicked(null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });
        builder.create().show();
    }

    private void buildCommentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_comment_dialog);
        dialog.setTitle("Add Comment");
        // onClick
        final Button postButton = (Button) dialog.findViewById(R.id.post_button);
        final EditText commentEditText = (EditText) dialog.findViewById(R.id.comment_edit_text);
        final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rating_radio_group);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentEditText.getText().toString();
                double rating = Double.parseDouble((String) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()).getTag());
                String author = getCurrentUser().getDisplayName();
                String provider = "Restaurants";
                String date = new Date().toString();
                Review review = new Review(provider, author, rating, date, comment);
                mReviews.add(review);
                mInternalReviews.child(getCurrentUser().getUid()).child(resId).push().setValue(review);
                // TODO Handle the Date format // FIXME: 10/05/2017
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                postButton.setEnabled(true);
                double rating = Double.parseDouble((String) radioGroup.findViewById(i).getTag());
                Log.d(TAG, "onCheckedChanged: rating: " + rating);
            }
        });
        dialog.show();
    }

    private void makeGoogleAPICall() {
        AutocompleteFilter filter = new AutocompleteFilter
                .Builder()
                .setCountry("US")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        // New York City South-West, North-East
        LatLngBounds bounds = new LatLngBounds(new LatLng(40.323663772, -73.989829374),
                new LatLng(40.723663772, -73.489829374));

        PendingResult<AutocompletePredictionBuffer> result =
                Places
                        .GeoDataApi
                        .getAutocompletePredictions(mGoogleApiClient, mRestaurantName, bounds, filter);

        result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
            @Override
            public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                if (buffer.getCount() > 0) {
                    AutocompletePrediction prediction = buffer.get(0);
                    String placeId = prediction.getPlaceId();
                    // Try to make another API call here
                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId).setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                Place place = places.get(0);
                                String placeId = place.getId();
                                GooglePlacesNetworkUtils.get(placeId, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        try {
                                            JSONObject result = response.getJSONObject("result");
                                            JSONObject openingHours = result.getJSONObject("opening_hours");
                                            JSONArray weekDayText = openingHours.getJSONArray("weekday_text");
                                            JSONArray reviews = result.getJSONArray("reviews");
                                            // Restaurant additional info
                                            boolean openNow = openingHours.getBoolean("open_now");
                                            String[] weekDay = new String[weekDayText.length()];
                                            for (int i = 0; i < weekDay.length; i++) {
                                                weekDay[i] = (String) weekDayText.get(i);
                                                mOpeningHoursTextView.append(weekDay[i] + "\n\n");
                                            }
                                            // Reviews
                                            for (int i = 0; i < reviews.length(); i++) {
                                                JSONObject review = reviews.getJSONObject(i);
                                                String author = review.getString("author_name");
                                                double reviewRating = review.getDouble("rating");
                                                String timeDescription = review.getString("relative_time_description");
                                                String text = review.getString("text");
                                                Review googleReview = new Review("Google", author, reviewRating, timeDescription, text);
                                                mReviews.add(googleReview);
                                            }
                                            updateOpenNowText(openNow);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        Log.w(TAG, "onFailure: Failed with status code: " + statusCode);
                                    }
                                });
                            } else {
                                Log.w(TAG, "onResult: Place not found.");
                                mOpeningHoursCard.setVisibility(View.INVISIBLE);
                            }
                            places.release();
                        }
                    });
                } else {
                    Log.w(TAG, "onResult: Place not found.");
                    mOpeningHoursCard.setVisibility(View.INVISIBLE);
                }
                buffer.release();
            }
        });
    }
}
