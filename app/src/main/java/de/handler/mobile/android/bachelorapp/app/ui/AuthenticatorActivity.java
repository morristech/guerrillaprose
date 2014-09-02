package de.handler.mobile.android.bachelorapp.app.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaTypeController;
import de.handler.mobile.android.bachelorapp.app.controllers.UserController;
import de.handler.mobile.android.bachelorapp.app.database.Guerrilla;
import de.handler.mobile.android.bachelorapp.app.database.MediaType;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnUserListener;
import de.handler.mobile.android.bachelorapp.app.ui.dialogs.VerifyUserDialogFrament_;

/**
 * Account activity
 */
@EActivity(R.layout.activity_authenticator)
public class AuthenticatorActivity extends BaseActivity implements OnUserListener, View.OnClickListener {

    public static final String DIALOG_REGISTER_USER_STRINGS_EXTRA = "user_strings_extra";
    public static final String DIALOG_REGISTER_TITLE_EXTRA = "dialog_register_title_extra";

    public static final String USER_REMOVAL = "user_change_itent_extra";

    private static final String VERIFYDIALOG_TAG = "Verify Dialog";
    public static final String CHANGE_USER = "verify_dialog_change_user";
    public static final String CURRENT_USER = "verify_dialog_old_user";
    public static final String DELETE_USER = "verify_dialog_delete_user";

    private String mPassword;
    private boolean mUserRemoval;

    @Bean
    MediaTypeController mediaTypeController;

    @ViewById(R.id.authenticator_progress_bar)
    ProgressBar progressBar;

    @ViewById(R.id.activity_authenticator_background)
    ImageView mImageView;

    @ViewById(R.id.activity_authenticator_email)
    EditText mEmailEditText;

    @ViewById(R.id.activity_authenticator_password)
    EditText mPasswordEditText;

    @ViewById(R.id.activity_authenticator_name)
    EditText mNameEditText;

    @ViewById(R.id.activity_authenticator_surname)
    EditText mSurnameEditText;

    @ViewById(R.id.activity_authenticator_button_cancel)
    Button mCancelButton;

    @ViewById(R.id.activity_authenticator_button_ok)
    Button mOkButton;

    @App
    BachelorApp app;

    @Bean
    UserController userController;

    private Guerrilla mUser;
    private boolean mNewLocalUser;
    private String mOldEmail;


    @AfterInject
    void initListener() {
        userController.addListener(this);
    }

