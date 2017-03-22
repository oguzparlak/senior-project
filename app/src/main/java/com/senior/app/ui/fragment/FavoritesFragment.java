package com.senior.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class FavoritesFragment extends BaseFragment {

    @Override
    Query getQuery(DatabaseReference reference) {

        // Hide the progress-bar no matter what
        mProgressBar.setVisibility(View.INVISIBLE);

        if (getUser() != null) {
            // Hide the fav-message
            mFavMessageView.setVisibility(View.INVISIBLE);
            mSignUpButton.setVisibility(View.INVISIBLE);
            return reference.child("/favorites/").child(getUser().getUid());
        } else {
            // Show the fav-message
            mFavMessageView.setVisibility(View.VISIBLE);
            mSignUpButton.setVisibility(View.VISIBLE);
            return reference.child("/favorites");
        }
    }
}
