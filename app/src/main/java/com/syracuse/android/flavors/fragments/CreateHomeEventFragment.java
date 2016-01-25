package com.syracuse.android.flavors.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.syracuse.android.flavors.activities.CreateHomeEventActivity;
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
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Fragment to display create event screen
 *
 * created by Sandesh
 */
public class CreateHomeEventFragment extends Fragment {

    private static final String ARG_OPTION = "argument_option";
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
    com.rengwuxian.materialedittext.MaterialAutoCompleteTextView addressTxt = null;
    Button createEventBtn;

    com.rengwuxian.materialedittext.MaterialEditText eventNameET;
    com.rengwuxian.materialedittext.MaterialEditText menuETVal;
    com.rengwuxian.materialedittext.MaterialEditText costET;
    com.rengwuxian.materialedittext.MaterialEditText maxGuests;

    FloatingActionMenu actionsMenu;
    EventDetails details;
    EventImages images;

    Uri image1Uri;
    Uri image2Uri;
    Uri image3Uri;
    Uri image4Uri;
    Uri cameraImgUri;

    boolean saveEventRes = true;
    boolean imageSave = false;
    boolean isStartTime = false;
    boolean isEndTime = false;
    int counter = 0;

    public CreateHomeEventFragment() {
    }


    // Create a new instance of the create home event fragment
    public static CreateHomeEventFragment newInstance(int option) {
        CreateHomeEventFragment fragment = new CreateHomeEventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_OPTION, option);
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
        rootView = inflater.inflate(R.layout.fragment_create_home_event, container, false);
        details = ((CreateHomeEventActivity) getActivity()).getEventDetails();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Create Home Event");
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
        eventNameET.addValidator(new RegexpValidator("Event name is Invalid","[a-zA-Z0-9]+([ '-][a-zA-Z0-9]*)*"));
        menuETVal = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.menuET);
        costET = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.costET);
        costET.addValidator(new RegexpValidator("Cost is Invalid!", "\\d+\\.?\\d*"));
        maxGuests = (com.rengwuxian.materialedittext.MaterialEditText) rootView.findViewById(R.id.maxGuestsVal);
        maxGuests.addValidator(new RegexpValidator("Max Guests is Invalid!", "\\d+"));
        menuETVal.addValidator(new RegexpValidator("Menu is Invalid", "[a-zA-Z0-9]+([ '-][a-zA-Z0-9]*)*"));

        addressTxt = (com.rengwuxian.materialedittext.MaterialAutoCompleteTextView) rootView.findViewById(R.id.locationVal);
        galleryButton = (FloatingActionButton) rootView.findViewById(R.id.galleryButton);
        cameraButton = (FloatingActionButton) rootView.findViewById(R.id.cameraButton);
        actionsMenu = (FloatingActionMenu) rootView.findViewById(R.id.multiple_actions);

        //added for autocomplete of text with google places
        addressTxt.setThreshold(3);
        addressTxt.setOnItemClickListener(((CreateHomeEventActivity) getActivity()).getmAutocompleteClickListener());
        addressTxt.setAdapter(((CreateHomeEventActivity) getActivity()).getmPlaceArrayAdapter());

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

        addressTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if(addressTxt.getText() != null)
                        getLatLongFromAddress(addressTxt.getText().toString());
                }
            }
        });

        addressTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                getLatLongFromAddress(addressTxt.getText().toString());
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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
                if (eventNameET.validate() && maxGuests.validate() && menuETVal.validate() && costET.validate())
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


        // Work Around to Fix the Scrollview effect when scrolling google maps
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
        map = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        return rootView;
    }

    // Sublime Picker Callback
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

    // Applies a StyleSpan to the supplied text
    private SpannableStringBuilder applyBoldStyle(String text) {
        SpannableStringBuilder ss = new SpannableStringBuilder(text);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }



    /*
     *  Function to trigger a new event on clicking save button
     */
    public boolean saveEvent() throws ParseCustomException, IOException {
        try {
            saveEventRes = true;
            MyApplication application = (MyApplication) getActivity().getApplicationContext();
            details.setEventName(eventNameET.getText().toString());
            details.setUserId(application.getApplicationManager().getId());
            details.setMenu(menuETVal.getText().toString());
            details.setCost(Double.parseDouble(costET.getText().toString()));
            details.setMaxGuests(Integer.parseInt(maxGuests.getText().toString()));
            details.setAvailGuests(Integer.parseInt(maxGuests.getText().toString()));
            details.setAddress(addressTxt.getText().toString());
            details.setEventType("Home");
            details.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // Saved successfully.
                        String id = details.getObjectId();
                        Log.d(CreateHomeEventFragment.class.getCanonicalName(), "Event Created Successfully!");
                        Log.d(CreateHomeEventFragment.class.getCanonicalName(), "The object id is: " + id);
                    } else {
                        Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Failed to create event error: " + e.getMessage());
                        // The save failed.
                        saveEventRes = false;
                    }
                }
            });
            if (!saveEventRes) {
                Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Failed to save event");
                throw new ParseCustomException("Failed to save event :");
            }


            for (int i = 1; i <= counter; i++) {

                ParseFile photoFile;
                images = new EventImages();
                ParseRelation<ParseObject> relation = images.getRelation("EventId");
                relation.add(details);
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap bitmap;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                // Save the scaled image to Parse
                try {
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

                    byte[] inputData = byteArrayOutputStream.toByteArray();
                    photoFile = new ParseFile("event_photo" + i + ".jpg", inputData);
                } catch (FileNotFoundException e) {
                    saveEventRes = false;
                    Log.e(CreateHomeEventFragment.class.toString(), "File Not found in saveEvent()");
                    throw e;
                } catch (IOException e) {
                    saveEventRes = false;
                    Log.e(CreateHomeEventFragment.class.toString(), "IO exception in saveEvent()");
                    throw e;
                }

                images.setImageFile(photoFile);
                try {
                    images.save();
                } catch (ParseException e1) {
                    Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Failed to save image! Trying again");
                    e1.printStackTrace();
                    i = 0;
                }
            }
            //throw parse exception if error in save
            if (!saveEventRes) throw new ParseCustomException("Exception in saveEvent()");
            else return true;

        } catch (Exception e) {
            Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Exception in saveEvent()");
            throw new ParseCustomException("Exception in saveEvent()");
        }

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

    // Perform action on action of uploading images through gallery or camera
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
            } catch (Exception e) {
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
        } else  {
            Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Failed to get intent data, result code is " + resultCode);
        }

    }

    // Get latitude and longitude from address
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
                String cityName = addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex() - 1);
                boolean containsNumber  = hasNumber(cityName);

                if(containsNumber) {
                    //remove everything after last space
                    if (null != cityName && cityName.length() > 0) {
                        int endIndex = cityName.lastIndexOf(" ");
                        if (endIndex != -1) {
                            cityName = cityName.substring(0, endIndex);
                        }
                    }
                }

                cityName = cityName + ", " + addresses.get(0).getCountryName();

                details.setCity(cityName);
                LatLng location = new LatLng(latitude, longitude);
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(location)
                        .title("House Event Address")
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

    // method to confirm save event
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
                            Log.e(CreateHomeEventFragment.class.getCanonicalName(), "Failed to create event ! " + e.getMessage());
                        }
                    }
                })
                .show();
    }

    // Method to update the user event count
    public void updateUserEventCount() throws ParseException {
        MyApplication application = (MyApplication) getActivity().getApplicationContext();
        List<ParseObject> userList;
        String id = application.getApplicationManager().getId();
        ParseQuery<ParseObject> user = ParseQuery.getQuery("EventUsers");
        user.whereEqualTo("Fb", id);
        userList = user.find();
        EventUsers eventUsers = (EventUsers)userList.get(0);
        eventUsers.increment("EventsOrganised");
        eventUsers.saveInBackground();
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

    // Method to delete event images
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

    //method to check if a string contains number
    private boolean hasNumber(String address){
        return address.matches(".*\\d+.*");
    }
}
