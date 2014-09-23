package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.ui.BigPictureActivity_;
import de.handler.mobile.android.bachelorapp.app.ui.ProseGalleryActivity;

/**
 * displays image of guerrilla prose
 */
@EFragment(R.layout.fragment_gallery_title)
public class GalleryTitleFragment extends Fragment implements View.OnClickListener {

    @App
    BachelorApp app;

    @ViewById(R.id.fragment_gallery_title_image)
    ImageView mImage;

    @ViewById(R.id.fragment_gallery_title_image_author)
    TextView mCredits;

    @ViewById(R.id.fragment_gallery_title_banner)
    TextView mTitleBanner;

    private Bitmap mBitmap;

    @Background
    @AfterViews
    void init() {
        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        if (myTypeface != null) {
            mTitleBanner.setTypeface(myTypeface);
            mCredits.setTypeface(myTypeface);
        }

        int position = getArguments().getInt(ProseGalleryActivity.GALLERY_POSITION_EXTRA);
        mBitmap = app.getPagerImages().get(position);
        mImage.setImageBitmap(mBitmap);
        mImage.setOnClickListener(this);
        mCredits.setText(app.getTitleImageAuthor());
    }

    @Override
    public void onClick(View v) {
        app.setTitleImage(mBitmap);
        getActivity().startActivity(new Intent(getActivity(), BigPictureActivity_.class));
    }


    @Background
    void getRemoteImage(Media media) {
        try {
            String guerrillaProseServer
                    = "http://mortoncornelius.no-ip.biz/guerrilla-prose/public/index.php";
            URL url = new URL(guerrillaProseServer + media.getRemote_url());
            Object content = url.getContent();
            InputStream is = (InputStream) content;
            Drawable mDrawable = Drawable.createFromStream(is, "src");

            mImage.setImageDrawable(mDrawable);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
