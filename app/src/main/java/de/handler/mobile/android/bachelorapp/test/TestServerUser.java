package de.handler.mobile.android.bachelorapp.test;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService_;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

/**
 * Tests Server User Communication
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class TestServerUser {

    private GuerrillaService_ guerrillaService;
    private Guerrilla testUser;

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

        Assert.assertNotEquals(Long.valueOf(-1L), testUser.getId());

        guerrillaService.login(testUser.getEmail(), testUser.getPassword());
    }


    @After
    public void tearDown() {
        guerrillaService.deleteUser(testUser.getId());
        guerrillaService.logout(testUser.getEmail());
    }


    @Test
    public void testSetUser() {
        Assert.assertNotNull(testUser.getId());
    }


    @Test
    public void testUpdateUser() {

        testUser = guerrillaService.updateUser(
                testUser.getId(),
                testUser.getEmail(),
                "AlteredTestSurname",
                testUser.getName());

        Assert.assertNotEquals(Long.valueOf(-1L), testUser.getId());
    }


    @Test
    public void testGetUsers() {
        Assert.assertNotNull(guerrillaService.getUsers());
    }


    @Test
    public void testGetUser() {
        Assert.assertNotNull(guerrillaService.getUser(testUser.getEmail()));
    }

}
