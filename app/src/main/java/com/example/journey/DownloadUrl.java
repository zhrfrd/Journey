package com.example.journey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUrl
{
    public String readUrl(String strUrl)
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try
        {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();   // Creating an http connection to communicate with url
            urlConnection.connect();   // Connecting to url
            iStream = urlConnection.getInputStream();   // Reading data from url

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";

            while ((line = br.readLine()) != null)
                sb.append(line);

            data = sb.toString();
            br.close();

        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                iStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            urlConnection.disconnect();
        }

        return data;
    }
}