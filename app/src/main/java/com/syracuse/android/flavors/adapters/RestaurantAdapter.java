package com.syracuse.android.flavors.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.syracuse.android.flavors.MyApplication;
import com.syracuse.android.flavors.R;
import com.syracuse.android.flavors.util.RestaurantUtility;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Adapter for Restaurant list
 */
public class RestaurantAdapter  extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder>{
    private List<Map<String,?>> mDataSet;
    private Context mContext;
    LruCache mImgMemoryCache;
    OnItemClickListener mItemClickListener;
    public android.util.LruCache<String,String> mDistanceMermoryCache;

    public RestaurantAdapter(Context myContext,List<Map<String,?>> myDataSet,LruCache myImgMemoryCache, android.util.LruCache<String,String> distanceMermoryCache){
        mContext = myContext;
        mDataSet = myDataSet;
        mImgMemoryCache = myImgMemoryCache;
        mDistanceMermoryCache = distanceMermoryCache;
    }

    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView vIcon;
        public TextView vTitle;
        public ImageView vRating;
        public TextView vReviewCount;
        public TextView vCategories;
        public TextView vTotalDistance;

        public ViewHolder(View v){
            super(v);
            vIcon = (ImageView)v.findViewById(R.id.icon);
            vTitle = (TextView)v.findViewById(R.id.name);
            vRating = (ImageView)v.findViewById(R.id.ratingImg);
            vReviewCount = (TextView)v.findViewById(R.id.reviewcount);
            vCategories = (TextView) v.findViewById(R.id.categories);
            vTotalDistance = (TextView) v.findViewById(R.id.gpsLocation);

            //set this to select restaurant
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItemClickListener != null){
                        mItemClickListener.onItemClick(v, getPosition());
                    }
                }
            });
        }

        @SuppressWarnings("unchecked")
        public void bindRestData(Map<String,?> restaurant){
            String restName = (String)restaurant.get("name");
            vTitle.setText(restName);
            vReviewCount.setText(restaurant.get("reviewCount") + " reviews");
            vCategories.setText((String)restaurant.get("categories"));

            String iconUrl = (String)restaurant.get("icon");
            String ratingUrl = (String)restaurant.get("rating");
            final Bitmap bitmap = (Bitmap)mImgMemoryCache.get(iconUrl);
            final Bitmap ratingBitmap = (Bitmap)mImgMemoryCache.get(ratingUrl);

            if(bitmap != null){
                vIcon.setImageBitmap(bitmap);
                vRating.setImageBitmap(ratingBitmap);
            }else{
                MyDownloadImageAsyncTask task = new MyDownloadImageAsyncTask(vIcon);
                task.execute(new String[] {iconUrl});
                DownloadRatingImageAsyncTask ratingTask = new DownloadRatingImageAsyncTask(vRating);
                ratingTask.execute(new String[] {ratingUrl});
            }


            MyApplication stateManager = (MyApplication) mContext.getApplicationContext();
            String latitude = stateManager.getApplicationManager().getLatitude();
            String longitude = stateManager.getApplicationManager().getLongitude();
            final String dist = mDistanceMermoryCache.get(iconUrl);
            if(dist !=null) {
                vTotalDistance.setText(dist);
            } else {
                EventsDistanceCalculatorAsyncTask loadDistance = new EventsDistanceCalculatorAsyncTask(iconUrl,vTotalDistance,(Double)restaurant.get("latitude") ,
                        (Double)restaurant.get("longitude"), latitude, longitude);
                loadDistance.execute();
            }
        }
    }

    //Create new views
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.restaurant_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Map<String,?> restaurant = mDataSet.get(position);
        holder.bindRestData(restaurant);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    private class MyDownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        public MyDownloadImageAsyncTask(ImageView imv){
            imageViewReference = new WeakReference<>(imv);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            for(String url : urls){
                Log.v("URL :",url);
                String updatedUrl = url.replace("ms.jpg","l.jpg");
                Log.v("Updated URL :",updatedUrl);
                bitmap = RestaurantUtility.downloadImage(updatedUrl);

                if(bitmap != null){
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    mImgMemoryCache.put(url, bitmap);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                final ImageView imageView = imageViewReference.get();
                if(imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    //async task to download rating image
    private class DownloadRatingImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> ratingImgViewReference;
        public DownloadRatingImageAsyncTask(ImageView ratingImg){
            ratingImgViewReference = new WeakReference<>(ratingImg);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            for(String url : urls){
                bitmap = RestaurantUtility.downloadImage(url);
                if(bitmap != null){
                    mImgMemoryCache.put(url, bitmap);
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                final ImageView imageView = ratingImgViewReference.get();
                if(imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

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

        @Override
        @SuppressWarnings("unchecked")
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
                    result_in_miles = result_in_miles.replace("mi", "miles").replace("ft","feet").replace(",","").trim();
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
}
