package com.senior.app.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.senior.app.R;
import com.senior.app.ui.viewholder.RestaurantViewHolder;

import java.util.ArrayList;

import model.Restaurant;

public class SearchActivity extends BaseActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();

    public static final String QUERY_EXTRA = "QUERY_EXTRA";
    public static final String DATABASE_REFERENCE_EXTRA = "DATABASE_REFERENCE_EXTRA";
    public static final String SPINNER_INDEX_EXTRA = "SPINNER_INDEX_EXTRA";
    public static final String CITY_INDEX_EXTRA = "CITY_INDEX_EXTRA";

    private Query mQuery;
    private FirebaseRecyclerAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private String mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String keyword = getIntent().getStringExtra(QUERY_EXTRA);
        mDatabaseReference = getIntent().getStringExtra(DATABASE_REFERENCE_EXTRA);

        // Toolbar arrow and title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(keyword);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Firebase Query
        mQuery = buildQuery(keyword);

        mRecyclerView = (RecyclerView) findViewById(R.id.search_activity_recycler);

        // Init LayoutManager and Adapter
        mLayoutManager = new GridLayoutManager(this, 2);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = createAdapter();

        mRecyclerView.setAdapter(mAdapter);

    }

    private FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder> createAdapter() {
        return new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(Restaurant.class,
                R.layout.restaurant_list_item,
                RestaurantViewHolder.class, mQuery) {
            @Override
            protected void populateViewHolder(RestaurantViewHolder viewHolder, final Restaurant restaurant, int position) {
                final DatabaseReference restaurantRef =  getRef(position);
                final String restaurantKey = restaurantRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Launch DetailActivity
                        Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                        intent.putExtra(DetailActivity.NAME_EXTRA, restaurant.getName());
                        intent.putExtra(DetailActivity.RES_ID_EXTRA, restaurantKey);
                        intent.putExtra(DetailActivity.PHONE_NUMBER_EXTRA, restaurant.getPhoneNumber());
                        intent.putExtra(DetailActivity.DATABASE_ROOT_EXTRA, mDatabaseReference);
                        intent.putExtra(DetailActivity.ADDRESS_EXTRA, restaurant.getAddress());
                        intent.putStringArrayListExtra(DetailActivity.PHOTOS_EXTRA, (ArrayList<String>) restaurant.getPhotos());
                        intent.putStringArrayListExtra(DetailActivity.CUISINES_EXTRA, (ArrayList<String>) restaurant.getCuisines());
                        intent.putExtra(DetailActivity.RESTAURANT_EXTRA, restaurant);
                        intent.putExtra(DetailActivity.SPINNER_INDEX_EXTRA, getIntent().getIntExtra(SPINNER_INDEX_EXTRA, -1));
                        intent.putExtra(DetailActivity.LAST_CITY_INDEX_EXTRA, getIntent().getIntExtra(CITY_INDEX_EXTRA, -1));
                        startActivity(intent);
                    }
                });

                // If user liked any restaurants update UI accordingly
                if (getCurrentUser() != null) {
                    if (restaurant.getStars().containsKey(getCurrentUser().getUid())) {
                        viewHolder.getLikeButton().setLiked(true);
                    } else {
                        viewHolder.getLikeButton().setLiked(false);
                    }
                }

                viewHolder.bind(restaurant, new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        if (getCurrentUser() != null) {
                            // handle like here, update the database
                            mRootReference.child("favorites").child(getCurrentUser().getUid()).child(restaurantKey).setValue(restaurant);
                            restaurant.getStars().put(getCurrentUser().getUid(), true);
                            // update the restaurant_reference
                            mRootReference.child(mDatabaseReference).child(restaurantKey).setValue(restaurant);
                        } else {
                            popDialog();
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        // if user can unlike the button, this means user is already authenticated
                        mRootReference.child("favorites").child(getCurrentUser().getUid()).child(restaurantKey).removeValue();
                        restaurant.getStars().remove(getCurrentUser().getUid());
                        mRootReference.child(mDatabaseReference).child(restaurantKey).setValue(restaurant);
                    }
                });
            }
        };
    }

    private void popDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Authentication required to like the restaurants !")
                .setTitle("Authenticate !");
        builder.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.AppTheme)
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER)
                        .build(), LOGIN_REQUEST);
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

    private Query buildQuery(String keyword) {
        return mRootReference.
                child(mDatabaseReference).
                orderByChild("name").
                equalTo("\n" + keyword + "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_activity);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                Log.d(TAG, "onQueryTextSubmit: " + keyword);
                mQuery = buildQuery(keyword);
                FirebaseRecyclerAdapter newAdapter = createAdapter();
                mRecyclerView.setAdapter(newAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }
}
