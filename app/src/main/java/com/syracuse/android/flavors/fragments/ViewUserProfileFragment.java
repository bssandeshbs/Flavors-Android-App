package com.syracuse.android.flavors.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.model.EventUsers;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/** Created By Sandesh
 *
 * Fragment to view user profile information
 *
 */
public class ViewUserProfileFragment extends Fragment {

    private static final String USER_ID = "user_id";

    TextView emailId;
    TextView name;
    TextView dateTxt = null;
    TextView dobVal = null;
    TextView foodInterests;
    TextView eventsOrganised = null;
    TextView mobileNumber;

    com.pkmmte.view.CircularImageView profilePic;
    Button updateProfile;
    ImageView dateImg = null;

    EventUsers user;
    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
    Button facebookProfileButton;

    public ViewUserProfileFragment() {

    }

    //Create a instance of view user profile fragment
    public static ViewUserProfileFragment newInstance(String userId,String screen) {
        ViewUserProfileFragment fragment = new ViewUserProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        args.putString("FROM_SCREEN", screen);
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
        final String id = this.getArguments().getString(USER_ID);
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_view_user_profile, container, false);

        emailId = (TextView)rootView.findViewById(R.id.email);
        name = (TextView)rootView.findViewById(R.id.name);
        profilePic = (com.pkmmte.view.CircularImageView) rootView.findViewById(R.id.circleView);
        updateProfile = (Button) rootView.findViewById(R.id.updateProfileBt);
        dateImg = (ImageView) rootView.findViewById(R.id.dateImg);
        dateTxt = (TextView) rootView.findViewById(R.id.dateTxt);
        dobVal = (TextView) rootView.findViewById(R.id.dateVal);
        foodInterests = (TextView) rootView.findViewById(R.id.foodInterestET);
        mobileNumber = (TextView) rootView.findViewById(R.id.mobileNumberET1);
        eventsOrganised = (TextView) rootView.findViewById(R.id.eventsOrgainisedTV);
        facebookProfileButton = (Button) rootView.findViewById(R.id.facebookProfileButton);
        facebookProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.facebook.com/"+id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        getParseEventUser(id);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile Information");
        return rootView;
    }

    /*
    Method to get the parse event user from parse
  */
    public EventUsers getParseEventUser(final String id) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
        query.whereEqualTo("Fb",id).getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    Log.d("user name", object.getString("Name"));
                    user = (EventUsers) object;
                    Date date = user.getDate();
                    if (date != null) dobVal.setText(format.format(date));
                    foodInterests.setText(user.getInterest());
                    mobileNumber.setText(user.getMobile());
                    name.setText(user.getName());
                    emailId.setText(user.getEmail());
                    eventsOrganised.setText(String.valueOf(user.getEventsOrganised()));
                    MyProfileImageDownloadAsyncTask profilePicDw = new MyProfileImageDownloadAsyncTask(profilePic,user.getId());
                    profilePicDw.execute();
                } else {
                    Log.e(ViewUserProfileFragment.class.getCanonicalName(),"Error while retrieving the user from parse.");
                }

            }
        });
        return user;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        String id = this.getArguments().getString("FROM_SCREEN");
        if(!id.equals("event_detail_screen")) inflater.inflate(R.menu.user_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.update_profile:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, UpdateProfileFragment.newInstance(1))
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item); // important line
    }

    //Async task to get profile image
    private class MyProfileImageDownloadAsyncTask extends AsyncTask<String, String, String> {
        private final WeakReference<com.pkmmte.view.CircularImageView> imageViewWeakReference;
        private String id;

        public MyProfileImageDownloadAsyncTask(com.pkmmte.view.CircularImageView image, String fbId) {
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

            EventUsers image = null;
            ParseFile eventIcon = null;
            //set event icon
            if (eventImagesList!=null && eventImagesList.size() > 0) {
                image = (EventUsers) eventImagesList.get(0);
                eventIcon = image.getProfilePic();
                imageUrl = eventIcon.getUrl();
            }
            return imageUrl;
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if (imageViewWeakReference != null && imageUrl != null) {
                final com.pkmmte.view.CircularImageView image = imageViewWeakReference.get();
                if (image != null) {
                    Picasso.with(getActivity().getApplicationContext()).load(imageUrl).into(image);
                }
            }
        }
    }
}
