package com.senior.app.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.senior.app.R;
import com.squareup.picasso.Picasso;

import model.Restaurant;

/**
 * Created by Oguz on 23/02/2017.
 */

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private ImageView mThumbImage;
    private TextView mRestaurantTitle;
    private TextView mPriceIndicator;
    private TextView mRatingIndicator;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        mThumbImage = (ImageView) itemView.findViewById(R.id.restaurant_thumb_image_view);
        mRestaurantTitle = (TextView) itemView.findViewById(R.id.restaurant_title);
        mPriceIndicator = (TextView) itemView.findViewById(R.id.restaurant_price_indicator);
        mRatingIndicator = (TextView) itemView.findViewById(R.id.restaurant_rating_indicator);
    }

    public void bind(Restaurant restaurant) {
        if (restaurant.getThumb().isEmpty()) {
            mThumbImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(itemView.getContext()).load(restaurant.getThumb()).into(mThumbImage);
        }
        mRestaurantTitle.setText(restaurant.getName());
        //mPriceIndicator.setText(restaurant.getPriceRating());
        mRatingIndicator.setText(String.valueOf(restaurant.getRating()));
    }
}
