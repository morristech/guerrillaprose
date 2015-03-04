package de.handler.mobile.android.bachelorapp.app;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService_;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests Server User Communication
 */
@Config(emulateSdk = 18)
@RunWith(de.handler.mobile.android.bachelorapp.app.RobolectricGradleTestRunner.class)
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

       assertThat(testUser.getId(), not(-1L));

        guerrillaService.login(testUser.getEmail(), testUser.getPassword());
    }


    @After
    public void tearDown() {
        guerrillaService.deleteUser(testUser.getId());
        guerrillaService.logout(testUser.getEmail());
    }


    @Test
    public void testSetUser() {
        assertNotNull(testUser.getId());
    }


    @Test
    public void testUpdateUser() {

        testUser = guerrillaService.updateUser(
                testUser.getId(),
                testUser.getEmail(),
                "AlteredTestSurname",
                testUser.getName());

        assertThat(-1L, not(testUser.getId()));
    }


    @Test
    public void testGetUsers() {
        assertNotNull(guerrillaService.getUsers());
    }


    @Test
    public void testGetUser() {
        assertNotNull(guerrillaService.getUser(testUser.getEmail()));
    }

}
