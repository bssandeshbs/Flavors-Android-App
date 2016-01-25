package com.syracuse.android.flavors.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * Model class to store event images
 *
 * Created by Rahul on 7/14/2015.
 */
@ParseClassName("EventImages")
public class EventImages extends ParseObject {

    public String getEventId() {
        return getString("EventId");
    }

    public ParseFile getImageFile() {
        return getParseFile("ImageFile");
    }

    public void setEventId(ParseObject event) {
        ParseRelation<ParseObject> relation = ParseUser.getCurrentUser().getRelation("EventId");
        relation.add(event);
    }

    public void setImageFile(ParseFile imageFile) {
        put("ImageFile", imageFile);
    }

    public static ParseQuery<UserDetail> getQuery() {
        return ParseQuery.getQuery(UserDetail.class);
    }
}
