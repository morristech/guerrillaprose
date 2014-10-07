package de.handler.mobile.android.bachelorapp.app.location;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnLocationListener;

/**
 * Responsible for handling location related stuff
 */
@EBean
public class LocationHandler implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    @App
    BachelorApp app;

    OnLocationListener onDataReceivedListener;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationClient mLocationClient;
    private FragmentActivity mActivity;
    private boolean connected = false;


    public void setCallingActivity(FragmentActivity activity) {
        mActivity = activity;
        onDataReceivedListener = (OnLocationListener) activity;
    }

    public void init() {
        mLocationClient = new LocationClient(mActivity, this, this);
    }

    public void connect() {
        mLocationClient.connect();
    }

    public void disconnect() {
        mLocationClient.disconnect();
    }


    public Location getLocation() {
        if (mLocationClient != null) {
            return mLocationClient.getLastLocation();
        }
        return null;
    }

    public boolean googlePlayServicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(mActivity);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    mActivity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(mActivity.getSupportFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        if (mLocationClient != null && mLocationClient.getLastLocation() != null) {
            Log.d("Location Updates:", mLocationClient.getLastLocation().toString());
            onDataReceivedListener.onLocationReceived(mLocationClient.getLastLocation());
        } else {
            onDataReceivedListener.onLocationReceived(null);
        }
    }

    @Override
    public void onDisconnected() {
        connected = false;
        onDataReceivedListener.onLocationReceived(mLocationClient.getLastLocation());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public boolean isConnected() {
        return connected;
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    public String getLocationString(Location currentLocation) {

        if (currentLocation != null
                && currentLocation.getLatitude() != 0
                && currentLocation.getLongitude() != 0) {


            Geocoder geocoder =
                    new Geocoder(mActivity, Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses;
            try {

                addresses = geocoder.getFromLocation(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("Berlin");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(currentLocation.getLatitude()) +
                        " , " +
                        Double.toString(currentLocation.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return "Berlin";
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                // Return the text
                return String.format(
                        "%s, %s",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
            } else {
                return "Berlin";
            }
        }
        else return "Berlin";
    }

}
