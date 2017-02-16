package com.senior.app.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Will be the holder of Fragments
 * All fragments will be rendered in a same way
 * Fragment will contain a RecyclerView to display items
 * But the content may change depending on the Firebase Query
 * The difference between them is they have different queries and data
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent re-creation objects when orientation changes
        // This will save time when Firebase Request to be handled
        setRetainInstance(true);
    }
}
