package com.syracuse.android.flavors.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.activities.CreateRestEventActivity;
import com.syracuse.android.flavors.activities.EventActivity;
import com.syracuse.android.flavors.model.EventDetails;
import com.syracuse.android.flavors.model.EventImages;
import com.syracuse.android.flavors.model.EventUsers;
import com.syracuse.android.flavors.util.ParseCustomException;
import com.syracuse.android.flavors.util.UserPicture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Fragment to create restaurant event
 * <p/>
 * Created by Rahul
 */
public class CreateRestaurantEventFragment extends Fragment {

    private static final String RESTAURANT = "restaurant";
    private static final int SELECT_SINGLE_PICTURE = 101;
    private static final int CAMERA_PICTURE = 102;
    public static final String IMAGE_TYPE = "image/*";

    private GoogleMap map;
    ImageView image1 = null;
    ImageView image2 = null;
    ImageView image3 = null;
    ImageView image4 = null;
    FloatingActionButton galleryButton;
    FloatingActionButton cameraButton;

    ImageView startTimeImg = null;
    ImageView endTimeImg = null;
    TextView startTimeVal = null;
    TextView endTimeVal = null;
    TextView startTimeTxt = null;
    TextView endTimeTxt = null;
    TextView locationVal = null;

    Button createEventBtn;
    com.rengwuxian.materialedittext.MaterialEditText maxGuests;
    com.rengwuxian.materialedittext.MaterialEditText eventNameET;
    com.rengwuxian.materialedittext.MaterialEditText menuET;

    EventDetails details;
    EventImages images;
    FloatingActionMenu actionsMenu;
    static Bitmap restaurantBitmap;

    Uri image1Uri;
    Uri image2Uri;
    Uri image3Uri;
    Uri image4Uri;
    Uri cameraImgUri;

    boolean saveEventRes = true;
    boolean isStartTime = false;
    boolean isEndTime = false;
    int counter = 0;

    public CreateRestaurantEventFragment() {
    }

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {
        }

