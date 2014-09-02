package de.handler.mobile.android.bachelorapp.app.controllers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProseDao;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;


/**
 * Handles Guerrilla Prose related operations and combines local database
 * as well as remote database operations
 */
@EBean
public class ProseController {

    private Context mContext;
    private GuerrillaProseDao mProseDao;
    private ArrayList<OnProseListener> mProseListeners;


    public ProseController(Context context) {
        this.mContext = context;
        this.mProseListeners = new ArrayList<OnProseListener>();
    }


    // Set up the beans
    @Bean
    MediaController mediaController;

    @Bean
    UserController userController;

    @Bean
    RestServiceErrorHandler errorHandler;

    // Inject the rest service
    @RestService
    GuerrillaService guerrillaService;

    // Inject the application context
    @App
    BachelorApp app;



    @AfterInject
    public void initRestService() {
        errorHandler.setContext(mContext);
        guerrillaService.setRestErrorHandler(errorHandler);
    }


    /**
     * listeners can register using this method
     */
    public void addListener(OnProseListener listener) {
        this.mProseListeners.add(listener);
    }

    /**
     * get the database session from the application context to ensure
     * that database is kept open until app closes. Recommended way by green dao
     */
    private void openDatabase(){
        mProseDao = app.getGuerrillaProseDao();
    }


