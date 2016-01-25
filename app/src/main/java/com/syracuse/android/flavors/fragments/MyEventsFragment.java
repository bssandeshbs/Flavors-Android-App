package com.syracuse.android.flavors.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.MyEventsActivity;
import com.syracuse.android.flavors.adapters.UserListAdapter;
import com.syracuse.android.flavors.model.EventRegisteredUsers;
import com.syracuse.android.flavors.util.LocalBitMapDecoder;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Fragment to display the events associated
 * Created by Sandesh
 */
public class MyEventsFragment extends Fragment {

    private static final String EVENTS = "argument_option";
    List<Map<String, ?>> userMapList;
    Map<String, Object> userMap;
    UserListAdapter userListAdapter;
    String eventName;
    Calendar startCal;
    Calendar endCal;
    String address;
    String foodMenu;
    ShareActionProvider shareActionProvider;
    String objectId;
    boolean deleteResult;
    String displayDate;

    public MyEventsFragment() {

    }

    // Create new instance of my events fragment
    public static MyEventsFragment newInstance(HashMap<String, ?> movie) {
        MyEventsFragment fragment = new MyEventsFragment();
        Bundle args = new Bundle();
        args.putSerializable(EVENTS, movie);
        fragment.setArguments(args);
        return fragment;

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
        View rootView;
        userMapList = new ArrayList();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Events");


        HashMap map = (HashMap) this.getArguments().getSerializable(EVENTS);
        eventName = (String) map.get("EventName");
        foodMenu = (String) map.get("Menu");
        final Date beginTime = (Date) map.get("StartTime");
        final Date endTime = (Date) map.get("EndTime");
        objectId = (String) map.get("ObjectId");
        address = (String) map.get("Address");
        startCal = new GregorianCalendar();
        startCal.setTime(beginTime);
        endCal = new GregorianCalendar();
        endCal.setTime(endTime);

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE, dd MMM yyyy 'at' hh:mm a");
        displayDate = inputFormat.format(beginTime);

        rootView = inflater.inflate(R.layout.fragment_my_events, container, false);

        final TextView menuTV = (TextView) rootView.findViewById(R.id.menuTV);
        final TextView totalGuestTV = (TextView) rootView.findViewById(R.id.totalCountTV);
        final TextView startTimeTV = (TextView) rootView.findViewById(R.id.startTimeTVVal);
        final TextView addressTV = (TextView) rootView.findViewById(R.id.venueTV);

        ListView userList = (ListView) rootView.findViewById(R.id.userList);
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final HashMap<String, ?> itemMap = (HashMap<String, ?>) parent.getItemAtPosition(position);
                String fbId = (String) itemMap.get("Fb");
                String url = "http://www.facebook.com/" + fbId;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        userListAdapter = new UserListAdapter(getActivity().getApplicationContext(), userMapList);

        MyEventsUsersDownloadAsyncTask asyncTask = new MyEventsUsersDownloadAsyncTask(userListAdapter, objectId);
        asyncTask.execute();

        userList.setAdapter(userListAdapter);
        int maxGuests = (Integer)map.get("MaxGuests");
        int guestsAvailable = (Integer)map.get("GuestsAvailable");
        menuTV.setText((String) map.get("Menu"));
        totalGuestTV.setText(maxGuests-guestsAvailable + " Foodies Joined the Event");

        startTimeTV.setText(displayDate);
        address = address.replace("\n", " ").replace("\r", " ");
        addressTV.setText(address);

        return rootView;
    }

    // Async Task to get the list of users registered to the event
    private class MyEventsUsersDownloadAsyncTask extends AsyncTask<String, Void, List<ParseObject>> {

        private final WeakReference<UserListAdapter> adapterReference;
        private final String eventId;

        public MyEventsUsersDownloadAsyncTask(UserListAdapter userListAdapter, String id) {
            adapterReference = new WeakReference<>(userListAdapter);
            eventId = id;
        }

        @Override
        protected List<ParseObject> doInBackground(String... params) {
            List<ParseObject> userList = new ArrayList<>();
            List<ParseObject> registeredUserList;
            try {
                ParseQuery<ParseObject> eventRegisteredUsersQuery = ParseQuery.getQuery("EventRegisteredUsers");
                eventRegisteredUsersQuery.whereEqualTo("EventId", eventId);
                eventRegisteredUsersQuery.selectKeys(Arrays.asList("UserId"));
                registeredUserList = eventRegisteredUsersQuery.find();

                List<String> userIdList = new ArrayList<>();
                for (int i = 0; i < registeredUserList.size(); i++) {
                    int size = registeredUserList.size();
                    Log.v("Number of Users : ", String.valueOf(size));
                    EventRegisteredUsers user = (EventRegisteredUsers) registeredUserList.get(i);
                    String userId = user.getUserId();
                    userIdList.add(userId);
                }

                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
                query.whereContainedIn("Fb", userIdList);
                query.selectKeys(Arrays.asList("Name", "Email", "ProfilePic","Fb"));
                userList = query.find();

            } catch (ParseException e) {
                Log.e(MyEventsActivity.class.getCanonicalName(), "Exception occurred getting the events :" + e.getMessage());
            }
            return userList;
        }

