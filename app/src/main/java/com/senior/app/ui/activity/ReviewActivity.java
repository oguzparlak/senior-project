package com.senior.app.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.senior.app.R;
import com.senior.app.ui.adapter.ReviewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Review;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = ReviewActivity.class.getSimpleName();
    public static final String REVIEWS_EXTRA = "REVIEWS_EXTRA";
    public static final String THUMB_EXTRA = "THUMB_EXTRA";

    private RecyclerView mReviewsRecycler;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        mToolbar = (Toolbar) findViewById(R.id.review_activity_toolbar);

        mToolbar.setTitle("Reviews");

        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.review_activity_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Add Review Button Tapped");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Extras
        List<Review> reviews = (List<Review>) getIntent().getSerializableExtra(REVIEWS_EXTRA);
        String thumbImage = getIntent().getStringExtra(THUMB_EXTRA);

        mReviewsRecycler = (RecyclerView) findViewById(R.id.parallax_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mReviewsRecycler.setLayoutManager(manager);
        mReviewsRecycler.setHasFixedSize(true);

        // Adapter, Parallax Scroll
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);

        View view = LayoutInflater.from(this).inflate(R.layout.parallax_custom_view, mReviewsRecycler, false);
        ImageView parallaxImage = (ImageView) view.findViewById(R.id.parallax_thumb_image_view);
        Picasso.with(this).load(thumbImage).into(parallaxImage);

        reviewAdapter.setParallaxHeader(view, mReviewsRecycler);

        mReviewsRecycler.setAdapter(reviewAdapter);

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
}
