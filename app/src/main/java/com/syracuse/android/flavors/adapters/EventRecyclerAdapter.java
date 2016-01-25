package com.syracuse.android.flavors.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.rey.material.widget.Button;
import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.model.EventDetails;
import com.syracuse.android.flavors.model.EventImages;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*Adapter to show individual row items in event list*/
public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    private List<ParseObject> mDataSet;
    private Context mContext;
    int tabIndex;
    List<ParseObject> eventImagesList;
    private HashMap<String, String> imageMap;
    public LruCache<String,String> mDistanceMermoryCache;
    private DisplayImageOptions options;
    ImageLoader imageLoader;
    OnItemClickListener myItemClickListener;

    public EventRecyclerAdapter(Context myContext, List<ParseObject> myDataSet, LruCache<String,String> distanceMermoryCache,int myTabIndex) {
        mContext = myContext;
        mDataSet = myDataSet;
        tabIndex = myTabIndex;
        mDistanceMermoryCache = distanceMermoryCache;
        imageMap = new HashMap<>();

        //universal image loader
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20)).build();
    }

    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView vEventIcon;
        public TextView vEventName;
        public TextView vMenu;
        public TextView vTime;
        public TextView vGuestsAvailable;
        public TextView votalDistance;
        public TextView vaddress;
        public Button typeButton;

        public ViewHolder(View v) {
            super(v);
            vEventIcon = (ImageView) v.findViewById(R.id.eventIcon);
            vEventName = (TextView) v.findViewById(R.id.eventName);
            vMenu = (TextView) v.findViewById(R.id.eventMenu);
            vTime = (TextView) v.findViewById(R.id.eventTime);
            vGuestsAvailable = (TextView) v.findViewById(R.id.guestsAvailable);
            votalDistance = (TextView) v.findViewById(R.id.totaldistance);
            vaddress = (TextView) v.findViewById(R.id.addressTV);
            typeButton = (com.rey.material.widget.Button)v.findViewById(R.id.eventType);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myItemClickListener != null) {
                        myItemClickListener.onItemClick(v,getPosition());
                    }
                }
            });
        }

        public void bindEventData(EventDetails event) {
            String eventName = event.getEventName();
            String eventMenu = event.getMenu();
            Date startTime = event.getStartTime();
            int guestsAvailable = event.getAvailGuests();
            String eventType = event.getEventType();

            //set event type
            if(eventType.equals("Restaurant")){
                typeButton.setText("R");
                typeButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corners_red));
            }else{
                typeButton.setText("H");
                typeButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_corners_green));
            }

            vEventName.setText(eventName);
            vMenu.setText(eventMenu);
            String address = event.getAddress().replace("\n", " ").replace("\r", " ");
            vaddress.setText(address);

            //set time of event

            SimpleDateFormat inputFormat = new SimpleDateFormat("EE, dd MMM yyyy 'at' hh:mm a");
            String displayDate = inputFormat.format(startTime);
            MyApplication stateManager = (MyApplication) mContext.getApplicationContext();
            String latitude = stateManager.getApplicationManager().getLatitude();
            String longitude = stateManager.getApplicationManager().getLongitude();

            vTime.setText(displayDate);
            vGuestsAvailable.setText(guestsAvailable + " seats available");

            String objectId = event.getObjectId();
            MyEventsImageDownloadAsyncTask task = new MyEventsImageDownloadAsyncTask(vEventIcon, objectId);
            task.execute();

            final String dist = mDistanceMermoryCache.get(objectId);
            if(dist !=null) {
                votalDistance.setText(dist);
            }
            else {
                EventsDistanceCalculatorAsyncTask loadDistance = new EventsDistanceCalculatorAsyncTask(objectId,votalDistance, event.getLatitude(),
                        event.getLongitude(), latitude, longitude);
                loadDistance.execute();
            }

        }
    }

    //Create new views
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.event_details_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        EventDetails event = (EventDetails) mDataSet.get(position);
        holder.bindEventData(event);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    //TODO: Save use cache to store images, handle exception logging
    private class MyEventsImageDownloadAsyncTask extends AsyncTask<String, String, String> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private String objectId;

        public MyEventsImageDownloadAsyncTask(ImageView image, String id) {
            imageViewWeakReference = new WeakReference<>(image);
            objectId = id;
        }

        @Override
        protected String doInBackground(String... params) {
            String imageUrl;
            if (imageMap.get(objectId) != null) {
                imageUrl = imageMap.get(objectId);
                return imageUrl;
            }

            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventImages");
                query.whereEqualTo("EventId", objectId);
                query.selectKeys(Arrays.asList("ImageFile"));
                eventImagesList = query.find();
                Log.d("result" + eventImagesList.size(), "size");
            } catch (ParseException e) {
                e.printStackTrace();
            }


            EventImages image = null;
            ParseFile eventIcon = null;
            //set event icon
            if (eventImagesList.size() > 0) {
                image = (EventImages) eventImagesList.get(0);
                eventIcon = image.getImageFile();
                imageUrl = eventIcon.getUrl();
            } else {
                imageUrl = "drawable://" + R.drawable.default_image;
            }
            imageMap.put(objectId, imageUrl);
            return imageUrl;
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if(imageLoader == null) imageLoader = ImageLoader.getInstance();
            if (imageViewWeakReference != null && imageUrl != null) {
                final ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    imageLoader.displayImage(imageUrl, imageViewWeakReference.get(), options);
                }
            }
        }
    }


    //TODO: Prompt user to turn on gps if not on : http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial/
    private class EventsDistanceCalculatorAsyncTask extends AsyncTask<String, Void, String> {
        private final WeakReference<TextView> distanceViewWeakReference;
        private Double prelatitute;
        private Double prelongitude;
        private String latitude;
        private String longitude;
        private String objectId;

        public EventsDistanceCalculatorAsyncTask(String id,TextView text, Double prelatitude,
                                                 Double preLongitude, String latitudeVal, String longitudeVal) {
            distanceViewWeakReference = new WeakReference<>(text);
            this.prelatitute = prelatitude;
            this.prelongitude = preLongitude;
            this.latitude = latitudeVal;
            this.longitude = longitudeVal;
            this.objectId = id;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected String doInBackground(String... params) {
            String result_in_miles = "";
            String url = "http://maps.google.com/maps/api/directions/xml?origin="
                    + latitude + "," + longitude + "&destination=" + prelatitute
                    + "," + prelongitude + "&sensor=false&units=miles";
            String tag[] = {"text"};
            HttpResponse response = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                response = httpClient.execute(httpPost, localContext);
                InputStream is = response.getEntity().getContent();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = builder.parse(is);
                if (doc != null) {
                    NodeList nl;
                    ArrayList args = new ArrayList();
                    for (String s : tag) {
                        nl = doc.getElementsByTagName(s);
                        if (nl.getLength() > 0) {
                            Node node = nl.item(nl.getLength() - 1);
                            args.add(node.getTextContent());
                        } else {
                            args.add("");
                        }
                    }
                    result_in_miles = String.format("%s", args.get(0));
                    result_in_miles = result_in_miles.replace("mi", "miles away").replace("ft","feet away");
                    mDistanceMermoryCache.put(objectId, result_in_miles);
                    Log.d(EventRecyclerAdapter.class.getCanonicalName(), "Distance in miles :" + result_in_miles);
                }
            } catch (Exception e) {
                Log.e(EventRecyclerAdapter.class.getCanonicalName(), "Exception occured while getting distance");
                return null;
            }
            return result_in_miles;
        }

        @Override
        protected void onPostExecute(String distance) {
            if (distance != null) {
                final TextView textView = distanceViewWeakReference.get();
                if (textView != null) {
                    textView.setText(distance);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.myItemClickListener = mItemClickListener;
    }
}
