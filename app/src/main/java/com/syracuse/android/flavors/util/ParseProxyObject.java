package com.syracuse.android.flavors.util;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


// By Jamie Chapman, @chappers57
// class to get parse object

public class ParseProxyObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public ParseProxyObject(ParseObject object) {

        // Loop the keys in the ParseObject
        for(String key : object.keySet()) {
            @SuppressWarnings("rawtypes")
            Class classType = object.get(key).getClass();
            if(classType == byte[].class || classType == String.class ||
                    classType == Integer.class || classType == Boolean.class || classType == Double.class || classType == Date.class) {
                values.put(key, object.get(key));
            } else if(classType == ParseUser.class) {
                ParseProxyObject parseUserObject = new ParseProxyObject((ParseObject)object.get(key));
                values.put(key, parseUserObject);
            } else {
                // You might want to add more conditions here, for embedded ParseObject, ParseFile, etc.
            }
        }
        values.put("ObjectId", object.getObjectId());
    }

    public String getString(String key) {
        if(has(key)) {
            return (String) values.get(key);
        } else {
            return "";
        }
    }

    public int getInt(String key) {
        if(has(key)) {
            return (Integer)values.get(key);
        } else {
            return 0;
        }
    }

    public Boolean getBoolean(String key) {
        if(has(key)) {
            return (Boolean)values.get(key);
        } else {
            return false;
        }
    }

    public byte[] getBytes(String key) {
        if(has(key)) {
            return (byte[])values.get(key);
        } else {
            return new byte[0];
        }
    }

    public ParseProxyObject getParseUser(String key) {
        if(has(key)) {
            return (ParseProxyObject) values.get(key);
        } else {
            return null;
        }
    }

    public Boolean has(String key) {
        return values.containsKey(key);
    }
}
