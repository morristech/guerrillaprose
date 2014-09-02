package de.handler.mobile.android.bachelorapp.app.api.flickr;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ResourceHttpMessageConverter;

/**
 * Gets actual picture from Flickr server
 * considering its server location and identification
 * The following are the available sizes:
 * s	kleines Quadrat 75 x 75
 * q	large square 150x150
 * t	Thumbnail, 100 an der Längsseite
 * m	klein, 240 an der Längsseite
 * n	small, 320 on longest side
 * -	klein, 500 an der Längsseite
 * z	Mittel 640, 640 an der längsten Seite
 * c	Mittel 800, 800 an der längsten Seite†
 * b	Groß, 1024 an der längsten Seite*
 * o	Originalbild, entweder JPG, GIF oder PNG, je nach Quellformat
 */
@Rest(converters = {ResourceHttpMessageConverter.class})
public interface FlickrPhotoRestService extends RestClientErrorHandling {
    @Get("https://farm{farm}.static.flickr.com/{server}/{id}_{secret}_b.jpg")
    @Accept(MediaType.IMAGE_JPEG)
    ResponseEntity<Resource> getFlickrPhoto(int farm, String server, long id, String secret);

    @Get("https://farm{farm}.static.flickr.com/{server}/{id}_{secret}_z.jpg")
    @Accept(MediaType.IMAGE_JPEG)
    ResponseEntity<Resource> getSmallFlickrPhoto(int farm, String server, long id, String secret);

}
