package com.syracuse.android.flavors.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.syracuse.android.flavors.util.CityUtility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

//adapter to set values of cities
public class CityArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> cityList;
    private ArrayFilter mFilter;

    @SuppressWarnings("unchecked")
    public CityArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        cityList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public String getItem(int position) {
        return cityList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                // Query the autocomplete API for the entered constraint
                MyCityDownloadAsyncTask downloadCityList = new MyCityDownloadAsyncTask(CityArrayAdapter.this);
                String url = CityUtility.CITY_URL + constraint;
                Log.d(getClass().getCanonicalName(), url);
                downloadCityList.execute(new String[]{url});

                if (cityList != null) {
                    // Results
                    results.values = cityList;
                    results.count = cityList.size();
                }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                // The API returned at least one result, update the data.
                notifyDataSetChanged();
            } else {
                // The API did not return any results, invalidate the data set.
                notifyDataSetInvalidated();
            }
        }
    }

    //async task to get city data from geobytes web service
    private class MyCityDownloadAsyncTask extends AsyncTask<String, Void, CityUtility> {
        private final WeakReference<CityArrayAdapter> adapterReference;

        public MyCityDownloadAsyncTask(CityArrayAdapter adapter) {
            adapterReference = new WeakReference<>(adapter);
        }

        @Override
        protected CityUtility doInBackground(String... urls) {
            CityUtility cityData = new CityUtility();
            for (String url : urls) {
                cityData.downloadCityDataJson(url);
            }
            return cityData;
        }

        @Override
        protected void onPostExecute(CityUtility cityData) {
            if (cityList != null) {
                cityList.clear();
            }
            cityList = cityData.getCityList();

            final CityArrayAdapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        }
    }
}