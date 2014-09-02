package de.handler.mobile.android.bachelorapp.app.api.flickr.flickrJson;


import de.handler.mobile.android.bachelorapp.app.api.flickr.flickrJson.flickrPhotos.FlickrPhoto;

/**
 * Keeps an array of requested information
 * instance variables have to exist for rest request
 * even if not needed
 */
public class FlickrPhotos {
    private int page;
    private long pages;
    private int perpage;
    private long total;
    private FlickrPhoto[] photo;

    public FlickrPhoto[] getPhoto() {
        return photo;
    }


}
