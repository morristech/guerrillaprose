package de.handler.mobile.android.bachelorapp.app.interfaces;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Definition of app specific SharedPreferences for Android Annotations
 */
@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface AppPreferences {

    @DefaultBoolean(true)
    boolean firstStart();
}
