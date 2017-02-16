package com.senior.app.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Oguz on 16/02/2017.
 */

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.RestaurantViewHolder> {

    // Data Source
    // List<Restaurant> mRestaurants;

    // OnClickHandler
    private OnClickHandler mOnClickHandler;

    public RestaurantsAdapter(OnClickHandler onClickHandler) {
        mOnClickHandler = onClickHandler;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface OnClickHandler {
        void onClick(/*Restaurant Object*/);
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Call onClickHandler's onClick
            int position = getAdapterPosition();
            // Get the item at the specific position
            // Call onClick on that object
            mOnClickHandler.onClick(/*Restaurant Object*/);
        }
    }
}
