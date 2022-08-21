package com.example.journey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GooglePlacesApi
{
    public ArrayList<String> autoComplete(String input)   //Method used to retrieve the results of the research while typing on the research box (Called inside LocationAutocompleteAdapter)
    {
        ArrayList <String> arrayList = new ArrayList();
        HttpURLConnection connection = null;   //Subclass of URLConnection
        StringBuilder jsonResult = new StringBuilder();
        try
        {
            //Request to Google API and receive the response in stringBuilder
            StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?");
            sb.append("input=" + input);
            sb.append("&key=AIzaSyBV1-aIaajlChV0XbgWJaPOQ3KIQfM5T-g");
            URL url = new URL(sb.toString());   //Pointer to a www resource
            connection = (HttpURLConnection) url.openConnection();
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());   //Read from the stream

            int read;
            char[] buff = new char[1024];   //Maximum size

            while ((read = inputStreamReader.read(buff)) != -1)   ///While there are characters to read from the stream
                jsonResult.append(buff, 0, read);   //Add to the URL (StringBuilder) the character read
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            JSONObject jsonObject = new JSONObject(jsonResult.toString());
            JSONArray prediction = jsonObject.getJSONArray("predictions");   //"predictions" contain an array of results of Google Places from the JSON file

            for (int i = 0; i < prediction.length(); i ++)
                arrayList.add(prediction.getJSONObject(i).getString("description"));   //Add to an ArrayList the name of the places ("description")
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return arrayList;   //Return the list of places
    }
}
