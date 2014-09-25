package de.handler.mobile.android.bachelorapp.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.api.flickr.FlickrJson;
import de.handler.mobile.android.bachelorapp.app.api.flickr.FlickrManager;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.controllers.NetworkController;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.controllers.Tag;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.interfaces.AppPreferences_;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnFlickrListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;

/**
 * Initiates the application and shows a initial picture as long as it takes
 */
@EActivity(R.layout.activity_splash)
public class SplashActivity extends Activity implements OnFlickrListener, OnProseListener, View.OnClickListener {

    @Bean
    ProseController proseController;

    @Bean
    NetworkController networkController;

    @Bean
    MediaController mediaController;

    @Bean
    FlickrManager flickrManager;

    @Pref
    AppPreferences_ prefs;


    @App
    BachelorApp app;

    @ViewById(R.id.splash_error_container)
    RelativeLayout mErrorContainer;

    @ViewById(R.id.splash_standard_container)
    LinearLayout mStandardContainer;

    @ViewById(R.id.splash_reload_button)
    ImageButton mReloadButton;

    @ViewById(R.id.progress_bar)
    ProgressBar mProgressBar;

    @ViewById(R.id.progress_bar_round)
    ProgressBar mProgressBarRound;


    @ViewById(R.id.splashTitle_text)
    TextView mTitleText;

    @ViewById(R.id.splash_status_textview)
    TextView mStatusText;

    @ViewById(R.id.splash_network_textview)
    TextView mNetworkText;


    private int mProgress = 0;
    private final Context mFinalContext = this;


    @AfterInject
    void initBeans() {
        flickrManager.addListener(this);
        proseController.addListener(this);
    }

    @AfterViews
    void init() {
        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        mTitleText.setTypeface(myTypeface);
        mStatusText.setTypeface(myTypeface);
        mNetworkText.setTypeface(myTypeface);

        // init dao session for app context as recommended by green dao
        app.openDaoSession();

        // init cache
        app.initBitmapCache();
        app.initMediaCache();

        // init image loader for volley
        app.initImageLoader();

        networkController.checkNetworkState();

        // Get actual phone's API level and show round progress bar
        // if it is higher than 10. Before 11 the round progress bar
        // shows ugly artifacts
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mProgressBarRound.setVisibility(View.VISIBLE);
        } else {
            mProgressBarRound.setVisibility(View.GONE);
        }

        // Set the maximum progress for the horizontal progress bar.
        // It is updated in the OnDataReceivedListener's overridden
        // method onStatusUpdate
        int fullProgress;
        if (app.isMobileNetwork()) {
            fullProgress = 2;
        } else {
            fullProgress = 4;
        }
        mProgressBar.setMax(fullProgress);

