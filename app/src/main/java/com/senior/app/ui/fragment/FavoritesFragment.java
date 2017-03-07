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
        return reference.child("/new-york-popular");
    }
}
