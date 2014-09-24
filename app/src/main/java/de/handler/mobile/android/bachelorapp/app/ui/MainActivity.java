package de.handler.mobile.android.bachelorapp.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.api.flickr.FlickrJson;
import de.handler.mobile.android.bachelorapp.app.api.flickr.FlickrManager;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaTypeController;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.controllers.Tag;
import de.handler.mobile.android.bachelorapp.app.controllers.UserController;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnBackPressedListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnFlickrListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaTypeListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnUserListener;
import de.handler.mobile.android.bachelorapp.app.ui.adapters.NavigationDrawerAdapter;
import de.handler.mobile.android.bachelorapp.app.ui.adapters.SectionsPagerAdapter;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.ContentListFragment;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity  implements ActionBar.TabListener, ListView.OnItemClickListener,
        OnProseListener, OnMediaListener, OnMediaTypeListener, OnUserListener, OnFlickrListener {

    // Static final variables
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 100;
    public static final int REQUEST_CODE_SELECT_IMAGE = 101;

    public static final String PROSEDIALOG_TAG = "Prose Dialog";
    public static final String DIALOG_PROSE_MEDIA_EXTRA = "dialog_prose_image_extra";
    public static final String DIALOG_PROSE_IS_UPDATE = "dialog_prose_is_update_extra";

    public static final String DELETEDIALOG_TAG = "Delete Dialog";


    private int mAlreadyShownCount = 0;
    private boolean mProseIsUpdate;



    // Boolean used for camera Nexus workaround
    private boolean mReturningWithResult;

    // Time for calculating interval between last back press action and new one
    // --> exit only when pressed twice
    private long mLastBackPressTime = System.currentTimeMillis();
    private static long BACK_PRESS_RESET = 6000;


    // The {@link android.support.v4.view.PagerAdapter} that will provide
    // fragments for each of the sections.
    SectionsPagerAdapter mSectionsPagerAdapter;

    // The OnBackPressed Interface for fragments
    // that want to react to these kind of events
    private ArrayList<OnBackPressedListener> mOnBackPressedListenerList;

    // The {@link ViewPager} that will host the section contents.
    @ViewById(R.id.main_pager)
    ViewPager mViewPager;

    /**
     * The Beans of this app including all controller classes as well as the flickr manager
     */
    @Bean
    MediaController mediaController;

    @Bean
    MediaTypeController mediaTypeController;

    @Bean
    ProseController proseController;

    @Bean
    UserController userController;

    @Bean
    FlickrManager flickrManager;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @AfterInject
    void initBeans() {
        flickrManager.addListener(this);
    }


    @AfterViews
    void init() {
        // Set Callbacks
        proseController.addListener(this);
        mediaController.addListener(this);
        userController.addListener(this);

        // If the app starts for the first time a user profile has to be created.
        this.checkForFirstStart();

        // Set up Action Bar
        ActionBar actionBar = setupActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up TabViewPager
        this.setupViewPager(actionBar);

        // Set up the Navigation Drawer
        this.setupNavigationDrawer();
    }


    private void checkForFirstStart() {
        if (app.isServerOnline()) {

            // if this is the first start of the app create a user profile
            if (prefs.firstStart().get()) {

                // Tell the Shared Preferences that the app has already started
                // at least once
                prefs.edit().firstStart().put(false).apply();

                // Open the register dialog
                this.startAuthentication();

                // initiate local database with remote mediaTypes
                mediaTypeController.addListener(this);
                mediaTypeController.getRemoteMediaTypes();
            } else {
                if (userController.getLocalUser() == null) {
                    // Something went wrong on registration - reset app to zero and restart
                    Toast.makeText(this,
                            "Something went wrong on registration. Please try again",
                            Toast.LENGTH_SHORT).show();
                    prefs.edit().firstStart().put(true).apply();

                    ArrayList<MediaType> types =
                            new ArrayList<MediaType>(mediaTypeController.getMediaTypes());
                    for (MediaType type: types) {
                        mediaTypeController.deleteMediaType(type);
                    }

                    this.checkForFirstStart();
                }
            }
        }
    }


    // Remote mediaTypes received on first app start
    @Override
    public void onMediaTypesReceived(final List<MediaType> types) {
        ArrayList<String> typeStrings = new ArrayList<String>();

        // If the mediaType is not already in database insert it
        // otherwise update it - useful for matching the ids from server
        for (MediaType mediaType : types) {
            typeStrings.add(mediaType.getMedia_type());
            mediaTypeController.setMediaType(mediaType);
        }

        // Update server if necessary with app mediaTypes
        if (!typeStrings.contains(MediaTypeController.MEDIA_TYPE_AUDIO)) {
            mediaTypeController.setRemoteMediaType(MediaTypeController.MEDIA_TYPE_AUDIO);
        }

        if (!typeStrings.contains(MediaTypeController.MEDIA_TYPE_VIDEO)) {
            mediaTypeController.setRemoteMediaType(MediaTypeController.MEDIA_TYPE_VIDEO);
        }

        if (!typeStrings.contains(MediaTypeController.MEDIA_TYPE_IMAGE)) {
            mediaTypeController.setRemoteMediaType(MediaTypeController.MEDIA_TYPE_IMAGE);
        }
    }

    // MediaType added on server, get id and save in local database
    @Override
    public void onMediaTypeSet(MediaType mediaType) {
        mediaTypeController.setMediaType(mediaType);
    }


    @Background
    void setupViewPager(final ActionBar actionBar) {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mOnBackPressedListenerList = new ArrayList<OnBackPressedListener>();
        mSectionsPagerAdapter = new SectionsPagerAdapter(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    /**
     * Method is called when Activity was called for Result
     * Returns with the result corresponding to request code
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                // reset flickr credits
                app.setImageFromFlickr(false);
                app.setTitleImageAuthor(userController.getLocalUser().getSurname() + " " + userController.getLocalUser().getName());
                app.setCurrentMedia(null);

                mReturningWithResult = true;
            } else if (resultCode == RESULT_CANCELED) {
                mReturningWithResult = false;
            }
        }

        if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                // reset flickr credits
                app.setImageFromFlickr(false);
                app.setTitleImageAuthor(userController.getLocalUser().getSurname() + " " + userController.getLocalUser().getName());
                app.setCurrentMedia(null);

                Uri selectedImageUri = data.getData();
                String selectedImagePath = mediaController.getRealPathFromURI(this, selectedImageUri);
                Log.i("Image File Path", "" + selectedImagePath);

                Bitmap bitmap = mediaController.getImageFromDisk(selectedImagePath);
                app.setTitleImage(bitmap);

                // refresh ViewPager / SectionsPagerAdapter
                this.refreshSectionsPagerAdapter();
            }
        }
    }


    // Workaround for Activity State Loss
    // (source for activity state loss: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html)
    // (source for workaround: http://stackoverflow.com/questions/16265733/failure-delivering-result-onactivityforresult)
    @Override
    protected void onPostResume() {
        super.onPostResume();
        // returning successfully from camera
        if (mReturningWithResult) {

            // One more workaround for bug for nexus devices - no data is returned in intent
            // --> uri is manually stored in app object and retrieved
            Media pendingMedia =
                    mediaController.createPendingMedia(
                            MediaTypeController.MEDIA_TYPE_IMAGE, app.getTempImageUri());

            if (pendingMedia != null) {
                Bitmap bitmap = mediaController.getImageFromDisk(pendingMedia.getUrl());

                // Pending media is media without text
                ArrayList<Media> medias = app.getPendingMedia();
                medias.add(pendingMedia);
                app.setPendingMedia(medias);

                // Set taken image as title image
                app.setTitleImage(bitmap);

                // when image comes from camera it is already stored on sd card
                // unlike the flickr image which is only stored in memory
                // tell the app
                app.setImageFromCam(true);

                // refresh ViewPager / SectionsPagerAdapter
                this.refreshSectionsPagerAdapter();
            }
        } else {
            app.setImageFromCam(false);
        }
    }


    @UiThread
    void refreshSectionsPagerAdapter() {
        mOnBackPressedListenerList.clear();

        try {
            mViewPager.getAdapter().notifyDataSetChanged();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }



    @Background
    void setupNavigationDrawer() {
        // Get string array from strings.xml
        String[] navigationStrings = getResources().getStringArray(R.array.navigation_drawer_strings);

        // Set the adapter for the list view
        drawerList.setAdapter(new NavigationDrawerAdapter(this,
                R.layout.adapter_drawer_list_item, navigationStrings));

        // Set the list's click listener
        drawerList.setOnItemClickListener(this);
    }

    /**
     * What to do when clicking on an item in Navigation Drawer
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                if (app.isServerOnline()) {
                    this.startAuthentication();
                } else {
                    Toast.makeText(this, getString(R.string.offline_user), Toast.LENGTH_LONG).show();
                }
                break;
            case 1:
                if (app.isServerOnline()) {
                    this.deleteUser();
                } else {
                    Toast.makeText(this, getString(R.string.offline_user), Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    private void deleteUser() {
        if (app.isServerOnline()) {
            Intent intent = new Intent(this, AuthenticatorActivity_.class);
            intent.putExtra(AuthenticatorActivity.USER_REMOVAL, true);
            startActivity(intent);
        }
    }

    private void startAuthentication() {
        if (app.isServerOnline()) {
            Intent intent = new Intent(this, AuthenticatorActivity_.class);
            intent.putExtra(AuthenticatorActivity.USER_REMOVAL, false);
            startActivity(intent);
        }
    }

    /**
     * Calback methods
     */

    /**
     * Reactions to Tab Actions
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * Prose Callback methods
     */
    @Override
    public void onNewProse(GuerrillaProse prose, boolean sharedUpdated) {
        // Check if it is a new guerrilla prose or an update
        // NEW
        if (prose.getId() == null) {
            if (prose.getShared()) {
                // If it is a new shared prose everything is handled in onRemoteMediaSet Callback
                // proseController.setRemoteProse(prose);
                if (mediaController.getMedia(prose.getMedia_id()) == null
                        && prose.getRemote_media_id() != null) {

                    mediaController.getRemoteMediaString(prose.getRemote_media_id());
                }
            } else {
                proseController.setLocalProse(prose, true);
            }

            // UPDATE
        } else {
            MediaType mediaType = mediaTypeController.getMediaType(MediaTypeController.MEDIA_TYPE_IMAGE);
            Media media = mediaController.getMedia(prose.getMedia_id());

            // if guerrilla prose is to be shared
            if (prose.getShared()) {
                // if shared status changed now it was not shared before
                if (sharedUpdated) {
                    mProseIsUpdate = true;
                    Bitmap bitmap = mediaController.getImageFromDisk(media.getUrl());
                    mediaController.setRemoteMedia(mediaType.getId(), bitmap, app.getTitleImageAuthor());
                } else {
                    mediaController.updateRemoteMedia(media, prose.getRemote_media_id());
                }
                // if guerrilla prose is meant to stay offline
            } else {
                // if shared status changed now it was shared before
                if (sharedUpdated) {
                    proseController.deleteRemoteProse(prose.getRemote_id());
                    mediaController.deleteRemoteMedia(prose.getRemote_media_id());

                    mediaController.updateMedia(media);
                } else {
                    proseController.updateLocalProse(prose);
                }
            }
        }
    }

    /**
     * Prose Callback functions
     *
     * when creating always do something with SHARED prose before doing it to LOCAL one
     * when deleting always do something with LOCAL prose before doing it to SHARED one (Rollback)
     */
    // If shared, set first remotely then locally
    @UiThread
    @Override
    public void onRemoteProseSet(GuerrillaProse prose) {
        proseController.getRemoteProse();
        prose.setShared(app.getGuerrillaProse().getShared());

        // The remote prose id is set in remote prose id.
        // As the server uses the id and also sets
        // the remote prose id field reset it to avoid local database errors
        if (mProseIsUpdate) {
            proseController.updateLocalProse(prose);
        } else {
            prose.setId(null);
            proseController.setLocalProse(prose, true);
        }
    }

    @UiThread
    @Override
    public void onLocalProseSet(GuerrillaProse prose) {
        app.setGuerrillaProse(prose);
        proseController.getLocalProse();
    }



    // Update first remotely then locally
    @UiThread
    @Override
    public void onRemoteProseUpdated(GuerrillaProse prose) {
        proseController.updateLocalProse(prose);
    }

    @UiThread
    @Override
    public void onLocalProseUpdated(GuerrillaProse prose) {
        // As it may occur that the user "unshares" his guerrilla prose
        // it is necessary to also update remote prose when in onNewProse
        // it seems to be a matter of local prose.
        // As the local prose is always set after the remote prose
        // the function can also be called here instead of in onRemoteProseUpdated.
        app.setGuerrillaProse(prose);
        proseController.getRemoteProse();
        proseController.getLocalProse();
    }



    // Delete first locally then remotely
    @UiThread
    @Override
    public void onLocalProseDeleted(Long remoteProseId, boolean shared) {
        // If it also was shared delete the shared one on server, too.
        if (shared) {
            proseController.deleteRemoteProse(remoteProseId);
        }

        app.setGuerrillaProse(null);
        proseController.getLocalProse();
    }

    // Delete first locally
    @UiThread
    @Override
    public void onRemoteProseDeleted() {
        proseController.getRemoteProse();
    }



    // Get all tags from app and refresh ViewPager
    @UiThread
    @Override
    public void onLocalProseReceived(List<GuerrillaProse> proseList) {
        // Update tags in app
        // Reset remote tags in app
        ArrayList<Tag> tags = proseController.getTags(proseList);
        app.setLocalTags(tags);

        Tag tag;
        if (tags.size() > 0) {
            tag = proseController.getMostPopularTag(tags);
        } else {
            tag = proseController.getFallbackTag();
        }

        flickrManager.getFlickrData(tag);

        // Match local and online proses in case of former error
        this.refreshSectionsPagerAdapter();
    }

    @Background
    @Override
    public void onRemoteProseByUserReceived(List<GuerrillaProse> proseList) {

        ArrayList<GuerrillaProse> locals =
                new ArrayList<GuerrillaProse>(app.getDaoSession()
                        .getGuerrillaProseDao()
                        .loadAll());

        ArrayList<Long> localRemoteIds = new ArrayList<Long>(locals.size());

        for (GuerrillaProse local : locals) {
            localRemoteIds.add(local.getRemote_id());
        }

        if (proseList != null && locals.size() < proseList.size()) {
            for (GuerrillaProse remote : proseList) {
                if (!localRemoteIds.contains(remote.getRemote_id())) {
                    remote.setShared(true);
                    remote.setId(null);
                    proseController.setLocalProse(remote, false);
                }
            }
        }
        proseController.getLocalProse();
    }


    @UiThread
    @Override
    public void onRemoteProseReceived(List<GuerrillaProse> proseList) {
        app.setRemoteProses(new ArrayList<GuerrillaProse>(proseList));

        // Reset remote tags in app
        ArrayList<Tag> tags = proseController.getTags(proseList);
        app.setRemoteTags(tags);

        this.refreshSectionsPagerAdapter();
    }



    // Unused methods
    @UiThread
    @Override
    public void onRemoteProseReceived(GuerrillaProse prose) {
        // React here to special remote prose requests
        // For Security
        if (!prose.getShared()) {
            prose.setShared(true);
        }
    }

    @Override
    public void onRemoteProseByTagReceived(List<GuerrillaProse> proseList) {
        for (GuerrillaProse prose : proseList) {
            // For Security
            if (!prose.getShared()) {
                prose.setShared(true);
            }
        }
    }

    @Override
    public void onLocalProseReceived(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseByTagReceived(List<GuerrillaProse> prose) {

    }

    /**
     * Flickr Callback methods
     */
    @Override
    public void onFlickrDataReceived(FlickrJson photoData) {
        if (photoData != null) {
            flickrManager.getFlickrAuthor(photoData);
        }
    }

    @Override
    public void onFlickrAuthorReceived(FlickrJson photoData, String author) {
        flickrManager.getFlickrPhoto(photoData, app.isMobileNetwork());
        app.setImageFromFlickr(true);
        app.setTitleImageAuthor(author);
    }

    @Override
    public void onFlickrPhotoReceived(Bitmap bitmap) {
        app.setTitleImage(bitmap);
        this.refreshSectionsPagerAdapter();
    }



    // React on configuration changes
    // reloadSectionPagerAdapter as the Fragment
    // objects are destroyed when the configuration / orientation changes
    // --> setRetainInstance(true) in childFragmentManager not possible
    // Also enter in Manifest for corresponding Activity
    // android:configChanges="orientation|keyboardHidden|screenSize"
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.refreshSectionsPagerAdapter();
    }


    @UiThread
    @Override
    public void onRemoteMediaSet(Long mediaId) {
        GuerrillaProse prose = app.getGuerrillaProse();
        if (mediaId != null) {
            prose.setRemote_media_id(mediaId);
            proseController.setRemoteProse(prose);
        } else {
            this.onRemoteMediaCanceled();
        }
    }

    @UiThread
    @Override
    public void onRemoteMediaUpdated(Long mediaId) {
        GuerrillaProse prose = app.getGuerrillaProse();
        if (mediaId != null && mediaId != -1L) {
            proseController.updateRemoteProse(prose);
        } else {
            Log.e("MAIN_ACTIVITY", "updating media resulted in error. Updating prose anyway");
            proseController.updateRemoteProse(prose);
        }
    }

    @UiThread
    @Override
    public void onRemoteMediaCanceled() {
        this.onNewProse(app.getGuerrillaProse(), false);
        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_LONG).show();
    }

    @UiThread
    @Override
    public void onRemoteMediaStringReceived(String base64, Long remoteMediaId) {
        GuerrillaProse prose = app.getGuerrillaProse();
        Media media = mediaController.getMedia(prose.getMedia_id());

        Uri uri = mediaController.getMediaFileUri();
        Bitmap bitmap = mediaController.base64ToBitmap(base64);
        mediaController.storeImage(new File(uri.getPath()), bitmap, 100);
        media.setUrl(uri.getPath());
        media.setMedia_author(app.getTitleImageAuthor());

        media.setId(null);
        mediaController.setMedia(media);
        prose.setMedia(media);

        proseController.updateLocalProse(prose);
    }

    @Override
    public void onRemoteMediaReceived(Media media) {

    }

    @Override
    public void onLocalMediaSet(Long mediaId) {

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


    private android.os.Handler timeHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mAlreadyShownCount > 0) {
                mAlreadyShownCount = 0;
            }
        }
    };

    // Override onBackPressed for reacting inside
    // the FragmentStatePagerAdapter to these events
    @Override
    public void onBackPressed() {
        if (this.mLastBackPressTime < System.currentTimeMillis() - 1500) {

            if (mAlreadyShownCount == 0) {
                timeHandler.sendEmptyMessageDelayed(1, BACK_PRESS_RESET);
                mAlreadyShownCount++;
            } else if (mAlreadyShownCount < 5) {
                Toast.makeText(this, getString(R.string.close_app), Toast.LENGTH_SHORT).show();
                mAlreadyShownCount++;
            }

            this.mLastBackPressTime = System.currentTimeMillis();
            for (OnBackPressedListener listener : mOnBackPressedListenerList) {
                try {
                    listener.onBackPressed();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            // If user presses again the app closes
        } else {
            super.onBackPressed();
        }
    }

    public void setOnBackPressedListener(ContentListFragment fragment) {
        mOnBackPressedListenerList.add(fragment);
    }

    @Override
    public void onUserChange(Guerrilla user) {

    }

    @Override
    public void onRemoteUserSet(Guerrilla user) {

    }

    @Override
    public void onRemoteUserReceived(Guerrilla user) {

    }

    @Override
    public void onRemoteUserUpdated(Guerrilla user) {

    }

    @Override
    public void onRemoteUserDeleted(Long id) {

    }

    @Override
    public void onLocalUserSet(Guerrilla user) {

    }

    @Override
    public void onLocalUserUpdated(Guerrilla user) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onPasswordVerified(Guerrilla user) {

    }

    @Override
    public void onLoggedIn(String isLoggedIn) {
        Guerrilla user = userController.getLocalUser();
        user.setRemember_token(isLoggedIn);
        userController.updateLocalUser(user);
        Log.d("MAINACTIVITY_LOGIN", "remember_token: " + isLoggedIn);
    }
}