        if (app.isAppConnected()) {
            networkController.pingServer();

            if (app.isMobileNetwork()) {
                this.onMobileNetwork();
            } else {
                this.getFlickrData();
            }
        } else {
            this.showNetworkInactive();
        }
    }

    @Background
    public void onMobileNetwork() {
        // Sleep the time the server ping needs to answer
        try {
            Thread.sleep(NetworkController.PING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
        app.setTitleImage(bitmap);
        app.setImageFromFlickr(false);

        mProgress++;
        mProgressBar.setProgress(mProgress);


        if (app.isServerOnline()) {
            this.getGuerrillaData();

        // Start Offline Functionality
        } else {
            mProgress++;
            mProgressBar.setProgress(mProgress);

            this.showInactiveServerToast();
            proseController.getLocalProse();
        }
    }


    private void showNetworkInactive() {
        mStandardContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        mReloadButton.setOnClickListener(this);
    }


    // On Click Listener for reload button
    // Activates wifi and shows progress bar until
    // network receiver gets a connection and the
    // app has finished loading flickr and guerrilla data
    @Override
    public void onClick(View v) {

        if (app.isAppConnected()) {
            mErrorContainer.setVisibility(View.GONE);
            mStandardContainer.setVisibility(View.VISIBLE);

            networkController.pingServer();
            this.getFlickrData();
        }
    }


    /**
     * Network receiver
     * Checks if connectivity changes and reacts to the event
     * */
    @Receiver(actions = ConnectivityManager.CONNECTIVITY_ACTION,
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    @Background
    void onConnectionChange() {
        networkController.checkNetworkState();
        Log.i(getLocalClassName(), "NETWORKSTATECHANGE");
    }


    /**
     * Get picture data matching the most popular tag from flickr
     * If all data is received onFlickrDataReceived interface
     * is called from the flickrManager
     */
    private void getFlickrData() {
        flickrManager.getFlickrData(proseController.getMostPopularTag(app.getLocalTags()));
    }

    /**
     * One step accomplished. Augment counter
     * */
    @Override
    public void onFlickrDataReceived(FlickrJson photoData) {
        mProgress++;
        mProgressBar.setProgress(mProgress);
        flickrManager.getFlickrAuthor(photoData);
    }

    @Override
    public void onFlickrAuthorReceived(FlickrJson photoData, String author) {
        /**
         * Gets the actual image byte stream matching the data from
         * previous called getFlickrData method
         */
        mProgress++;
        mProgressBar.setProgress(mProgress);
        flickrManager.getFlickrPhoto(photoData, app.isMobileNetwork());
    }


    /**
     * One step accomplished. Augment counter
     * */
    @UiThread
    @Override
    public void onFlickrPhotoReceived(Bitmap bitmap) {
        mProgress++;
        mProgressBar.setProgress(mProgress);

        // If the guerrilla prose server is not reachable
        // immediately continue with app start
        if (app.isServerOnline()) {
            this.getGuerrillaData();
        } else {
            this.showInactiveServerToast();
            proseController.getLocalProse();
        }
    }

    @UiThread
    public void showInactiveServerToast() {
        Toast.makeText(this, getString(R.string.server_unreachbale), Toast.LENGTH_LONG).show();
    }


    /**
     * Get all guerrilla prose from server
     * or if there are more than 20 tags
     * get the prose belonging to the
     * 20 most popular tags
     */
    public void getGuerrillaData() {
        proseController.getRemoteProse();
    }

    /**
     * Remote prose received:
     * Augment progress
     * */
    @Override
    public void onRemoteProseReceived(List<GuerrillaProse> proses) {
        mProgress++;
        mProgressBar.setProgress(mProgress);

        // Get local data
        proseController.getLocalProse();
    }

    @Override
    public void onLocalProseReceived(List<GuerrillaProse> proses) {
        // If it is mobile network there is no flickr image
        if (app.isMobileNetwork()
                && proses.size() > 0
                && proses.get(0).getMedia() != null) {

            Bitmap bitmap = mediaController.getImageFromDisk(proses.get(0).getMedia().getUrl());

            if (bitmap != null) {
                app.setTitleImage(bitmap);
            }
        }

        ArrayList<Tag> tags = proseController.getTags(proses);
        app.setLocalTags(tags);

        this.startMainActivity();
    }

    @Override
    public void onRemoteProseByUserReceived(List<GuerrillaProse> proseList) {

    }


    /**
     * Start MainActivity. Everything is done and prepared
     * */
    void startMainActivity() {
        Intent intent = new Intent(this, MainActivity_.class);
        startActivity(intent);
        finish();
    }


    // This is necessary as on orientation change the asynchronous
    // tasks where always executed again due to former destruction
    // of the activity. Handling orientation changes on our own prevents that.
    // We just don't do anything on orientation change, yieha!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Unused Interface methods
     * */

    @Override
    public void onNewProse(GuerrillaProse prose, boolean serverStateChanged) {

    }

    @Override
    public void onRemoteProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseReceived(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseByTagReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onRemoteProseDeleted() {

    }

    @Override
    public void onLocalProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseReceived(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseByTagReceived(List<GuerrillaProse> prose) {

    }


    @Override
    public void onLocalProseDeleted(Long remoteProseId, boolean shared) {

    }
}
