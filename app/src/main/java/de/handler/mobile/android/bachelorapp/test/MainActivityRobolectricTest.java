
package de.handler.mobile.android.bachelorapp.test;

import android.support.v7.app.ActionBarActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;


/**
 * Nino Handler
 * BachelorApp
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityRobolectricTest {


    @Before
    public void setup() {

    }


    @Test
    public void testFullActivityLifecycle() {
        ActionBarActivity activity = Robolectric.buildActivity(MainActivity_.class)
                .create()
                .start()
                .resume()
                .visible()
                .get();
        Assert.assertNotNull(activity);
    }
}
