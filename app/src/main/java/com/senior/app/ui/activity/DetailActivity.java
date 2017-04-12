package com.senior.app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.app.R;

import java.util.ArrayList;
import java.util.Map;

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

    // Firebase Database
    private DatabaseReference mOrigin;
    private DatabaseReference mZomato;

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

    // Google Map
    private GoogleMap mGoogleMap;
    private CameraUpdate mCameraUpdate;

    private String mRestaurantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeView();

        mRestaurantName = getIntent().getStringExtra(NAME_EXTRA).replace("\n", "").replace("\r", "");
        String resId = getIntent().getStringExtra(RES_ID_EXTRA);
        String databaseRoot = getIntent().getStringExtra(DATABASE_ROOT_EXTRA);
        String phoneNumber = getIntent().getStringExtra(PHONE_NUMBER_EXTRA);
        ArrayList<String> photos = getIntent().getStringArrayListExtra(PHOTOS_EXTRA);
        ArrayList<String> cuisines = getIntent().getStringArrayListExtra(CUISINES_EXTRA);

        mRestaurantTitle.setText(mRestaurantName);

        /**
         * Cuisines
         */
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

        // Firebase Test
        mOrigin = FirebaseDatabase.getInstance().getReference(databaseRoot).child(resId);
        mZomato = FirebaseDatabase.getInstance().getReference("nyc-zomato-external").child(resId);

        // Read the data
        mOrigin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Origin: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    /**
     * Handle click events here
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.detail_activity_add_fav_button) {
            // add the current res to favs, if the user is authenticated
        } else if (id == R.id.detail_activity_post_comment_button) {
            // intent to comment
        }
    }
}
