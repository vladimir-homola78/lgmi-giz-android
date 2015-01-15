package com.ibrow.de.giz.siegelklarheit;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter for the Tour swipe view.
 *
 * @see com.ibrow.de.giz.siegelklarheit.TourActivity
 * @author Pete
 */
public class TourPagerAdapter extends FragmentPagerAdapter {

    public TourPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return TourActivity.NUM_PAGES;
    }

    @Override
    public Fragment getItem(int position) {
        return TourFragment.newInstance(position +1 );
    }
}
