package de.handler.mobile.android.bachelorapp.app.ui;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.helper.CustomNetworkImageView;
import de.handler.mobile.android.bachelorapp.app.helper.MemoryCache;

@EActivity(R.layout.activity_big_picture)
public class BigPictureActivity extends BaseActivity {

    @ViewById(R.id.activity_big_picture_image_view)
    CustomNetworkImageView imageView;

    @ViewById(R.id.activity_big_picture_text_view)
    TextView imageArtist;

    @ViewById(R.id.activity_big_picture_image_view_flickr_credits)
    TextView imageFlickrCredits;

    @ViewById(R.id.activity_big_picture_title_textview)
    TextView titleText;

    @AfterViews
    void init() {
        setupActionBar();
        imageView.setAdjustViewBounds(true);

        MemoryCache mMemoryCache = app.getMemoryCache();

        if (app.getCurrentMedia() != null &&
                app.getCurrentMedia().getRemote_url() != null) {

            Media media = app.getCurrentMedia();

            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(this), mMemoryCache);
            int start = media.getRemote_url().lastIndexOf("/");
            String url = media.getRemote_url().substring(start);

            String mediaDir = "http://mortoncornelius.no-ip.biz/guerrilla-prose/public/media";
            imageView.setImageUrl(mediaDir + url, imageLoader);

            app.setCurrentMedia(null);
        } else {
            imageView.setLocalImageBitmap(app.getTitleImage());
        }

        // Create custom typeface
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        imageArtist.setTypeface(myTypeface);
        titleText.setTypeface(myTypeface);

        imageArtist.setText(app.getTitleImageAuthor());
        if (app.isImageFromFlickr()) {
            imageFlickrCredits.setVisibility(View.VISIBLE);
        }
    }

}
