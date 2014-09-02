package de.handler.mobile.android.bachelorapp.app.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.R;

@EActivity(R.layout.activity_big_picture)
public class BigPictureActivity extends BaseActivity {

    public static final String IMAGE_EXTRA = "image_extra";
    public static final String CREDITS_EXTRA = "author_extra";

    @ViewById(R.id.activity_big_picture_image_view)
    ImageView imageView;

    @ViewById(R.id.activity_big_picture_text_view)
    TextView imageArtist;

    @ViewById(R.id.activity_big_picture_image_view_flickr_credits)
    TextView imageFlickrCredits;

    @AfterViews
    void init() {
        setupActionBar();
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(app.getTitleImage());
        imageArtist.setText(app.getTitleImageAuthor());
        if (app.isImageFromFlickr()) {
            imageFlickrCredits.setVisibility(View.VISIBLE);
        }
    }

}
