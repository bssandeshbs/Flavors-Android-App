package com.syracuse.android.flavors.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for restaurant
 */
public class RestaurantData {

    List<Map<String,?>> restaurantList;

    //constructor
    public RestaurantData() {
        restaurantList = new ArrayList<>();
    }

    //return list of restaurants
    public List<Map<String,?>> getRestaurantList(){
        return restaurantList;
    }

    //return size of restaurant list
    public int getSize(){
        return restaurantList.size();
    }

    //method to get item
    public HashMap getItem(int i){
        if (i >=0 && i < restaurantList.size()){
            return (HashMap) restaurantList.get(i);
        } else
            return null;
    }
}
