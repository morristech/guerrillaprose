package de.handler.mobile.android.bachelorapp.app.interfaces;

import android.graphics.Bitmap;

import de.handler.mobile.android.bachelorapp.app.api.flickr.FlickrJson;

/**
 * Handles Data Flickr Transfers between two classes
 */
public interface OnFlickrListener {
    public void onFlickrDataReceived(FlickrJson photoData);
    public void onFlickrAuthorReceived(FlickrJson photoData, String author);
    public void onFlickrPhotoReceived(Bitmap bitmap);
}
