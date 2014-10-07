package de.handler.mobile.android.bachelorapp.app.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.BachelorApp;
import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.MediaController;
import de.handler.mobile.android.bachelorapp.app.controllers.ProseController;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;
import de.handler.mobile.android.bachelorapp.app.database.Media;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnBackPressedListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnMediaListener;
import de.handler.mobile.android.bachelorapp.app.interfaces.OnProseListener;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity;
import de.handler.mobile.android.bachelorapp.app.ui.MainActivity_;
import de.handler.mobile.android.bachelorapp.app.ui.ProseGalleryActivity;
import de.handler.mobile.android.bachelorapp.app.ui.ProseGalleryActivity_;
import de.handler.mobile.android.bachelorapp.app.ui.adapters.ProseListAdapter;
import de.handler.mobile.android.bachelorapp.app.ui.dialogs.DeleteDialogFragment_;
import de.handler.mobile.android.bachelorapp.app.ui.dialogs.ProseDialogFragment_;

@EFragment(R.layout.fragment_prose_list)
public class ContentListFragment extends ListFragment implements AdapterView.OnItemLongClickListener, OnMediaListener, OnProseListener, OnBackPressedListener {

    @Bean
    ProseController proseController;

    @Bean
    MediaController mediaController;


    @App
    BachelorApp app;


    private GuerrillaProse mProse;
    private Media mMedia;

