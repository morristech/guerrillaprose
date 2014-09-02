package de.handler.mobile.android.bachelorapp.app.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;
import de.handler.mobile.android.bachelorapp.app.ui.fragments.ContentFragment;

/**
 * Safety Question dialog before deleting guerrilla prose from database
 */
@EFragment
public class DeleteDialogFragment extends DialogFragment {

    private OnProseListener onProseListener;

    @Bean
    ProseController proseController;

    private ArrayList<GuerrillaProse> proseToDelete;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onProseListener = (OnProseListener) activity;
        } catch (Exception e) {
            Log.e(getTag(), "your activity has to implement OnProseListener");
            e.printStackTrace();
        }
        setRetainInstance(true);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        proseController.addListener(onProseListener);

        if (getArguments() != null) {
            // Security boolean
            proseToDelete = getArguments().getParcelableArrayList(ContentFragment.PROSE_LIST_FRAGMENT_PROSE_EXTRA);
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.dialog_delete_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Delete the guerrilla prose
                        for (GuerrillaProse prose : proseToDelete) {
                            proseController.deleteLocalProse(prose);
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
