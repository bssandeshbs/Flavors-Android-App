package com.syracuse.android.flavors.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.model.EventUsers;
import com.syracuse.android.flavors.util.NetworkChecker;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


import butterknife.ButterKnife;
import butterknife.InjectView;
import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;

/* Starting point of the application which provides the facebook login request */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    static CallbackManager callbackManager;
    AccessToken token;
    private String logOutText = "Log out";
    private List<Address> addresses;
    protected static final String TAG = "basic-location-sample";
    @InjectView(R.id.indeterminate_progress_library)
    ProgressBar indeterminateProgress;
    /* Provides the entry point to Google Play services */
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    LoginButton loginButton;
    String logOut;
    String cityName;
    String latitude;
    String longitude;
    Location updatedLocation = null;
    NetworkChecker checker;
    boolean connectionRes;
    TextView internetNotConnectedTV;
    TextView tryAgainLater;
    ImageButton refreshInternet;
    /* Represents a geographical location */
    protected Location mLastLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        checker = new NetworkChecker(this);
        connectionRes = checker.hasConnection();
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(6 * 100000)
                .setFastestInterval(1000);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();

        if (this.getIntent() != null) {
            int status = this.getIntent().getIntExtra("LOGIN_STATUS", 0);
            if (status == 100) {
                LoginManager.getInstance().logOut();
            }
        }

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        logOut = (String) loginButton.getText();
        refreshInternet = (ImageButton) findViewById(R.id.refreshInternet);
        refreshInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionRes = checker.hasConnection();
                onConnected(null);
            }
        });

        internetNotConnectedTV = (TextView) findViewById(R.id.internetConnection);
        tryAgainLater = (TextView) findViewById(R.id.internetConnectionTryAgain);
        ButterKnife.inject(this);
        indeterminateProgress.setIndeterminateDrawable(new IndeterminateProgressDrawable(this));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Function to trigger graph request to get the user attributes from facebook api
      Input : AccessToken*/
    public void triggerGraphRequest(AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("result", object.toString());
                        try {
                            String name = (String) object.getString("name");
                            String id = (String) object.getString("id");
                            String email = (String) object.getString("email");
                            MyApplication stateManager = (MyApplication) getApplicationContext();
                            stateManager.getApplicationManager().setEmail(email);
                            stateManager.getApplicationManager().setUserName(name);
                            stateManager.getApplicationManager().setId(id);
                            checkParseUser(email, name, id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void checkParseUser(final String email, final String name, final String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventUsers");
        query.whereEqualTo("Fb", id).getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d("user", "The user doesn't exist. Creating in Parse");
                    createUser(name, email, id);
                } else {
                    Log.d("Success! Found the user", "Logging in Success");
                }
            }
        });
    }

    public void createUser(String name, String email, String id) {
        CreateUserAsyncTask task = new CreateUserAsyncTask(name, email, id);
        task.execute();
    }

    private class CreateUserAsyncTask extends AsyncTask<String, Void, byte[]> {

        private String name;
        private String email;
        private String id;

        public CreateUserAsyncTask(String name, String email, String id) {
            this.name = name;
            this.email = email;
            this.id = id;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            Bitmap bitmap = null;
            byte[] byteArray = null;
            try {
                URL imageURL = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byteArray = out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteArray;
        }

        @Override
        protected void onPostExecute(byte[] byteArray) {
            ParseFile photoFile = new ParseFile("profile_pic.jpg", byteArray);
            EventUsers users = new EventUsers();
            users.setName(name);
            users.setEmail(email);
            users.setId(id);
            users.setEventsOrganised(0);
            users.setProfilePic(photoFile);
            users.saveInBackground();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updatedLocation = location;
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            if (connectionRes) getCityName();
            Log.d("GPS Update :", "Latitude :" + latitude + " Longitude:" + longitude);
            MyApplication stateManager = (MyApplication) getApplicationContext();
            stateManager.getApplicationManager().setLatitude(latitude);
            stateManager.getApplicationManager().setLongitude(longitude);
            stateManager.getApplicationManager().setFullyQualifiedCityName(cityName);
        } else {
            Toast.makeText(this, "Unable to get the location", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkGpsLocation() {
        if (updatedLocation != null) {
            latitude = String.valueOf(updatedLocation.getLatitude());
            longitude = String.valueOf(updatedLocation.getLongitude());
            if (connectionRes) getCityName();
            Log.d("GPS Update :", "Latitude :" + latitude + " Longitude:" + longitude);
            MyApplication stateManager = (MyApplication) getApplicationContext();
            stateManager.getApplicationManager().setLatitude(latitude);
            stateManager.getApplicationManager().setLongitude(longitude);
            stateManager.getApplicationManager().setFullyQualifiedCityName(cityName);
            return true;
        } else return false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            indeterminateProgress.setVisibility(View.INVISIBLE);
            tryAgainLater.setText("GPS not available");
            internetNotConnectedTV.setText("Please enable location services in settings");
            internetNotConnectedTV.setVisibility(View.VISIBLE);
            tryAgainLater.setVisibility(View.VISIBLE);
        } else {
            internetNotConnectedTV.setVisibility(View.INVISIBLE);
            tryAgainLater.setVisibility(View.INVISIBLE);
            if (connectionRes) {
                tryAgainLater.setVisibility(View.INVISIBLE);
                internetNotConnectedTV.setVisibility(View.INVISIBLE);
                refreshInternet.setVisibility(View.INVISIBLE);
                getLocation();
                initializeActivity();
            } else {
                refreshInternet.setVisibility(View.VISIBLE);
                indeterminateProgress.setVisibility(View.INVISIBLE);
                internetNotConnectedTV.setVisibility(View.VISIBLE);
                tryAgainLater.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void getLocation() {
        latitude = String.valueOf(mLastLocation.getLatitude());
        longitude = String.valueOf(mLastLocation.getLongitude());
        Log.d("Last location:", "Latitude :" + latitude + " Longitude:" + longitude);
        //call to get full name of city
        getCityName();
        MyApplication stateManager = (MyApplication) getApplicationContext();
        stateManager.getApplicationManager().setLatitude(latitude);
        stateManager.getApplicationManager().setLongitude(longitude);
        stateManager.getApplicationManager().setFullyQualifiedCityName(cityName);
    }

    public void initializeActivity() {
        if ((AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null) || logOutText.equalsIgnoreCase(logOut)) {
            triggerGraphRequest(AccessToken.getCurrentAccessToken());
            Intent i = new Intent(LoginActivity.this, EventActivity.class);
            i.putExtra("Activity", "EventActivity");
            startActivity(i);
            finish();
        } else {
            indeterminateProgress.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            loginButton.setReadPermissions(Arrays.asList("email")); // Request profile and email permissions
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loginButton.setVisibility(View.INVISIBLE);
                    token = loginResult.getAccessToken();
                    triggerGraphRequest(token);
                    Intent i = new Intent(LoginActivity.this, EventActivity.class);
                    i.putExtra("Activity", "EventActivity");
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), " Connection Failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException e) {
                    Toast.makeText(getApplicationContext(), " Connection Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getCityName() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
        } catch (IOException e) {
            Log.d("Exception", "Exception while getting location...");
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex() - 1);
        String[] addressArr;
        addressArr = address.split(" ");
        address = addressArr[addressArr.length - 2];
        cityName = addresses.get(0).getLocality() + ", " + address + ", " + addresses.get(0).getCountryName();
    }
}
