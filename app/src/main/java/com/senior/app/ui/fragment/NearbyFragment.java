package com.senior.app.ui.fragment;

import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class NearbyFragment extends BaseFragment {

    @Override
    Query getQuery(DatabaseReference reference) {
        mProgressBar.setVisibility(View.INVISIBLE);
        FirebaseUser user = getUser();
        if (user != null) {
            return reference.child("/nearby").child(user.getUid());
        } else {
            mFavMessageView.setVisibility(View.VISIBLE);
            mSignUpButton.setVisibility(View.VISIBLE);
            return reference.child("null-reference");
        }
    }

}
