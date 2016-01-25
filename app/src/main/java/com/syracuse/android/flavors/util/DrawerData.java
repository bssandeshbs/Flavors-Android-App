package com.syracuse.android.flavors.util;

import com.syracuse.android.flavors.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object to handle list items in Navigation Drawer
 */
public class DrawerData {

    public static final int TYPE1 = 1;
    List<Map<String,?>> drawerList;

    public List<Map<String, ?>> getDrawerList() {
        return drawerList;
    }

    public Map<String, ?> getItem(int position){
        return drawerList.get(position);
    }

    public int getSize(){
        return drawerList.size();
    }

    //constructor
    @SuppressWarnings("unchecked")
    public DrawerData(){
        HashMap item;
        drawerList = new ArrayList<>();

        //Item 1-8
        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_events);
        item.put("title", "Events");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_my_events);
        item.put("title", "My Events");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_events_joined);
        item.put("title", "Events Joined");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_add_event);
        item.put("title", "Add Home Event");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_add_event);
        item.put("title", "Add Restaurant Event");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_settings);
        item.put("title", "Settings");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.about_me);
        item.put("title", "My Profile");
        drawerList.add(item);

        item = new HashMap();
        item.put("type", TYPE1);
        item.put("icon", R.drawable.ic_exit);
        item.put("title", "Sign out");
        drawerList.add(item);
    }
}
