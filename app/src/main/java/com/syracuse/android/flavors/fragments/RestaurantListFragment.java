package com.syracuse.android.flavors.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.CreateRestEventActivity;
import com.syracuse.android.flavors.adapters.CityArrayAdapter;
import com.syracuse.android.flavors.adapters.RestaurantAdapter;
import com.syracuse.android.flavors.util.RestaurantData;
import com.syracuse.android.flavors.util.RestaurantUtility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;

/**
 * Fragment to show list of restaurants
 */
public class RestaurantListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RestaurantAdapter mRestaurantAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LruCache mImgMemoryCache;
    private RestaurantData restaurantData;
    private RestaurantUtility restaurantUtility;
    private OnListItemSelectedListener mListener;
    android.util.LruCache<String,String> distanceMermoryCache;
    ArrayList<String> searchArrayList;
    AutoCompleteTextView search = null;
    MyApplication stateManager;
    TextView noEvents;
    TextView citySearch;
    String searchText =null;
    ProgressBar indeterminateProgress;
    ScaleInAnimationAdapter scaleIdAnimatorAdapter;

    public RestaurantListFragment() {
    }

    public interface OnListItemSelectedListener {
        void onListItemSelected(int position, HashMap<String, ?> restaurantMap, Bitmap bitmap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        restaurantUtility = new RestaurantUtility();
        restaurantData = new RestaurantData();
        searchArrayList = new ArrayList<>();
        stateManager = (MyApplication) getActivity().getApplicationContext();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Restaurants");


        if (mImgMemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 18;

            mImgMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        if(distanceMermoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
            final int cacheSize = maxMemory/128;
            distanceMermoryCache = new android.util.LruCache<String,String>(cacheSize) {

                @Override
                protected int sizeOf(String key, String value) {
                    return value.length()/1024;
                }
            };
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.restList);
        noEvents = (TextView)rootView.findViewById(R.id.noEventsMessage);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRestaurantAdapter = new RestaurantAdapter(getActivity(), restaurantData.getRestaurantList(), mImgMemoryCache,distanceMermoryCache);
      //  mRecyclerView.setAdapter(mRestaurantAdapter);

        scaleIdAnimatorAdapter = new ScaleInAnimationAdapter(mRestaurantAdapter);
        // alphaAdapter.setInterpolator(new OvershootInterpolator());
        scaleIdAnimatorAdapter.setFirstOnly(true);
        mRecyclerView.setAdapter(scaleIdAnimatorAdapter);


        indeterminateProgress = (ProgressBar) rootView.findViewById(R.id.indeterminate_progress_library);

        ButterKnife.inject(getActivity());
        indeterminateProgress.setIndeterminateDrawable(new IndeterminateProgressDrawable(getActivity().getApplicationContext()));



        if (restaurantData.getSize() == 0) {
            MyDownloadJsonAsyncTask downloadJson = new MyDownloadJsonAsyncTask(scaleIdAnimatorAdapter, stateManager.getApplicationManager().getFullyQualifiedCityName());
            downloadJson.execute();
        }

        mRestaurantAdapter.setOnItemClickListener(new RestaurantAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                HashMap<String, ?> restaurant = (HashMap<String, ?>)restaurantData.getItem(position);
                if(null != restaurant && null != mImgMemoryCache){
                    Bitmap bitmap = (Bitmap)mImgMemoryCache.get(restaurant.get("icon"));
                    mListener.onListItemSelected(position, restaurant, bitmap);
                }

            }
        });

        return rootView;
    }

    private class MyDownloadJsonAsyncTask extends AsyncTask<String, Void, RestaurantUtility> {
        private final WeakReference<ScaleInAnimationAdapter> adapterReference;
        String mCity;

        public MyDownloadJsonAsyncTask(ScaleInAnimationAdapter adapter, String city) {
            adapterReference = new WeakReference<>(adapter);
            mCity = city;
        }

        @Override
        protected RestaurantUtility doInBackground(String... urls) {
            restaurantUtility.downloadRestaurantData(mCity);
            return restaurantUtility;
        }

        @Override
        protected void onPostExecute(RestaurantUtility utilityData) {
            restaurantData.getRestaurantList().clear();

            indeterminateProgress.setVisibility(View.INVISIBLE);

            if(utilityData.getRestaurantList().size() == 0) {
                noEvents.setVisibility(View.VISIBLE);
            }else {
                noEvents.setVisibility(View.GONE);
            }

            for (int i = 0; i < utilityData.getSize(); i++) {
                restaurantData.getRestaurantList().add(utilityData.getRestaurantList().get(i));
            }

            if (adapterReference != null) {
                final ScaleInAnimationAdapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnListItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement onFragmentInteraction Listener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list_event, menu);
        ActionBar actionBar = ((CreateRestEventActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("");
        ImageView searchGPS = null;

        MenuItem menuItem = menu.findItem(R.id.action_search);
        if (menuItem != null) {
            search = (AutoCompleteTextView) menuItem.getActionView().findViewById(R.id.citySearch);
            searchGPS = (ImageView) menuItem.getActionView().findViewById(R.id.search_icon);
            citySearch = (TextView)menuItem.getActionView().findViewById(R.id.textView);

        }

        if(searchGPS !=null) {
            searchGPS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    citySearch.setVisibility(View.GONE);
                    search.setVisibility(View.VISIBLE);
                    search.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                    search.setText("");
                    search.setFocusable(true);
                }
            });
        }


        if (search != null) {

            searchText = stateManager.getApplicationManager().getFullyQualifiedCityName();
            citySearch.setText(searchText);

            //set default city
            search.setText(stateManager.getApplicationManager().getFullyQualifiedCityName());
            //added for autocomplete of text with google places
            search.setThreshold(3);

            //initialize this array with data
            final CityArrayAdapter adapter = new CityArrayAdapter(getActivity().getApplicationContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    android.R.id.text1);
            search.setAdapter(adapter);

            search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    mImgMemoryCache.evictAll();
                    restaurantData.getRestaurantList().clear();
                    mRecyclerView.removeAllViews();
                    indeterminateProgress.setVisibility(View.VISIBLE);
                    String city = adapter.getItem(position);
                    searchText = city;
                    citySearch.setText(searchText);
                    citySearch.setVisibility(View.VISIBLE);
                    search.setVisibility(View.GONE);

                    MyDownloadJsonAsyncTask downloadJson = new MyDownloadJsonAsyncTask(scaleIdAnimatorAdapter, city);
                    downloadJson.execute();
                    mRestaurantAdapter.notifyDataSetChanged();
                }
            });

        }
    }


}
