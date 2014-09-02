package de.handler.mobile.android.bachelorapp.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.controllers.UserController;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaListener;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.GalleryContainerFragment_;

/**
 * Displays Guerrilla Prose in one list
 */
@EActivity(R.layout.activity_gallery)
public class ProseGalleryActivity extends BaseActivity implements OnMediaListener {

    @ViewById(R.id.gallery_pager)
    ViewPager mViewPager;

    @ViewById(R.id.activity_gallery_progress_bar)
    ProgressBar mProgressBar;

    @Bean
    ProseController proseController;

    @Bean
    UserController userController;

    @Bean
    MediaController mediaController;

    @App
    BachelorApp app;



    private List<Media> mMediaList = new ArrayList<Media>();
    private ArrayList<GuerrillaProse> mProseList;
    private List<Bitmap> mImageList = new ArrayList<Bitmap>();
    private int mListFragmentPosition;


    public static final String GALLERY_PROSE_PROSE_LIST_EXTRA = "gallery_prose_prose_list_extra";
    public static final String GALLERY_PROSE_EXTRA = "gallery_prose_extra";
    public static final String GALLERY_POSITION_EXTRA = "gallery_item_position_extra";
    private File mCacheDir;


    @AfterViews
    public void init() {
        mCacheDir = getCacheDir();

        ActionBar actionBar = setupActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mediaController.addListener(this);
        Bundle bundle = getIntent().getBundleExtra("gallery");

        mProseList = bundle.getParcelableArrayList(GALLERY_PROSE_PROSE_LIST_EXTRA);
        mListFragmentPosition = bundle.getInt(GALLERY_POSITION_EXTRA);

        app.setImageFromFlickr(false);

        for (GuerrillaProse prose : mProseList) {
            mediaController.getRemoteMedia(prose.getRemote_media_id());
        }
    }




    @UiThread
    public void setupViewPager() {
        GalleryPagerAdapter galleryPagerAdapter = new GalleryPagerAdapter();

        mProgressBar.setVisibility(View.GONE);
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(galleryPagerAdapter);
        mViewPager.setCurrentItem(mListFragmentPosition-1, true);
    }

    @Override
    public void onRemoteMediaStringReceived(String base64, Long media) {

        Bitmap bitmap = mediaController.base64ToBitmap(base64);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
        }
        mImageList.add(bitmap);

        // TODO: put images into cache

        if (mImageList.size() == mProseList.size()) {
            app.setPagerImages(mImageList);
            this.setupViewPager();
        }
    }

    @Override
    public void onRemoteMediaReceived(Media media) {
        mMediaList.add(media);
        app.addPagerAuthor(media.getMedia_author());
        mediaController.getRemoteMediaString(media.getId());
    }

    @Override
    public void onRemoteMediaSet(Long mediaId) {

    }

    @Override
    public void onRemoteMediaUpdated(Long mediaId) {

    }

    @Override
    public void onRemoteMediaCanceled() {

    }

    @Override
    public void onLocalMediaSet(Long mediaId) {

    }


    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.     *
     */
    // Use FragmentStatePagerAdapter as image in fragment has to be destroyed every time
    private class GalleryPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

        public GalleryPagerAdapter() {
            super(getSupportFragmentManager());
            mViewPager.setOnPageChangeListener(this);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            app.setTitleImageAuthor(mMediaList.get(position).getMedia_author());
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page
            GalleryContainerFragment_ gallery = new GalleryContainerFragment_();

            Bundle bundle = new Bundle();
            bundle.putParcelable(GALLERY_PROSE_EXTRA, mProseList.get(position));
            bundle.putInt(GALLERY_POSITION_EXTRA, position);

            gallery.setArguments(bundle);
            return gallery;
        }


        @Override
        public int getCount() {
            // Show size of total pages.
            return mProseList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            // POSITION_NONE makes it possible to reload the PagerAdapter:
            // Called when the host view is attempting to determine if an item's position
            // has changed. Returns POSITION_UNCHANGED if the position of the given item has
            // not changed or POSITION_NONE if the item is no longer present in the adapter.
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d("FragmentStatePagerAdapter", "page: " + String.valueOf(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Intent intent = new Intent(this, WebActivity_.class);
                intent.putExtra(WebActivity.URI, "https://github.com/itsmortoncornelius/guerrillaprose/wiki");
                startActivity(intent);
                return true;
            case R.id.action_get_old_media:
                if (userController.getLocalUser() != null && app.isServerOnline()) {
                    proseController.getRemoteProseForUser(userController.getLocalUser().getId());
                } else {
                    Toast.makeText(this, getString(R.string.no_user), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_restart_app:
                Intent restartIntent = new Intent(this, SplashActivity_.class);
                startActivity(restartIntent);
                this.finish();
                return true;
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
