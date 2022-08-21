package com.example.journey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseData   //Get information of the points of interest from the json file and paste it to the HashMap googlePlaceMap
{
    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try
        {
            jsonObject = new JSONObject((String) jsonData);
            jsonArray = jsonObject.getJSONArray("results");   //Information relative to the places of the location
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray)
    {
        int placesCount = jsonArray.length();    //Number of places
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;   //HashMap is slow (To change)

        for (int i = 0; i < placesCount; i++)
        {
            try
            {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);   //Add the places to the List

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)   //Get information relative to the place from the JSON file
    {
        //Variables
        HashMap<String, String> googlePlaceMap = new HashMap<String, String>();   //HashMap is slow (To change)
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String rating = "";
        String price_level = "";
        String name = "";

        try
        {
            //Retrieve the information relative to the location from the json object
            if (!googlePlaceJson.isNull("name"))
                placeName = googlePlaceJson.getString("name");
            if (!googlePlaceJson.isNull("vicinity"))
                vicinity = googlePlaceJson.getString("vicinity");
            if(!googlePlaceJson.isNull("rating"))
                rating = googlePlaceJson.getString("rating");
            if(!googlePlaceJson.isNull("price_level"))
                price_level = googlePlaceJson.getString("price_level");
            if(!googlePlaceJson.isNull("name"))
                name = googlePlaceJson.getString("name");

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");

            //Assign the information relative to the location retrieved from the json object
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("rating", rating);
            googlePlaceMap.put("price_level", price_level);
            googlePlaceMap.put("name", name);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return googlePlaceMap;   //Return the HashMap variable containing the information (place_name, vicinity, la, lng...) relative to the place. Used by GetPlacesData.java
    }
}
