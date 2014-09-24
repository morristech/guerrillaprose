package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.helper.MemoryCache;
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
    NetworkImageView mImage;

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
        MemoryCache mMemoryCache = app.getMemoryCache();

        if (media != null && media.getRemote_url() != null) {
            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(getActivity()), mMemoryCache);
            int start = media.getRemote_url().lastIndexOf("/");
            String url = media.getRemote_url().substring(start);

            String mediaDir = "http://mortoncornelius.no-ip.biz/guerrilla-prose/public/media";
            mImage.setImageUrl(mediaDir + url, imageLoader);
        }

        mImage.setOnClickListener(this);
        mCredits.setText(app.getTitleImageAuthor());
    }

    @Override
    public void onClick(View v) {
        getActivity().startActivity(new Intent(getActivity(), BigPictureActivity_.class));
    }
}
