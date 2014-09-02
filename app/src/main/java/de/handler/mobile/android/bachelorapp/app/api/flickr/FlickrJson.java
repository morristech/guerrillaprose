package de.handler.mobile.android.bachelorapp.app.api.flickr;


import de.handler.mobile.android.bachelorapp.app.api.flickr.flickrJson.FlickrPhotos;

/**
 * Contains the Image from Flickr Rest API
 */
public class FlickrJson {
    FlickrPhotos photos;
    String stat;

    public FlickrPhotos getPhotos() {
        return photos;
    }
}
