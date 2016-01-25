package com.syracuse.android.flavors.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.EventDetailActivity;
import com.syracuse.android.flavors.model.EventImages;
import com.syracuse.android.flavors.model.EventRegisteredUsers;
import com.syracuse.android.flavors.model.EventUsers;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by Sandesh
 *
 * Fragment displaying the complete event information
 */
public class EventDetailFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener , ObservableScrollViewCallbacks {

    private static HashMap<String, Object> eventMap;

    TextView eventNameTV;
    TextView menuTV;
    TextView startDateTV;
    TextView locationTV;
    TextView attendText;
    Button createdBy;
    Button joinEvent;
    ImageView eventCreatorImage;
    List<ParseObject> eventImagesList;
    SliderLayout sliderShow;

    private String hostedBy;
    private String id;
    private HashMap<Integer, String> userMap;
    ShareActionProvider shareActionProvider;
    private int guestsAvailable;
    String eventName;
    String foodMenu;
    String address;
    Calendar startCal;
    Calendar endCal;
    String displayDate;
    int defaultImg;

    private int mParallaxImageHeight;
    Toolbar bar;

    public EventDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        final MyApplication application = (MyApplication) getActivity().getApplicationContext();
        id = application.getApplicationManager().getId();
        userMap = new HashMap<>();

