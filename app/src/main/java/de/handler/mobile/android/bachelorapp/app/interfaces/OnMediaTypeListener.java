package de.handler.mobile.android.bachelorapp.app.interfaces;

import java.util.List;

import de.handler.mobile.android.bachelorapp.app.database.MediaType;

/**
 * Listens for media type changes
 * Only to be used on first app start
 */
public interface OnMediaTypeListener {

    public void onMediaTypeSet(MediaType mediaType);
    public void onMediaTypesReceived(List<MediaType> types);
}