        @Override
        public void onDateTimeRecurrenceSet(int year, int monthOfYear, int dayOfMonth,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm");
            Date date;
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
            date = cal.getTime();
            String dateStr = format.format(cal.getTime());
            if (isStartTime) {
                details.setStartTime(date);
                startTimeVal.setText(dateStr);
                isStartTime = false;
            } else if (isEndTime) {
                details.setEndTime(date);
                endTimeVal.setText(dateStr);
                isEndTime = false;
            }
        }
    };

    // Create Instance of restuarant event fragment
    public static CreateRestaurantEventFragment newInstance(HashMap<String, ?> restaurantMap, Bitmap bitmap) {
        CreateRestaurantEventFragment fragment = new CreateRestaurantEventFragment();
        Bundle args = new Bundle();
        args.putSerializable(RESTAURANT, restaurantMap);
        restaurantBitmap = bitmap;
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
        rootView = inflater.inflate(R.layout.fragment_create_rest_event, container, false);
        HashMap<String, ?> restaurant = (HashMap<String, ?>) getArguments().getSerializable(RESTAURANT);
        details = ((CreateRestEventActivity) getActivity()).getEventDetails();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Create Restaurant Event");

        image1 = (ImageView) rootView.findViewById(R.id.picture1);
        image2 = (ImageView) rootView.findViewById(R.id.picture2);
        image3 = (ImageView) rootView.findViewById(R.id.picture3);
        image4 = (ImageView) rootView.findViewById(R.id.picture4);
        startTimeImg = (ImageView) rootView.findViewById(R.id.startTimeIV);
        endTimeImg = (ImageView) rootView.findViewById(R.id.endTimeIV);
        startTimeVal = (TextView) rootView.findViewById(R.id.startTimeValTV);
        endTimeVal = (TextView) rootView.findViewById(R.id.endTimeValTv);
        startTimeTxt = (TextView) rootView.findViewById(R.id.startTimeTxt);
        endTimeTxt = (TextView) rootView.findViewById(R.id.endTimeTxt);
        createEventBtn = (Button) rootView.findViewById(R.id.createEventBtn);
        eventNameET = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.eventNameET);
        menuET = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.menuET);
        eventNameET.addValidator(new RegexpValidator("Event name is Invalid", "[a-zA-Z0-9]+([ '-][a-zA-Z0-9]*)*"));
        menuET.addValidator(new RegexpValidator("Menu is Invalid", "[a-zA-Z0-9]+([ '-][a-zA-Z0-9]*)*"));

        locationVal = (TextView) rootView.findViewById(R.id.locationVal);
        galleryButton = (FloatingActionButton) rootView.findViewById(R.id.galleryButton);
        maxGuests = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.maxGuestsVal);
        cameraButton = (FloatingActionButton) rootView.findViewById(R.id.cameraButton);
        actionsMenu = (FloatingActionMenu) rootView.findViewById(R.id.multiple_actions);
        String eventAddress = restaurant.get("name") + "\n" + restaurant.get("address");
        String formattedAddress = eventAddress.replace("\n", " ").replace("\r", " ");
        locationVal.setText(formattedAddress);

        map = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        //call to show location in maps
        if (!restaurant.isEmpty()) {
            getLatLongFromAddress((String) restaurant.get("address") + " " + restaurant.get("city"));
        }

        //show delete image option on long click
        image1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().startActionMode(new ActionBarCallBack(1));
                return true;
            }
        });

        image2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().startActionMode(new ActionBarCallBack(2));
                return true;
            }
        });

        image3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().startActionMode(new ActionBarCallBack(3));
                return true;
            }
        });

        image4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().startActionMode(new ActionBarCallBack(4));
                return true;
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType(IMAGE_TYPE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.select_picture)), SELECT_SINGLE_PICTURE);
                actionsMenu.close(true);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo;
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyPhotos");
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
                    }
                }

                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                photo = new File(dir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");

                cameraImgUri = Uri.fromFile(photo);
                startActivityForResult(intent, CAMERA_PICTURE);
                actionsMenu.close(true);
            }
        });


        createEventBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (maxGuests.validate() && eventNameET.validate())
                    confirmSaveEventDialogue(v);
            }
        });

        //OnClickListener for setting start time
        startTimeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isStartTime = true;
            }
        });

        startTimeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isStartTime = true;
            }
        });

        //OnClickListener for setting end time
        endTimeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isEndTime = true;
            }
        });

        endTimeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalender();
                isEndTime = true;
            }
        });

        final ScrollView mainScrollView = (ScrollView) rootView.findViewById(R.id.createEventScrollView);
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


        return rootView;
    }


    @Override
    public void onDestroyView() {
        MapFragment mapFragment = (MapFragment) getActivity()
                .getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            getActivity().getFragmentManager().beginTransaction()
                    .remove(mapFragment).commit();

        super.onDestroyView();
    }

    /*
        Function to trigger a new event on clicking save button
     */
    public boolean saveEvent() throws ParseCustomException, IOException {
        try {
            saveEventRes = true;
            MyApplication application = (MyApplication) getActivity().getApplicationContext();
            details.setEventName(eventNameET.getText().toString());
            details.setUserId(application.getApplicationManager().getId());
            details.setMenu(menuET.getText().toString());
            details.setMaxGuests(Integer.parseInt(maxGuests.getText().toString()));
            details.setAvailGuests(Integer.parseInt(maxGuests.getText().toString()));
            details.setAddress(locationVal.getText().toString());
            details.setEventType("Restaurant");
            details.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // Saved successfully.
                        String id = details.getObjectId();
                        Log.d(CreateRestaurantEventFragment.class.getCanonicalName(), "Event Created Successfully!");
                        Log.d(CreateRestaurantEventFragment.class.getCanonicalName(), "The object id is: " + id);
                    } else {
                        Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "Failed to create event error: " + e.getMessage());
                        // The save failed.
                        saveEventRes = false;
                    }
                }
            });
            if (!saveEventRes) {
                throw new ParseCustomException("Failed to save event :");
            }


            for (int i = 0; i <= counter; i++) {
                //  InputStream iStream = null;

                ParseFile photoFile;
                images = new EventImages();
                ParseRelation<ParseObject> relation = images.getRelation("EventId");
                relation.add(details);
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap bitmap;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] inputData = null;
                // Save the scaled image to Parse
                try {
                    if (i == 0) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        if (restaurantBitmap != null) {
                            restaurantBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            inputData = stream.toByteArray();
                        }
                    } else {
                        if (i == 1 && image1Uri != null) {
                            bitmap = new UserPicture(image1Uri, cr).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 55, byteArrayOutputStream);
                        } else if (i == 2 && image2Uri != null) {
                            bitmap = new UserPicture(image2Uri, cr).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 55, byteArrayOutputStream);
                        } else if (i == 3 && image3Uri != null) {
                            bitmap = new UserPicture(image3Uri, cr).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 55, byteArrayOutputStream);
                        } else if (i == 4 && image4Uri != null) {
                            bitmap = new UserPicture(image4Uri, cr).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 55, byteArrayOutputStream);
                        }
                        inputData = byteArrayOutputStream.toByteArray();
                    }

                    if (inputData != null)
                        photoFile = new ParseFile("event_photo" + i + ".jpg", inputData);
                    else return true;
                } catch (FileNotFoundException e) {
                    saveEventRes = false;
                    Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "File Not found in saveEvent()");
                    throw e;
                } catch (IOException e) {
                    saveEventRes = false;
                    Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "IO exception in saveEvent()");
                    throw e;
                }

                images.setImageFile(photoFile);
                try {
                    images.save();
                } catch (ParseException e1) {
                    Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "Failed to save image! Trying again");
                    e1.printStackTrace();
                    i = -1;
                }
            }

            //throw parse exception if error in save
            if (!saveEventRes) throw new ParseCustomException("Exception in saveEvent()");
            else return true;

        } catch (Exception e) {
            Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "Exception in saveEvent()" + e.getMessage());
            throw new ParseCustomException("Exception in saveEvent()");
        }

    }

    // Update user event count
    public void updateUserEventCount() throws ParseException {
        MyApplication application = (MyApplication) getActivity().getApplicationContext();
        List<ParseObject> userList;
        String id = application.getApplicationManager().getId();
        ParseQuery<ParseObject> user = ParseQuery.getQuery("EventUsers");
        user.whereEqualTo("Fb", id);
        userList = user.find();
        EventUsers eventUsers = (EventUsers) userList.get(0);
        eventUsers.increment("EventsOrganised");
        eventUsers.saveInBackground();
    }

    // Validates & returns SublimePicker options
    Pair<Boolean, SublimeOptions> getOptions() {
        SublimeOptions options = new SublimeOptions();
        int displayOptions = 0;
        displayOptions |= SublimeOptions.ACTIVATE_TIME_PICKER;
        displayOptions |= SublimeOptions.ACTIVATE_DATE_PICKER;
        options.setPickerToShow(SublimeOptions.Picker.TIME_PICKER);
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(displayOptions);
        // If 'displayOptions' is zero, the chosen options are not valid
        return new Pair<>(displayOptions != 0 ? Boolean.TRUE : Boolean.FALSE, options);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cameraImgUri != null) {
            outState.putString("cameraImageUri", cameraImgUri.toString());
        }
    }

    // on save of event images
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap = null;
            Uri selectedImageUri = null;
            try {
                if (requestCode == SELECT_SINGLE_PICTURE) {
                    selectedImageUri = data.getData();
                    bitmap = new UserPicture(selectedImageUri, cr).getBitmap();
                } else if (requestCode == CAMERA_PICTURE) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    selectedImageUri = getUri(bitmap);
                }
            } catch (IOException e) {
                Log.e(CreateRestaurantEventFragment.class.getCanonicalName(), "Exception :" + e.getMessage());
                e.printStackTrace();
            }

            counter++;
            if (counter == 1) {
                image1.setImageBitmap(bitmap);
                image1Uri = selectedImageUri;
            } else if (counter == 2) {
                image2.setImageBitmap(bitmap);
                image2Uri = selectedImageUri;
            } else if (counter == 3) {
                image3.setImageBitmap(bitmap);
                image3Uri = selectedImageUri;
            } else if (counter == 4) {
                image4.setImageBitmap(bitmap);
                image4Uri = selectedImageUri;
            }
        } else {
            Log.e(CreateRestaurantEventFragment.class.getSimpleName(), "Failed to get intent data, result code is " + resultCode);
        }
    }

    // Function to get Latitude and Longitude from address
    private void getLatLongFromAddress(String address) {

        Geocoder geoCoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                details.setLatitude(latitude);
                details.setLongitude(longitude);

                //code to get fully qualified name for city
                String cityName = addresses.get(0).getAddressLine(1);

                //remove everything after last space
                if (null != cityName && cityName.length() > 0) {
                    int endIndex = cityName.lastIndexOf(" ");
                    if (endIndex != -1) {
                        cityName = cityName.substring(0, endIndex);
                    }
                }
                cityName = cityName + ", " + addresses.get(0).getCountryName();

                details.setCity(cityName);
                LatLng location = new LatLng(latitude, longitude);
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(location)
                        .title("Restaurant Event Address")
                        .snippet(address)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.map_plotter)));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCalender() {
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

    // provide confirmation for saving event
    public void confirmSaveEventDialogue(View v) {
        new SweetAlertDialog(v.getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Save new event?")
                .setCancelText("No, Cancel !")
                .setConfirmText("Yes, Save !")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.setTitleText("Cancelled!")
                                .setContentText("Event not saved !")
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
                            boolean result = saveEvent();
                            if (result) {
                                sDialog.setTitleText("Success!")
                                        .setContentText("Your event has been successfully created")
                                        .setConfirmText("OK")
                                        .showCancelButton(false)
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                updateUserEventCount();
                                sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Intent i = new Intent(getActivity(), EventActivity.class);
                                        i.putExtra("Activity", "EventActivity");
                                        startActivity(i);
                                        getActivity().finish();

                                    }
                                });
                            } else {
                                sDialog.setTitleText("Failure!")
                                        .setContentText("Event not yet created !")
                                        .setConfirmText("OK")
                                        .showCancelButton(false)
                                        .setCancelClickListener(null)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            }
                        } catch (Exception e) {
                            sDialog.setTitleText("Failure!")
                                    .setContentText("Event not yet created !")
                                    .setConfirmText("OK")
                                    .showCancelButton(false)
                                    .setCancelClickListener(null)
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            Log.e(CreateRestaurantEventFragment.this.toString(), "Failed to create event " + e.getMessage());
                        }
                    }
                })
                .show();
    }

    //method to get URI
    private Uri getUri(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
        String path = MediaStore.Images.Media.insertImage(getActivity().getApplicationContext()
                .getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    class ActionBarCallBack implements ActionMode.Callback {
        int position;

        public ActionBarCallBack(int position) {
            this.position = position;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.item_delete:
                    deleteImage(position);
                    mode.finish();
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete_image, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

    // function to delete uploaded event images
    private void deleteImage(int position) {
        switch (position) {
            case 1:
                if (image2.getDrawable() == null || ((BitmapDrawable) image2.getDrawable()).getBitmap() == null) {
                    image1.setImageBitmap(null);
                    image1Uri = null;
                } else {
                    image1.setImageBitmap(((BitmapDrawable) image2.getDrawable()).getBitmap());
                    image1Uri = getUri(((BitmapDrawable) image2.getDrawable()).getBitmap());
                    if (image3.getDrawable() == null || ((BitmapDrawable) image3.getDrawable()).getBitmap() == null) {
                        image2.setImageBitmap(null);
                        image2Uri = null;
                    } else {
                        image2.setImageBitmap(((BitmapDrawable) image3.getDrawable()).getBitmap());
                        image2Uri = getUri(((BitmapDrawable) image3.getDrawable()).getBitmap());
                        if (image4.getDrawable() == null || ((BitmapDrawable) image4.getDrawable()).getBitmap() == null) {
                            image3.setImageBitmap(null);
                            image3Uri = null;
                        } else {
                            image3.setImageBitmap(((BitmapDrawable) image4.getDrawable()).getBitmap());
                            image3Uri = getUri(((BitmapDrawable) image4.getDrawable()).getBitmap());
                            image4.setImageBitmap(null);
                            image4Uri = null;
                        }
                    }
                }
                counter--;
                break;
            case 2:
                if (image3.getDrawable() == null || ((BitmapDrawable) image3.getDrawable()).getBitmap() == null) {
                    image2.setImageBitmap(null);
                    image2Uri = null;
                } else {
                    image2.setImageBitmap(((BitmapDrawable) image3.getDrawable()).getBitmap());
                    image2Uri = getUri(((BitmapDrawable) image3.getDrawable()).getBitmap());
                    if (image4.getDrawable() == null || ((BitmapDrawable) image4.getDrawable()).getBitmap() == null) {
                        image3.setImageBitmap(null);
                        image3Uri = null;
                    } else {
                        image3.setImageBitmap(((BitmapDrawable) image4.getDrawable()).getBitmap());
                        image3Uri = getUri(((BitmapDrawable) image4.getDrawable()).getBitmap());
                        image4.setImageBitmap(null);
                        image4Uri = null;
                    }
                }
                counter--;
                break;
            case 3:
                if (image4.getDrawable() == null || ((BitmapDrawable) image4.getDrawable()).getBitmap() == null) {
                    image3.setImageBitmap(null);
                    image3Uri = null;
                } else {
                    image3.setImageBitmap(((BitmapDrawable) image4.getDrawable()).getBitmap());
                    image3Uri = getUri(((BitmapDrawable) image4.getDrawable()).getBitmap());
                    image4.setImageBitmap(null);
                    image4Uri = null;
                }
                counter--;
                break;
            case 4:
                image4.setImageBitmap(null);
                image4Uri = null;
                counter--;
                break;
            default:
                break;
        }
    }
}
