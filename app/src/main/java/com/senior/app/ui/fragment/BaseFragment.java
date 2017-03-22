package com.senior.app.ui.fragment;

import android.content.DialogInterface;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.senior.app.R;
import com.senior.app.ui.viewholder.RestaurantViewHolder;

import java.util.List;

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
            protected void populateViewHolder(RestaurantViewHolder viewHolder, final Restaurant restaurant, int position) {
                // Following configurations will be applied to whole elements in the RecyclerView
                final DatabaseReference restaurantRef =  getRef(position);

                // Set OnClickListener
                final String restaurantKey = restaurantRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Launch DetailActivity
                        Log.d(TAG, "onClick: res_id: " + restaurantKey);
                    }
                });

                // Bind to View
                viewHolder.bind(restaurant, new View.OnClickListener() {

                    // Handle star click
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: starClicked !");
                        if (getUser() == null) {
                            Log.d(TAG, "onClick: User is not authenticated!");

                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage("Authentication required to like the restaurants !")
                                    .setTitle("Authenticate !");

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d(TAG, "onClick: Authenticate the user with FirebaseUI");
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d(TAG, "onClick: User cancelled the dialog");
                                }
                            });

                            builder.create().show();

                        } else {
                            Log.d(TAG, "onClick: User is authenticated, handle favorites!");
                            mRootReference.child("/favorites/").child(getUser().getUid()).child(restaurantKey).setValue(restaurant);
                        }
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
