package de.handler.mobile.android.bachelorapp.app.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.controllers.Tag;

/**
 * Image Adapter used in GridView Fragments
 */
public class ImageTagAdapter extends BaseAdapter {

    private final int mLayoutFile;
    private final ArrayList<Tag> mTags;
    private Context mContext;


    public ImageTagAdapter(Context context, ArrayList<Tag> tags, int layoutFile) {
        mContext = context;
        mTags = tags;
        mLayoutFile = layoutFile;
    }

    public int getCount() {
        return mTags.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // Get layout
        // Changed this from recycled view pattern to actual pattern
        // as it showed the tags in unpredictable order, sometimes twice, sometimes false
        view = inflater.inflate(mLayoutFile, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.image_adapter_image);
        TextView textView = (TextView) view.findViewById(R.id.image_adapter_text);

        if (mTags.size() < 1) {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }

        Drawable drawable = mContext.getResources().getDrawable(R.drawable.tag_item_selector).mutate();
        String tag = mTags.get(position).getTag();

        if (tag.equals("")) {
            tag = mContext.getString(R.string.no_tag);
        }

        drawable = this.determineTagColor(tag, drawable);
        imageView.setImageDrawable(drawable);

        // Set text
        textView.setText(tag);
        return view;
    }


    // Used to overlay the tag items with a colour defined concerning the first letters
    private Drawable determineTagColor(String tag, Drawable image) {

        if (tag != null && image != null) {

            PorterDuff.Mode porterDuffMode = PorterDuff.Mode.SRC_ATOP;

            // Check if colours are working on these devices
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
                porterDuffMode = PorterDuff.Mode.MULTIPLY;
            }

            if (tag.toLowerCase().startsWith("a")
                    || tag.toLowerCase().startsWith("m")
                    || tag.toLowerCase().startsWith("x")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_dark), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("b")
                    || tag.toLowerCase().startsWith("n")
                    || tag.toLowerCase().startsWith("z")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_bright), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("c")
                    || tag.toLowerCase().startsWith("o")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_light), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("d")
                    || tag.toLowerCase().startsWith("p")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_gray_light), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("e")
                    || tag.toLowerCase().startsWith("q")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_gray_bright), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("f")
                    || tag.toLowerCase().startsWith("r")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_green_dark), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("g")
                    || tag.toLowerCase().startsWith("s")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_green_light), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("h")
                    || tag.toLowerCase().startsWith("t")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_dark), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("i")
                    || tag.toLowerCase().startsWith("u")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_light), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("j")
                    || tag.toLowerCase().startsWith("v")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_purple), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("k")
                    || tag.toLowerCase().startsWith("w")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_red_dark), porterDuffMode);
            } else if (tag.toLowerCase().startsWith("l")
                    || tag.toLowerCase().startsWith("x")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_red_light), porterDuffMode);
            } else {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_dark), porterDuffMode);
            }
        }

        return image;
    }
}
