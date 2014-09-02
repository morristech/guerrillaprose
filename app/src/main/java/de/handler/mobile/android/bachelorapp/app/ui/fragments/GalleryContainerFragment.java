package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import de.handler.mobile.android.bachelorapp.app.R;

/**
 * Gallery Fragment - contains two fragments
 * The Title Fragment and the Content Fragment
 */
@EFragment(R.layout.fragment_gallery)
public class GalleryContainerFragment extends Fragment {

    FragmentManager mFragmentManager;

    @AfterViews
    void init() {

        GalleryTitleFragment_ galleryTitleFragment = new GalleryTitleFragment_();
        galleryTitleFragment.setArguments(getArguments());

        GalleryContentFragment_ galleryContentFragment = new GalleryContentFragment_();
        galleryContentFragment.setArguments(getArguments());

        mFragmentManager = getChildFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_gallery_container_first, galleryTitleFragment)
                .replace(R.id.fragment_gallery_container_second, galleryContentFragment)
                .commit();
    }
}
