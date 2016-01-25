package com.syracuse.android.flavors.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.syracuse.android.yelp.RunMe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for restaurants
 */
public class RestaurantUtility {

    List<Map<String,?>> restaurantList;

    //constructor
    public RestaurantUtility() {
        restaurantList = new ArrayList<>();
    }

    // Download an image from online
    public static Bitmap downloadImage(String url) {
        Bitmap bitmap = null;

        InputStream stream = getHttpConnection(url);
        if(stream!=null) {
            bitmap = BitmapFactory.decodeStream(stream);
            try {
                stream.close();
            }catch (IOException e1) {
                Log.d("MyDebugMsg", "IOException in downloadImage()");
                e1.printStackTrace();
            }
        }

        return bitmap;
    }

    //return list of restaurants
    public List<Map<String,?>> getRestaurantList(){
        return restaurantList;
    }

    // Download a Json file from online
    public static String downloadJSON(String url) {
        String json=null, line;

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
            Log.d("Exc:", "UnknownHostException in getHttpConnection()");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("Exc:", "Exception in getHttpConnection()");
            ex.printStackTrace();
        }
        return stream;
    }

    //method to get restaurant data from yelp
    public void downloadRestaurantData(String city) {
        restaurantList.clear(); // clear the list
        restaurantList = new RunMe().start(city);
    }

    //return size of restaurant list
    public int getSize(){
        return restaurantList.size();
    }
}
