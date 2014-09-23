package de.handler.mobile.android.bachelorapp.app.database;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS
// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table MEDIA.
 */
public class Media implements android.os.Parcelable {

    private Long id;
    private String media_author;
    /** Not-null value. */
    private String url;
    private String remote_url;
    private Long media_type_id;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MediaDao myDao;

    private MediaType mediaType;
    private Long mediaType__resolvedKey;

    private List<GuerrillaProse> guerrillaProseList;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Media() {
    }

    public Media(Long id) {
        this.id = id;
    }

    public Media(Long id, String media_author, String url, String remote_url, Long media_type_id) {
        this.id = id;
        this.media_author = media_author;
        this.url = url;
        this.remote_url = remote_url;
        this.media_type_id = media_type_id;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMediaDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedia_author() {
        return media_author;
    }

    public void setMedia_author(String media_author) {
        this.media_author = media_author;
    }

    /** Not-null value. */
    public String getUrl() {
        return url;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemote_url() {
        return remote_url;
    }

    public void setRemote_url(String remote_url) {
        this.remote_url = remote_url;
    }

    public Long getMedia_type_id() {
        return media_type_id;
    }

    public void setMedia_type_id(Long media_type_id) {
        this.media_type_id = media_type_id;
    }

    /** To-one relationship, resolved on first access. */
    public MediaType getMediaType() {
        Long __key = this.media_type_id;
        if (mediaType__resolvedKey == null || !mediaType__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MediaTypeDao targetDao = daoSession.getMediaTypeDao();
            MediaType mediaTypeNew = targetDao.load(__key);
            synchronized (this) {
                mediaType = mediaTypeNew;
            	mediaType__resolvedKey = __key;
            }
        }
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        synchronized (this) {
            this.mediaType = mediaType;
            media_type_id = mediaType == null ? null : mediaType.getId();
            mediaType__resolvedKey = media_type_id;
        }
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<GuerrillaProse> getGuerrillaProseList() {
        if (guerrillaProseList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GuerrillaProseDao targetDao = daoSession.getGuerrillaProseDao();
            List<GuerrillaProse> guerrillaProseListNew = targetDao._queryMedia_GuerrillaProseList(id);
            synchronized (this) {
                if(guerrillaProseList == null) {
                    guerrillaProseList = guerrillaProseListNew;
                }
            }
        }
        return guerrillaProseList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetGuerrillaProseList() {
        guerrillaProseList = null;
    }

    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    public Media(Parcel in) {
        id = in.readLong();
        url = in.readString();
        media_type_id = in.readLong();
    }

    public Media(String url, Long mediaTypeId) {
        this.url = url;
        this.media_type_id = mediaTypeId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    /**
     private Long id;
     private String url;
     private Long mediaTypeId;
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeLong(id);
            dest.writeString(url);
            dest.writeLong(media_type_id);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static final Parcelable.Creator<Media> CREATOR
            = new Parcelable.Creator<Media>() {
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
    // KEEP METHODS END

}
