package de.handler.mobile.android.bachelorapp.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService_;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests Media Server Communication
 */
@Config(emulateSdk = 18)
@RunWith(de.handler.mobile.android.bachelorapp.app.RobolectricGradleTestRunner.class)
public class TestServerMedia {

    private Guerrilla testUser;
    private GuerrillaService_ guerrillaService;
    private Media testMedia;
    private MediaType testMediaType;
    private String base64String;

    @Before
    public void setup() throws IOException {
        testUser = new Guerrilla("testUser@web.de", "testPassword", "testName", "testSurname");

        Context context = Robolectric.buildActivity(MainActivity_.class).create().get();

        guerrillaService = new GuerrillaService_(context);

        testUser = new Guerrilla("testUser@web.de", "testPassword", "testName", "testSurname");

        testUser = guerrillaService.setUser(
                testUser.getEmail(),
                testUser.getPassword(),
                testUser.getSurname(),
                testUser.getName());

        String rememberToken = guerrillaService.login(testUser.getEmail(), testUser.getPassword());


        Bitmap testImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        Long mediaTypeId = guerrillaService.setMediaType("testMediaType");
        testMediaType = new MediaType(mediaTypeId, "image");



        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getString(R.string.app_name));

        File mediaFile = new File(mediaStorageDir + "test");
        FileOutputStream fos;
        fos = new FileOutputStream(mediaFile);
        boolean result = testImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        Assert.assertEquals(true, result);
        fos.flush();
        fos.close();

        Assert.assertTrue(mediaFile.exists());

        testMedia = new Media(mediaFile.getPath(), mediaTypeId);
        //Convert binary image file to byte array to base64 encoded string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        testImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Long mediaId = guerrillaService.setMedia(testMediaType.getId(), base64String, "test");
    }


    @After
    public void tearDown() {
        guerrillaService.login(testUser.getEmail(), testUser.getPassword());
        guerrillaService.deleteMedia(testMedia.getId());
        guerrillaService.deleteUser(testUser.getId());
        guerrillaService.deleteMediaType(testMediaType.getMedia_type());
        guerrillaService.logout(testUser.getEmail());

        File file = new File(testMedia.getUrl());
        boolean deleted = file.delete();

        Assert.assertTrue(deleted);
    }


    @Test
    public void testSetMedia() {
        assertNotNull(testMedia.getId());
        assertThat(-1L, not(testMedia.getId()));
        assertThat(-2L, not(testMedia.getId()));
    }


    @Test
    public void testUpdateMedia() {

        Long id = guerrillaService.updateMedia(
                testMedia.getId(),
                base64String,
                testMedia.getMedia_type_id());

        assertThat(-1L, not(id));
        assertThat(-2L, not(id));
    }


    @Test
    public void testGetMedias() {
        assertNotNull(guerrillaService.getMedias());
    }


    @Test
    public void testGetMedia() {
        Media media = guerrillaService.getMedia(testMedia.getId());
        assertNotNull(media);
    }


    @Test
    public void testGetBase64String() {
        String base64 = guerrillaService.getMediaBase64String(testMedia.getId());
        assertThat("", not(base64));

        byte[] decodedString = Base64.decode(base64.getBytes(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);

        assertNotNull(bitmap);
    }
}
