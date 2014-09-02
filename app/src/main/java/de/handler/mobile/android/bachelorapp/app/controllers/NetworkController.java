package de.handler.mobile.android.bachelorapp.app.controllers;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Message;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.rest.RestService;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;

/**
 * Provides the network related methods
 */
@EBean
public class NetworkController {

    @SystemService
    ConnectivityManager connectivityManager;

    @SystemService
    WifiManager wifi;

    @RestService
    GuerrillaService guerrillaService;

    @Bean
    RestServiceErrorHandler restErrorHandler;

    @App
    BachelorApp app;

    public static final int PING_TIME = 8000;
    private AsyncTask<Void, Void, Boolean> mAsyncTask;


    @AfterInject
    void initRestErrorHandler() {
        guerrillaService.setRestErrorHandler(restErrorHandler);
    }

    public void checkNetworkState() {

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            app.setAppConnected(activeNetwork.isConnected());
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                app.setWifiNetwork(true);
            } else {
                app.setWifiNetwork(false);
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                app.setMobileNetwork(true);
            } else {
                app.setMobileNetwork(false);
            }
        } else {
            app.setAppConnected(false);
            app.setMobileNetwork(false);
            app.setWifiNetwork(false);
        }
    }

    private android.os.Handler timeHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mAsyncTask != null) {
                mAsyncTask.cancel(true);
                app.setServerOnline(false);
            }
        }
    };

    public void pingServer() {

        timeHandler.sendEmptyMessageDelayed(1, PING_TIME);
        mAsyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
                protected Boolean doInBackground(Void... params) {
                    return guerrillaService.pingServer();
                }

                @Override
                protected void onPostExecute(Boolean isOnline) {
                    super.onPostExecute(isOnline);
                    timeHandler.removeMessages(1);

                    if (isOnline != null) {
                        app.setServerOnline(isOnline);
                    } else {
                        app.setServerOnline(false);
                    }
                }
        }.execute();
    }
}
