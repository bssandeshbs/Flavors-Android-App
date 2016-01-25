package com.syracuse.android.flavors.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.BaseActivity;
import com.syracuse.android.flavors.model.EventImages;
import com.syracuse.android.flavors.model.EventUsers;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment to display the events joined by the user
 * Created by Sandesh
 */
public class EventsJoinedFragment extends Fragment {

    List<ParseObject> eventImagesList;
    private static final String EVENTS = "argument_option";
    SliderLayout sliderShow;
    ShareActionProvider shareActionProvider;
    String eventName;
    Calendar startCal;
    Calendar endCal;
    String address;
    String foodMenu;
    MyApplication application;
    String hostedBy;
    String displayDate;
    int defaultImg;

    public EventsJoinedFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        application = (MyApplication) getActivity().getApplicationContext();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Events Joined");

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_events_joined, container, false);
        HashMap eventMap = (HashMap) this.getArguments().getSerializable(EVENTS);
        eventName = (String) eventMap.get("EventName");
        foodMenu = (String) eventMap.get("Menu");
        final Date beginTime = (Date) eventMap.get("StartTime");
        final Date endTime = (Date) eventMap.get("EndTime");
        address = (String) eventMap.get("Address");
        final Double latitude = (Double) eventMap.get("Latitude");
        final Double longitude = (Double) eventMap.get("Longitude");
        hostedBy = (String) eventMap.get("UserId");

        startCal = new GregorianCalendar();
        startCal.setTime(beginTime);

        endCal = new GregorianCalendar();
        endCal.setTime(endTime);

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE, dd MMM yyyy 'at' hh:mm a");
        displayDate = inputFormat.format(beginTime);

        ImageView eventsJoined = (ImageView)rootView.findViewById(R.id.navigationIcon);
        eventsJoined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+latitude+","+longitude));
                startActivity(intent);
            }
        });

        final TextView menuTV = (TextView) rootView.findViewById(R.id.menuTV);
        final TextView startTimeTV = (TextView) rootView.findViewById(R.id.startTimeTVVal);
        final TextView venueTV = (TextView) rootView.findViewById(R.id.venueTV);
        final ImageView profilePic = (ImageView) rootView.findViewById(R.id.profilePic);

        sliderShow = (SliderLayout) rootView.findViewById(R.id.slider);
        sliderShow.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
        sliderShow.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderShow.setDuration(8000);

        menuTV.setText((String) eventMap.get("Menu"));
        startTimeTV.setText(displayDate);
        venueTV.setText(address);

        Button createdBy = (Button) rootView.findViewById(R.id.createdBy);
        createdBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getActivity(), BaseActivity.class);
                profileIntent.putExtra("Activity", "Profile");
                profileIntent.putExtra("id", hostedBy);
                startActivity(profileIntent);
                getActivity().finish();
            }
        });

        MyEventsImageDownloadAsyncTask task = new MyEventsImageDownloadAsyncTask(sliderShow, (String) eventMap.get("EventId"));
        task.execute();

        EventCreaterNameDownload createrTask = new EventCreaterNameDownload(createdBy, hostedBy);
        createrTask.execute();

        MyProfileImageDownloadAsyncTask profilePicDw = new MyProfileImageDownloadAsyncTask(profilePic, hostedBy);
        profilePicDw.execute();

        return rootView;
    }


    public static EventsJoinedFragment newInstance(HashMap<String, ?> movie) {
        EventsJoinedFragment fragment = new EventsJoinedFragment();
        Bundle args = new Bundle();
        args.putSerializable(EVENTS, movie);
        fragment.setArguments(args);
        return fragment;

    }


    //TODO: Save use cache to store images, handle exception logging
    private class MyEventsImageDownloadAsyncTask extends AsyncTask<String, String, List<String>> implements BaseSliderView.OnSliderClickListener {
        private final WeakReference<SliderLayout> imageViewWeakReference;
        private String objectId;

        public MyEventsImageDownloadAsyncTask(com.daimajia.slider.library.SliderLayout layout, String id) {
            imageViewWeakReference = new WeakReference<>(layout);
            objectId = id;
        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> imageUrls = new ArrayList<>();
            String imageUrl;

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventImages");
                query.whereEqualTo("EventId", objectId);
                query.selectKeys(Arrays.asList("ImageFile"));
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
            if (imageViewWeakReference != null && imageUrls != null && imageUrls.size() > 0) {

                for (String url : imageUrls) {
                    TextSliderView textSliderView = new TextSliderView(getActivity());
                    // initialize a SliderLayout

                    textSliderView.image(url)
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(this);
                    com.daimajia.slider.library.SliderLayout layout = imageViewWeakReference.get();
                    if (layout != null) {
                        layout.addSlider(textSliderView);
                    }
                }
            } else {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                com.daimajia.slider.library.SliderLayout layout = imageViewWeakReference.get();
                textSliderView.image(defaultImg)
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);
                if (layout != null) {
                    layout.addSlider(textSliderView);
                }
                sliderShow.setVisibility(View.VISIBLE);
                sliderShow.setDuration(98000000);
            }
        }

        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            Log.v("Slider", "Clicked");
        }
    }

    //TODO: Save use cache to store images, handle exception logging
    private class EventCreaterNameDownload extends AsyncTask<String, String, String> {
        private final WeakReference<Button> buttonViewWeakReference;
        private String objectId;

        public EventCreaterNameDownload(Button button, String id) {
            buttonViewWeakReference = new WeakReference<Button>(button);
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
            if (buttonViewWeakReference != null && buttonName != null) {
                final Button button = buttonViewWeakReference.get();
                if (button != null) {
                    button.setText("Event organised by " + buttonName);
                }
            }
        }
    }

    //Async to get profile picture
    private class MyProfileImageDownloadAsyncTask extends AsyncTask<String, String, String> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private String id;

        public MyProfileImageDownloadAsyncTask(ImageView image, String fbId) {
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
                    //         imageLoader.displayImage(imageUrl, imageViewWeakReference.get(), options);
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
        intentShare.putExtra(Intent.EXTRA_TEXT, eventDetails.toString());
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
            Toast.makeText(getActivity(), "The event user has not provided email details", Toast.LENGTH_SHORT).show();
        }


    }

}
