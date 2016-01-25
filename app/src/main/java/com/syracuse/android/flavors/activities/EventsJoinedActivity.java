package com.syracuse.android.flavors.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.adapters.EventsJoinedPageAdapter;
import com.syracuse.android.flavors.model.EventRegisteredUsers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Activity to View Events Joined by user */
public class EventsJoinedActivity extends BaseActivity {

    ViewPager viewPager;
    EventsJoinedPageAdapter myFragmentPageAdapter;
    List<Map<String, String>> myEventsListFinal;
    TextView noEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myEventsListFinal = new ArrayList<>();
        myFragmentPageAdapter = new EventsJoinedPageAdapter(getSupportFragmentManager(), myEventsListFinal);
        viewPager = (ViewPager) findViewById(R.id.pager);
        noEvents = (TextView) findViewById(R.id.noEventsMessage);
        viewPager.setAdapter(myFragmentPageAdapter);
        viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                final float normalized_position = Math.abs(Math.abs(position) - 1);
                page.setScaleX(normalized_position / 2 + 0.5f);
                page.setScaleY(normalized_position / 2 + 0.5f);
            }
        });
        MyApplication application = (MyApplication) getApplicationContext();
        String id = application.getApplicationManager().getId();

        MyEventsDownloadAsyncTask asyncTask = new MyEventsDownloadAsyncTask(myFragmentPageAdapter, id);
        asyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events_joined, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class MyEventsDownloadAsyncTask extends AsyncTask<String, Void, List<ParseObject>> {

        private final WeakReference<EventsJoinedPageAdapter> adopterReference;
        private final String userId;

        public MyEventsDownloadAsyncTask(EventsJoinedPageAdapter myFragmentPageAdapter, String id) {
            adopterReference = new WeakReference<>(myFragmentPageAdapter);
            this.userId = id;
        }

        @Override
        protected List<ParseObject> doInBackground(String... params) {
            List<ParseObject> myEventsList = new ArrayList<>();
            List<ParseObject> registeredUserList;
            try {
                ParseQuery<ParseObject> eventRegisteredUsersQuery = ParseQuery.getQuery("EventRegisteredUsers");
                eventRegisteredUsersQuery.whereEqualTo("UserId", userId);
                eventRegisteredUsersQuery.selectKeys(Arrays.asList("EventId"));
                registeredUserList = eventRegisteredUsersQuery.find();
                List<String> eventList = new ArrayList<>();
                for (int i = 0; i < registeredUserList.size(); i++) {
                    int size = registeredUserList.size();
                    Log.v("tet", String.valueOf(size));
                    EventRegisteredUsers user = (EventRegisteredUsers) registeredUserList.get(i);
                    String event = user.getEventId();
                    eventList.add(event);
                }

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");
                query.whereGreaterThanOrEqualTo("StartTime", new Date());
                query.whereContainedIn("objectId", eventList);
                query.selectKeys(Arrays.asList("EventName", "StartTime", "EndTime", "Latitude", "Longitude", "Menu", "Address", "UserId"));
                myEventsList = query.find();

            } catch (ParseException e) {
                Log.e(MyEventsActivity.class.getCanonicalName(), "Exception occurred getting the events :" + e.getMessage());
            }
            return myEventsList;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(List<ParseObject> myEventsList) {
            if (myEventsList.size() == 0) {
                noEvents.setVisibility(View.VISIBLE);
            } else {
                noEvents.setVisibility(View.INVISIBLE);
            }

            for (int i = 0; i < myEventsList.size(); i++) {
                String name = myEventsList.get(i).getString("EventName");
                String menu = myEventsList.get(i).getString("Menu");
                Date startTime = myEventsList.get(i).getDate("StartTime");
                Date endTime = myEventsList.get(i).getDate("EndTime");
                String address = myEventsList.get(i).getString("Address");
                String eventId = myEventsList.get(i).getObjectId();
                String userId = myEventsList.get(i).getString("UserId");
                Double latitude = (Double) myEventsList.get(i).getNumber("Latitude");
                Double longitude = (Double) myEventsList.get(i).getNumber("Longitude");

                Map map = new HashMap<>();

                map.put("EventName", name);
                map.put("Menu", menu);
                map.put("StartTime", startTime);
                map.put("EndTime", endTime);
                map.put("EventId", eventId);
                map.put("Address", address);
                map.put("EventId", eventId);
                map.put("UserId", userId);
                map.put("Latitude", latitude);
                map.put("Longitude", longitude);

                myEventsListFinal.add(map);
            }
            final EventsJoinedPageAdapter adapter = adopterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(EventsJoinedActivity.this, EventActivity.class);
        i.putExtra("Activity", "EventActivity");
        startActivity(i);
        finish();
        super.onBackPressed();
    }
}
