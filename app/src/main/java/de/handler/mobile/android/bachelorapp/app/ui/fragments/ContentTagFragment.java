package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.controllers.Tag;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;
import de.handler.mobile.android.bachelorapp.app.ui.adapters.ImageTagAdapter;
import de.handler.mobile.android.bachelorapp.app.ui.dialogs.DeleteDialogFragment_;


/**
 * Tag Grid Fragment
 */
@EFragment(R.layout.fragment_tag_grid)
public class ContentTagFragment extends Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnProseListener {

    @ViewById(R.id.fragment_tag_gridview)
    GridView mGridView;

    @ViewById(R.id.fragment_tag_progress_bar)
    ProgressBar mProgressBar;

    @Bean
    ProseController proseController;

    @App
    BachelorApp app;

    private static final String BACKSTACK_ENTRY = "listFragment";

    private boolean mIsLocal = true;
    private boolean mDelete = false;

    @AfterViews
    void init() {
        mIsLocal = getArguments().getBoolean(ContentFragment.PROSE_DISPLAY_LOCAL);

        proseController.addListener(this);

        // Get the tags from global app object
        ArrayList<Tag> tags;
        if (mIsLocal) {
            tags = app.getLocalTags();
        } else {
            tags = app.getRemoteTags();
        }

        mGridView.setAdapter(new ImageTagAdapter(getActivity(), tags, R.layout.adapter_image_grid_item));
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
    }



    /**
     * Display List Functionality
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String tag;
        mProgressBar.setVisibility(View.VISIBLE);

        if (mIsLocal) {
            tag = app.getLocalTags().get(position).getTag();
            proseController.getLocalProse(tag);
        } else {
            tag = app.getRemoteTags().get(position).getTag();
            proseController.getRemoteProse(tag);
        }
    }



    // Local Prose By Tag has been received
    // If local prose has been looked up in database this callback function is called
    @Override
    public void onLocalProseByTagReceived(List<GuerrillaProse> proseList) {
        if (mDelete) {
            mDelete = false;
            this.startDeleteDialogFragment(new ArrayList<GuerrillaProse>(proseList));
        } else {
            this.startListFragment(proseList);
        }
    }


    // Remote Prose By Tag has been received
    @Override
    public void onRemoteProseByTagReceived(List<GuerrillaProse> proseList) {
        // For Security enable again the shared option and set the tag
        if (proseList != null) {
            for (GuerrillaProse prose : proseList) {
                prose.setShared(true);
            }
        }
        this.startListFragment(proseList);
    }

    @UiThread
    void startListFragment(List<GuerrillaProse> proseList) {
        mProgressBar.setVisibility(View.GONE);

        ContentListFragment_ proseListFragment = new ContentListFragment_();
        Bundle bundle = new Bundle();

        if (proseList != null) {
            bundle.putParcelableArrayList(ContentFragment.PROSE_LIST_FRAGMENT_TAG_EXTRA,
                    new ArrayList<Parcelable>(proseList));
        }

        if (mIsLocal) {
            bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, true);
        } else {
            bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, false);
        }

        proseListFragment.setArguments(bundle);

        try {
            getParentFragment().getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_display_main_container, proseListFragment)
                    .addToBackStack(BACKSTACK_ENTRY)
                    .commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }




    /**
     * Delete Functionality
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mDelete = true;
        if (mIsLocal) {
            // Get prose matching the local tag and respond in
            // callback function onLocalProseByTagReceived(List) / below
            proseController.getLocalProse(app.getLocalTags().get(position).getTag());
            return true;
        }

        // If it is remote prose don't react as the local prose
        // is deleted first then automatically the remote ones (happens in MainActivity)
        return false;
    }

    
    private void startDeleteDialogFragment(ArrayList<GuerrillaProse> proseList) {
        DeleteDialogFragment_ deleteDialogFragment = new DeleteDialogFragment_();

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, mIsLocal);
        bundle.putParcelableArrayList(ContentFragment.PROSE_LIST_FRAGMENT_PROSE_EXTRA, proseList);

        deleteDialogFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(deleteDialogFragment, MainActivity.DELETEDIALOG_TAG)
                .commit();
    }



    // Unused callback functions
    @Override
    public void onNewProse(GuerrillaProse prose, boolean sharedStateChanged) {

    }

    @Override
    public void onRemoteProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseReceived(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onRemoteProseDeleted() {

    }

    @Override
    public void onLocalProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseReceived(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onRemoteProseByUserReceived(List<GuerrillaProse> proseList) {

    }

    @Override
    public void onLocalProseDeleted(Long remoteProseId, boolean shared) {

    }
}
