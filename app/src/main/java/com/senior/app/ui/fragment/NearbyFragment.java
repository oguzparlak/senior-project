package com.senior.app.ui.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class NearbyFragment extends BaseFragment {

    @Override
    Query getQuery(DatabaseReference reference) {
        return reference.child("/new-york-city").orderByChild("rating").limitToLast(50);
    }

}
