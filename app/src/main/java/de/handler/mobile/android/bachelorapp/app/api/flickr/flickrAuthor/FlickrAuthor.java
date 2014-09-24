package de.handler.mobile.android.bachelorapp.app.api.flickr.flickrAuthor;

/**
 * Container for author
 * Used to retrieve the necessary credentials from the flickr servers
 * for copyright / license purposes for the shown picture
 */
public class FlickrAuthor {
    public AuthorPhoto photo;
    private String stat;

    public class AuthorPhoto {
        public Owner owner;

        public class Owner {
            public String realname;
            public String username;
        }
    }


}
