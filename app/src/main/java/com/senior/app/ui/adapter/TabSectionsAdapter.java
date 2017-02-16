package com.senior.app.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.senior.app.R;

/**
 * Created by Oguz on 16/02/2017.
 */

public class TabSectionsAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public TabSectionsAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                // Explore Fragment
                return null;
            case 2:
                // NearbyFragment
                return null;
            case 3:
                // Favorites Fragment
                return null;
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
