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
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.database.MediaTypeDao;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaTypeListener;


/**
 * Handles MediaType related operations and combines local database
 * as well as remote database operations
 */
@EBean
public class MediaTypeController {

    private Context mContext;
    private MediaTypeDao mMediaTypeDao;
    private ArrayList<OnMediaTypeListener> listeners;

    // Database
    public static final String MEDIA_TYPE_AUDIO = "audio";
    public static final String MEDIA_TYPE_IMAGE = "image";
    public static final String MEDIA_TYPE_VIDEO = "video";

    // Server
    @RestService
    GuerrillaService guerrillaService;

    // Inject error handler
    @Bean
    RestServiceErrorHandler errorHandler;

    // Inject the application context
    @App
    BachelorApp app;


    public MediaTypeController(Context mContext) {
        this.mContext = mContext;
        this.listeners = new ArrayList<OnMediaTypeListener>();
    }


    @AfterInject
    public void initRestService() {
        errorHandler.setContext(mContext);
        guerrillaService.setRestErrorHandler(errorHandler);
    }



    /**
     * listeners can register using this method
     */
    public void addListener(OnMediaTypeListener listener) {
        this.listeners.add(listener);
    }


    /**
     * get the database session from the application context to ensure
     * that database is kept open until app closes. Recommended way by green dao
     */
    private void openDatabase() {
        mMediaTypeDao = app.getMediaTypeDao();
    }


    /**
     * Local CRUD Methods
     */
    public Long setMediaType(MediaType mediaType) {
        this.openDatabase();
        return mMediaTypeDao.insert(mediaType);
    }


    public void updateMediaType(MediaType mediaType) {
        this.openDatabase();
        mMediaTypeDao.update(mediaType);
    }


    public void deleteMediaType(MediaType mediaType) {
        this.openDatabase();
        mMediaTypeDao.delete(mediaType);
    }


    public MediaType getMediaType(long mediaTypeId) {
        this.openDatabase();
        try {
            return mMediaTypeDao.load(mediaTypeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MediaType getMediaType(String mediaType) {
        this.openDatabase();

        MediaType type = null;
        List<MediaType> list = mMediaTypeDao.queryBuilder().where(MediaTypeDao.Properties.Media_type.eq(mediaType)).list();
        if (list != null && list.size() > 0) {
            type = list.get(0);
        }
        return type;
    }


    public List<MediaType> getMediaTypes() {
        this.openDatabase();
        return mMediaTypeDao.loadAll();
    }



    /**
     * Remote CRUD Methods
     */
    @Background
    public void setRemoteMediaType(String remoteMediaType) {

        if (app.isServerOnline()) {
            Long id = guerrillaService.setMediaType(remoteMediaType);

            MediaType mediaType = new MediaType(id, remoteMediaType);

            for (OnMediaTypeListener listener : this.listeners) {
                listener.onMediaTypeSet(mediaType);
            }
        }
    }


    @Background
    public void getRemoteMediaTypes() {
        if (app.isServerOnline()) {
            List<MediaType> types = guerrillaService.getMediaTypes();

            for (OnMediaTypeListener listener : this.listeners) {
                listener.onMediaTypesReceived(types);
            }
        }
    }
}
