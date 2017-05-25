package com.senior.app.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import model.Review;
import model.Restaurant;
import utils.GooglePlacesNetworkUtils;
import utils.ZomatoNetworkUtils;

public class DetailActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {

    // TODO Fix minor bugs in DetailActivity, Details, Zomato Links, Rating
    // TODO Zomato Reviews Istanbul, Matching...

    private static final String TAG = DetailActivity.class.getSimpleName();

    // Static Fields EXTRAS ...
    public static final String NAME_EXTRA = "NAME_EXTRA";
    public static final String RES_ID_EXTRA = "RES_ID_EXTRA";
    public static final String DATABASE_ROOT_EXTRA = "DATABASE_ROOT_EXTRA";
    public static final String PHOTOS_EXTRA = "PHOTOS_EXTRA";
    public static final String CUISINES_EXTRA = "CUISINES_EXTRA";
    public static final String PHONE_NUMBER_EXTRA = "PHONE_NUMBER_EXTRA";
    public static final String ADDRESS_EXTRA = "ADDRESS_EXTRA";
    public static final String RESTAURANT_EXTRA = "RESTAURANT_EXTRA";
    public static final String SPINNER_INDEX_EXTRA = "SPINNER_INDEX_EXTRA";
    public static final String LAST_CITY_INDEX_EXTRA = "LAST_CITY_INDEX_EXTRA";

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
    private int spinnerIndex;
    private int lastCityIndex;

    private Restaurant mRestaurant;
    private List<Review> mReviews;

    private boolean mapMarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeView();

        mRestaurantName = getIntent().getStringExtra(NAME_EXTRA).replace("\n", "").replace("\r", "");
        resId = getIntent().getStringExtra(RES_ID_EXTRA);
        spinnerIndex = getIntent().getIntExtra(SPINNER_INDEX_EXTRA, 0);
        lastCityIndex = getIntent().getIntExtra(LAST_CITY_INDEX_EXTRA, 0);
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
        mZomato = FirebaseDatabase.getInstance().getReference(ZomatoNetworkUtils.getZomatoRestaurantReference(databaseRoot)).child(resId);
        mZomatoReviews = FirebaseDatabase.getInstance().getReference(ZomatoNetworkUtils.getZomatoReviewReference(databaseRoot)).child(resId);
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
                        Random random = new Random();
                        double rating = random.nextInt(4) + 1;
                        Review review = new Review(provider, username, rating, "Unknown", text);
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
                    mapMarked = true;
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
            launchReviewActivity();
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

    private void launchReviewActivity() {
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra(ReviewActivity.REVIEWS_EXTRA, (Serializable) mReviews);
        reviewIntent.putExtra(ReviewActivity.THUMB_EXTRA, mRestaurant.getPhotos().get(0));
        reviewIntent.putExtra(ReviewActivity.RES_ID_EXTRA, resId);
        startActivity(reviewIntent);
    }

    private void buildCommentDialog() {
        final Dialog dialog = new Dialog(this);
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
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                String dateString = dateFormat.format(date);
                Review review = new Review(provider, author, rating, dateString, comment);
                mReviews.add(review);
                mInternalReviews.child(getCurrentUser().getUid()).child(resId).push().setValue(review);
                Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_detail),
                        "Your review has successfully created. ", Snackbar.LENGTH_SHORT);
                snackbar.setAction("Click to see it!", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchReviewActivity();
                    }
                });
                snackbar.show();
                dialog.dismiss();
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

    private LatLngBounds getBounds() {
        switch (spinnerIndex) {
            case 0: // New York
                return new LatLngBounds(new LatLng(40.323663772, -73.989829374),
                        new LatLng(40.723663772, -73.489829374));
            case 1: // Istanbul
                return new LatLngBounds(new LatLng(41.015, 28.979),
                        new LatLng(41.05137, 28.58));
            case 2: // London
                return new LatLngBounds(new LatLng(51.08530, -0.076132),
                        new LatLng(51.508350, -0.176132));
            case 3: // Nearby
                return new LatLngBounds(new LatLng(41.015, 28.979),
                        new LatLng(41.05137, 28.58));
            default:
                return new LatLngBounds(new LatLng(40.323663772, -73.989829374),
                        new LatLng(40.723663772, -73.489829374));
        }
    }

    private AutocompleteFilter buildAutoCompleteFilter() {
        String countryTag;
        if (spinnerIndex == 0) {
            countryTag = "US";
        } else if (spinnerIndex == 1) {
            countryTag = "TR";
        } else if (spinnerIndex == 2) {
            countryTag = "GB";
        } else if (spinnerIndex == 3) {
            countryTag = "TR";
        } else {
            countryTag = "US";
        }
        return new AutocompleteFilter
                .Builder()
                .setCountry(countryTag)
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();
    }

    private void makeGoogleAPICall() {
        AutocompleteFilter filter = buildAutoCompleteFilter();

        LatLngBounds bounds = getBounds();

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
                                            // Try to update the Google Map if map is not marked
                                            if (!mapMarked) {
                                                JSONObject geometry = result.getJSONObject("geometry");
                                                JSONObject location = geometry.getJSONObject("location");
                                                LatLng coordinates = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                                                mCameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 15);
                                                mGoogleMap.addMarker(new MarkerOptions().position(coordinates).title(mRestaurantName));
                                                mGoogleMap.moveCamera(mCameraUpdate);
                                            }
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
