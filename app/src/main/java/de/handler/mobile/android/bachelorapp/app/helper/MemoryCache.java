package de.handler.mobile.android.bachelorapp.app.helper;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

import org.androidannotations.annotations.EBean;

import de.handler.mobile.android.bachelorapp.app.database.Media;

/**
 * Stores already downloaded bitmaps in a memory cache that they do not have to be downloaded again during session
 */
@EBean
public class MemoryCache implements ImageLoader.ImageCache {
    private LruCache<String, Media> mMediaMemoryCache;

    public MemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/2th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMediaMemoryCache = new LruCache<String, Media>(cacheSize);
    }

    public Media getMedia(String key) {
        return mMediaMemoryCache.get(key);
    }

    public void putMedia(String key, Media media) {
        if (this.getMedia(key) == null) {
            mMediaMemoryCache.put(key, media);
        }
    }

    @Override
    public Bitmap getBitmap(String url) {
        return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {

    }
}