package de.handler.mobile.android.bachelorapp.app.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.handler.mobile.android.bachelorapp.app.R;
import de.handler.mobile.android.bachelorapp.app.database.GuerrillaProse;

/**
 * Used to display prose contents
 */
public class ProseListAdapter extends BaseAdapter {

    private Context mContext;
    private List<GuerrillaProse> mGuerrillaProseList = new ArrayList<GuerrillaProse>();

    public ProseListAdapter(Context context, List<GuerrillaProse> proseList) {
        this.mContext = context;
        if (proseList != null) {
            this.mGuerrillaProseList = proseList;
        }
    }

    @Override
    public int getCount() {
        return mGuerrillaProseList.size();
    }

    @Override
    public Object getItem(int position) {
        return mGuerrillaProseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mGuerrillaProseList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        // "recycled view" design pattern
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_prose_list_item, null);

            ImageView image = (ImageView) view.findViewById(R.id.adapter_prose_icon);
            TextView imageText = (TextView) view.findViewById(R.id.adapter_prose_icon_text);

            TextView text = (TextView) view.findViewById(R.id.adapter_prose_text);

            Drawable drawable = mContext.getResources().getDrawable(R.drawable.tag_item_selector).mutate();
            String titleString = mGuerrillaProseList.get(position).getTitle();
            drawable = this.determineListItemColor(titleString, drawable);
            image.setImageDrawable(drawable);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            imageText.setText(mGuerrillaProseList.get(position).getTitle());
            text.setText(mGuerrillaProseList.get(position).getText());

        } else {
            view = convertView;
        }
        return view;
    }

    // Define the colours concerning the first letters of a tag
    private Drawable determineListItemColor(String title, Drawable image) {

        if (title != null && image != null) {

            if (title.toLowerCase().startsWith("a")
                    || title.toLowerCase().startsWith("x")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_dark), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("n")
                    || title.toLowerCase().startsWith("z")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_bright), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("c")
                    || title.toLowerCase().startsWith("o")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_blue_light), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("p")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_gray_light), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("q")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_gray_bright), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("f")
                    || title.toLowerCase().startsWith("r")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_green_dark), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("d")
                    || title.toLowerCase().startsWith("g")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_green_light), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("h")
                    || title.toLowerCase().startsWith("t")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_dark), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("i")
                    || title.toLowerCase().startsWith("u")
                    || title.toLowerCase().startsWith("e")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_light), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("b")
                    || title.toLowerCase().startsWith("j")
                    || title.toLowerCase().startsWith("v")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_purple), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("k")
                    || title.toLowerCase().startsWith("w")
                    || title.toLowerCase().startsWith("s")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_red_dark), PorterDuff.Mode.SRC_ATOP);
            } else if (title.toLowerCase().startsWith("l")
                    || title.toLowerCase().startsWith("x")
                    || title.toLowerCase().startsWith("m")) {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_red_light), PorterDuff.Mode.SRC_ATOP);
            } else {
                image.setColorFilter(mContext.getResources().getColor(R.color.guerrilla_orange_dark), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return image;
    }

}
