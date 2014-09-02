package de.handler.mobile.android.bachelorapp.app.interfaces;

import java.util.List;

import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;

/**
 * Interface for communication between Fragments and Activity
 * Used for GuerrillaProse transfer
 */
public interface OnProseListener {
    public void onNewProse(GuerrillaProse prose, boolean sharedUpdated);

    public void onRemoteProseSet(GuerrillaProse prose);
    public void onLocalProseSet(GuerrillaProse prose);

    public void onRemoteProseUpdated(GuerrillaProse prose);
    public void onLocalProseUpdated(GuerrillaProse prose);

    public void onLocalProseDeleted(Long remoteMediaId, boolean shared);
    public void onRemoteProseDeleted();

    public void onRemoteProseReceived(GuerrillaProse prose);
    public void onLocalProseReceived(GuerrillaProse prose);


    public void onRemoteProseByTagReceived(List<GuerrillaProse> prose);
    public void onLocalProseByTagReceived(List<GuerrillaProse> prose);

    public void onRemoteProseReceived(List<GuerrillaProse> prose);
    public void onLocalProseReceived(List<GuerrillaProse> prose);

    public void onRemoteProseByUserReceived(List<GuerrillaProse> proseList);
}
