package de.handler.mobile.android.bachelorapp.app.interfaces;

import android.location.Location;

/**
 * Informs implementing app about location updates
 */
public interface OnLocationListener {
    public void onLocationReceived(Location lastLocation);
}
