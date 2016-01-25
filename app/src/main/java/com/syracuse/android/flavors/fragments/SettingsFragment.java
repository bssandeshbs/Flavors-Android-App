package com.syracuse.android.flavors.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.adapters.SettingsListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 8/5/2015 by Rahul
 * Fragment to show settings menu
 */
public class SettingsFragment extends Fragment{

    SettingsListAdapter settingsListAdapter;
    List<Map<String, Object>> settingsMapList;
    HashMap<String, Object> settingMap;
    private static final String FEEDBACK = "Send Feedback";
    private static final String RATE = "Rate us on the Play Store";
    private static final String TNC = "Terms of Service";
    private static final String PRIVACY = "Privacy Policy";
    private static final String CREDITS = "Acknowledgements";
    private static final String ABOUT = "About us";


    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        settingsMapList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ListView settingsList = (ListView) rootView.findViewById(R.id.settingsList);
        PopulateSettings();
        settingsListAdapter = new SettingsListAdapter(getActivity().getApplicationContext(), settingsMapList);
        settingsList.setAdapter(settingsListAdapter);


        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                switch (position) {
                    case 0:
                        sendEmailNotification();
                        break;
                    case 1:
                        rateOnPlayStore();
                        break;
                    case 2:
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new TermsFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                    case 3:
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new PrivacyFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                    case 4:
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new AckFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                    case 5:
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new AboutFragment())
                                .addToBackStack(null)
                                .commit();
                        break;
                    default:
                        break;
                }
            }
        });

        return rootView;
    }

    private void PopulateSettings(){

        settingMap = new HashMap<>();
        settingMap.put("rowName", FEEDBACK);
        settingMap.put("rowIcon", R.drawable.settings_mail);
        settingsMapList.add(settingMap);

        settingMap = new HashMap<>();
        settingMap.put("rowName", RATE);
        settingMap.put("rowIcon", R.drawable.settings_rate);
        settingsMapList.add(settingMap);

        settingMap = new HashMap<>();
        settingMap.put("rowName", TNC);
        settingMap.put("rowIcon", R.drawable.settings_terms);
        settingsMapList.add(settingMap);

        settingMap = new HashMap<>();
        settingMap.put("rowName", PRIVACY);
        settingMap.put("rowIcon", R.drawable.settings_privacy);
        settingsMapList.add(settingMap);

        settingMap = new HashMap<>();
        settingMap.put("rowName", CREDITS);
        settingMap.put("rowIcon", R.drawable.settings_credit);
        settingsMapList.add(settingMap);

        settingMap = new HashMap<>();
        settingMap.put("rowName", ABOUT);
        settingMap.put("rowIcon", R.drawable.settings_about);
        settingsMapList.add(settingMap);
    }

    /*Method to send email to app developers*/
    private void sendEmailNotification() {
        String address = getActivity().getResources().getString(R.string.app_email_id);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {address});
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /*Method to rate app on play store*/
    private void rateOnPlayStore(){
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
