package com.syracuse.android.flavors.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.BaseActivity;
import com.syracuse.android.flavors.model.EventUsers;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Fragment to update user profile
 *
 * Created by Sandesh
 */
public class UpdateProfileFragment extends Fragment {

    private static final String ARG_OPTION = "argument_option";

    TextView emailId;
    TextView name;
    TextView dateTxt = null;
    TextView dobVal = null;

    com.pkmmte.view.CircularImageView profilePic;
    Button updateProfile;
    ImageView dateImg = null;

    com.rengwuxian.materialedittext.MaterialEditText foodInterests;
    com.rengwuxian.materialedittext.MaterialEditText mobileNumber;

    Date date ;
    EventUsers user;

    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
    boolean isStartTime;

    public UpdateProfileFragment() {

    }

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {
            //rlDateTimeRecurrenceInfo.setVisibility(View.GONE);
        }

        @Override
        public void onDateTimeRecurrenceSet(int year, int monthOfYear, int dayOfMonth,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {

            date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
            date = cal.getTime();
            String dateStr = format.format(cal.getTime());
            if(isStartTime){
                dobVal.setText(dateStr);
                isStartTime = true;
            }
        }
    };

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
        rootView = inflater.inflate(R.layout.fragment_update_profile, container, false);
        final MyApplication application = (MyApplication) getActivity().getApplicationContext();
        final String id = application.getApplicationManager().getId();
        getParseEventUser(id);

        emailId = (TextView)rootView.findViewById(R.id.email);
        name = (TextView)rootView.findViewById(R.id.name);
        profilePic = (com.pkmmte.view.CircularImageView) rootView.findViewById(R.id.circleView);
        updateProfile = (Button) rootView.findViewById(R.id.updateProfileBt);
        dateImg = (ImageView) rootView.findViewById(R.id.dateImg);
        dateTxt = (TextView) rootView.findViewById(R.id.dateTxt);
        dobVal = (TextView) rootView.findViewById(R.id.dateVal);
        foodInterests = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.foodInterestET);
        mobileNumber = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.mobileNumberET1);

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setParseEventUser(id);
                MyApplication application = (MyApplication) getActivity().getApplicationContext();
                String id = application.getApplicationManager().getId();
                Intent profileIntent = new Intent(getActivity(), BaseActivity.class);
                profileIntent.putExtra("Activity", "Profile");
                profileIntent.putExtra("id", id);
                startActivity(profileIntent);
                getActivity().finish();
            }
        });

        //OnClickListener for setting start time
        dateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isStartTime = true;
            }
        });

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isStartTime = true;
            }
        });

        MyDownloadImageAsyncTask task = new MyDownloadImageAsyncTask(profilePic,name,emailId);
        task.execute();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Update Profile");


        return rootView;
    }

    /*
       Method to update the parse event user
     */
    public void setParseEventUser(String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
        query.whereEqualTo("Fb",id).getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    Log.d("user name", object.getString("Name"));
                    EventUsers user =  (EventUsers) object;
                    if(isStartTime) {
                        user.setDate(date);
                    }
                    user.setInterests(foodInterests.getText().toString());
                    user.setMobile(mobileNumber.getText().toString());
                    user.saveInBackground();
                    Toast.makeText(getActivity(),"Profile updated successfully",Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Error ", "Error while retrieving the user from parse.");
                }

            }
        });

    }

    /*
     Method to get the parse event user from parse
   */
    public EventUsers getParseEventUser(String id) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
        query.whereEqualTo("Fb",id).getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    Log.d("user name", object.getString("Name"));
                    user = (EventUsers) object;
                    Date date = user.getDate();
                    if(date !=null) dobVal.setText(format.format(date));
                    foodInterests.setText(user.getInterest());
                    mobileNumber.setText(user.getMobile());
                } else {
                    Log.e("Error ", "Error while retrieving the user from parse.");
                }

            }
        });
        return user;
    }


    public static UpdateProfileFragment newInstance(int option) {
        UpdateProfileFragment fragment = new UpdateProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_OPTION, option);
        fragment.setArguments(args);
        return fragment;

    }

    private class MyDownloadImageAsyncTask extends AsyncTask<String,Void,Void> {

        private final WeakReference<com.pkmmte.view.CircularImageView> imageViewWeakReference;
        private final WeakReference<TextView> nameWeakReference;
        private final WeakReference<TextView> emailWeakReference;

        private String id;
        public MyDownloadImageAsyncTask(com.pkmmte.view.CircularImageView image,TextView nameTV,TextView emailTV) {
            imageViewWeakReference = new WeakReference<com.pkmmte.view.CircularImageView>(image);
            nameWeakReference = new WeakReference<>(nameTV);
            emailWeakReference = new WeakReference<>(emailTV);
        }

        @Override
        protected Void doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            com.pkmmte.view.CircularImageView imageView = imageViewWeakReference.get();
            MyApplication application = (MyApplication) getActivity().getApplicationContext();
            String userID = application.getApplicationManager().getId();
            Picasso.with(getActivity().getApplicationContext()).load("https://graph.facebook.com/" + userID+ "/picture?type=large").into(imageView);

            if(nameWeakReference!=null && emailWeakReference!=null) {
                final TextView name = nameWeakReference.get();
                final TextView email = emailWeakReference.get();
                String emailVal = application.getApplicationManager().getEmail();
                String nameVal = application.getApplicationManager().getUserName();
                name.setText(nameVal);
                email.setText(emailVal);
            }
        }
    }

    private void showCalender(){
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(mFragmentCallback);

        // Options
        Pair<Boolean, SublimeOptions> optionsPair = getOptions();

        if (!optionsPair.first) { // If options are not valid
            Toast.makeText(getActivity(), "No pickers activated",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Valid options
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second);
        pickerFrag.setArguments(bundle);

        pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(getFragmentManager(), "SUBLIME_PICKER");
    }

    // Validates & returns SublimePicker options
    Pair<Boolean, SublimeOptions> getOptions() {
        SublimeOptions options = new SublimeOptions();
        int displayOptions = 0;
        displayOptions |= SublimeOptions.ACTIVATE_DATE_PICKER;
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(displayOptions);
        // If 'displayOptions' is zero, the chosen options are not valid
        return new Pair<>(displayOptions != 0 ? Boolean.TRUE : Boolean.FALSE, options);
    }

}
