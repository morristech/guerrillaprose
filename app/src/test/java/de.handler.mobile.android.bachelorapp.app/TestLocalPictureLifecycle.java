package de.handler.mobile.android.bachelorapp.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

/**
 * Tests if a picture is correctly created and deleted
 */
@Config(emulateSdk = 18)
@RunWith(de.handler.mobile.android.bachelorapp.app.RobolectricGradleTestRunner.class)
public class TestLocalPictureLifecycle {

    private File mMediaFile;



    @Before
    public void setup() throws IOException {
        Context context = Robolectric.buildActivity(MainActivity_.class).create().get();

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getString(R.string.app_name));

        mMediaFile = new File(mediaStorageDir + "test");

        Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        Assert.assertNotNull(bitmap);

        FileOutputStream fos;
        fos = new FileOutputStream(mMediaFile);
        boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        Assert.assertEquals(true, result);

        fos.flush();
        fos.close();
    }

    @Test
    public void testSaveImage() throws IOException {
        Assert.assertEquals(true, mMediaFile.exists());
    }


    @Test
    public void testDeleteImage() throws IOException {
        Assert.assertEquals(true, mMediaFile.delete());
    }

}
