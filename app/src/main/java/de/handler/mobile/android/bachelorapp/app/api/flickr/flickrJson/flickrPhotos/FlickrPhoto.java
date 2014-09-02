package de.handler.mobile.android.bachelorapp.app.api.flickr.flickrJson.flickrPhotos;

/**
 * Actual data needed from Flickr servers
 * for retrieving image from servers
 */
public class FlickrPhoto {
    private long id;
    private String owner;
    private String secret;
    private String server;
    private int farm;
    private String title;
    private int ispublic;
    private int isfamily;
    private int isfriend;

    public long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public int getFarm() {
        return farm;
    }

    public String getTitle() {
        return title;
    }

    public int getIspublic() {
        return ispublic;
    }

    public int getIsfamily() {
        return isfamily;
    }

    public int getIsfriend() {
        return isfriend;
    }

}
