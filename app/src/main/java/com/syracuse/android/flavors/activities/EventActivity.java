package com.syracuse.android.flavors.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseObject;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.adapters.EventListAdapter;
import com.syracuse.android.flavors.fragments.EventFragment;
import com.syracuse.android.flavors.model.EventDetails;
import com.syracuse.android.flavors.util.ParseProxyObject;

//Activity for Events
public class EventActivity extends BaseActivity implements
        EventFragment.OnListItemSelectedListener {

    EventListAdapter eventListAdapter;
    ViewPager eventViewPager;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventListAdapter = new EventListAdapter(getSupportFragmentManager());
        eventViewPager = (ViewPager) findViewById(R.id.eventViewPager);
        eventViewPager.setAdapter(eventListAdapter);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            System.exit(1);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemSelected(int position, ParseObject object) {
        Log.v("parse", "object");
        EventDetails eventObj = (EventDetails) object;
        String name = eventObj.getObjectId();
        Log.v("parse", name);
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("Activity", "EventDetailActivity");
        ParseProxyObject ppo = new ParseProxyObject(eventObj);
        intent.putExtra("parseEventObj", ppo);
        startActivity(intent);
    }
}

