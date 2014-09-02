package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;
import de.handler.mobile.android.bachelorapp.app.ui.dialogs.ProseDialogFragment_;

/**
 * Component for writing the text / guerrilla prose
 */
@EFragment(R.layout.fragment_prose)
public class ProseFragment extends Fragment implements View.OnClickListener {

    @ViewById(R.id.write_fragment_edit_text)
    EditText mEditText;

    @ViewById(R.id.fragment_write_iconSend)
    ImageButton mIconSend;

    @App
    BachelorApp app;


    @AfterViews
    void init() {
        // set OnClickListener for send icon in addition to EditorAction
        // as EditorAction is only applied in horizontal and editing mode.
        // In vertical layout an action has to be manually defined
        mIconSend.setOnClickListener(this);

        // OnEditorActionListener is the Call back for the action defined
        // in fragment_write.xml for the editText field. In horizontal mode
        // when the editText field covers the whole screen a button is shown
        // and when clicking it the action defined here is executed
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    reviewGuerrillaProse();
                    handled = true;
                }
                return handled;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_write_iconSend:
                this.reviewGuerrillaProse();
                break;
        }
    }


    @Background
    void reviewGuerrillaProse() {
        ProseDialogFragment_ proseDialogFragment = new ProseDialogFragment_();

        // instantiate a new Prose object and assign all known information
        // up to now this is only the text...
        GuerrillaProse prose = new GuerrillaProse();
        prose.setText(String.valueOf(mEditText.getText()));
        // ... and the image
        Bitmap bitmap = app.getTitleImage();

        // add text from editText to new VerificationDialogFragment
        Bundle bundle = new Bundle();
        app.setGuerrillaProse(prose);
        bundle.putParcelable(MainActivity.DIALOG_PROSE_MEDIA_EXTRA, bitmap);
        bundle.putBoolean(MainActivity.DIALOG_PROSE_IS_UPDATE, false);

        proseDialogFragment.setArguments(bundle);

        // get FragmentManager and add the VerificationDialogFragment to the back stack
        // --> show the fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(proseDialogFragment, MainActivity.PROSEDIALOG_TAG)
                .commit();
    }
}
