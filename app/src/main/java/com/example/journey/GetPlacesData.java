package com.example.journey;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class GetPlacesData extends AsyncTask<Object, String, String>
{

    String googlePlacesData, url;
    GoogleMap googleMap;
    MarkerOptions markerOptions;

    private Context context;

    //Constructor (In order to get the context of the class to show the toast)
    public GetPlacesData(Context context)
    {
        this.context = context;
    }

    @Override   //Perform background computations that can take long time
    protected String doInBackground(Object... params)   //Pass as many Object parameters as desired (...)
    {
        try
        {
            googleMap = (GoogleMap) params[0];
            url = (String) params[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
        }
        catch (Exception e)
        {
        }

        return googlePlacesData;
    }

    @Override   //Invoked after doInBackground() finishes
    protected void onPostExecute(String result)
    {
        List<HashMap<String, String>> nearbyPlacesList = null;   //HashMap is slow (To change)
        ParseData dataParser = new ParseData();
        nearbyPlacesList =  dataParser.parse(result);

        ShowNearbyPlaces(nearbyPlacesList);
    }

    public void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList)
    {
        for (int i = 0; i < nearbyPlacesList.size(); i++)
        {
            markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);   //Retrieve the information (lat, lng, place_name...) from the place extracted on ParseData.java
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String rating = googlePlace.get("rating");
            String price_level = googlePlace.get("price_level");
            String name= googlePlace.get("name");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName).snippet("\nLocation: " + vicinity + "\nRating: " + rating + "\nPrice level: " + price_level + "\nname: " + name);
            googleMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 14));
        }
    }
}