package com.senior.app.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.doodle.android.chips.ChipsView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.app.R;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    // Static Fields EXTRAS ...
    public static final String NAME_EXTRA = "NAME_EXTRA";
    public static final String RES_ID_EXTRA = "RES_ID_EXTRA";
    public static String DATABASE_ROOT_EXTRA = "DATABASE_ROOT_EXTRA";
    public static String PHOTOS_EXTRA = "PHOTOS_EXTRA";
    public static String CUISINES_EXTRA = "CUISINES_EXTRA";

    // Firebase Database
    private DatabaseReference mOrigin;
    private DatabaseReference mZomato;

    // UI Components
    private SliderLayout mSliderLayout;
    private ChipsView mChipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeView();

        String name = getIntent().getStringExtra(NAME_EXTRA);
        String resId = getIntent().getStringExtra(RES_ID_EXTRA);
        String databaseRoot = getIntent().getStringExtra(DATABASE_ROOT_EXTRA);
        ArrayList<String> photos = getIntent().getStringArrayListExtra(PHOTOS_EXTRA);
        ArrayList<String> cuisines = getIntent().getStringArrayListExtra(CUISINES_EXTRA);

        /**
         * Add photo urls to slider
         */
        for (String url : photos) {
            DefaultSliderView sliderView = new DefaultSliderView(this);
            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);
            mSliderLayout.addSlider(sliderView);
        }

        /**
         * Add cuisines to chipView
         */
        mChipsView.getEditText().setFocusable(false);
        for (String cuisine : cuisines) {
            mChipsView.addChip(cuisine, null, null, true);
        }

        getSupportActionBar().setTitle(name);

        // Firebase Test
        mOrigin = FirebaseDatabase.getInstance().getReference(databaseRoot).child(resId);
        mZomato = FirebaseDatabase.getInstance().getReference("nyc-zomato-external").child(resId);

        // Read the data
        mOrigin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Origin: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mZomato.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Zomato: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeView() {
        mSliderLayout = (SliderLayout) findViewById(R.id.slider_layout);
        mChipsView = (ChipsView) findViewById(R.id.chips_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }
}
