package com.senior.app.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.senior.app.R;
import com.senior.app.ui.activity.DetailActivity;
import com.senior.app.ui.activity.MainActivity;
import com.senior.app.ui.viewholder.RestaurantViewHolder;

import java.util.ArrayList;

import model.Restaurant;

/**
 * Will be the abstraction of Fragments
 * All fragments will be rendered in a same way
 * Fragment will contain a RecyclerView to display items
 * But the content may change depending on the Firebase Query
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    protected RecyclerView mRecyclerView;
    protected ProgressBar mProgressBar;
    protected TextView mFavMessageView;
    protected Button mSignUpButton;

    protected GridLayoutManager mLayoutManager;
    protected FirebaseRecyclerAdapter mFirebaseAdapter;

    protected DatabaseReference mRootReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent re-creation objects when orientation changes
        // This will save time when Network Operations to be handled
        // setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Init LayoutManager and Adapter
        mLayoutManager = new GridLayoutManager(getActivity(), 2);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        Query databaseQuery = getQuery(mRootReference);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(Restaurant.class,
                R.layout.restaurant_list_item,
                RestaurantViewHolder.class, databaseQuery) {

            @Override
            protected void populateViewHolder(final RestaurantViewHolder viewHolder, final Restaurant restaurant, int position) {
                // Following configurations will be applied to whole elements in the RecyclerView
                final DatabaseReference restaurantRef =  getRef(position);

                final String restaurantKey = restaurantRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Launch DetailActivity
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(DetailActivity.NAME_EXTRA, restaurant.getName());
                        intent.putExtra(DetailActivity.RES_ID_EXTRA, restaurantKey);
                        intent.putExtra(DetailActivity.PHONE_NUMBER_EXTRA, restaurant.getPhoneNumber());
                        intent.putExtra(DetailActivity.DATABASE_ROOT_EXTRA, getDatabaseRoot(getCityIndex()));
                        intent.putExtra(DetailActivity.ADDRESS_EXTRA, restaurant.getAddress());
                        intent.putStringArrayListExtra(DetailActivity.PHOTOS_EXTRA, (ArrayList<String>) restaurant.getPhotos());
                        intent.putStringArrayListExtra(DetailActivity.CUISINES_EXTRA, (ArrayList<String>) restaurant.getCuisines());
                        startActivity(intent);
                    }
                });

                // If user liked any restaurants update UI accordingly
                if (getUser() != null) {
                    if (restaurant.getStars().containsKey(getUser().getUid())) {
                        viewHolder.getLikeButton().setLiked(true);
                    } else {
                        viewHolder.getLikeButton().setLiked(false);
                    }
                }

                // Handle Favorites Fragment as well
                MainActivity activity = (MainActivity) getActivity();
                if (activity.getCurrentTabPosition() == 2) {
                    viewHolder.getLikeButton().setLiked(true);
                }

                // Bind to View
                viewHolder.bind(restaurant, new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        if (getUser() != null) {
                            // handle like here, update the database
                            mRootReference.child("favorites").child(getUser().getUid()).child(restaurantKey).setValue(restaurant);
                            restaurant.getStars().put(getUser().getUid(), true);
                            // update the restaurant_reference
                            mRootReference.child(getDatabaseRoot(getCityIndex())).child(restaurantKey).setValue(restaurant);
                        } else {
                            popDialog();
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        // if user can unlike the button, this means user is already authenticated
                        mRootReference.child("favorites").child(getUser().getUid()).child(restaurantKey).removeValue();
                        restaurant.getStars().remove(getUser().getUid());
                        mRootReference.child(getDatabaseRoot(getCityIndex())).child(restaurantKey).setValue(restaurant);
                    }
                });

                // hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
                mFavMessageView.setVisibility(View.INVISIBLE);
                mSignUpButton.setVisibility(View.INVISIBLE);

            }

        };

        mRecyclerView.setAdapter(mFirebaseAdapter);

    }

    /**
     * Pop a dialog if the user clicked like button without authentication
     */
    private void popDialog() {
        final MainActivity activity = (MainActivity) getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Authentication required to like the restaurants !")
                .setTitle("Authenticate !");
        builder.setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.onSignUpButtonClicked(null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });
        builder.create().show();
        activity.selectTabAt(2);
    }

    public int getCityIndex() {
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity.getLastCityIndex();
    }

    public String getDatabaseRoot(int index) {
        switch (index) {
            case 0:
                return "new-york-city";
            case 1:
                return "istanbul";
            default:
                return "new-york-city";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.base_fragment_layout, container, false);

        initializeView(view);

        mRootReference = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    /**
     * Get the query corresponding to each tab
     */
    abstract Query getQuery(DatabaseReference reference);

    /**
     * Set the references of view components
     */
    private void initializeView(@NonNull View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_restaurants);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_fragments);
        mFavMessageView = (TextView) view.findViewById(R.id.fav_message);
        mSignUpButton = (Button) view.findViewById(R.id.signUpBtn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFirebaseAdapter != null) {
            mFirebaseAdapter.cleanup();
        }
    }

    protected FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}
