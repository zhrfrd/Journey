package com.example.journey;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData //Hold reference to the polyline and directions leg
{
    private Polyline polyline;
    private DirectionsLeg leg;

    public PolylineData(Polyline polyline, DirectionsLeg leg)
    {
        this.polyline = polyline;
        this.leg = leg;
    }

    public Polyline getPolyline()
    {
        return polyline;
    }

    public DirectionsLeg getLeg()
    {
        return leg;
    }
<<<<<<< HEAD

    @Override
    public String toString()
    {
        return "PolylineData{" + "polyline=" + polyline + ", leg=" + leg + '}';
    }
=======
>>>>>>> 9c288cef5db6128889f42d5ed3233234ef585524
}