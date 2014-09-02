package de.handler.mobile.android.bachelorapp.app.ui.fragments;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import de.handler.mobile.android.bachelorapp.app.R;

/**
 * First Fragment in ViewPager
 * Contains Title Fragment and Write Fragment
 */
@EFragment(R.layout.fragment_home)
public class HomeFragment extends Fragment {

    FragmentManager mFragmentManager;

    @AfterViews
    void init() {
        TitleFragment_ titleFragment = new TitleFragment_();
        titleFragment.setArguments(getArguments());

        mFragmentManager = this.getChildFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_home_container_first, titleFragment)
                .replace(R.id.fragment_home_container_second, new ProseFragment_())
                .commit();
    }

}
