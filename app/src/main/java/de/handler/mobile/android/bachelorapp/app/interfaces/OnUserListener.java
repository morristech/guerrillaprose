package de.handler.mobile.android.bachelorapp.app.interfaces;

import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;

/**
 * Interface for notifying implementing activity that
 * user has entered his/her credentials
 */
public interface OnUserListener {
    public void onUserChange(Guerrilla user);

    public void onRemoteUserSet(Guerrilla user);
    public void onRemoteUserReceived(Guerrilla user);
    public void onRemoteUserUpdated(Guerrilla user);
    public void onRemoteUserDeleted(Long id);

    public void onLocalUserSet(Guerrilla user);
    public void onLocalUserUpdated(Guerrilla user);
    public void onCancel();

    public void onPasswordVerified(Guerrilla user);
    public void onLoggedIn(String isLoggedIn);
}
