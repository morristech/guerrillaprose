package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnBackPressedListener;


/**
 * Contains all written texts from user
 */
@EFragment(R.layout.fragment_content)
public class ContentFragment extends BaseGridFragment {

    public static final String PROSE_LIST_FRAGMENT_TAG_EXTRA = "prose_list_fragment_tag_extra";
    public static final String PROSE_LIST_FRAGMENT_PROSE_EXTRA = "prose_list_fragment_prose_extra";
    public static final String PROSE_DISPLAY_LOCAL = "is_prose_fragment_local";


    @AfterViews
    void init() {
        this.startTagGridFragment();
    }


    private void startTagGridFragment() {

        ContentTagFragment_ tagFragment = new ContentTagFragment_();
        // Get arguments from MainActivity and forward them to the tag fragment
        tagFragment.setArguments(getArguments());

        // As this is already a fragment use the ChildFragmentManager to
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_display_main_container, tagFragment)
                .commit();
    }
}