    private List<GuerrillaProse> mProseList;
    private boolean mIsLocal;
    private boolean mLocalPictureDeleted;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(this);
    }


    @AfterViews
    void init() {
        mediaController.addListener(this);
        proseController.addListener(this);

        MainActivity_ mainActivity = (MainActivity_) getActivity();
        mainActivity.setOnBackPressedListener(this);

        this.setUpHeader(null);
        // Get Extras
        mProseList = getArguments()
                .getParcelableArrayList(ContentFragment.PROSE_LIST_FRAGMENT_TAG_EXTRA);
        mIsLocal = getArguments().getBoolean(ContentFragment.PROSE_DISPLAY_LOCAL);

        // Set Title
        this.setUpHeader(mProseList.get(0).getTag());

        this.initListAdapter(mProseList);
    }


    // TODO: add up navigation or display tag as title
    private void setUpHeader(String title) {
        View padding = null;
        TextView texViewTitle = null;
        if (title == null) {
            // Set a padding to list view at the size of the action bar
            padding = new View(getActivity());
        } else {
            texViewTitle = new TextView(getActivity());
            texViewTitle.setText(title);
        }

        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    getResources().getDisplayMetrics());
        }

        if (title == null) {
            padding.setMinimumHeight(actionBarHeight);
            padding.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
        } else {
            texViewTitle.setMinimumHeight(actionBarHeight);
            texViewTitle.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_blue_dark));
        }

        // Add the header / padding
        try {
            getListView().addHeaderView(padding);
            getListView().addHeaderView(texViewTitle);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    @UiThread
    void initListAdapter(List<GuerrillaProse> proseList) {
        // Find GuerrillaProse matching tag and prepare the ListView
        ProseListAdapter adapter = new ProseListAdapter(getActivity(), proseList);
        setListAdapter(adapter);
    }


    /**
     * Display Prose Functionality
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        setListShown(false);

        app.setTitleImageAuthor("");

        // As the list has an header the position is -1
        mProse = mProseList.get(position -1);

        if (mIsLocal) {
            Media media = mediaController.getMedia(mProse.getMedia_id());
            Bitmap bitmap = this.getLocalBitmap(media);

            if (bitmap == null) {
                mediaController.getRemoteMedia(mProse.getRemote_media_id());
            } else {
                this.startProseDialog(bitmap);
            }
        } else {
            this.startProseGallery(new ArrayList<GuerrillaProse>(mProseList), position);
        }
    }

    private Bitmap getLocalBitmap(Media media) {
        Bitmap bitmap = null;

        if (media != null) {
            //MediaType mediaType = mediaTypeController.getMediaType
            //        (media.getMedia_type_id());

            // In further implementations the media may also be audio or video.
            // Therefore check if it is an image and if yes put it into bundle
            //if (mediaType.getMedia_type().equals(MediaTypeController.MEDIA_TYPE_IMAGE)) {
            if (media.getUrl() != null && !media.getUrl().equals("")) {
                bitmap = mediaController.getImageFromDisk(media.getUrl());
            }
            //}
        } else {
            Log.e("CONTENT_LIST_FRAGMENT", "An error occurred when trying to access the prose' media");
        }
        return bitmap;
    }

    @UiThread
    @Override
    public void onRemoteMediaReceived(Media media) {

        if (media != null) {

            // In further implementations the media may also be audio or video.
            // Therefore check if it is an image and if yes put it into bundle
            Bitmap bitmap = this.getLocalBitmap(media);

            // Local image has been deleted
            if (bitmap == null && app.isServerOnline()) {
                mLocalPictureDeleted = true;
                mMedia = media;
                mediaController.getRemoteMediaString(mProse.getRemote_media_id());
            } else {
                this.startProseDialog(bitmap);
            }
        }
    }

    // The media has been retrieved from the server. Now one can react.
    @UiThread
    @Override
    public void onRemoteMediaStringReceived(String base64, Long remoteMediaId) {
        Bitmap bitmap = null;

        if (base64 != null) {
            // In further implementations the media may also be audio or video.
            if (!base64.equals("")) {
                bitmap = mediaController.base64ToBitmap(base64);
            }
        }

        if (mLocalPictureDeleted && bitmap != null) {
            mLocalPictureDeleted = false;
            Uri uri = mediaController.getMediaFileUri();
            mediaController.storeImage(
                    new File(uri.getPath()),
                    bitmap,
                    100);
            mMedia.setUrl(uri.getPath());
            mediaController.updateMedia(mMedia);
            mProse.setMedia(mMedia);
        } else if (mLocalPictureDeleted) {
            bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.watermark);
        }

        this.startProseDialog(bitmap);
    }

    private void startProseGallery(ArrayList<GuerrillaProse> proseList, int position) {
        Intent intent = new Intent(getActivity(), ProseGalleryActivity_.class);
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(ProseGalleryActivity.GALLERY_PROSE_PROSE_LIST_EXTRA, proseList);
        bundle.putInt(ProseGalleryActivity.GALLERY_POSITION_EXTRA, position);

        intent.putExtra("gallery", bundle);
        startActivity(intent);

        setListShown(true);
    }


    private void startProseDialog(Bitmap bitmap) {
        ProseDialogFragment_ proseDialogFragment = new ProseDialogFragment_();

        Bundle bundle = new Bundle();
        app.setGuerrillaProse(mProse);
        bundle.putParcelable(MainActivity.DIALOG_PROSE_MEDIA_EXTRA, bitmap);
        bundle.putBoolean(MainActivity.DIALOG_PROSE_IS_UPDATE, true);

        // Attach the bundle to the fragment
        proseDialogFragment.setArguments(bundle);

        try {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(proseDialogFragment, MainActivity.PROSEDIALOG_TAG)
                    .commit();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        setListShown(true);
    }



    /**
     * Delete Functionality
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mIsLocal) {
            // Get local prose by id. The id is set ProseListAdapter
            // and is the same as the matching guerrilla prose
            proseController.getLocalProse(id);
            return true;
        }

        // If it is a remote prose don't react as the local prose
        // is deleted first then automatically the remote one (happens in MainActivity)
        return false;
    }

    @Override
    public void onNewProse(GuerrillaProse prose, boolean sharedUpdated) {

    }

    @Override
    public void onRemoteProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseSet(GuerrillaProse prose) {

    }

    @Override
    public void onRemoteProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseUpdated(GuerrillaProse prose) {

    }

    @Override
    public void onLocalProseDeleted(Long remoteProseId, boolean shared) {

    }

    @Override
    public void onRemoteProseDeleted() {

    }

    @Override
    public void onRemoteProseReceived(GuerrillaProse prose) {

    }

    // The prose has been retrieved from database. One can react.
    @Override
    public void onLocalProseReceived(GuerrillaProse prose) {
        // Create a list as the deleteDialogFragment is also used for list of prose
        ArrayList<GuerrillaProse> proseList = new ArrayList<GuerrillaProse>();
        proseList.add(prose);
        this.startDeleteDialogFragment(proseList);
    }

    @Override
    public void onRemoteProseByTagReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onLocalProseByTagReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onRemoteProseReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onLocalProseReceived(List<GuerrillaProse> prose) {

    }

    @Override
    public void onRemoteProseByUserReceived(List<GuerrillaProse> proseList) {

    }

    private void startDeleteDialogFragment(ArrayList<GuerrillaProse> toDelete) {
        DeleteDialogFragment_ deleteDialogFragment = new DeleteDialogFragment_();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ContentFragment.PROSE_LIST_FRAGMENT_PROSE_EXTRA, toDelete);
        bundle.putBoolean(ContentFragment.PROSE_DISPLAY_LOCAL, !mIsLocal);

        deleteDialogFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(deleteDialogFragment, MainActivity.DELETEDIALOG_TAG)
                .commit();
    }



    @Override
    public void onBackPressed() {
        // Try / Catch block necessary as the activity might already have been destroyed
        // In this case the following action is not necessary
        try {
            getParentFragment().getChildFragmentManager().popBackStack();
        } catch (IllegalStateException e) {
            Log.e("DISPLAY_FRAGMENT", e.getMessage());
        }
    }


    // Unused callback functions
    @Override
    public void onRemoteMediaSet(Long mediaId) {

    }

    @Override
    public void onRemoteMediaUpdated(Long mediaId) {

    }

    @Override
    public void onRemoteMediaCanceled() {

    }

    @Override
    public void onLocalMediaSet(Long mediaId) {

    }
}
