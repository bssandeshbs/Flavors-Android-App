package com.syracuse.android.flavors.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Model class to store event information
 *
 * Created by Rahul on 7/13/2015.
 *
 */

@ParseClassName("Events")
public class EventDetails  extends ParseObject {

    public String getAddress() {
        return getString("Address");
    }

    public void setAddress(String address) {
        put("Address",address);
    }

    public Date getStartTime() {
        return getDate("StartTime");
    }

    public void setStartTime(Date startTime) {
        put("StartTime",startTime);
    }

    public Date getEndTime() {
        return getDate("EndTime");
    }

    public void setEndTime(Date endTime) {
        put("EndTime",endTime);
    }

    public int getMaxGuests() {
        return getInt("MaxGuests");
    }

    public void setMaxGuests(int maxGuests) {
        put("MaxGuests",maxGuests);
    }

    public int getAvailGuests() {
        return getInt("GuestsAvailable");
    }

    public void setAvailGuests(int availGuests) {
        put("GuestsAvailable",availGuests);
    }

    public String getMenu() {
        return getString("Menu");
    }

    public void setMenu(String menu) {
        put("Menu",menu);
    }

    public String getEventType() {
        return getString("EventType");
    }

    public void setEventType(String eventType) {
        put("EventType",eventType);
    }

    public double getCost() {
        return getDouble("Cost");
    }

    public void setCost(double cost) {
        put("Cost",cost);
    }

    public String getEmailId() {
        return getString("EmailId");
    }

    public void setEmailId(String emailId) {
        put("EmailId",emailId);
    }

    public double getLatitude() {
        return getDouble("Latitude");
    }

    public void setLatitude(double latitude) {
        put("Latitude",latitude);
    }

    public double getLongitude() {
        return getDouble("Longitude");
    }

    public void setLongitude(double longitude) {
        put("Longitude",longitude);
    }

    public String getEventName() {
        return getString("EventName");
    }

    public void setEventName(String eventName) {
        put("EventName",eventName);
    }

    public String getCity() {
        return getString("City");
    }

    public void setCity(String city) {
        put("City",city);
    }

    public String getUserId() {return getString("UserId");}

    public void setUserId(String userId) {put("UserId",userId);}

    public static ParseQuery<EventDetails> getQuery() {
        return ParseQuery.getQuery(EventDetails.class);
    }
}
