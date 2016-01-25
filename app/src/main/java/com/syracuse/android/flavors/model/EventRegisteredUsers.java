package com.syracuse.android.flavors.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Model class to store events registered by users
 *
 * Created by Sandesh on 7/21/2015.
 */
@ParseClassName("EventRegisteredUsers")
public class EventRegisteredUsers extends ParseObject {

    public String getUserId() {
        return getString("UserId");
    }

    public void setUserId(String userId){
        put("UserId",userId);
    }

    public String getEventId(){
        return getString("EventId");
    }

    public void setEventId(String eventId){
        put("EventId",eventId);
    }
}
