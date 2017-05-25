package com.senior.app.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.senior.app.R;
import com.senior.app.ui.fragment.BaseFragment;
import com.senior.app.ui.fragment.ExploreFragment;
import com.senior.app.ui.fragment.FavoritesFragment;
import com.senior.app.ui.fragment.NearbyFragment;

import java.lang.ref.WeakReference;

public class TabSectionsAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = TabSectionsAdapter.class.getSimpleName();
    private Fragment mCurrentFragment;
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

    public String getFragmentTag(int pos){
        return "android:switcher:"+R.id.container+":"+pos;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
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
