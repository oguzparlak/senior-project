package com.senior.app.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.senior.app.R;
import com.senior.app.ui.adapter.TabSectionsAdapter;
import com.senior.app.ui.fragment.BaseFragment;
import com.senior.app.ui.fragment.ExploreFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import model.Restaurant;
import utils.ZomatoNetworkUtils;

public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, TabLayout.OnTabSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase Auth Test
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private static final int LOCATION_REQUEST = 2;

    private static final String SPINNER_INDEX = "SPINNER_INDEX";

    private int lastCityIndex = 0;
    private int currentSpinnerIndex;
    private int choice = -1;

    private TabSectionsAdapter mTabSectionsAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    // Spinner
    private Spinner mSpinner;

    private boolean firstNearbyCall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
        
        // Init FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                
                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: User logged out");
                } else {
                    Log.d(TAG, "onAuthStateChanged: User logged in");
                }
            }
        };

        // persist the state of the spinner on app is restarted
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        int spinnerIndex = preferences.getInt(SPINNER_INDEX, 0);
        mSpinner.setSelection(spinnerIndex);

        getSupportActionBar().setTitle(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: newQuery: " + query);
                // Create an Intent to SearchActivity
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra(SearchActivity.QUERY_EXTRA, query);
                searchIntent.putExtra(SearchActivity.DATABASE_REFERENCE_EXTRA, getDatabaseRoot(getLastCityIndex()));
                searchIntent.putExtra(SearchActivity.CITY_INDEX_EXTRA, getLastCityIndex());
                searchIntent.putExtra(SearchActivity.SPINNER_INDEX_EXTRA, getCurrentSpinnerIndex());
                startActivity(searchIntent);
                View v = getCurrentFocus();
                if (v != null) {
                    getCurrentFocus().clearFocus();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem refresh = menu.findItem(R.id.action_search);
        if (inNearbyTab()) {
            refresh.setIcon(R.drawable.quantum_ic_refresh_white_24);
        } else {
            refresh.setIcon(R.drawable.ic_search_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            showFilterDialog();
        } else if (id == R.id.action_search) {
            if (inNearbyTab()) {
                // make a new API call with Zomato
                makeAsyncHttpCall();
            } else {
                // Open the search UI
            }
            return true;
        } else if (id == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            firstNearbyCall = true;
            Toast.makeText(this, "Signed-out", Toast.LENGTH_LONG).show();
            selectTabAt(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean inNearbyTab() {
        return getCurrentTabPosition() == 1;
    }

    /**
     * Handle Google Places Requests here
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectTabAt(0);
                Toast.makeText(this, "Welcome, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // user pressed back button
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

    }

    /**
     * Spinner Callbacks
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        int favIndex = adapterView.getCount() - 1;
        int nearbyIndex = favIndex - 1;
        if (pos == favIndex) {
            // Select the fav tab
            selectTabAt(2);
            // if previous index is not nearby, log the city
        } else if (pos == nearbyIndex) {
            // Select the nearby tab
            selectTabAt(1);
            // if previous index is not fav, log the city
        } else {
            lastCityIndex = pos;
            // Select the explore tab
            selectTabAt(0);
            // reload the fragment accordingly
            mViewPager.getAdapter().notifyDataSetChanged();
        }
        // set current spinner index
        setSpinnerIndex(pos);
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }

    private void setSpinnerIndex(int index) {
        currentSpinnerIndex = index;
    }
    
    public int getLastCityIndex() {
        return lastCityIndex;
    }

    public int getCurrentSpinnerIndex() {
        return mSpinner.getSelectedItemPosition();
    }

    public void selectTabAt(int pos) {
        TabLayout.Tab tab = mTabLayout.getTabAt(pos);
        if (tab != null) {
            tab.select();
        }
    }

    public int getCurrentTabPosition() {
        return mTabLayout.getSelectedTabPosition();
    }

    /**
     * Tab Callbacks
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // handle tab selections
        int currentPosition = tab.getPosition();
        int favIndex = mSpinner.getAdapter().getCount() - 1;
        int nearbyIndex = favIndex - 1;
        supportInvalidateOptionsMenu();
        switch (currentPosition) {
            case 0:
                // set selection to last saved city index
                mSpinner.setSelection(lastCityIndex, true);
                break;
            case 1:
                mSpinner.setSelection(nearbyIndex);
                if (firstNearbyCall) {
                    makeAsyncHttpCall();
                    firstNearbyCall = false;
                }
                break;
            case 2:
                mSpinner.setSelection(favIndex);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // do nothing
        int position = tab.getPosition();
        if (position == 0) {
            // log city index
            lastCityIndex = currentSpinnerIndex;
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // do nothing
    }

    /**
     * Get the lat, lng
     */
    private Location getLastLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);

        }

        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    // Handle location results here
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    makeAsyncHttpCall();

                } else {
                    // permission denied, show toast
                    Toast.makeText(this,
                            "You need to allow permission for location changes",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Make HTTP call with ZomatoAPI
     */
    private void makeAsyncHttpCall() {
        Location location = getLastLocation();
        if (location != null) {
            String url = ZomatoNetworkUtils.buildURL(location.getLatitude(), location.getLongitude());
            ZomatoNetworkUtils.get(url, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // remove all values first
                    if (userLoggedIn())
                        mRootReference.child("/nearby").child(getCurrentUser().getUid()).removeValue();
                    try {
                        JSONArray restaurantsArray = response.getJSONArray("restaurants");
                        for (int i = 0; i < restaurantsArray.length(); i++) {
                            JSONObject restaurant = restaurantsArray.getJSONObject(i);
                            JSONObject restaurantJson = restaurant.getJSONObject("restaurant");
                            String name = restaurantJson.getString("name");
                            JSONObject rating = restaurantJson.getJSONObject("user_rating");
                            String aggregateRating = rating.getString("aggregate_rating");
                            String thumb = restaurantJson.getString("thumb");
                            String id = restaurantJson.getString("id");
                            List<String> photos = new ArrayList<>();
                            photos.add(thumb);
                            Restaurant res = new Restaurant(name, null, null, photos, null, aggregateRating, null, null);
                            if (userLoggedIn()) {
                                mRootReference.child("/nearby").child(getCurrentUser().getUid()).child(id).setValue(res);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.w(TAG, "onFailure: Failed with statusCode: " + statusCode);
                }
            });
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Sort by")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setSingleChoiceItems(R.array.filter_array, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            choice = 0;
                        } else if (i == 1) {
                            choice = 1;
                        } else if (i == 2) {
                            choice = 2;
                        }
                        mViewPager.getAdapter().notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                });

        builder.create().show();
    }

    public int getChoice() {
        return choice;
    }

    public Query getQuery(DatabaseReference reference, int choice) {
        switch (choice) {
            case 0:
                return reference.child(getDatabaseRoot(getLastCityIndex())).orderByChild("rating").limitToLast(200);
            case 1:
                return reference.child(getDatabaseRoot(getLastCityIndex())).orderByChild("name").limitToFirst(200);
            case 2:
                reference.child(getDatabaseRoot(getLastCityIndex())).orderByChild("reviewCount").limitToLast(200);
            default:
                return reference.child(getDatabaseRoot(getLastCityIndex())).orderByChild("rating").limitToFirst(200);
        }
    }

    public String getDatabaseRoot(int index) {
        switch (index) {
            case 0:
                return "new-york-city";
            case 1:
                return "istanbul";
            case 2:
                return "london";
            default:
                return "new-york-city";
        }
    }

    private boolean userLoggedIn() {
        return getCurrentUser() != null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthListener != null)
            mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SPINNER_INDEX, lastCityIndex);
        editor.apply();

        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }

}
