package com.syracuse.android.flavors.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Utility class for getting list of autocomplete cities
 */
public class CityUtility {

    public static String CITY_URL="http://gd.geobytes.com/AutoCompleteCity?callback=?&q=";
    ArrayList<String> cityList;

    //constructor
    public CityUtility() {
        cityList = new ArrayList<>();
    }

    //return list of restaurants
    public ArrayList<String> getCityList(){
        return cityList;
    }

    // Makes HttpURLConnection and returns InputStream
    public static InputStream getHttpConnection(String urlString) {
        InputStream stream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", "UnknownHostexception in getHttpConnection()");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in getHttpConnection()");
            ex.printStackTrace();
        }
        return stream;
    }

    //return size of restaurant list
    public int getSize(){
        return cityList.size();
    }

    public void downloadCityDataJson(String json_url) {
        cityList.clear(); // clear the list
        String CityArray = downloadJSON(json_url);
        if (CityArray == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+json_url);
            return;
        }

        try {
            CityArray = CityArray.replaceAll("\\?\\s\\(", "");
            CityArray = CityArray.replaceAll("\\)\\;", "");
            CityArray = CityArray.replaceAll("\\?\\(", "");
            JSONArray moviesJsonArray = new JSONArray(CityArray);
            for (int i = 0; i < moviesJsonArray.length(); i++) {
                String city = (String) moviesJsonArray.get(i);
                if (city != null) {
                    cityList.add(city);
                }
            }
        } catch (JSONException ex) {
            Log.d("Exc:", "JSONException in downloadCityDataJson");
            ex.printStackTrace();
        }
    }

    // Download a Json file from online
    public static String downloadJSON(String url) {
        String json=null, line;

        url = url.replaceAll(" ", "%20");
        InputStream stream = getHttpConnection(url);
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder out = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                json = out.toString();
            } catch (IOException ex) {
                Log.d("Exc:", "IOException in downloadJSON()");
                ex.printStackTrace();
            }
        }
        return json;
    }
}
