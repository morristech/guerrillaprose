package de.handler.mobile.android.bachelorapp.app.api.flickr;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import de.handler.mobile.android.bachelorapp.app.api.flickr.flickrAuthor.FlickrAuthor;

/**
 * Manages the primary communication between Flickr API and App
 * And is the base class for all new flickr API functions
 */
@Rest(
        rootUrl = "https://api.flickr.com/services/rest/?api_key=6ad15c797585b5eb0ca266f9e9cb73ac&format=json&nojsoncallback=1",
        converters = {GsonHttpMessageConverter.class})
public interface FlickrDataRestService extends RestClientErrorHandling {

    @Get("&method=flickr.de.handler.mobile.android.guerrillaprose.test.echo&name=value&per_page=1")
    FlickrJson getFlickrTestImage();

    @Get("&method=flickr.photos.search&tags={tags}&per_page=1")
    FlickrJson getFlickrImage(String tags);

    @Get("&method=flickr.photos.getInfo&photo_id={imageId}&user_id={userToken}")
    FlickrAuthor getAuthor(String userToken, long imageId);
}
