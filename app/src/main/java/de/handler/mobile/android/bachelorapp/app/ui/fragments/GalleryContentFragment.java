package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;
import de.handler.mobile.android.bachelorapp.app.ui.ProseGalleryActivity;

/**
 * Similar to the Verification Dialog
 * Shows the information for one guerrilla prose *
 */
@EFragment(R.layout.fragment_gallery_content)
public class GalleryContentFragment extends Fragment {

    @ViewById(R.id.fragment_gallery_content_tag)
    TextView textViewTag;

    @ViewById(R.id.fragment_gallery_content_title)
    TextView textViewTitle;

    @ViewById(R.id.fragment_gallery_content_content)
    TextView textViewText;

    @ViewById(R.id.fragment_gallery_content_author)
    TextView textViewAuthor;



    @AfterViews
    void init() {
        GuerrillaProse prose = getArguments().getParcelable(ProseGalleryActivity.GALLERY_PROSE_EXTRA);
        if (prose != null) {
            textViewTag.setText(prose.getTag());
            textViewTitle.setText(prose.getTitle());
            textViewText.setText(prose.getText());
            if (prose.getAuthor() != null && !prose.getAuthor().equals("")) {
                textViewAuthor.setText(getActivity().getString(R.string.author) + ": " + prose.getAuthor());
            } else {
                textViewAuthor.setText(getActivity().getString(R.string.author) + ": " + prose.getUser_id().toString());
            }
        }
    }
}
