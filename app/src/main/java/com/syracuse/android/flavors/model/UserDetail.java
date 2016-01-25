package com.syracuse.android.flavors.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Model class to store user details
 *
 * Created by Sandesh on 7/8/2015.
 */
@ParseClassName("Test")
public class UserDetail extends ParseObject {

    public String getName() {
        return getString("Name");
    }

    public String getId() {
        return getString("Id");
    }

    public void setName(String name) {
        put("Name",name);
    }

    public void setId(String id) {
        put("Id",id);
    }

    public static ParseQuery<UserDetail> getQuery() {
        return ParseQuery.getQuery(UserDetail.class);
    }
}
