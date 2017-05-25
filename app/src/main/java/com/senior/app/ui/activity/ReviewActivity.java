package com.senior.app.ui.activity;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.senior.app.R;
import com.senior.app.ui.adapter.ReviewAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import model.Review;

public class ReviewActivity extends BaseActivity {

    private static final String TAG = ReviewActivity.class.getSimpleName();

    public static final String REVIEWS_EXTRA = "REVIEWS_EXTRA";
    public static final String THUMB_EXTRA = "THUMB_EXTRA";
    public static final String RES_ID_EXTRA = "RES_ID_EXTRA";

    private RecyclerView mReviewsRecycler;
    private ReviewAdapter mReviewAdapter;
    private List<Review> mReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Get Extras
        mReviews = (List<Review>) getIntent().getSerializableExtra(REVIEWS_EXTRA);
        String thumbImage = getIntent().getStringExtra(THUMB_EXTRA);
        final String resId = getIntent().getStringExtra(RES_ID_EXTRA);

        getSupportActionBar().setTitle("Reviews");

        mReviewsRecycler = (RecyclerView) findViewById(R.id.parallax_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mReviewsRecycler.setLayoutManager(manager);
        mReviewsRecycler.setHasFixedSize(true);

        // Adapter, Parallax Scroll
        mReviewAdapter = new ReviewAdapter(mReviews);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.review_activity_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Add Review Button Tapped");
                // Pop Dialog
                final Dialog dialog = new Dialog(ReviewActivity.this);
                dialog.setContentView(R.layout.add_comment_dialog);

                dialog.setTitle("Add Comment");
                // onClick
                final Button postButton = (Button) dialog.findViewById(R.id.post_button);
                final EditText commentEditText = (EditText) dialog.findViewById(R.id.comment_edit_text);
                final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rating_radio_group);
                postButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String comment = commentEditText.getText().toString();
                        double rating = Double.parseDouble((String) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()).getTag());
                        String author = getCurrentUser().getDisplayName();
                        String provider = "Restaurants";
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                        String dateString = dateFormat.format(date);
                        Review review = new Review(provider, author, rating, dateString, comment);
                        mReviews.add(review);
                        mRootReference.child("internal-reviews").child(getCurrentUser().getUid()).child(resId).push().setValue(review);
                        mReviewAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                        postButton.setEnabled(true);
                        double rating = Double.parseDouble((String) radioGroup.findViewById(i).getTag());
                        Log.d(TAG, "onCheckedChanged: rating: " + rating);
                    }
                });

                dialog.show();

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View view = LayoutInflater.from(this).inflate(R.layout.parallax_custom_view, mReviewsRecycler, false);
        ImageView parallaxImage = (ImageView) view.findViewById(R.id.parallax_thumb_image_view);
        Picasso.with(this).load(thumbImage).into(parallaxImage);

        mReviewAdapter.setParallaxHeader(view, mReviewsRecycler);

        mReviewsRecycler.setAdapter(mReviewAdapter);

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
