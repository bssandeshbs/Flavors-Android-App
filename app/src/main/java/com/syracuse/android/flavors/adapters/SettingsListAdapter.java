package com.syracuse.android.flavors.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.syracuse.android.flavors.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter for items in Settings screen
 */
public class SettingsListAdapter extends BaseAdapter {

    private List<Map<String, Object>> mDataSet;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public SettingsListAdapter(Context context, List<Map<String, Object>> settingsMapList) {
        mContext = context;
        mDataSet = settingsMapList;
        inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public Map<String, ?> getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        ImageView rowIcon;
        TextView rowName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.settings_row, null);
        holder.rowIcon = (ImageView) rowView.findViewById(R.id.rowIcon);
        holder.rowName = (TextView) rowView.findViewById(R.id.rowName);

        if (mDataSet.size() > 0) {
            int drawImg = (int) mDataSet.get(position).get("rowIcon");
            Bitmap imgBitmap = BitmapFactory.decodeResource(mContext.getResources(), drawImg);
            holder.rowIcon.setImageBitmap(imgBitmap);
            holder.rowName.setText((String) mDataSet.get(position).get("rowName"));
        }
        return rowView;
    }
}
