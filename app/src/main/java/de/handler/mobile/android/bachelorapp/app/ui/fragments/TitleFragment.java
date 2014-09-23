package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.ui.BigPictureActivity_;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;

/**
 * Shown in HomeFragment on top
 * Shows Image and lets the user record new media
 */
@EFragment(R.layout.fragment_title)
public class TitleFragment extends Fragment implements View.OnClickListener {

    @ViewById(R.id.fragmentTitle_image)
    ImageView mImageView;

    @ViewById(R.id.fragmentTitle_camera)
    ImageButton mCameraButton;

    @ViewById(R.id.fragmentTitle_gallery)
    ImageButton mGalleryButton;

    @ViewById(R.id.fragmentTitle_image_author)
    TextView mImageTextView;

    @ViewById(R.id.fragmentTitle_text)
    TextView mFragmentTitleText;

    @Bean
    MediaController mediaController;


    @App
    BachelorApp app;


    private Bitmap mBitmap;

    @AfterViews
    void init() {
        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");

        mBitmap = app.getTitleImage();

        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
        } else {
            mImageView.setImageBitmap(mBitmap);
        }

        mImageTextView.setText(app.getTitleImageAuthor());
        mImageTextView.setTypeface(myTypeface);
        mFragmentTitleText.setTypeface(myTypeface);

        mImageView.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mGalleryButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragmentTitle_camera:
                this.startCamera();
                break;
            case R.id.fragmentTitle_image:
                this.startBigPicture();
                break;
            case R.id.fragmentTitle_gallery:
                this.startGallery();
                break;
        }
    }

    private void startBigPicture() {
        app.setTitleImage(mBitmap);
        getActivity().startActivity(new Intent(getActivity(), BigPictureActivity_.class));
    }

    private void startGallery() {
        getActivity().startActivityForResult(
                new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                MainActivity.REQUEST_CODE_SELECT_IMAGE);
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri mediaFileUri = mediaController.getMediaFileUri();
        // due to workaround (activity state loss --> support library) set uri of new file in app object
        // only used here and if user cancels camera operation --> onActivityResult (MainActivity)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaFileUri);
        app.setTempImageUri(mediaFileUri);
        getActivity().startActivityForResult(takePictureIntent, MainActivity.REQUEST_CODE_IMAGE_CAPTURE);
    }
}
