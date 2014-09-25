package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.helper.CustomNetworkImageView;
import de.handler.mobile.android.bachelorapp.app.ui.ProseGalleryActivity;

/**
 * displays image of guerrilla prose
 */
@EFragment(R.layout.fragment_gallery_title)
public class GalleryTitleFragment extends Fragment {

    @App
    BachelorApp app;

    @ViewById(R.id.fragment_gallery_title_image)
    CustomNetworkImageView mImage;

    @ViewById(R.id.fragment_gallery_title_image_author)
    TextView mCredits;

    @ViewById(R.id.fragment_gallery_title_banner)
    TextView mTitleBanner;

    @AfterViews
    void init() {
        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        if (myTypeface != null) {
            mTitleBanner.setTypeface(myTypeface);
            mCredits.setTypeface(myTypeface);
        }

        Media media = getArguments().getParcelable(ProseGalleryActivity.GALLERY_MEDIA_EXTRA);

        if (media != null && media.getRemote_url() != null) {
            ImageLoader imageLoader = app.getImageLoader();

            int start = media.getRemote_url().lastIndexOf("/");
            String url = media.getRemote_url().substring(start);

            String mediaDir = "http://mortoncornelius.no-ip.biz/guerrilla-prose/public/media";
            mImage.setImageUrl(mediaDir + url, imageLoader);
            mImage.setErrorImageResId(R.drawable.watermark);
        }

        mCredits.setText(app.getTitleImageAuthor());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
