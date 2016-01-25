package com.syracuse.android.flavors.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.fragments.CreateRestaurantEventFragment;
import com.syracuse.android.flavors.fragments.RestaurantListFragment;
import com.syracuse.android.flavors.model.EventDetails;

import java.util.HashMap;

//Activity for creating a Restaurant event
public class CreateRestEventActivity extends BaseActivity
        implements RestaurantListFragment.OnListItemSelectedListener{

    private EventDetails details;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        details = new EventDetails();
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RestaurantListFragment(),"restuarant_list_fragment").addToBackStack("restuarant_list_fragment")
                    .commit();
        }
    }

    public EventDetails getEventDetails() {
        return details;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {int id = item.getItemId();
    if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int  count = getSupportFragmentManager().getBackStackEntryCount();
        if(count <= 1) {
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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onListItemSelected(int position, HashMap<String, ?> restaurant, Bitmap bitmap) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CreateRestaurantEventFragment.newInstance(restaurant, bitmap), "create_restuarant_event")
                .addToBackStack("create_restaurant_event")
                .commit();
    }
}