    /**
     * Local CRUD Methods
     */
    @Background
    public void setLocalProse(GuerrillaProse prose) {
        this.openDatabase();

        try {
            prose.setId(mProseDao.insert(prose));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.local_not_saved), Toast.LENGTH_LONG).show();
        }

        for (OnProseListener listener : mProseListeners) {
            listener.onLocalProseSet(prose);
        }
    }


    @Background
    public void getLocalProse() {
        this.openDatabase();
        List<GuerrillaProse> proses = mProseDao.loadAll();

        for (OnProseListener listener : mProseListeners) {
            listener.onLocalProseReceived(proses);
        }
    }


    @Background
    public void getLocalProse(Long id) {
        this.openDatabase();
        GuerrillaProse prose = null;
        try {
            prose = mProseDao.load(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (OnProseListener listener : mProseListeners) {
            listener.onLocalProseReceived(prose);
        }
    }


    @Background
    public void getLocalProse(String tag) {
        this.openDatabase();
        List<GuerrillaProse> proses = mProseDao.queryBuilder()
                .where(GuerrillaProseDao.Properties.Tag.eq(tag)).list();

        for (OnProseListener listener : mProseListeners) {
            listener.onLocalProseByTagReceived(proses);
        }
    }


    @Background
    public void updateLocalProse(GuerrillaProse prose) {
        this.openDatabase();
        if (prose != null && prose.getId() != null) {
            mProseDao.update(prose);

            for (OnProseListener listener : mProseListeners) {
                listener.onLocalProseUpdated(prose);
            }
        } else {
            getLocalProse();
        }
    }


    @Background
    public void deleteLocalProse(GuerrillaProse prose) {
        this.openDatabase();
        Long mediaId = prose.getMedia_id();
        Long remoteProseId = prose.getRemote_id();
        boolean shared = false;
        if (prose.getShared() != null) {
            shared = prose.getShared();
        }
        Media media = mediaController.getMedia(mediaId);

        prose.delete();
        // delete picture from sd card
        boolean deleted = mediaController.deleteMediaFromDisk(Uri.parse(media.getUrl()));
        Log.d("PROSE_CONTROLLER", "image deleted, result: " + deleted);

        for (OnProseListener listener : mProseListeners) {
            listener.onLocalProseDeleted(remoteProseId, shared);
        }
    }



    /**
     * Remote CRUD Methods
     * All methods annotated with @Background(id="cancellable task")
     * will be cancelled on error in error handler
     */
    @Background(id = "cancellable_task")
    public void setRemoteProse(GuerrillaProse prose) {

        if (app.isServerOnline()) {
            prose = guerrillaService.setProse(
                    prose.getTag(),
                    prose.getTitle(),
                    prose.getText(),
                    prose.getMedia_id(),
                    prose.getRemote_media_id(),
                    prose.getUser_id(),
                    prose.getAuthor());

            for (OnProseListener listener : mProseListeners) {
                listener.onRemoteProseSet(prose);
            }
        }

    }


    @Background(id = "cancellable_task")
    public void getRemoteProse() {
        if (app.isServerOnline()) {
            List<GuerrillaProse> proses = guerrillaService.getProses();

            if (proses != null) {
                List<Tag> tags = getTags(new ArrayList<GuerrillaProse>(proses));
                Collections.sort(tags, new Sorter());
                if (tags.size() > 40) {
                    tags = tags.subList(0, 40);
                    proses = this.sortProseByTag(tags, proses);
                }

                // update App
                app.setRemoteTags(new ArrayList<Tag>(tags));
                app.setRemoteProses(new ArrayList<GuerrillaProse>(proses));

                for (OnProseListener listener : mProseListeners) {
                    listener.onRemoteProseReceived(proses);
                }
            }
        }
    }

    private ArrayList<GuerrillaProse> sortProseByTag(List<Tag> tags, List<GuerrillaProse> proseList) {
        ArrayList<GuerrillaProse> sortedProseList = new ArrayList<GuerrillaProse>();
        for (Tag tag : tags) {
            String tagString = tag.getTag();
            for (GuerrillaProse prose : proseList) {
                if (prose.getTag().toLowerCase().equals(tagString.toLowerCase())) {
                    sortedProseList.add(prose);
                }
            }
        }

        return sortedProseList;
    }


    @Background(id = "cancellable_task")
    public void getRemoteProse(String tag) {

        List<GuerrillaProse> result = new ArrayList<GuerrillaProse>();

        if (app.isServerOnline()) {
            result = guerrillaService.getProses(tag);
        }

        for (OnProseListener listener : mProseListeners) {
            listener.onRemoteProseByTagReceived(result);
        }
    }


    @Background(id = "cancellable_task")
    public void getRemoteProseForUser(Long userId) {
        List<GuerrillaProse> proseList = guerrillaService.getProseForUser(userId);

        for (OnProseListener listener : mProseListeners) {
            listener.onRemoteProseByUserReceived(proseList);
        }
    }


    @Background(id = "cancellable_task")
    public void updateRemoteProse(GuerrillaProse prose) {

        if (app.isServerOnline()) {

            // As this method is filtered on the server side the authentication has to be set
            Guerrilla guerrilla = userController.getLocalUser();
            guerrillaService.setHttpBasicAuth(guerrilla.getEmail(), guerrilla.getPassword());
            prose = guerrillaService.updateProse(
                    prose.getRemote_id(),
                    prose.getTag(),
                    prose.getTitle(),
                    prose.getText(),
                    prose.getMedia_id(),
                    prose.getRemote_media_id(),
                    prose.getUser_id(),
                    prose.getAuthor());

            for (OnProseListener listener : mProseListeners) {
                listener.onRemoteProseUpdated(prose);
            }
        }
    }


    @Background
    public void deleteRemoteProse(Long remoteProseId) {

        if (app.isServerOnline()) {
            // As this method is filtered on the server side the authentication has to be set
            Guerrilla guerrilla = userController.getLocalUser();
            guerrillaService.setHttpBasicAuth(guerrilla.getEmail(), guerrilla.getPassword());
            guerrillaService.deleteProse(remoteProseId);

            for (OnProseListener listener : mProseListeners) {
                listener.onRemoteProseDeleted();
            }
        }
    }


    /**
     * Utils
     */
    // Method sorts out the tags of each guerrilla prose and adjusts tag count
    public ArrayList<Tag> getTags(List<GuerrillaProse> proses) {
        ArrayList<Tag> tags = new ArrayList<Tag>();
        for (GuerrillaProse prose : proses) {
            String tag = prose.getTag();
            tag = tag.replace(" ", "");
            boolean found = false;

            // iterate through tags and if tag already in
            // list augment the counter
            for (Tag tagVO : tags) {
                if (tagVO.getTag().toUpperCase().equals(tag.toUpperCase())) {
                    found = true;
                    int tagCount = tagVO.getCount() + 1;
                    tagVO.setCount(tagCount);
                }
            }

            if (!found) {
                Tag newTag = new Tag(1, tag);
                tags.add(newTag);
            }
        }
        return tags;
    }


    // Get the most popular tag
    public Tag getMostPopularTag(ArrayList<Tag> tags) {
        Collections.sort(tags, new Sorter());
        if (tags.size() > 0) {
            return tags.get(0);
        } else {
            return this.getFallbackTag();
        }
    }

    // If no local tag is available get a fallback tag
    // which is the current month
    public Tag getFallbackTag() {
        // Fallback for first start or empty tag list
        // Get current month fully announced
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("MMMM");
        return new Tag(1, format.format(date));
    }


    // Custom sorter for the Collections.sort() function
    public static class Sorter implements Comparator<Tag> {

        @Override
        public int compare(Tag first, Tag second) {
            return  (first.getCount()>second.getCount() ? -1 :
                    (first.getCount()== second.getCount() ? 0 : 1));
        }
    }
}
