package com.syracuse.android.flavors.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
/**
 * Model class to store users registered with the event
 *
 * Created by Sandesh on 7/14/2015.
 */
@ParseClassName("EventUsers")
public class EventUsers extends ParseObject {

    public String getId() {return getString("Fb");}

    public void setId(String id) { put("Fb",id);}

    public String getName() {
        return getString("Name");
    }

    public String getEmail() {
        return getString("Email");
    }

    public void setName(String name) {
        put("Name",name);
    }

    public void setEmail(String email) {
        put("Email",email);
    }

    public Date getDate() { return getDate("Dob");}

    public void setDate(Date date) { put("Dob",date); }

    public String getInterest() {return getString("Interests");}

    public void setInterests(String interests) {put("Interests",interests);}

    public String getMobile() {return getString("Mobile");}

    public void setMobile(String mobile) {put("Mobile",mobile);}


    public ParseFile getProfilePic() {
        return getParseFile("ProfilePic");
    }

    public void setProfilePic(ParseFile imageFile) {
        put("ProfilePic", imageFile);
    }

    public void setEventsOrganised(int eventsOrganised) {
        put("EventsOrganised",eventsOrganised);
    }

    public int getEventsOrganised() {
        return getInt("EventsOrganised");
    }

    public static ParseQuery<EventUsers> getQuery() {
        return ParseQuery.getQuery(EventUsers.class);
    }
}
