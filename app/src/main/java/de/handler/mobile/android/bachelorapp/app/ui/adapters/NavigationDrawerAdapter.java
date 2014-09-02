package de.handler.mobile.android.bachelorapp.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.handler.mobile.android.bachelorapp.app.R;

/**
 * Class for custom ListView Adapter
 */
public class NavigationDrawerAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private int mLayoutFile;
    private String[] mTexts;

    public NavigationDrawerAdapter(Context context, int layoutFile, String[] texts) {
        mLayoutFile = layoutFile;
        mTexts = texts;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mTexts.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (convertView == null) {
            view = inflater.inflate(mLayoutFile, null);
        }

        TextView text = (TextView) (view != null ? view.findViewById(R.id.list_item_text) : null);

        // Setting all values in listview
        if (text != null) {
            text.setText(mTexts[position]);
        }

        return view;
    }
}
