package com.example.journey;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class MyCompassActivity extends AppCompatActivity implements SensorEventListener, LocationListener
{
    //Variables
    private Boolean mLocationPermissionGranted = false;
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private TextView tvHeading;
    private Location origin = new Location("A");
    private Location target = new Location("B");
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float distance;

    //Overrides Methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        //Get the intent from the previous activity
        Intent intent = getIntent();
        LatLng latLngLocation = intent.getParcelableExtra("location");  //Get destination
        LatLng latLngOrigin = intent.getParcelableExtra("origin");   //Get origin
        distance= intent.getFloatExtra("distance", 1);   //Get distance

        double targetLat = latLngLocation.latitude;
        double targetLng = latLngLocation.longitude;
        double originLat = latLngOrigin.latitude;
        double originLng = latLngOrigin.longitude;

        image = findViewById(R.id.imageViewCompass);
        tvHeading = findViewById(R.id.tvHeading);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);   //Allow to access the device sensor

        //Get device latitude and longitude
        origin.setLatitude(originLat);
        origin.setLongitude(originLng);

        //Get target latitude and longitude
        target.setLatitude(targetLat);
        target.setLongitude(targetLng);

        getDeviceLocation();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //getDeviceLocation();

        //Get the angle around the z axis rotated
        float degree = Math.round(event.values[0]);   //"accuracy" field
        degree += 1;

        float bearing = origin.bearingTo(target);   //Measurement of direction between origin and destination  (360 degrees)
        degree = (bearing - degree) * -1;   //Changing of the degree when the sensor change
        tvHeading.setText("The selected point is " + Float.toString(distance) + " meters away from you!");

        //Rotation animation
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(210);   //Animation duration
        ra.setFillAfter(true);   //Animate the compass after the reservation status

        //Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;   //Update the current device direction to allow the compass animation
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);   //Stop the listener in order to save battery
    }

    //METHODS
    //Get device location
    public void getDeviceLocation()
    {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    //UN USED OVERRIDE METHODS

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
<<<<<<< HEAD

=======
>>>>>>> 9c288cef5db6128889f42d5ed3233234ef585524
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
<<<<<<< HEAD

=======
>>>>>>> 9c288cef5db6128889f42d5ed3233234ef585524
    }

    @Override
    public void onProviderEnabled(String provider)
    {
<<<<<<< HEAD

=======
>>>>>>> 9c288cef5db6128889f42d5ed3233234ef585524
    }

    @Override
    public void onProviderDisabled(String provider)
    {
<<<<<<< HEAD

=======
>>>>>>> 9c288cef5db6128889f42d5ed3233234ef585524
    }
}
