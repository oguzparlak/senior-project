package com.senior.app.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.senior.app.R;
import com.senior.app.ui.adapter.TabSectionsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import model.Restaurant;
import utils.ZomatoNetworkUtils;

public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, TabLayout.OnTabSelectedListener {

    // TODO Handle Favorites
    // TODO Handle Auth
    // TODO Handle Data Matching

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int LOCATION_REQUEST = 2;

    private static final String SPINNER_INDEX = "SPINNER_INDEX";

    private TabSectionsAdapter mTabSectionsAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    // Spinner
    private Spinner mSpinner;

    private FloatingActionButton mLocationFab;

    private boolean locationRequested; // Keep control of location request, just the app is started, otherwise manually
    private int lastSpinnerIndex; // To be saved into sharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();

        // persist the state of the spinner on app is restarted
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        mSpinner.setSelection(preferences.getInt(SPINNER_INDEX, 1));

        getSupportActionBar().setTitle(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_search) {
            openPlacePicker();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends a Place Picker Request
     */
    private void openPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "openPlacePicker: Play Services not available.");
        }
    }

    /**
     * Handle Google Places Requests here
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Get the data of the selected place
                Place place = PlacePicker.getPlace(this, data);
                Log.d(TAG, "onActivityResult: " + place.getName());
            }
        }
    }

    /**
     * Referencing the views
     */
    private void initializeView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabSectionsAdapter = new TabSectionsAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mTabSectionsAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(this);

        // Spinner
        mSpinner = (Spinner) findViewById(R.id.city_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.city_array, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.dropdown_item);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(this);

        mLocationFab = (FloatingActionButton) findViewById(R.id.fab);
        mLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Refresh the data anytime mLocationFab is tapped
                pushLocationData();
            }
        });
    }

    /**
     * Spinner Callbacks
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(pos));
        int lastIndex = adapterView.getCount() - 1;
        // TODO Handle Spinner Changes, Firebase, API Calls... etc
        if (pos != lastIndex) {
            lastSpinnerIndex = pos;
        } else {
            TabLayout.Tab tab =  mTabLayout.getTabAt(1);
            tab.select();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "onNothingSelected: Nothing Selected");
    }

    /**
     * Tab Callbacks
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // push the data once when the onCreate is called
        if (!locationRequested && tab.getPosition() == 1) {
            pushLocationData();
            locationRequested = true;
        }

        if (tab.getPosition() != 1) {
            // hide the fab
            mLocationFab.hide();
            // set previously selected
            mSpinner.setSelection(lastSpinnerIndex);
        } else {
            // show the fab
            mLocationFab.show();
            // save the previous selected index
            lastSpinnerIndex = mSpinner.getSelectedItemPosition();
            mSpinner.setSelection(getResources().getStringArray(R.array.city_array).length - 1, true);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // do nothing
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // do nothing
    }

    private void pushLocationData() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);

        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            Log.d(TAG, "pushLocationData: Lat: " + location.getLatitude());
            Log.d(TAG, "pushLocationData: Lng: " + location.getLongitude());

            // Make an API Call
            ZomatoNetworkUtils.get(ZomatoNetworkUtils.buildURL(location.getLatitude(),
                    location.getLongitude()), null, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            try {
                                JSONArray restaurants = response.getJSONArray("restaurants");

                                for (int i = 0; i < restaurants.length(); i++) {
                                    JSONObject restaurant = restaurants.getJSONObject(i);
                                    JSONObject restaurantDetails = restaurant.getJSONObject("restaurant");
                                    long id = restaurantDetails.getLong("id");
                                    String thumb = restaurantDetails.getString("thumb");
                                    String name = restaurantDetails.getString("name");
                                    mRootReference.child("nearby").child(String.valueOf(id)).setValue(new Restaurant(id, name, thumb, 0, 0));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
            );
        }

    }

    // Handle location results here
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    pushLocationData();

                } else {
                    // permission denied, show toast
                    Toast.makeText(this,
                            "You need to allow permission for location changes",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SPINNER_INDEX, lastSpinnerIndex);
        editor.apply();

    }

}
