package de.handler.mobile.android.bachelorapp.app.controllers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.api.BackgroundExecutor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.api.GuerrillaService;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaDao;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaListener;


/**
 * Handles Media related operations and combines local database
 * as well as remote database operations
 */
@EBean
public class MediaController {

    private Context mContext;
    private MediaDao mMediaDao;
    private ArrayList<OnMediaListener> listeners;

    // Define different image qualities depending on the source
    public static final int IMAGE_QUALITY_FLICKR = 100;
    public static final int IMAGE_QUALITY_CAMERA = 50;

    // Define a delay for the server operations
    private static final int DELAY_MILLIS = 10000;


    public MediaController(Context context) {
        mContext = context;
        listeners = new ArrayList<OnMediaListener>();
    }


    // Set up the beans
    @Bean
    MediaTypeController mediaTypeController;

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
    public void addListener(OnMediaListener listener) {
        this.listeners.add(listener);
    }


    /**
     * get the database session from the application context to ensure
     * that database is kept open until app closes. Recommended way by green dao
     */
    private void openDatabase() {
        mMediaDao = app.getDaoSession().getMediaDao();
    }


    /**
     * Local CRUD Methods
     */
    public Long setMedia(Media media) {
        this.openDatabase();
        return mMediaDao.insert(media);
    }


    public void updateMedia(Media media) {
        this.openDatabase();
        mMediaDao.update(media);
    }


    public List<Media> getMedia() {
        this.openDatabase();
        return mMediaDao.loadAll();
    }


    public Media getMedia(Long id) {
        try {
            this.openDatabase();
            return mMediaDao.load(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Remote CRUD Methods
     */
    // Timer for cancelling tasks that take too long. Receives a call
    // by an empty message after the defined DELAY_MILLIS
    private android.os.Handler timeHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // All methods annotated with @Background(id="cancellable task")
            // will be cancelled
            BackgroundExecutor.cancelAll("cancellable_task", true);
        }
    };


    @Background(id="cancellable_task")
    public void setRemoteMedia(final Long mediaTypeId, Bitmap bitmap, String credits) {
        if (app.isServerOnline()) {
            String base64 = this.bitmapToBase64(bitmap);

            // Start timer
            timeHandler.sendEmptyMessageDelayed(1, DELAY_MILLIS);

            Long mediaId = guerrillaService.setMedia(mediaTypeId, base64, credits);

            // Inform the listeners
            for (OnMediaListener onMediaListener : listeners) {
                onMediaListener.onRemoteMediaSet(mediaId);
            }

        }
    }


    @Background(id="cancellable_task")
    public void updateRemoteMedia(Media media, Long remoteMediaId) {
        if (app.isServerOnline()) {
            if (media != null) {
                Bitmap bitmap = this.getImageFromDisk(media.getUrl());
                String base64 = this.bitmapToBase64(bitmap);

                // Start timer
                timeHandler.sendEmptyMessageDelayed(1, DELAY_MILLIS);

                // As this method is filtered on the server side the authentication has to be set
                Guerrilla guerrilla = userController.getLocalUser();
                guerrillaService.setHttpBasicAuth(guerrilla.getEmail(), guerrilla.getPassword());
                Long id = guerrillaService.updateMedia(remoteMediaId, base64, media.getMedia_type_id());

                // Inform the listeners
                for (OnMediaListener onMediaListener : listeners) {
                    onMediaListener.onRemoteMediaUpdated(id);
                }
            } else {
                // Inform the listeners
                for (OnMediaListener onMediaListener : listeners) {
                    onMediaListener.onRemoteMediaCanceled();
                }
            }
        }
    }


    @Background
    public void deleteRemoteMedia(Long id) {
        if (app.isServerOnline()) {
            Guerrilla guerrilla = userController.getLocalUser();

            // As this method is filtered on the server side the authentication has to be set
            guerrillaService.setHttpBasicAuth(guerrilla.getEmail(), guerrilla.getPassword());
            guerrillaService.deleteMedia(id);
        }
    }


    // Background operations with the same id will be executed after each other
    // This is important in ProseGalleryActivity
    @Background(serial = "media_retrieval")
    public void getRemoteMedia(Long mediaId) {
        if (app.isServerOnline()) {

            Media media = new Media();
            if (mediaId != null) {
                media = guerrillaService.getMedia(mediaId);
            }

            // Inform the listeners
            for (OnMediaListener onMediaListener : listeners) {
                onMediaListener.onRemoteMediaReceived(media);
            }
        }
    }

    // Background operations with the same id will be executed after each other
    // This is important in ProseGalleryActivity
    @Background(serial = "media_retrieval")
    public void getRemoteMediaString(Long mediaId) {
        if (app.isServerOnline()) {
            String base64 = "";

            if (mediaId != null) {
                base64 = guerrillaService.getMediaBase64String(mediaId);
            }

            // Inform the listeners
            for (OnMediaListener onMediaListener : listeners) {
                onMediaListener.onRemoteMediaStringReceived(base64, mediaId);
            }
        }
    }


    /**
     * Methods for working with media on local storage
     * */
    // Get a unique uri
    public Uri getMediaFileUri() {
        // get actual time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // prepare media storage / externally as images can be used system wide
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                mContext.getString(R.string.app_name));

        // Store images internally as they have to be removed on uninstall
        //File mediaStorageDir = mContext.getFilesDir();

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // prepare media file name
        File mediaFile = new File(
                mediaStorageDir.getPath() + File.separator
                        + mContext.getString(R.string.app_name).toUpperCase()
                        + "_"
                        + timeStamp + ".jpg");

        app.setLastFileUri(Uri.fromFile(mediaFile));
        // get uri from file to find it again
        return Uri.fromFile(mediaFile);
    }


