package de.handler.mobile.android.bachelorapp.app.interfaces;

import de.handler.mobile.android.bachelorapp.app.database.Media;

/**
 * Interface for communication between Fragments and Activity
 * Used for Media transfer
 */
public interface OnMediaListener {
    public void onRemoteMediaStringReceived(String base64, Long mediaId);
    public void onRemoteMediaReceived(Media media);
    public void onRemoteMediaSet(Long mediaId);
    public void onRemoteMediaUpdated(Long mediaId);
    public void onRemoteMediaCanceled();
    public void onLocalMediaSet(Long mediaId);
}
