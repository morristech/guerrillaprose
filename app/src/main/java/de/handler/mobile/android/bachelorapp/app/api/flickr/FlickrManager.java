package de.handler.mobile.android.bachelorapp.app.api.flickr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;
import de.handler.mobile.android.bachelorapp.app.api.flickr.flickrAuthor.FlickrAuthor;
import de.handler.mobile.android.bachelorapp.app.api.flickr.flickrJson.flickrPhotos.FlickrPhoto;
import de.handler.mobile.android.bachelorapp.app.controllers.Tag;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnFlickrListener;

/**
 * Accumulates methods for the Flickr API
 * Flickr image retrieval is organized in 3 steps:
 * 1. get data (where to find the image, on which server, information about the author, etc.)
 * 2. get author for image
 * 3. get actual image
 */
@EBean
public class FlickrManager {

    @RestService
    FlickrDataRestService flickrDataRestService;

    @RestService
    FlickrPhotoRestService flickrPhotoRestService;

    @Bean
    RestServiceErrorHandler errorHandler;

    @App
    BachelorApp app;


    ArrayList<OnFlickrListener> listeners = new ArrayList<OnFlickrListener>();


    @AfterInject
    void prepareRestHandlers() {
        // Do not set the Context as errors from flickr servers are not
        // important enough for Toasts
        flickrDataRestService.setRestErrorHandler(errorHandler);
        flickrPhotoRestService.setRestErrorHandler(errorHandler);
    }

    public void addListener(OnFlickrListener listener) {
        listeners.add(listener);
    }

    /**
     * Get the information data for a tag
     */
    @Background
    public void getFlickrData(Tag tag) {
        app.setImageFromFlickr(true);
        FlickrJson data = flickrDataRestService.getFlickrImage(tag.getTag());
        for (OnFlickrListener listener : listeners) {
            listener.onFlickrDataReceived(data);
        }
    }


    /**
     * Get the photo data
     */
    @Background
    public void getFlickrPhoto(FlickrJson photoData, boolean isMobile) {
        Bitmap bitmap = null;
        if (photoData != null && photoData.getPhotos() != null) {

            for (FlickrPhoto photo : photoData.getPhotos().getPhoto()) {
                ResponseEntity<Resource> response;

                if (isMobile) {
                    // If only the mobile network is available take a small image
                    response = flickrPhotoRestService.getSmallFlickrPhoto(
                            photo.getFarm(),
                            photo.getServer(),
                            photo.getId(),
                            photo.getSecret());
                } else {
                    // If only wifi is available get a bigger one
                    response = flickrPhotoRestService.getFlickrPhoto(
                            photo.getFarm(),
                            photo.getServer(),
                            photo.getId(),
                            photo.getSecret());
                }

                try {
                    bitmap = BitmapFactory.decodeStream(response.getBody().getInputStream());

                    app.setTitleImage(bitmap);
                    app.setImageFromFlickr(true);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }


        for (OnFlickrListener listener : listeners) {
            listener.onFlickrPhotoReceived(bitmap);
        }
    }


    /**
     * Get the author to the image
     */
    @Background
    public void getFlickrAuthor(FlickrJson photoData) {
        try {
            FlickrPhoto flickrPhoto = photoData.getPhotos().getPhoto()[0];
            FlickrAuthor author = flickrDataRestService.getAuthor(flickrPhoto.getOwner(), flickrPhoto.getId());

            String name = "";
            if (author.photo.owner.realname != null && !author.photo.owner.realname.equals("")) {
                name = author.photo.owner.realname;
            } else if (author.photo.owner.username != null) {
                name = author.photo.owner.username;
            }

            app.setTitleImageAuthor(name);
            for (OnFlickrListener listener : listeners) {
                listener.onFlickrAuthorReceived(photoData, name);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            for (OnFlickrListener listener : listeners) {
                listener.onFlickrAuthorReceived(photoData, "");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            for (OnFlickrListener listener : listeners) {
                listener.onFlickrAuthorReceived(photoData, "");
            }
        }
    }
}
