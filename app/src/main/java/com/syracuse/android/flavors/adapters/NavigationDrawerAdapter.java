package com.syracuse.android.flavors.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.util.DrawerData;

import java.util.List;
import java.util.Map;

/**
 * Adapter for Navigation Drawer
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private List<Map<String, ?>> mDataSet;
    private Context mContext;
    OnItemClickListener mItemClickListener;

    public NavigationDrawerAdapter(Context myContext, List<Map<String, ?>> myDataSet) {
        mContext = myContext;
        mDataSet = myDataSet;
    }

    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView vIcon;
        public TextView vTitle;

        private int vViewType;
        private View vView;

        public ViewHolder(View v, int viewType) {
            super(v);
            vView = v;
            vViewType = viewType;
            vIcon = (ImageView) v.findViewById(R.id.icon);
            vTitle = (TextView) v.findViewById(R.id.title);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mItemClickListener != null){
                        mItemClickListener.onItemClick(view, getPosition());
                        view.setSelected(true);
                    }
                    notifyDataSetChanged();
                }
            });

        }

        public void bindData(Map<String, ?> item, int position) {
            final Intent intent = ((Activity) mContext).getIntent();
            if(position == intent.getIntExtra("position", 0)){
                vView.setBackgroundColor(Color.argb(100, 198, 40, 40));
            }else{
                vView.setBackgroundColor(0x00000000);
            }

            switch (vViewType){
                case DrawerData.TYPE1:
                    if(vIcon != null){
                        vIcon.setImageResource((Integer)item.get("icon"));
                    }
                    if(vTitle != null){
                        vTitle.setText((String)item.get("title"));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //Create new views
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case DrawerData.TYPE1:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.drawer_list_item_type, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.drawer_list_item_type, parent, false);
                break;
        }
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, ?> item = mDataSet.get(position);
        holder.bindData(item, position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, ?> item = mDataSet.get(position);
        return (Integer) item.get("type");
    }

}
