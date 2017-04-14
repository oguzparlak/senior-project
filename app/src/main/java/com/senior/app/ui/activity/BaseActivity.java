package com.senior.app.ui.activity;

import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.senior.app.R;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int LOGIN_REQUEST = 3;

    protected GoogleApiClient mGoogleApiClient;
    protected DatabaseReference mRootReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleClient();
        mRootReference = FirebaseDatabase.getInstance().getReference();

    }

    private synchronized void buildGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(Places.GEO_DATA_API).
                addApi(Places.PLACE_DETECTION_API).
                addApi(LocationServices.API).
                enableAutoManage(this, this).
                build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected DatabaseReference getFavoritesReference() {
        DatabaseReference mFavoritesReference = null;
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            mFavoritesReference = mRootReference.child("favorites").child(currentUser.getUid());
        }
        return mFavoritesReference;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    /**
     * Launch FirebaseUI Intent
     */
    public void onSignUpButtonClicked(View view) {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.AppTheme)
                .setProviders(
                        AuthUI.EMAIL_PROVIDER,
                        AuthUI.GOOGLE_PROVIDER)
                .build(), LOGIN_REQUEST);
    }


}
