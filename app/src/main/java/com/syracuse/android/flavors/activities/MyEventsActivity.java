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
import com.syracuse.android.flavors.adapters.MyEventsPageAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*View Events created by user*/
public class MyEventsActivity extends BaseActivity {

    ViewPager viewPager;
    MyEventsPageAdapter myFragmentPageAdapter;
    List<Map<String, String>> myEventsListFinal;
    TextView noEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myEventsListFinal = new ArrayList<>();
        myFragmentPageAdapter = new MyEventsPageAdapter(getSupportFragmentManager(), myEventsListFinal);
        noEvents = (TextView) findViewById(R.id.noEventsMessage);
        viewPager = (ViewPager) findViewById(R.id.pager);
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
        getMenuInflater().inflate(R.menu.menu_my_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class MyEventsDownloadAsyncTask extends AsyncTask<String, Void, List<ParseObject>> {

        private String id;
        private final WeakReference<MyEventsPageAdapter> adopterReference;

        public MyEventsDownloadAsyncTask(MyEventsPageAdapter myFragmentPageAdapter, String userId) {
            adopterReference = new WeakReference<>(myFragmentPageAdapter);
            this.id = userId;
        }

        @Override
        protected List<ParseObject> doInBackground(String... params) {
            List<ParseObject> myEventsList = new ArrayList<>();
            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");
                query.whereGreaterThanOrEqualTo("StartTime", new Date());
                query.whereEqualTo("UserId", id);
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
                int maxGuests = myEventsList.get(i).getInt("MaxGuests");
                int guestsAvailable = myEventsList.get(i).getInt("GuestsAvailable");
                String address = myEventsList.get(i).getString("Address");
                String objectId = myEventsList.get(i).getObjectId();

                Map map = new HashMap();
                map.put("EventName", name);
                map.put("Menu", menu);
                map.put("MaxGuests", maxGuests);
                map.put("GuestsAvailable", guestsAvailable);
                map.put("StartTime", startTime);
                map.put("EndTime", endTime);
                map.put("ObjectId", objectId);
                map.put("Address", address);
                myEventsListFinal.add(map);
            }
            final MyEventsPageAdapter adapter = adopterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(MyEventsActivity.this, EventActivity.class);
        i.putExtra("Activity", "EventActivity");
        startActivity(i);
        finish();
        super.onBackPressed();
    }
}
