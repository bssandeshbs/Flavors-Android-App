package com.syracuse.android.flavors.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.adapters.NavigationDrawerAdapter;
import com.syracuse.android.flavors.fragments.SettingsFragment;
import com.syracuse.android.flavors.fragments.ViewUserProfileFragment;
import com.syracuse.android.flavors.util.DrawerData;
import java.lang.ref.WeakReference;

/*Base Activity for all activities*/
public class BaseActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mDrawerList;
    private NavigationDrawerAdapter mNavigationDrawerAdapter;
    private com.pkmmte.view.CircularImageView profilePic;
    private TextView name;
    private TextView email;
    ImageView imageView = null;
    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        imageLoader = ImageLoader.getInstance();
        super.onCreate(savedInstanceState);
        String callingActivity = getIntent().getStringExtra("Activity");

        //switch to inflate custom layout for activities
        if(callingActivity != null) {
            switch (callingActivity) {
                case "EventActivity":
                    setContentView(R.layout.activity_event_list);
                    break;
                case "MyEventsActivity":
                    setContentView(R.layout.activity_my_events);
                    break;
                case "EventsJoined":
                    setContentView(R.layout.activity_events_joined);
                    break;
                case "RestaurantActivity":
                    setContentView(R.layout.activity_create_rest_event);
                    break;
                case "Profile":
                    setContentView(R.layout.activity_base);
                    String id = getIntent().getStringExtra("id");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, ViewUserProfileFragment.newInstance(id,"view_profile_screen"))
                            .commit();
                    break;
                case "Settings":
                    setContentView(R.layout.activity_base);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new SettingsFragment())
                            .commit();
                    break;
                default:
                    setContentView(R.layout.activity_base);
                    break;
            }
        }else{
            setContentView(R.layout.activity_base);
        }

        //set toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("Flavors");
        //set fb profile picture and other details
        profilePic = (com.pkmmte.view.CircularImageView) findViewById(R.id.circleView);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                MyApplication application = (MyApplication) getApplicationContext();
                String id = application.getApplicationManager().getId();
                Intent profileIntent = new Intent(BaseActivity.this, BaseActivity.class);
                profileIntent.putExtra("Activity", "Profile");
                profileIntent.putExtra("id", id);
                startActivity(profileIntent);
                finish();

            }
        });
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);

        //Setup the recycler view for Navigation Drawer
        mDrawerList = (RecyclerView) findViewById(R.id.drawer_list);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mNavigationDrawerAdapter = new NavigationDrawerAdapter(this, new DrawerData().getDrawerList());
        mNavigationDrawerAdapter.setOnItemClickListener(new NavigationDrawerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectItem(position);
            }
        });
        mDrawerList.setAdapter(mNavigationDrawerAdapter);

        //Setup the drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View view) {
                initialize();
                super.onDrawerOpened(view);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onPostCreate(Bundle savedInstanceState) {
        mDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    //Switch case for navigation drawer selection
    private void selectItem (int position){
        mDrawerLayout.closeDrawers();
        switch (position){
            case 0:
                Intent eventIntent = new Intent(this, EventActivity.class);
                eventIntent.putExtra("Activity", "EventActivity");
                eventIntent.putExtra("position", position);
                startActivity(eventIntent);
                finish();
                break;
            case 1:
                Intent myEventIntent = new Intent(this, MyEventsActivity.class);
                myEventIntent.putExtra("Activity", "MyEventsActivity");
                myEventIntent.putExtra("position", position);
                startActivity(myEventIntent);
                finish();
                break;
            case 2:
                Intent eventsJoinedIntent = new Intent(this, EventsJoinedActivity.class);
                eventsJoinedIntent.putExtra("Activity", "EventsJoined");
                eventsJoinedIntent.putExtra("position", position);
                startActivity(eventsJoinedIntent);
                finish();
                break;
            case 3:
                Intent intent = new Intent(this, CreateHomeEventActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
                finish();
                break;
            case 4:
                Intent restaurantIntent = new Intent(this, CreateRestEventActivity.class);
                restaurantIntent.putExtra("Activity", "RestaurantActivity");
                restaurantIntent.putExtra("position", position);
                startActivity(restaurantIntent);
                finish();
                break;
            case 5:
                Intent settingsIntent = new Intent(this, BaseActivity.class);
                settingsIntent.putExtra("Activity", "Settings");
                settingsIntent.putExtra("position", position);
                startActivity(settingsIntent);
                break;
            case 6:
                MyApplication application = (MyApplication) getApplicationContext();
                String id = application.getApplicationManager().getId();
                Intent profileIntent = new Intent(this, BaseActivity.class);
                profileIntent.putExtra("Activity", "Profile");
                profileIntent.putExtra("id", id);
                profileIntent.putExtra("position", position);
                startActivity(profileIntent);
                break;
            case 7:
                Intent logOffIntent = new Intent(this,LoginActivity.class);
                logOffIntent.putExtra("LOGIN_STATUS", 100);
                logOffIntent.putExtra("position", position);
                startActivity(logOffIntent);
                finish();
                break;
            default:
                break;
        }
    }

    //Async task to download facebook image
    private class MyDownloadImageAsyncTask extends AsyncTask<String, Void, Void> {
        private final WeakReference<com.pkmmte.view.CircularImageView> imageViewWeakReference;
        private final WeakReference<TextView> nameWeakReference;
        private final WeakReference<TextView> emailWeakReference;

        public MyDownloadImageAsyncTask(com.pkmmte.view.CircularImageView image, TextView nameTV, TextView emailTV) {
            imageViewWeakReference = new WeakReference<>(image);
            nameWeakReference = new WeakReference<>(nameTV);
            emailWeakReference = new WeakReference<>(emailTV);
        }

        @Override
        protected Void doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (imageViewWeakReference != null) {
                imageView = imageViewWeakReference.get();
                MyApplication application = (MyApplication) getApplicationContext();
                String userID = application.getApplicationManager().getId();
                Picasso.with(getApplicationContext()).load("https://graph.facebook.com/" + userID + "/picture?type=large").resize(80, 80).into(imageView);
            }
            if (nameWeakReference != null && emailWeakReference != null) {
                final TextView name = nameWeakReference.get();
                final TextView email = emailWeakReference.get();
                MyApplication application = (MyApplication) getApplicationContext();
                String emailVal = application.getApplicationManager().getEmail();
                String nameVal = application.getApplicationManager().getUserName();
                name.setText(nameVal);
                email.setText(emailVal);
            }
        }
    }

    public void initialize() {
        if(profilePic.getDrawable() == null) {
            MyDownloadImageAsyncTask task = new MyDownloadImageAsyncTask(profilePic, name, email);
            task.execute();
        }
    }
}
