package com.senior.app.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.senior.app.R;
import com.senior.app.ui.fragment.BaseFragment;
import com.senior.app.ui.fragment.ExploreFragment;
import com.senior.app.ui.fragment.FavoritesFragment;
import com.senior.app.ui.fragment.NearbyFragment;

public class TabSectionsAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public TabSectionsAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                // Explore Fragment
                return new ExploreFragment();
            case 1:
                // NearbyFragment
                return new NearbyFragment();
            case 2:
                // Favorites Fragment
                return new FavoritesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.tab_section_explore);
            case 1:
                return mContext.getString(R.string.tab_section_nearby);
            case 2:
                return mContext.getString(R.string.tab_section_favorites);
            default:
                return null;
        }
    }
}
