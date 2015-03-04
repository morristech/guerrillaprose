package de.handler.mobile.android.bachelorapp.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.handler.mobile.android.bachelorapp.app.controllers.MediaTypeController;
import de.handler.mobile.android.bachelorapp.app.database.DaoMaster;
import de.handler.mobile.android.bachelorapp.app.database.DaoSession;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaDao;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProseDao;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaDao;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.database.MediaTypeDao;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

/**
 * Tests if a guerrilla prose is correctly stored, updated and deleted
 */
@Config(emulateSdk = 18)
@RunWith(de.handler.mobile.android.bachelorapp.app.RobolectricGradleTestRunner.class)
public class TestLocalGuerrillaProseLifecycle {

    private Long userId;

    private GuerrillaDao userDao;
    private MediaDao mediaDao;
    private MediaTypeDao mediaTypeDao;
    private GuerrillaProseDao proseDao;

    private Guerrilla guerrilla;
    private Media media;
    private MediaType mediaType;
    private GuerrillaProse prose;

    private Bitmap bitmap;

    @Before
    public void setup() throws IOException {
        Context context = Robolectric.buildActivity(MainActivity_.class).create().get();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "guerrillaprose-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        this.userDao = daoSession.getGuerrillaDao();
        this.mediaDao = daoSession.getMediaDao();
        this.mediaTypeDao = daoSession.getMediaTypeDao();
        this.proseDao = daoSession.getGuerrillaProseDao();

        guerrilla = new Guerrilla("testEmail@junit.de", "testPassword", "testSurname", "testName");
        this.userId = userDao.insert(guerrilla);

        this.mediaType = new MediaType(MediaTypeController.MEDIA_TYPE_IMAGE);
        Long mediaTypeId = mediaTypeDao.insert(mediaType);

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                context.getString(R.string.app_name));

        File mediaFile = new File(mediaStorageDir + "test");

        Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        Assert.assertNotNull(bitmap);

        FileOutputStream fos;
        fos = new FileOutputStream(mediaFile);
        boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        Assert.assertEquals(true, result);

        fos.flush();
        fos.close();

        media = new Media(mediaFile.getPath(), mediaTypeId);
    }

    @After
    public void tearDown() {
        userDao.delete(guerrilla);

        File file = new File(media.getUrl());
        boolean deleted = file.delete();

        Assert.assertTrue(deleted);
    }

    @Test
    public void testGuerrillaProseCreation() {
        GuerrillaProse prose =
                new GuerrillaProse("Title", "Text", "Tag", false,
                        Calendar.getInstance().getTime(), userId, media.getId());

        Long id = proseDao.insert(prose);
        Assert.assertFalse(id == -1L);
    }


    @Test
    public void testGuerrillaProseUpdate() {
        GuerrillaProse prose =
                new GuerrillaProse("Title", "Text", "Tag", false,
                        Calendar.getInstance().getTime(), userId, media.getId());

        proseDao.insert(prose);

        Long id = prose.getId();
        prose.setShared(true);

        proseDao.update(prose);

        Assert.assertTrue(proseDao.load(id).getShared());
        Assert.assertEquals(media.getId(), proseDao.load(id).getMedia_id());
    }



    @Test
    public void testGuerrillaProseDelete() {
        GuerrillaProse prose =
                new GuerrillaProse("Title", "Text", "Tag", false,
                        Calendar.getInstance().getTime(), userId, media.getId());

        proseDao.insert(prose);

        proseDao.delete(prose);
        Assert.assertNull(proseDao.load(prose.getId()));
    }

}