    // Store image on sd card
    public void storeImage(File mediaFile, Bitmap bitmap, int quality) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Delete everything on local storage
    public void clearLocalStorage() {
        // prepare media storage / externally as images can be used system wide
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                mContext.getString(R.string.app_name));

        if (mediaStorageDir.isDirectory()) {
            mediaStorageDir.delete();
        }
    }

    // "Mobile devices typically have constrained system resources.
    // Android devices can have as little as 16MB of memory available
    // to a single application. [...]
    // Bitmaps take up a lot of memory, especially for rich images like photographs.
    // For example, the camera on the Galaxy Nexus takes photos up to 2592x1936 pixels
    // (5 megapixels). If the bitmap configuration used is ARGB_8888
    // (the default from the Android 2.3 onward) then loading this image
    // into memory takes about 19MB of memory (2592*1936*4 bytes), immediately
    // exhausting the per-app limit on some devices."
    // (source: http://developer.android.com/training/displaying-bitmaps/index.html)
    // --> right compression of photographs is very important
    public Bitmap getImageFromDisk(String url) {
        try {
            File file = new File(url);
            Log.d("MEDIA_CONTROLLER", "getImageFromDisk: File exists: " + file.exists());

            BitmapFactory.Options options = new BitmapFactory.Options();

            // pretend decode progress
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), options);

            // Get display size of device to adjust image size
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();

            // Before API 13
            int width, height;
            if (Build.VERSION.SDK_INT < 13) {
                width = display.getWidth();
                height = display.getHeight();
                // After API 12
            } else {
                display.getSize(size);
                width = size.x;
                height = size.y;
            }

            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            // Determine how much to scale down the image
            int temp1 = ((imageWidth * 2) / width);
            int temp2 = ((imageHeight * 2) / height);
            int scaleFactor = Math.max(temp1, temp2);

            // now really decode
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            options.inPurgeable = true;

            return BitmapFactory.decodeFile(file.getPath(), options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.watermark);
        }
    }

    // If retrieving the image from the gallery this will convert the uri to a readable path
    @SuppressWarnings("deprecation")
    public String getRealPathFromURI(Activity acitivity, Uri contentUri) {
        Cursor cursor;
        // Old fashioned way
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = acitivity.managedQuery(contentUri,
                    proj,       // Which columns to return
                    null,       // WHERE clause; which rows to return (all rows)
                    null,       // WHERE clause selection arguments (none)
                    null);      // Order-by clause (ascending by name)

            // New way
        } else {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            cursor = acitivity.getContentResolver().query(contentUri, projection, null, null, null);
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }


    // If the image comes from camera the uri has to be stored in application context due
    // to a workaround for nexus camera app.
    public Media createPendingMedia(String mediaTypeString, Uri fileUri) {
        long mediaTypeId = mediaTypeController.getMediaType(mediaTypeString).getId();
        return new Media(fileUri.getPath(), mediaTypeId);
    }


    // Delete local media file
    public boolean deleteMediaFromDisk(Uri uri) {
        if (uri != null) {
            File file = new File(uri.getPath());
            return file.delete();
        }
        return false;
    }


    // Convert Bitmaps to Base64 String
    public String bitmapToBase64(Bitmap src) {
        String str = null;
        if (src != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            if (app.isImageFromCam()) {
                src.compress(android.graphics.Bitmap.CompressFormat.PNG, 10, os);
            } else {
                src.compress(android.graphics.Bitmap.CompressFormat.PNG, 30, os);
            }
            byte[] byteArray = os.toByteArray();
            str = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        }
        return str;
    }

    // ... and back
    public Bitmap base64ToBitmap(String src)    {
        Bitmap bitmap = null;
        if (src != null) {
            // Decoding with no wrap as default values did not work
            byte[] decodedString = Base64.decode(src.getBytes(), Base64.NO_WRAP);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return bitmap;
    }
}
