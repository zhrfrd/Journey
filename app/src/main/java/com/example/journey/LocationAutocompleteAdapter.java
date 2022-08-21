package com.example.journey;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class LocationAutocompleteAdapter extends ArrayAdapter implements Filterable
{
    ArrayList<String> results;
    int resource;
    Context context;
    GooglePlacesApi placeApi = new GooglePlacesApi();

    public LocationAutocompleteAdapter(@NonNull Context context, int resId)   //Value cannot be null
    {
        super(context, resId);
        this.context = context;
        this.resource = resId;   //layout of textview (simple_list_item_1 --> See MapActivity
    }

    @Override
    public Filter getFilter()   //Create a new filter
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)   //Invoked by the thread to filter the result according to the constraint
            {
                FilterResults filterResults = new FilterResults();   //Holds the result of the filtered operation (2 results): values --> Value of the result
                                                                     //                                                        count --> The number of the results
                if(constraint != null)
                {
                    results = placeApi.autoComplete(constraint.toString());   //Show results by using the string typed on the search box as a constraint

                    filterResults.values = results;   //Actual result from the filtered operation
                    filterResults.count = results.size();   //Number of the results
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)   //Invoked inside the UI thread to show he filtered result in real time
            {
                if((results != null) && (results.count > 0))
                    notifyDataSetChanged();

                else
                    notifyDataSetInvalidated();
            }
        };

        return filter;
    }

    @Override
    public int getCount()
    {
        return results.size();
    }

    @Override
    public String getItem(int pos)
    {
        return results.get(pos);
    }

}