        //universal image loader
        new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20)).build();
    }


    public static EventDetailFragment newInstance(HashMap<String, Object> values) {
        EventDetailFragment fragment = new EventDetailFragment();
        eventMap = values;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        if(getActivity() !=null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Event Details");
        }

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            bar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            bar.setBackgroundColor(Color.parseColor("#00FFFFFF"));
            final EventDetailActivity activity = (EventDetailActivity)getActivity();
            bar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null != activity) {
                        activity.onBackPressed();
                    }
                }
            });
        }
        Double latitude = (Double) eventMap.get("Latitude");
        Double longitude = (Double) eventMap.get("Longitude");
        address = (String) eventMap.get("Address");
        foodMenu = (String) eventMap.get("Menu");

        Date startDate = (Date) eventMap.get("StartTime");
        Date endDate = (Date) eventMap.get("EndTime");

        startCal = new GregorianCalendar();
        startCal.setTime(startDate);

        endCal = new GregorianCalendar();
        endCal.setTime(endDate);

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE, dd MMM yyyy 'at' hh:mm a");
        displayDate = inputFormat.format(startDate);

        LatLng location = new LatLng(latitude, longitude);
        hostedBy = (String) eventMap.get("UserId");
        final String objectId = (String) eventMap.get("ObjectId");
        guestsAvailable = (int) eventMap.get("GuestsAvailable");
        eventName = (String) eventMap.get("EventName");
        eventNameTV = (TextView) rootView.findViewById(R.id.eventNameTV);
        eventNameTV.setText(eventName);

        sliderShow = (SliderLayout) rootView.findViewById(R.id.slider);
        sliderShow.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);

        createdBy = (Button) rootView.findViewById(R.id.userCreatedButton);
        eventCreatorImage = (ImageView) rootView.findViewById(R.id.eventOrganiserPic);

        createdBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ViewUserProfileFragment.newInstance(hostedBy,"event_detail_screen"))
                        .commit();
            }
        });
        menuTV = (TextView) rootView.findViewById(R.id.menuTV);
        startDateTV = (TextView) rootView.findViewById(R.id.DateTV);
        locationTV = (TextView) rootView.findViewById(R.id.locationTV);
        joinEvent = (Button) rootView.findViewById(R.id.joinEventBt);
        attendText = (TextView) rootView.findViewById(R.id.attendText);

        EventRegisterAsyncTask registerTask = new EventRegisterAsyncTask(objectId);
        registerTask.execute();


        joinEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EventRegisteredUsers users = new EventRegisteredUsers();
                users.setUserId(id);
                users.setEventId(objectId);
                users.saveInBackground();

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");

                // Retrieve the object by id
                query.getInBackground(objectId, new GetCallback<ParseObject>() {
                    public void done(ParseObject event, ParseException e) {
                        if (e == null) {
                            event.put("GuestsAvailable", guestsAvailable - 1);
                            event.saveInBackground();
                        }
                    }
                });
                new SweetAlertDialog(v.getContext())
                        .setTitleText("Congratulations!")
                        .setContentText("Successfully registered for the event!")
                        .show();
                attendText.setText("You are registered for this event");
                joinEvent.setVisibility(View.GONE);
            }
        });

        menuTV.setText(foodMenu);
        startDateTV.setText(displayDate);
        locationTV.setText(address);

        GoogleMap map = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(location)
                .title("House Event Address")
                .snippet(address)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_plotter)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));

        final ScrollView mainScrollView = (ScrollView) rootView.findViewById(R.id.eventDetailScrollView);
        ImageView transparentImageView = (ImageView) rootView.findViewById(R.id.transparent_image);
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        ObservableScrollView mScrollView = (ObservableScrollView) rootView.findViewById(R.id.eventDetailScrollView);
        mScrollView.setScrollViewCallbacks(this);

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        MyEventsImageDownloadAsyncTask task = new MyEventsImageDownloadAsyncTask((String) eventMap.get("ObjectId"));
        task.execute();

        EventCreaterNameDownload createrNameTask = new EventCreaterNameDownload(createdBy, hostedBy);
        createrNameTask.execute();

        EventCreatorImageDownloadAsyncTask creatorImage = new EventCreatorImageDownloadAsyncTask(eventCreatorImage, hostedBy);
        creatorImage.execute();

        return rootView;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.colorPrimary);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        bar.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(sliderShow, scrollY / 2);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private class EventRegisterAsyncTask extends AsyncTask<String, Void, List<ParseObject>> {
        private String mObjectId;

        public EventRegisterAsyncTask(String objectId) {
            mObjectId = objectId;
        }


        @Override
        protected List<ParseObject> doInBackground(String... params) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("EventRegisteredUsers");
            query.whereEqualTo("EventId", mObjectId);
            List<ParseObject> eventRegisterList = new ArrayList<>();
            query.selectKeys(Collections.singletonList("UserId"));
            try {
                eventRegisterList = query.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return eventRegisterList;
        }

        @Override
        protected void onPostExecute(List<ParseObject> listObjects) {
            EventRegisteredUsers registeredUser;
            if (listObjects.size() > 0) {
                for (int i = 0; i < listObjects.size(); i++) {
                    registeredUser = (EventRegisteredUsers) listObjects.get(i);
                    userMap.put(i, (String) registeredUser.get("UserId"));
                }
            }
            checkUser();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        sliderShow.stopAutoCycle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sliderShow.removeAllSliders();
        sliderShow.removeAllViews();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {

    }

    // Async to get the list of event images for the specific event selected
    private class MyEventsImageDownloadAsyncTask extends AsyncTask<String, String, List<String>> implements BaseSliderView.OnSliderClickListener {
        private String objectId;
        public MyEventsImageDownloadAsyncTask(String id) {
            objectId = id;
        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> imageUrls = new ArrayList<>();
            String imageUrl;

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventImages");
                query.whereEqualTo("EventId", objectId);
                query.selectKeys(Collections.singletonList("ImageFile"));
                eventImagesList = query.find();
                Log.d("result" + eventImagesList.size(), "size");
            } catch (ParseException e) {
                e.printStackTrace();
            }


            EventImages image;
            ParseFile eventIcon;
            //set event icon
            for (int i = 0; i < eventImagesList.size(); i++) {
                image = (EventImages) eventImagesList.get(i);
                eventIcon = image.getImageFile();
                imageUrl = eventIcon.getUrl();
                imageUrls.add(imageUrl);
            }

            if (eventImagesList.size() == 0) {
                defaultImg = R.drawable.default_image;
            }
            return imageUrls;
        }

        @Override
        protected void onPostExecute(List<String> imageUrls) {
            if (imageUrls != null && imageUrls.size() > 0) {
                for (String url : imageUrls) {
                    TextSliderView textSliderView = new TextSliderView(getActivity());
                    // initialize a SliderLayout
                    textSliderView.image(url)
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(this);

                    if (sliderShow != null) {
                        sliderShow.addSlider(textSliderView);
                    }
                }
                sliderShow.setVisibility(View.VISIBLE);
                if (imageUrls.size() == 1) {
                    sliderShow.setDuration(98000000);
                } else {
                    sliderShow.setDuration(8000);
                    sliderShow.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
                }

            } else {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView.image(defaultImg)
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);
                if (sliderShow != null) {
                    sliderShow.addSlider(textSliderView);
                    sliderShow.setVisibility(View.VISIBLE);
                    sliderShow.setDuration(98000000);
                }
            }
        }

        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            Log.v("Slider", "Clicked");
        }
    }


    //Async Task to get the event creator name
    private class EventCreaterNameDownload extends AsyncTask<String, String, String> {
        private final WeakReference<Button> buttonViewWeakReference;
        private String objectId;

        public EventCreaterNameDownload(Button button, String id) {
            buttonViewWeakReference = new WeakReference<>(button);
            objectId = id;
        }

        @Override
        protected String doInBackground(String... params) {
            List<ParseObject> eventImagesList = null;
            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
                query.whereEqualTo("Fb", objectId);
                query.selectKeys(Arrays.asList("Name"));
                eventImagesList = query.find();
                Log.d("result" + eventImagesList.size(), "size");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (eventImagesList != null && eventImagesList.size() > 0) {
                EventUsers users = (EventUsers) eventImagesList.get(0);
                return users.getName();
            } else return null;
        }

        @Override
        protected void onPostExecute(String buttonName) {
            if (buttonName != null) {
                final Button button = buttonViewWeakReference.get();
                if (button != null) {
                    button.setText("Event By \n" + buttonName);
                }
            }
        }
    }

    //method to check event registration for the logged in user
    private void checkUser() {
        if (hostedBy.equals(id)) {
            attendText.setText("This event is hosted by you");
            joinEvent.setVisibility(View.GONE);
        } else if (userMap.containsValue(id)) {
            attendText.setText("You are registered for this event");
            joinEvent.setVisibility(View.GONE);
        } else if (guestsAvailable == 0) {
            attendText.setText("This event is currently full!!");
            joinEvent.setVisibility(View.GONE);
        } else {
            attendText.setText("Would you like to register?");
            joinEvent.setVisibility(View.VISIBLE);
        }
    }

    //async to get the event creator photo
    private class EventCreatorImageDownloadAsyncTask extends AsyncTask<String, String, String> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private String id;

        public EventCreatorImageDownloadAsyncTask(ImageView image, String fbId) {
            imageViewWeakReference = new WeakReference<>(image);
            id = fbId;
        }

        @Override
        protected String doInBackground(String... params) {
            List<ParseObject> eventImagesList = null;
            String imageUrl = null;
            try {

                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
                query.whereEqualTo("Fb", id);
                query.selectKeys(Arrays.asList("ProfilePic"));
                eventImagesList = query.find();
                Log.d("result" + eventImagesList.size(), "size");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            EventUsers image;
            ParseFile eventIcon;
            //set event icon
            if (eventImagesList != null && eventImagesList.size() > 0) {
                image = (EventUsers) eventImagesList.get(0);
                eventIcon = image.getProfilePic();
                imageUrl = eventIcon.getUrl();
            }
            return imageUrl;
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if (imageViewWeakReference != null && imageUrl != null) {
                final ImageView image = imageViewWeakReference.get();
                if (image != null) {
                    if(getActivity() !=null)
                    Picasso.with(getActivity().getApplicationContext()).load(imageUrl).into(image);

                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_event_joined_fragment, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("text/plain");
        StringBuilder eventDetails = new StringBuilder("Event Details\n");
        eventDetails.append("Event Name : " + eventName + "\n").append("Address : " + address + "\n")
                .append("Date : " + displayDate + "\n").append("Menu : " + foodMenu + "\n");
        String res = eventDetails.toString();
        intentShare.putExtra(Intent.EXTRA_TEXT, res);
        shareActionProvider.setShareIntent(intentShare);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_to_calendar:
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, eventName)
                        .putExtra(CalendarContract.Events.DESCRIPTION, "Items Served at Event :" + foodMenu)
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, address)
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                startActivity(intent);
                return true;
            case R.id.email_event_users:
                sendEmailNotification();
                return true;
        }

        return super.onOptionsItemSelected(item); // important line
    }

    // Function to send email notification to users
    public void sendEmailNotification() {
        List<ParseObject> userList;
        EventUsers user = null;
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
            query.whereEqualTo("Fb", hostedBy);
            query.selectKeys(Arrays.asList("Email"));
            userList = query.find();
            Log.d("result" + eventImagesList.size(), "size");
            if (userList != null && userList.size() > 0) {
                user = (EventUsers) userList.get(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (user != null && user.getEmail() != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            String[] address = {user.getEmail()};
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, address);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Question Regarding Food Feast Event  " + eventName);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Toast.makeText(getActivity(), "The event user has not provided email details", Toast.LENGTH_SHORT);
        }
    }
}
