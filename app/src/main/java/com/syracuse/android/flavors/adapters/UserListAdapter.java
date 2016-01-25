package com.syracuse.android.flavors.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.syracuse.android.flavors.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter for User details on screen
 */
public class UserListAdapter extends BaseAdapter {

    private List<Map<String, ?>> mDataSet;
    private Context mContext;
    private static LayoutInflater inflater = null;
    ParseFile userImage = null;
    Bitmap userImgBm = null;

    public UserListAdapter(Context context, List<Map<String, ?>> userMapList) {
        mContext = context;
        mDataSet = userMapList;
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
        ImageView userImage;
        TextView userName;
        TextView userMail;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.event_users, null);
        holder.userImage = (ImageView) rowView.findViewById(R.id.userImage);
        holder.userName = (TextView) rowView.findViewById(R.id.userName);
        holder.userMail = (TextView) rowView.findViewById(R.id.userMail);

        if (mDataSet.size() > 0) {
            userImage = (ParseFile) mDataSet.get(position).get("ProfilePic");
            try {
                userImgBm = BitmapFactory.decodeByteArray(userImage.getData(), 0, userImage.getData().length);
            } catch (ParseException e) {
                Log.d(getClass().getCanonicalName(), "Error while retrieving user image");
            }
            holder.userImage.setImageBitmap(userImgBm);
            holder.userName.setText((String) mDataSet.get(position).get("Name"));
            holder.userMail.setText((String) mDataSet.get(position).get("Email"));
        }
        return rowView;
    }
}
