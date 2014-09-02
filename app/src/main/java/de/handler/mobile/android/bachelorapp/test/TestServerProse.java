package de.handler.mobile.android.bachelorapp.test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService_;
import de.handler.mobile.android.bachelorapp.app.database.DaoMaster;
import de.handler.mobile.android.bachelorapp.app.database.DaoSession;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProseDao;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

/**
 * Tests Server Prose Communication
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class TestServerProse {

    private GuerrillaService_ guerrillaService;
    private Guerrilla testUser;
    private GuerrillaProse testProse;
    private GuerrillaProseDao mProseDao;
    private MediaType testMediaType;

    @Before
    public void setup() {
        Context context = Robolectric.buildActivity(MainActivity_.class).create().get();

        guerrillaService = new GuerrillaService_(context);

        testUser = new Guerrilla("testUser@web.de", "testPassword", "testName", "testSurname");
        testUser = guerrillaService.setUser(
                testUser.getEmail(),
                testUser.getPassword(),
                testUser.getSurname(),
                testUser.getName());

        guerrillaService.login(testUser.getEmail(), testUser.getPassword());

        Long mediaTypeId = guerrillaService.setMediaType("test");
        testMediaType = new MediaType(mediaTypeId, "test");

        testProse = new GuerrillaProse("testTag", "testTitle", "testText", true,
                Calendar.getInstance().getTime(), null, testUser.getId());
        testProse = guerrillaService.setProse("testTag", "testTitle", "testText",
                mediaTypeId, 0L, testUser.getId(), "samson");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "guerrillaprose-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        mProseDao = daoSession.getGuerrillaProseDao();

        mProseDao.insertOrReplace(testProse);
    }


    @After
    public void tearDown() {
        guerrillaService.deleteUser(testUser.getId());
        guerrillaService.deleteProse(testProse.getId());
        guerrillaService.deleteMediaType(testMediaType.getMedia_type());
        guerrillaService.logout(testUser.getEmail());

        mProseDao.delete(testProse);
    }


    @Test
    public void testSetProse() {
        Assert.assertNotEquals(Long.valueOf(-1L), testProse.getId());
    }


    @Test
    public void testUpdateProse() {

        testProse = guerrillaService.updateProse(
                testProse.getRemote_id(),
                testProse.getTag(),
                "changedProseTitle",
                testProse.getText(),
                testProse.getMedia_id(),
                testProse.getRemote_media_id(),
                testProse.getUser_id(),
                "samson");

        testProse.setTitle("changedProseTitle");
        mProseDao.update(testProse);

        Assert.assertNotEquals(Long.valueOf(-1L), testProse.getId());
        Assert.assertEquals(mProseDao.load(testProse.getId()).getTitle(),
                guerrillaService.getProse(testProse.getId()).getTitle());

    }


    @Test
    public void testGetProses() {
        Assert.assertNotNull(guerrillaService.getProses());
    }


    @Test
    public void testGetProse() {
        Assert.assertNotNull(guerrillaService.getProse(testProse.getId()));
    }

}
