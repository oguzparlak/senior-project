package com.senior.app.ui.fragment;

import android.util.Log;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.senior.app.R;
import com.senior.app.ui.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import model.Restaurant;
import utils.ZomatoNetworkUtils;

public class ExploreFragment extends BaseFragment {

    // Return the Restaurants to be explored in selected city
    @Override
    Query getQuery(DatabaseReference reference) {
        MainActivity activity = (MainActivity) getActivity();
        return activity.getQuery(reference, activity.getChoice());
    }

}