        @Override
        protected void onPostExecute(List<ParseObject> myEventsList) {
            userMapList.clear();
            for (int i = 0; i < myEventsList.size(); i++) {
                String name = myEventsList.get(i).getString("Name");
                String mail = myEventsList.get(i).getString("Email");
                String id = myEventsList.get(i).getString("Fb");
                ParseFile profilePic = myEventsList.get(i).getParseFile("ProfilePic");
                userMap = new HashMap<>();
                userMap.put("Name", name);
                userMap.put("Email", mail);
                userMap.put("ProfilePic", profilePic);
                userMap.put("Fb",id);
                userMapList.add(userMap);
            }

            final UserListAdapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_my_events_fragment, menu);

        MenuItem shareItem =  menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("text/plain");
        StringBuilder eventDetails = new StringBuilder("Event Details\n");
        eventDetails.append("Event Name : " + eventName+"\n").append("Address : "+address+"\n")
                .append("Date : " + displayDate+"\n").append("Food Menu : " + foodMenu + "\n");
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
            case R.id.delete_event:
                deleteEvent(getView());
                return true;
            case R.id.email_event_users:
                sendEmailNotification();
        }
        return super.onOptionsItemSelected(item); // important line
    }

    // Send email notification to registered users
    public void sendEmailNotification() {
        List<String> addressList = getAddress();
        String[] addresses = new String[addressList.size()];
        addresses  = addressList.toArray(addresses);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Update ! Food Feast Event  "+eventName);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // function to get address
    public List<String> getAddress() {
        ArrayList<String> emailAddress = new ArrayList<String>();
        ArrayList<String> users = new ArrayList<String>();
        try {
            ParseQuery<ParseObject> eventRegisteredUsersQuery = ParseQuery.getQuery("EventRegisteredUsers");
            eventRegisteredUsersQuery.whereEqualTo("EventId",objectId);
            List<ParseObject>registeredUserList = eventRegisteredUsersQuery.find();
            for(ParseObject eventUser :registeredUserList) {
                String userId = eventUser.getString("UserId");
                users.add(userId);
            }
            ParseQuery<ParseObject> usersQuery = ParseQuery.getQuery("EventUsers");
            usersQuery.whereContainedIn("Fb", users);
            List<ParseObject> userList = usersQuery.find();
            for(ParseObject user :userList) {
                String emailId = user.getString("Email");
                emailAddress.add(emailId);
            }
            return emailAddress;
        } catch(Exception e) {
            return null;
        }

    }

    // Confirmation screen for event deletion
    public void deleteEvent(View v) {
        new SweetAlertDialog(v.getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete event?")
                .setCancelText("No, Cancel !")
                .setConfirmText("Yes, Save !")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.setTitleText("Cancelled!")
                                .setContentText("Event not deleted !")
                                .setConfirmText("OK")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        System.out.println("Save");
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        try {
                            boolean result = delete();
                            if (result) {
                                sDialog.setTitleText("Success!")
                                        .setContentText("Your event has been successfully deleted")
                                        .setConfirmText("OK")
                                        .showCancelButton(false)
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                            } else {
                                sDialog.setTitleText("Failure!")
                                        .setContentText("Event not deleted !")
                                        .setConfirmText("OK")
                                        .showCancelButton(false)
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            }
                        } catch (Exception e) {
                            sDialog.setTitleText("Failure!")
                                    .setContentText("Unable to delete Event, Please contact Administrator!")
                                    .setConfirmText("OK")
                                    .showCancelButton(false)
                                    .setCancelClickListener(null)
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            Log.e(MyEventsFragment.this.toString(), "Failed to delete event " + e.getMessage());
                        }
                    }
                })
                .show();
    }

    // Function to delete a event
    public boolean delete() {
        deleteResult = true;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Events");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        ParseQuery<ParseObject> eventRegisteredUsersQuery = ParseQuery.getQuery("EventRegisteredUsers");
                        eventRegisteredUsersQuery.whereEqualTo("EventId",objectId);
                        List<ParseObject>registeredUserList = eventRegisteredUsersQuery.find();
                        for(ParseObject eventUser :registeredUserList) {
                            eventUser.deleteInBackground();
                        }
                    } catch (ParseException e1) {
                        Log.e(MyEventsFragment.class.getCanonicalName(), "Delete Failed :"+e1.getMessage());
                        deleteResult = false;
                    }
                    object.deleteInBackground();
                    Log.v(MyEventsFragment.class.getCanonicalName(),"Delete Successful");
                } else {
                    Log.e(MyEventsFragment.class.getCanonicalName(), "Delete Failed");
                }
            }
        });
        return deleteResult;
    }
}
