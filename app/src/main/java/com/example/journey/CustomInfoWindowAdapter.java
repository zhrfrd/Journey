package com.example.journey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
{
    private final View mWindow;

    public CustomInfoWindowAdapter(Context context)
    {
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText (Marker marker, View view)
    {
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);
        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.snippet);

        tvTitle.setText(title);
        tvSnippet.setText(snippet);
    }

    //Show Info Window
    @Override
    public View getInfoWindow(Marker marker)
    {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    //Show Info Window Content
    @Override
    public View getInfoContents(Marker marker)
    {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}
