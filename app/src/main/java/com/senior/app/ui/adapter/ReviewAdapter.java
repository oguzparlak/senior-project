package com.senior.app.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.senior.app.R;

import java.util.List;

import model.Review;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by Oguz on 22/04/2017.
 */

public class ReviewAdapter extends ParallaxRecyclerAdapter<Review> {

    private List<Review> mReviews;

    public ReviewAdapter(List<Review> mReviews) {
        super(mReviews);
        this.mReviews = mReviews;
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, ParallaxRecyclerAdapter<Review> parallaxRecyclerAdapter, int i) {
        // Bind your view holder with data
        Review review = mReviews.get(i);
        // Bind here
        ReviewViewHolder holder = (ReviewViewHolder) viewHolder;
        holder.bind(review);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, ParallaxRecyclerAdapter<Review> parallaxRecyclerAdapter, int i) {
        return new ReviewViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_row, viewGroup, false));
    }

    @Override
    public int getItemCountImpl(ParallaxRecyclerAdapter<Review> parallaxRecyclerAdapter) {
        return mReviews.size();
    }

    private static class ReviewViewHolder extends RecyclerView.ViewHolder {

        // Implement your views here
        private ImageView mSourceImage;
        private BadgeView mRatingBadge;
        private TextView mAuthorTextView;
        private TextView mReviewContentTextView;
        private TextView mTimeDescTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            mSourceImage = (ImageView) itemView.findViewById(R.id.review_source_image_view);
            mRatingBadge = (BadgeView) itemView.findViewById(R.id.review_activity_badge_view);
            mAuthorTextView = (TextView) itemView.findViewById(R.id.author_text_view);
            mReviewContentTextView = (TextView) itemView.findViewById(R.id.entry_text_view);
            mTimeDescTextView = (TextView) itemView.findViewById(R.id.time_description_text_view);
        }

        public void bind(Review review) {
            // Set according to source, make switch-case
            switch (review.getSource()) {
                case "TripAdvisor":
                    mSourceImage.setImageResource(R.mipmap.ic_trip_advisor);
                    break;
                case "Zomato":
                    mSourceImage.setImageResource(R.mipmap.ic_zomato_icon);
                    break;
                case "Google":
                    mSourceImage.setImageResource(R.drawable.common_google_signin_btn_icon_light);
                    break;
                default:
                    mSourceImage.setImageResource(R.drawable.ic_launcher);
                    break;
            }
            mRatingBadge.setValue((float) review.getRating());
            mAuthorTextView.setText(review.getAuthor());
            mReviewContentTextView.setText(review.getText());
            mTimeDescTextView.setText(review.getTimeDescription());
        }
    }
}
