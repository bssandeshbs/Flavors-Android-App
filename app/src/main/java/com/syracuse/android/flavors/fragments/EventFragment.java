package com.syracuse.android.flavors.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.EventActivity;
import com.syracuse.android.flavors.activities.LoginActivity;
import com.syracuse.android.flavors.adapters.CityArrayAdapter;
import com.syracuse.android.flavors.adapters.EventRecyclerAdapter;
import com.syracuse.android.flavors.model.EventDetails;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;


/**
 *  Fragment which displays the list of events
 *
 *  Created by Rahul and Sandesh
 *
 */
public class EventFragment extends Fragment {

    private static final String ARG_OPTION = "option";
    private RecyclerView eventRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EventRecyclerAdapter eventAdapter;
    List<ParseObject> eventDetailList;
    Date trendingDate;
    int tabIndex;
    private OnListItemSelectedListener mListener;
    MyApplication stateManager;
    AutoCompleteTextView search = null;
    TextView citySearch;
    String searchText =null;
    ProgressBar indeterminateProgress;
    LruCache<String,String> distanceMermoryCache;
    TextView noEvents;
    ScaleInAnimationAdapter alphaAdapter;

    public interface OnListItemSelectedListener {
        void onListItemSelected(int position, ParseObject object);
    }

    public EventFragment() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 30);
        trendingDate = cal.getTime();
    }

    // create a new instance of event fragment
    public static EventFragment newInstance(int option) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_OPTION, option);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnListItemSelectedListener)activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString()+"must implement onFragmentInteraction Listener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        stateManager = (MyApplication) ((EventActivity)getActivity()).getApplicationContext();

        if(distanceMermoryCache == null) {
            final int maxmemory = (int) (Runtime.getRuntime().maxMemory()/1024);
            final int cacheSize = maxmemory/128;
            distanceMermoryCache = new LruCache<String,String>(cacheSize) {

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
        searchText = stateManager.getApplicationManager().getFullyQualifiedCityName();
        if( searchText == null )  {
            Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(mainIntent);
        }
        tabIndex = this.getArguments().getInt(ARG_OPTION);
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);
        eventDetailList = new ArrayList<>();
        eventRecyclerView = (RecyclerView)rootView.findViewById(R.id.eventList);
        eventRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        eventRecyclerView.setLayoutManager(mLayoutManager);
        eventAdapter = new EventRecyclerAdapter(getActivity(),eventDetailList,distanceMermoryCache,(int)getArguments().get(ARG_OPTION));

        //Animation
        alphaAdapter = new ScaleInAnimationAdapter(eventAdapter);
        alphaAdapter.setFirstOnly(true);
        eventRecyclerView.setAdapter(alphaAdapter);


        noEvents = (TextView)rootView.findViewById(R.id.noEventsMessage);
        eventAdapter.setOnItemClickListener(new EventRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //get event data from Events table
                EventDetailsAsyncTask task = new EventDetailsAsyncTask(mListener, position);
                task.execute();
            }
        });

        indeterminateProgress = (ProgressBar) rootView.findViewById(R.id.indeterminate_progress_library_event);
        indeterminateProgress.setVisibility(View.VISIBLE);
        ButterKnife.inject(getActivity());
        indeterminateProgress.setIndeterminateDrawable(new IndeterminateProgressDrawable(getActivity().getApplicationContext()));

        if(eventDetailList.size() == 0) {
            EventsDownloadAsyncTask task = new EventsDownloadAsyncTask(alphaAdapter, stateManager.getApplicationManager().getFullyQualifiedCityName());
            task.execute();
        }
         return rootView;
    }

    // Async Task to get the event details
    private class EventDetailsAsyncTask extends AsyncTask<String,Void,ParseObject> {
        private final WeakReference<OnListItemSelectedListener> weakListener;
        private int position;
        public EventDetailsAsyncTask(OnListItemSelectedListener listener,int pos) {
            weakListener = new WeakReference<>(listener);
            position = pos;
        }


        @Override
        protected ParseObject doInBackground(String... params) {
            EventDetails detail = (EventDetails)eventDetailList.get(position);
            String id =  detail.getObjectId();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");
            query.whereEqualTo("objectId", id);
            List<ParseObject> eventList = new ArrayList<>();
            query.selectKeys(Arrays.asList("objectId", "EventName", "Address", "StartTime", "EndTime",
                    "MaxGuests", "GuestsAvailable", "Menu", "Cost", "Latitude", "Longitude","UserId"));
            try {
                eventList = query.find();
            }catch(ParseException e){
                e.printStackTrace();
            }
            return eventList.get(0);
        }

        @Override
        protected  void onPostExecute(ParseObject object) {
            EventDetails myevent = (EventDetails) object;
            Log.v(EventFragment.class.getCanonicalName(),String.valueOf(myevent.getString("EventName")));
            mListener.onListItemSelected(position,myevent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list_event, menu);
        ActionBar actionBar = ((EventActivity)getActivity()).getSupportActionBar();
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
            //set default city

            searchText = stateManager.getApplicationManager().getFullyQualifiedCityName();
            citySearch.setText(searchText);

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
                    eventDetailList.clear();
                    eventRecyclerView.removeAllViews();
                    indeterminateProgress.setVisibility(View.VISIBLE);
                    String city = adapter.getItem(position);
                    searchText = city;
                    citySearch.setText(searchText);
                    citySearch.setVisibility(View.VISIBLE);
                    search.setVisibility(View.GONE);
                    stateManager.getApplicationManager().setFullyQualifiedCityName(city);

                    EventsDownloadAsyncTask downloadJson = new EventsDownloadAsyncTask(alphaAdapter, city);
                    downloadJson.execute();

                    Intent i = new Intent(getActivity(), EventActivity.class);
                    i.putExtra("Activity", "EventActivity");
                    startActivity(i);
                }
            });
        }
    }

    // Async task to get the events
    private class EventsDownloadAsyncTask extends AsyncTask<String,Void,List<ParseObject>> {

        private final WeakReference<ScaleInAnimationAdapter> adopterReference;
        private String cityName;
        public EventsDownloadAsyncTask(ScaleInAnimationAdapter myFragmentPageAdapter, String city) {
            adopterReference = new WeakReference<>(myFragmentPageAdapter);
            cityName = city;
        }

        @Override
        protected  List<ParseObject> doInBackground(String... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");
            query.whereGreaterThanOrEqualTo("StartTime", new Date());
            if(tabIndex == 2){
                query.whereLessThanOrEqualTo("StartTime", trendingDate);
            }

            //condition to check city name
            query.whereEqualTo("City", cityName);
            List<ParseObject> eventList = new ArrayList<>();
            try {
                eventList = query.find();
                Log.d("result"+eventList.size(),"size");
            }catch(ParseException e){
                e.printStackTrace();
            }

            if(tabIndex == 1){
                List<ParseObject> trendingEvents = new ArrayList();
                for(int i = 0; i < eventList.size(); i++){
                    int maxGuests = (int)eventList.get(i).get("MaxGuests");
                    int guestsAvailable = (int)eventList.get(i).get("GuestsAvailable");
                    float ratioGuests = (guestsAvailable * 100.0f) / maxGuests;
                    if(ratioGuests <= 50.0f){
                        trendingEvents.add(eventList.get(i));
                    }
                }
                return trendingEvents;
            }
            return eventList;
        }

        @Override
        protected void onPostExecute(List<ParseObject> myEventsList) {
            eventDetailList.clear();
            eventDetailList.addAll(myEventsList);
            if(myEventsList.size() ==0) {
                noEvents.setVisibility(View.VISIBLE);
            } else {
                noEvents.setVisibility(View.INVISIBLE);
            }
            indeterminateProgress.setVisibility(View.INVISIBLE);
            if(adopterReference != null) {
                final ScaleInAnimationAdapter adapter = adopterReference.get();
                if(adapter != null)  {
                    adapter.notifyDataSetChanged();
                    Log.d("Count",adapter.getItemCount()+" :"+eventAdapter.getItemCount());
                }
            }
        }
    }
}
