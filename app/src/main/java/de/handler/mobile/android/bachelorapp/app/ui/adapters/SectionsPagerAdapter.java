package de.handler.mobile.android.bachelorapp.app.ui.adapters;

/**
 * FragmentStatePagerAdapter which stores and handles the fragments
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Locale;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.ContentFragment;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.ContentFragment_;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.HomeFragment_;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    MainActivity mainActivity;

    public SectionsPagerAdapter(MainActivity activity) {
        super(activity.getSupportFragmentManager());
        mainActivity = activity;
    }

    // The basic three fragments of the app are instantiated here
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page
        switch (position) {
            case 0:
                return new HomeFragment_();
            case 1:
                ContentFragment_ local = new ContentFragment_();
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, true);
                local.setArguments(bundle);
                return local;
            case 2:
                ContentFragment_ remote = new ContentFragment_();
                bundle = new Bundle();
                bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, false);
                remote.setArguments(bundle);
                return remote;
        }
        return new Fragment();
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    // Show a title depending on language
    @Override
    public CharSequence getPageTitle(int position) {

        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mainActivity.getString(R.string.home).toUpperCase(l);
            case 1:
                return mainActivity.getString(R.string.device).toUpperCase(l);
            case 2:
                return mainActivity.getString(R.string.server).toUpperCase(l);
        }
        return null;
    }
}
