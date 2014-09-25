package de.handler.mobile.android.bachelorapp.app.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.util.ArrayList;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.api.RestServiceErrorHandler;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaTypeController;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.controllers.UserController;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;
import de.handler.mobile.android.bachelorapp.app.ui.AuthenticatorActivity;
import de.handler.mobile.android.bachelorapp.app.ui.AuthenticatorActivity_;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;


/**
 * Used to verify and assemble the written text
 * Formerly was used for remote and local view, now remote is another ViewPager
 * This only shows editable fields for the local use / altering a guerrilla prose
 */
@EFragment
public class ProseDialogFragment extends DialogFragment {

    private EditText mTitleEditText;
    private EditText mContentEditText;
    private EditText mTagEditText;

    private CheckBox mPublicCheckbox;

    private OnProseListener onProseListener;
    private OnMediaListener onMediaListener;

    private GuerrillaProse mProse;
    private Bitmap mBitmap;
    private Media mMedia;
    private boolean mUpdate;
    private boolean mServerStatusChanged;

    private ImageView mImageView;


    @App
    BachelorApp app;

    @Bean
    RestServiceErrorHandler restErrorHandler;

    @Bean
    MediaController mediaController;

    @Bean
    MediaTypeController mediaTypeController;

    @Bean
    UserController userController;

    @Bean
    ProseController proseController;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onProseListener = (OnProseListener) activity;
            onMediaListener = (OnMediaListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e("PROSE_DIALOG_FRAGMENT", "Your activity has to implement the onProseListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        proseController.addListener(onProseListener);
        mediaController.addListener(onMediaListener);
        restErrorHandler.setContext(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mProse = app.getGuerrillaProse();
            mBitmap = bundle.getParcelable(MainActivity.DIALOG_PROSE_MEDIA_EXTRA);
            mUpdate = bundle.getBoolean(MainActivity.DIALOG_PROSE_IS_UPDATE);
        }

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_prose, null);

        mImageView = (ImageView) view.findViewById(R.id.dialog_verification_background_image);
        TextView mImageAuthorTextView = (TextView) view.findViewById(R.id.dialog_verification_image_artist);

        if (!mUpdate && app.isImageFromFlickr()) {
            mImageAuthorTextView.setText(app.getTitleImageAuthor());
        }

        // Editable fields for local use
        mTitleEditText = (EditText) view.findViewById(R.id.dialog_verification_title);
        mTitleEditText.setTypeface(myTypeface);
        mContentEditText = (EditText) view.findViewById(R.id.dialog_verification_content);
        mContentEditText.setTypeface(myTypeface);
        mTagEditText = (EditText) view.findViewById(R.id.dialog_verification_tag);

        mPublicCheckbox = (CheckBox) view.findViewById(R.id.dialog_verification_checkbox);

        // If the server is not online one cannot share the prose
        if (!app.isServerOnline()) {
            mPublicCheckbox.setVisibility(View.GONE);
        }

        this.prepareDialog();

        // Build dialog
        AlertDialog.Builder builder = this.buildDialog(view);
        return builder.create();

    }



    private void prepareDialog() {
        if (userController.getLocalUser() == null) {
            // quit this dialog and return to login screen
            onNoUserSet();
        }

        // set image
        if (mBitmap != null && mImageView != null) {

            mImageView.setImageBitmap(mBitmap);
            mImageView.setAdjustViewBounds(true);
        }

        // set known strings
        mContentEditText.setText(mProse.getText());

        if (mUpdate) {
            mTitleEditText.setText(mProse.getTitle());
            mTagEditText.setText(mProse.getTag());

            mMedia = mProse.getMedia();

            // set public status
            if (mProse.getShared() != null) {
                mPublicCheckbox.setChecked(mProse.getShared());
            } else {
                mPublicCheckbox.setChecked(false);
            }
        }

    }

    private AlertDialog.Builder buildDialog(View view) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (!mUpdate) {
                            // store local image here as media id is needed
                            storeLocalImage();
                        }

                        // Normalize tag string
                        String tag = String.valueOf(mTagEditText.getText());
                        if (tag.endsWith(" ")) {
                            tag = tag.substring(0, tag.length()-2);
                        }
                        if (tag.startsWith(" ")) {
                            tag = tag.replaceFirst(" ", "");
                        }
                        tag = tag.toUpperCase();

                        // Set all new data
                        mProse.setTitle(String.valueOf(mTitleEditText.getText()));
                        mProse.setTag(tag);
                        mProse.setText(String.valueOf(mContentEditText.getText()));

                        mProse.setMedia(mMedia);


