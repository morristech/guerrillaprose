package de.handler.mobile.android.bachelorapp.app.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.androidannotations.annotations.EFragment;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnUserListener;
import de.handler.mobile.android.bachelorapp.app.ui.AuthenticatorActivity;

/**
 * Check Password when doing server relevant interaction
 */
@EFragment
public class VerifyUserDialogFrament extends DialogFragment {

    private EditText mEditTextPassword;
    private OnUserListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.listener = (OnUserListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(this.getTag(), "your activity has to implement the OnUserListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Guerrilla olduser = null;
        if (getArguments() != null) {
            olduser = getArguments().getParcelable(AuthenticatorActivity.CURRENT_USER);
        }

        // Get the layout inflater and inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_verify, null);

        mEditTextPassword = (EditText) view.findViewById(R.id.dialog_verify_password);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Guerrilla finalOldUser = olduser;
        builder .setView(view)
                .setTitle(R.string.dialog_verify_title).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newPassword = String.valueOf(mEditTextPassword.getText());

                if (finalOldUser != null && newPassword.equals(finalOldUser.getPassword())) {
                    listener.onPasswordVerified(finalOldUser);

                } else {
                    listener.onCancel();
                }
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
