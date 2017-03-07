package com.senior.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

    protected RecyclerView mRecyclerView;
    protected ProgressBar mProgressBar;

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

    // TODO 1 - Design and structure the database
    // TODO 2 - Modify the Restaurant class

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
            protected void populateViewHolder(RestaurantViewHolder viewHolder, Restaurant restaurant, int position) {
                // Following configurations will be applied to whole elements in the RecyclerView
                final DatabaseReference restaurantRef =  getRef(position);

                // Set OnClickListener
                final String restaurantKey = restaurantRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Launch DetailActivity
                    }
                });

                // Bind to View
                viewHolder.bind(restaurant);

                // hide progressbar
                mProgressBar.setVisibility(View.INVISIBLE);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFirebaseAdapter != null) {
           mFirebaseAdapter.cleanup();
        }
    }

    protected String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