                        // Define the author for this guerrilla prose as it is shown later online
                        if (userController.getLocalUser() != null) {
                            mProse.setUser_id(userController.getLocalUser().getId());
                            String author;
                            if (!userController.getLocalUser().getSurname().equals("") ||
                                    !userController.getLocalUser().getName().equals("")) {
                                author = userController.getLocalUser().getSurname() +
                                        " " + userController.getLocalUser().getName();
                            } else {
                                author = mProse.getUser_id().toString();
                            }
                            mProse.setAuthor(author);
                        }


                        if (mUpdate) {
                            // If user wants to "unshare" or share on update
                            // important for updating or setting remote media and prose
                            if (mProse.getShared() && !mPublicCheckbox.isChecked()
                                    || !mProse.getShared() && mPublicCheckbox.isChecked()) {
                                mServerStatusChanged = true;
                            }
                        }

                        // Is the guerrilla prose shared or not
                        mProse.setShared(mPublicCheckbox.isChecked());

                        // Set current guerrilla prose for media callbacks in MainActivity
                        app.setGuerrillaProse(mProse);

                        // If a prose is stored for the first time
                        if (!mUpdate) {
                            // Get copyright for image and store
                            String credits;
                            if (app.isImageFromFlickr()) {
                                credits = app.getTitleImageAuthor();
                            } else {
                                credits = userController.getLocalUser().getSurname() + " " +
                                        userController.getLocalUser().getName();
                            }
                            app.setTitleImageAuthor(credits);


                            // Store remote image --> guerrilla prose is public
                            if (mPublicCheckbox.isChecked()) {
                                // As one cannot retrieve the bitmap once send to MainActivity media has to be set here
                                MediaType mediaType = mediaTypeController.getMediaType(MediaTypeController.MEDIA_TYPE_IMAGE);
                                // Do not use the original mBitmap but the compressed one from the disk
                                mediaController.setRemoteMedia(mediaType.getId(),
                                        mediaController.getImageFromDisk(mMedia.getUrl()), credits);
                                // Further action in onRemoteMediaSet() in MainActivity

                            // Media is stored locally
                            } else {
                                // Just inform MainActivity that new prose shall be created
                                onProseListener.onNewProse(mProse, mServerStatusChanged);
                            }

                        // If it is an update
                        } else {
                            onProseListener.onNewProse(mProse, mServerStatusChanged);
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!mUpdate) {
                            if (app.isImageFromCam()) {
                                // remove the image already stored 
                                // special case when using the camera
                                deleteCameraImage();
                            }
                        }
                    }
                });
        return builder;
    }


    // There is no local user and the app cannot be used without
    private void onNoUserSet() {
        if (app.getPendingMedia() != null && app.getPendingMedia().size() > 0) {
            this.deleteCameraImage();
        }

        Toast.makeText(getActivity(), getString(R.string.no_user), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getActivity(), AuthenticatorActivity_.class);
        intent.putExtra(AuthenticatorActivity.USER_REMOVAL, false);
        startActivity(intent);

        // Dismiss the fragment and its dialog.
        dismiss();
    }


    // Store the image on disk
    private void storeLocalImage() {
        if (mBitmap != null) {
            mMedia = new Media();
            String url = "";

            if (!app.isImageFromCam()) {
                Uri uri = mediaController.getMediaFileUri();
                url = uri.getPath();
                mediaController.storeImage(new File(url), mBitmap, MediaController.IMAGE_QUALITY_FLICKR);
            } else {
                // Image comes from camera and media is already prepared
                // List should only be of size one
                ArrayList<Media> pendingMedia = app.getPendingMedia();
                for (Media media : pendingMedia) {
                    url = media.getUrl();
                    mediaController.storeImage(new File(url), mBitmap, MediaController.IMAGE_QUALITY_CAMERA);
                    mBitmap = mediaController.getImageFromDisk(url);
                }
            }

            // Get the media type -> still only image
            mMedia.setMedia_type_id(
                    mediaTypeController.getMediaType(MediaTypeController.MEDIA_TYPE_IMAGE).getId());
            
            // if the image comes from flickr set the copyright
            if (app.isImageFromFlickr()) {
                mMedia.setMedia_author(app.getTitleImageAuthor());
            
            // if not set the copyright to the local user as he chose an image 
            // from the gallery or made the picture by himself
            } else {
                mMedia.setMedia_author(userController.getLocalUser().getSurname() + " " +
                        userController.getLocalUser().getName());
                app.setTitleImageAuthor(userController.getLocalUser().getSurname() +
                        userController.getLocalUser().getName());
            }
            
            mMedia.setUrl(url);
            mMedia.setId(null);
            mMedia.setId(mediaController.setMedia(mMedia));
        }
    }


    // As the image from the camera is always stored even if the user pushes cancel
    // it has manually to be deleted here
    private void deleteCameraImage() {
        Uri uri = app.getTempImageUri();
        mediaController.deleteMediaFromDisk(uri);

        // reset pending media
        app.setPendingMedia(new ArrayList<Media>());
    }
}
