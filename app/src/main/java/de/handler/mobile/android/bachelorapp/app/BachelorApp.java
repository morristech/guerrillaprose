package de.handler.mobile.android.bachelorapp.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

import org.androidannotations.annotations.EApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.controllers.Tag;
import de.handler.mobile.android.bachelorapp.app.database.DaoMaster;
import de.handler.mobile.android.bachelorapp.app.database.DaoSession;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.helper.BitmapCache;

/**
 * Application Object for globally used data
 */
@EApplication
public class BachelorApp extends Application {

    private boolean imageFromCam = false;
    private boolean isAppConnected = false;
    private boolean isWifiNetwork = false;
    private boolean isMobileNetwork = false;
    private boolean isServerOnline = false;
    private boolean isLoggedIn = false;
    private boolean isImageFromFlickr = false;

    private Uri tempImageUri;
    private Uri lastFileUri;

    private Bitmap titleImage;
    private String titleImageAuthor;
    private GuerrillaProse guerrillaProse;
    private Media currentMedia = null;

    private ArrayList<Media> pendingMedia = new ArrayList<Media>();
    private ArrayList<Tag> localTags = new ArrayList<Tag>();
    private ArrayList<Tag> remoteTags = new ArrayList<Tag>();

    private ArrayList<GuerrillaProse> remoteProses = new ArrayList<GuerrillaProse>();
    private List<String> pagerAuthors = new ArrayList<String>();

    private ImageLoader imageLoader;
    private ImageLoader.ImageCache bitmapCache;
    private android.support.v4.util.LruCache<String, Media> mediaCache;

    private DaoSession mDaoSession;


    /**
     * Functions necessary due to camera workaround
     */
    public boolean isImageFromCam() {
        return this.imageFromCam;
    }

    public void setImageFromCam(boolean imageFromCam) {
        this.imageFromCam = imageFromCam;
    }

    public Uri getTempImageUri() {
        return this.tempImageUri;
    }

    public void setTempImageUri(Uri tempImageUri) {
        this.tempImageUri = tempImageUri;
    }


    /**
     * Function necessary due too image size
     */
    public void setTitleImage(Bitmap mTitleImage) {
        if (mTitleImage != null) {
            this.titleImage = mTitleImage;
        }
    }

    public Bitmap getTitleImage() {
        return titleImage;
    }

    public ArrayList<Media> getPendingMedia() {
        return pendingMedia;
    }

    public void setPendingMedia(ArrayList<Media> pendingMedia) {
        this.pendingMedia = pendingMedia;
    }


    /**
     * Connectivity
     */
    public boolean isAppConnected() {
        return isAppConnected;
    }

    public void setAppConnected(boolean isConnected) {
        this.isAppConnected = isConnected;
    }

    public boolean isWifiNetwork() {
        return isWifiNetwork;
    }

    public void setWifiNetwork(boolean isWifiNetwork) {
        this.isWifiNetwork = isWifiNetwork;
    }

    public boolean isMobileNetwork() {
        return isMobileNetwork;
    }

    public void setMobileNetwork(boolean isMobileNetwork) {
        this.isMobileNetwork = isMobileNetwork;
    }


    /**
     * Transfer data between Activities and Fragments that is too big
     */
    public ArrayList<Tag> getLocalTags() {
        return localTags;
    }

    public void setLocalTags(ArrayList<Tag> localTags) {
        Collections.sort(localTags, new Sorter());
        this.localTags = localTags;
    }

    public ArrayList<GuerrillaProse> getRemoteProses() {
        return remoteProses;
    }

    public void setRemoteProses(ArrayList<GuerrillaProse> remoteProses) {
        this.remoteProses = remoteProses;
    }

    public ArrayList<Tag> getRemoteTags() {
        return remoteTags;
    }

    public void setRemoteTags(ArrayList<Tag> remoteTags) {
        Collections.sort(remoteTags, new Sorter());
        this.remoteTags = remoteTags;
    }

    public boolean isServerOnline() {
        return isServerOnline;
    }

    public void setServerOnline(boolean isOnline) {
        this.isServerOnline = isOnline;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public GuerrillaProse getGuerrillaProse() {
        return guerrillaProse;
    }

    public void setGuerrillaProse(GuerrillaProse guerrillaProse) {
        this.guerrillaProse = guerrillaProse;
    }

    public void openDaoSession() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "guerrillaprose-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public String getTitleImageAuthor() {
        return titleImageAuthor;
    }

    public void setTitleImageAuthor(String titleImageAuthor) {
        this.titleImageAuthor = titleImageAuthor;
    }

    public boolean isImageFromFlickr() {
        return isImageFromFlickr;
    }

    public void setImageFromFlickr(boolean isImageFromFlickr) {
        this.isImageFromFlickr = isImageFromFlickr;
    }

    public Uri getLastFileUri() {
        return lastFileUri;
    }

    public void setLastFileUri(Uri lastFileUri) {
        this.lastFileUri = lastFileUri;
    }

    public List<String> getPagerAuthors() {
        return pagerAuthors;
    }

    public void addPagerAuthor(String pagerAuthor) {
        this.pagerAuthors.add(pagerAuthor);
    }

    public void clearSession() {
        mDaoSession.clear();
    }


    public Media getCurrentMedia() {
        return currentMedia;
    }

    public void setCurrentMedia(Media currentMedia) {
        this.currentMedia = currentMedia;
    }


    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void initImageLoader() {
        imageLoader = new ImageLoader(BitmapCache.newRequestQueue(this), bitmapCache);
    }


    // Cache
    public void initBitmapCache() {
        bitmapCache = new BitmapCache();
    }

    public LruCache<String, Media> getMediaCache() {
        return mediaCache;
    }

    public void initMediaCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4 th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mediaCache = new LruCache<String, Media>(cacheSize);
    }


    public static class Sorter implements Comparator<Tag> {

        @Override
        public int compare(Tag first, Tag second) {
            return  (first.getCount() > second.getCount() ? -1 :
                        (first.getCount() == second.getCount() ? 0 : 1)
                    );
        }
    }
}
