package com.syracuse.android.yelp;

import android.util.Log;

import com.google.gson.Gson;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RunMe {
    private String CONSUMER_KEY = "nOpg7dmycvicL-Hjqs7yGg";
    private String CONSUMER_SECRET = "zewdvl4_W_qoFhBAs3IBHJyh8QY";
    private String TOKEN = "gOuGCpFjkF65dNky4ee_RuIWXtL-valb";
    private String TOKEN_SECRET = "aIJ1VkKwrFcReQoi3lNNkyvTc60";
    private OAuthService service;
    private List<Map<String, ?>> dataList;
    private List<Map<String, ?>> dataListSearch;
    private HashMap dataMap;
    private String category;
    private String location;
    private Token accessToken;
    private Response response;
    private String rawData;
    private OAuthRequest request;
    private YelpSearchResult places;

    public List<Map<String, ?>> start(String city) {
        category = "restaurants";
        location = city;
        dataList = new ArrayList<>();
        dataListSearch = new ArrayList<>();

        // Execute a signed call to the Yelp service.
        service = new ServiceBuilder().provider(YelpV2API.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
        accessToken = new Token(TOKEN, TOKEN_SECRET);
        getYelpData(dataListSearch, "cafes,newamerican");
        dataList.addAll(dataListSearch);

        //second call
        dataListSearch.clear();
        getYelpData(dataListSearch, "indpak,mexican,chinese");
        dataList.addAll(dataListSearch);
        return dataList;
    }

    //private method to get Yelp API data
    private void getYelpData(List<Map<String, ?>> fillDataList, String restaurantFilter){
        request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("category", category);
        request.addQuerystringParameter("category_filter", restaurantFilter);
        request.addQuerystringParameter("sort", "2");
        request.addQuerystringParameter("limit", "20");
        service.signRequest(accessToken, request);
        response = request.send();
        rawData = response.getBody();
        // Sample of how to turn that text into Java objects.
        try {
            places = new Gson().fromJson(rawData, YelpSearchResult.class);
            System.out.println("Your search found " + places.getTotal() + " results.");
            System.out.println("Yelp returned " + places.getBusinesses().size() + " businesses in this request.");
            for (Business biz : places.getBusinesses()) {
                dataMap = new HashMap();
                dataMap.put("name", biz.getName());
                String addressCom = "";
                for (String address : biz.getLocation().getAddress()) {
                    addressCom = addressCom + address;
                }
                dataMap.put("address", addressCom);
                dataMap.put("city", biz.getLocation().getCity());
                dataMap.put("latitude", biz.getLocation().getCoordinate().getLatitude());
                dataMap.put("longitude", biz.getLocation().getCoordinate().getLongitude());
                dataMap.put("icon", biz.getImage_url());
                dataMap.put("rating", biz.getRatingImg_url());
                dataMap.put("reviewCount", biz.getReview_count());
                String category = biz.getCategories().get(0).get(0);
                dataMap.put("categories", category);

                //check if any value is null, we will not show such restaurant
                if (!dataMap.containsValue(null)) {
                    fillDataList.add(dataMap);
                }
            }
        } catch (Exception e) {
            Log.d(getClass().getCanonicalName(),"Error, could not parse returned data!");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new RunMe().start("Syracuse, NY, United States");
    }

}
