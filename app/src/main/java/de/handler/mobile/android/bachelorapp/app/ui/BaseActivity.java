package de.handler.mobile.android.bachelorapp.app.ui;

import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.Window;
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.NetworkController;
import de.handler.mobile.android.bachelorapp.app.interfaces.AppPreferences_;

/**
 * Base Activity Class containing everything used by everyone
 */
@EActivity
public abstract class BaseActivity extends ActionBarActivity {

    @Bean
    NetworkController networkController;


    // Shared Preferences as defined
    // in the AppPreferences Interface
    @Pref
    AppPreferences_ prefs;

    // The global app object keeping
    // the application's context
    @App
    BachelorApp app;

    // Initialize the NavigationDrawer
    @ViewById(R.id.main_drawer_list)
    ListView drawerList;

    @ViewById(R.id.main_drawer_layout)
    DrawerLayout mDrawerLayout;


    /**
     * Network receiver
     * Checks if connectivity changes and reacts to the event
     * */
    @Receiver(actions = ConnectivityManager.CONNECTIVITY_ACTION,
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void onNetworkChange() {
        networkController.checkNetworkState();
    }



    @AfterInject
    public void overlayActionBar() {
        // Request Action Bar overlay before setting content view a.k.a. before @AfterViews
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No menu by default
        return false;
    }


    void replaceFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .setTransition(android.R.animator.fade_in)
                .commit();
    }

    protected void removeFragment(Fragment fragment) {
        try {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .setTransition(android.R.animator.fade_out)
                    .commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    void addFragment(int containerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(containerId, fragment)
                .commit();
    }

    public ActionBar setupActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // make action bar transparent
        actionBar.setBackgroundDrawable(new ColorDrawable(R.color.transparent_white_80));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(R.color.transparent_white_80));
        actionBar.setSplitBackgroundDrawable(new ColorDrawable(R.color.transparent_white_80));

        // hide title bar and icon
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        return actionBar;
    }





}
