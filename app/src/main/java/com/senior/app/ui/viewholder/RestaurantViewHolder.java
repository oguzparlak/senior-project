package com.senior.app.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.senior.app.R;
import com.squareup.picasso.Picasso;

import model.Restaurant;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by Oguz on 23/02/2017.
 */

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private ImageView mThumbImage;
    private TextView mRestaurantTitle;
    private BadgeView mRatingBadge;
    private LikeButton mLikeButton;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        mThumbImage = (ImageView) itemView.findViewById(R.id.restaurant_thumb_image_view);
        mRestaurantTitle = (TextView) itemView.findViewById(R.id.restaurant_title);
        mRatingBadge = (BadgeView) itemView.findViewById(R.id.badge_view);
        mLikeButton = (LikeButton) itemView.findViewById(R.id.like_button);
    }

    public void bind(Restaurant restaurant, OnLikeListener onLikeListener) {
        if (restaurant.getPhotos().isEmpty()) {
            mThumbImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(itemView.getContext()).load(restaurant.getPhotos().get(0)).into(mThumbImage);
        }
        mRestaurantTitle.setText(restaurant.getName().replace("\n", "").replace("\r", ""));
        mLikeButton.setOnLikeListener(onLikeListener);
        mRatingBadge.setValue(restaurant.getRating());
    }

    public LikeButton getLikeButton() {
        return mLikeButton;
    }

}
