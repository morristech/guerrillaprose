package de.handler.mobile.android.bachelorapp.app.controllers;

import android.content.Context;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaDao;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnUserListener;

/**
 * Handles User related operations and combines local database
 * as well as remote database operations
 */
@EBean
public class UserController {

    private Context mContext;
    private GuerrillaDao mUserDao;
    private List<OnUserListener> mOnUserListeners;


    public UserController(Context context) {
        this.mContext = context;
        mOnUserListeners = new ArrayList<OnUserListener>();
    }


    // Set up the beans
    @Bean
    RestServiceErrorHandler errorHandler;

    // Inject the rest service
    @RestService
    GuerrillaService guerrillaService;

    // Inject the application context
    @App
    BachelorApp app;


    @AfterInject
    void initRestService() {
        errorHandler.setContext(mContext);
        guerrillaService.setRestErrorHandler(errorHandler);
    }

    /**
     * listeners can register using this method
     */
    public void addListener(OnUserListener listener) {
        this.mOnUserListeners.add(listener);
    }


    /**
     * get the database session from the application context to ensure
     * that database is kept open until app closes. Recommended way by green dao
     */
    private void openDatabase(){
        mUserDao = app.getGuerrillaDao();
    }


    /**
     * Local CRUD Methods
     */
    public void setLocalUser(Guerrilla user) {
        this.openDatabase();
        List<Guerrilla> users = mUserDao.queryBuilder()
                .where(GuerrillaDao.Properties.Email.eq(user.getEmail()))
                .list();

        if (users.size() > 0) {
            this.updateLocalUser(user);
        } else {
            mUserDao.insert(user);
        }

        for (OnUserListener listener : mOnUserListeners) {
            listener.onLocalUserSet(user);
        }
    }


    public void updateLocalUser(Guerrilla user) {
        this.openDatabase();
        Guerrilla guerrilla = mUserDao.load(user.getId());

        if (guerrilla != null) {
            guerrilla.setId(user.getId());
            guerrilla.setEmail(user.getEmail());
            guerrilla.setSurname(user.getSurname());
            guerrilla.setName(user.getName());
            mUserDao.update(guerrilla);

            for (OnUserListener listener : mOnUserListeners) {
                listener.onLocalUserUpdated(guerrilla);
            }
        }
    }


    public Guerrilla getLocalUser() {
        this.openDatabase();
        List<Guerrilla> users = mUserDao.queryBuilder().list();
        if (users.size() > 0) {
            return users.get(0);
        } else {
            return null;
        }
    }

    public void deleteLocalUser() {
        this.openDatabase();
        mUserDao.deleteAll();
    }



    /**
     * Remote CRUD Methods
     * All methods annotated with @Background(id="cancellable task")
     * will be cancelled on error in error handler
     */
    @Background(id = "cancellable_task")
    public void setRemoteUser(Guerrilla user) {

        if (app.isServerOnline()) {
            user = guerrillaService.setUser(
                    user.getEmail(),
                    user.getPassword(),
                    user.getSurname(),
                    user.getName());

            for (OnUserListener listener : mOnUserListeners) {
                listener.onRemoteUserSet(user);
            }
        }
    }


    @Background(id = "cancellable_task")
    public void updateRemoteUser(Guerrilla user, String oldEmail) {

        if (app.isServerOnline()) {
            guerrillaService.setHttpBasicAuth(oldEmail, user.getPassword());
            user = guerrillaService.updateUser(
                    user.getId(),
                    user.getEmail(),
                    user.getSurname(),
                    user.getName());

            for (OnUserListener listener : mOnUserListeners) {
                listener.onRemoteUserUpdated(user);
            }
        }
    }


    @Background(id = "cancellable_task")
    public void getRemoteUser(String email) {

        if (app.isServerOnline()) {
            Guerrilla user = guerrillaService.getUser(email);

            for (OnUserListener listener : mOnUserListeners) {
                listener.onRemoteUserReceived(user);
            }
        }
    }


    @Background(id = "cancellable_task")
    public void deleteRemoteUser(Guerrilla user) {
        if (app.isServerOnline()) {
            guerrillaService.setHttpBasicAuth(user.getEmail(), user.getPassword());
            Long id = guerrillaService.deleteUser(user.getId());

            for (OnUserListener listener : mOnUserListeners) {
                listener.onRemoteUserDeleted(id);
            }
        }
    }


    @Background(id = "cancellable_task")
    public void loginUser(Guerrilla user) {
        if (app.isServerOnline()) {
            String rememberToken = guerrillaService.login(user.getEmail(), user.getPassword());
            user.setRemember_token(rememberToken);
            for (OnUserListener listener : mOnUserListeners) {
                listener.onLoggedIn(rememberToken);
            }
        }
    }

    @Background(id = "cancellable_task")
    public void logoutUser(Guerrilla user) {
        if (app.isServerOnline()) {
            guerrillaService.login(user.getEmail(), user.getPassword());
        }
    }
}