    @AfterViews
    void init() {
        // Set up the action bar.
        ActionBar actionBar = setupActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(R.color.transparent_grey_80));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.user));

        mUserRemoval = getIntent().getBooleanExtra(USER_REMOVAL, false);
        if (mUserRemoval){
            actionBar.setTitle(getString(R.string.authenticator_delete_user));
            // Name Fields are not displayed on user change
            mSurnameEditText.setVisibility(View.GONE);
            mNameEditText.setVisibility(View.GONE);
        }

        mUser = userController.getLocalUser();
        if (mUser == null) {
            mUser = new Guerrilla();
            mNewLocalUser = true;
        } else {
            mOldEmail = mUser.getEmail();
            mEmailEditText.setText(mUser.getEmail());
            mSurnameEditText.setText(mUser.getSurname());
            mNameEditText.setText(mUser.getName());
        }

        mImageView.setImageBitmap(app.getTitleImage());
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_authenticator_button_ok:
                if (mUserRemoval) {
                    this.readUserCredentials();
                    this.startVerificationDialog();
                } else {
                    this.readUserCredentials();
                    if (this.checkUserCredentials()) {
                        this.startVerificationDialog();
                    }
                }
                break;
            case R.id.activity_authenticator_button_cancel:
                if (mNewLocalUser) {
                    this.resetApp();
                }
                finish();
                break;
        }
    }

    private void resetApp() {
        prefs.edit().firstStart().put(true).apply();
        ArrayList<MediaType> types =
                new ArrayList<MediaType>(mediaTypeController.getMediaTypes());
        for (MediaType type: types) {
            mediaTypeController.deleteMediaType(type);
        }
    }


    private void readUserCredentials() {

        mUser.setSurname(String.valueOf(mSurnameEditText.getText()));
        mUser.setName(String.valueOf(mNameEditText.getText()));
        mUser.setEmail(String.valueOf(mEmailEditText.getText()));
        mUser.setPassword(String.valueOf(mPasswordEditText.getText()));
        mPassword = String.valueOf(mPasswordEditText.getText());
    }

    private boolean checkUserCredentials() {
        // ensure that user entered email and password as it is essential for user management
        if (!isValidEmailAddress(String.valueOf(mEmailEditText.getText()))) {
            Toast.makeText(this, getString(R.string.authentication_no_valid_email),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }


    public static boolean isValidEmailAddress(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void startVerificationDialog() {
        progressBar.setVisibility(View.VISIBLE);

        VerifyUserDialogFrament_ verificationDialog = new VerifyUserDialogFrament_();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_USER, mUser);
        bundle.putBoolean(DELETE_USER, mUserRemoval);
        verificationDialog.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(verificationDialog, VERIFYDIALOG_TAG)
                .commit();
    }



    /**
     * User Callback functions
     */
    @Override
    public void onPasswordVerified(Guerrilla user) {
        progressBar.setVisibility(View.VISIBLE);
        if (mUserRemoval) {
            this.deleteUser();
        } else {
            this.setUser(user);
        }
    }

    private void deleteUser() {
        userController.deleteRemoteUser(userController.getLocalUser());
    }

    private void setUser(Guerrilla user) {
        mUser = user;
        if (mNewLocalUser) {
            userController.getRemoteUser(user.getEmail());
        } else {
            userController.updateRemoteUser(user, mOldEmail);
        }
    }


    @UiThread
    @Override
    public void onRemoteUserReceived(Guerrilla user) {
        if (user != null && user.getEmail() != null) {
            user.setPassword(mPassword);
            userController.updateRemoteUser(user, user.getEmail());
        } else {
            userController.setRemoteUser(mUser);
        }
    }


    @UiThread
    @Override
    public void onRemoteUserUpdated(Guerrilla user) {
        // user has been updated
        if (user != null) {
            if (mNewLocalUser) {
                user.setPassword(mPassword);
                userController.setLocalUser(user);
            } else {
                user.setPassword(mPassword);
                userController.updateLocalUser(user);
            }
        } else {
            this.onError();
        }
    }

    @Override
    public void onRemoteUserSet(Guerrilla user) {
        if (user != null && user.getEmail() != null) {
            user.setPassword(mPassword);
            if (!mNewLocalUser) {
                user.setId(mUser.getId());
            }
            mUser = user;
            userController.setLocalUser(user);
        } else {
            this.onError();
        }

    }


    @UiThread
    @Override
    public void onLocalUserSet(Guerrilla user) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, getString(R.string.user_created), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    public void onLocalUserUpdated(Guerrilla user) {
        if (user != null && user.getEmail() != null) {
            user.setPassword(mPassword);
            user.setId(mUser.getId());
            mUser = user;
        } else {
            this.onError();
        }
        progressBar.setVisibility(View.GONE);
        this.finish();
    }


    @UiThread
    void onError() {
        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    @UiThread
    @Override
    public void onLoggedIn(String rememberToken) {
    }


    @Override
    public void onUserChange(Guerrilla user) {
        mPassword = user.getPassword();
        userController.getRemoteUser(user.getEmail());
    }







    @UiThread
    @Override
    public void onRemoteUserDeleted(Long id) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, getString(R.string.user_deleted), Toast.LENGTH_SHORT)
                .show();
        userController.deleteLocalUser();
        this.resetApp();
        finish();
    }




    @Override
    public void onCancel() {
        if (!mNewLocalUser) {
            Toast.makeText(this, getString(R.string.wrong_credentials), Toast.LENGTH_SHORT).show();
        } else {
            userController.setLocalUser(mUser);
        }
        progressBar.setVisibility(View.GONE);
    }



    private void changeUser(Guerrilla user) {
        userController.getRemoteUser(user.getEmail());
    }



}
