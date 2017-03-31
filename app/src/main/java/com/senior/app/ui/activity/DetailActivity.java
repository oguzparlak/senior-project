package com.senior.app.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.app.R;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    // Static Fields EXTRAS ...
    public static final String NAME_EXTRA = "NAME_EXTRA";
    public static final String RES_ID_EXTRA = "RES_ID_EXTRA";
    public static String DATABASE_ROOT_EXTRA = "DATABASE_ROOT_EXTRA";

    // Firebase Database
    private DatabaseReference mOrigin;
    private DatabaseReference mZomato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name = getIntent().getStringExtra(NAME_EXTRA);
        String resId = getIntent().getStringExtra(RES_ID_EXTRA);
        String databaseRoot = getIntent().getStringExtra(DATABASE_ROOT_EXTRA);

        Log.d(TAG, "onCreate: name: " + name);
        Log.d(TAG, "onCreate: resId: " + resId);
        Log.d(TAG, "onCreate: databaseRoot: " + databaseRoot);


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
}
