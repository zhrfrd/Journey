package com.example.journey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener {
    //VARIABLES---------------------------------------------------------------------------------

    //Constants
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int DEFAULT_ZOOM = 18;

    //Widgets
    private AutoCompleteTextView mSearchStart;
    private AutoCompleteTextView mSearchDestination;
    private Button bAlong, bNearby;
    private ImageView mGps, mItinerary, mRefreshMap, mCompass, mDirections, mGetGps, imgClearStart, imgClearDestination;
    private ImageButton bRestaurants, bHotels, bAtms, bAttractions, bShopping, bPetrol, bHospitals, bBike, bCar;
    private TextView tRestaurant, tHotel, tAtm, tAttraction, tShopping, tPetrol, tHospital, tBike, tCar;
    private Spinner spinnerRadius, spinnerAccuracy;

    //Other variables
    private Boolean mLocationPermissionGranted = false;
    private Boolean isInterStopSelected = false;     //
    private Boolean isSearchRouteSelected = false;   //Check if the user can calculate the intermediate stops along the route
    private Boolean isRouteSelected = false;         //
    private Boolean isRouteCreated = false;
    private Boolean isMarkerSelected = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoApiContext mGeoApiContext = null;
    private LatLng latLngStart;
    private LatLng latLngDestination;
    private LatLng interPointLatLng;
    private LatLng latLngMarker;
    private MarkerOptions markerOptionStart;
    private MarkerOptions markerOptionDestination;
    private Polyline polyline;
    private int countLoc = 0;   //Counter for the locations
    private int proxRadius = 100;   //Default radius value
    private int accuracy = 5;   //Default accuracy value
    private String place;
    private String selected;
    private String[] itemsRadius = new String[]{"100 m", "200 m", "500 m", "1 km", "3 km", "5 km"};
    private String[] itemAccuracy = new String[]{"25 %", "50 %", "75 %", "100 %"};
    private float distanceCompass;
    private ArrayList<PolylineData> mPolylineData = new ArrayList<>();
    private ArrayAdapter<String> radiusItemsAdapter;
    private ArrayAdapter<String> accuracyItemsAdapter;
    private Location currentLocation, locationMarker;
    private List<LatLng> polylineSelectedPoints;

 
    //OVERRIDE METHODS---------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Welcome message
        Toast.makeText(this, "Map loaded!", Toast.LENGTH_SHORT).show();

        //Initialising map
        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        //If the user allow to use location...
        if (mLocationPermissionGranted)
        {
            getDeviceLocation();   //Function to get the location of the device

            //Automatically generated permission check
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;

            mMap.setMyLocationEnabled(true);   //Register device location and mark a blue icon on the current device location
            mMap.getUiSettings().setMyLocationButtonEnabled(false);   //Remove default GPS button on top-right of the screen

            init();   //If the user consent to track location, initialise the geolocation
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Initialising objects
        mSearchStart = findViewById(R.id.input_start);
        mSearchDestination = findViewById(R.id.input_destination);
        mGps = findViewById(R.id.ic_gps);
        mItinerary = findViewById(R.id.place_picker);
        mRefreshMap = findViewById(R.id.refresh_map);
        mCompass = findViewById(R.id.info_place);
        mDirections = findViewById(R.id.places_directions);

        mGetGps = findViewById(R.id.ic_magnificy1);

        mSearchStart.setAdapter(new LocationAutocompleteAdapter(this, android.R.layout.simple_list_item_1 ));       //Apply autocomplete adapter to get suggestions
        mSearchDestination.setAdapter(new LocationAutocompleteAdapter(this, android.R.layout.simple_list_item_1));  //

        bRestaurants = findViewById(R.id.bttRestaurant);
        bHotels = findViewById(R.id.bttHotel);
        bAtms = findViewById(R.id.bttAtm);
        bAttractions = findViewById(R.id.bttMonument);
        bShopping = findViewById(R.id.bttShopping);
        bPetrol = findViewById(R.id.bttGas);
        bHospitals = findViewById(R.id.bttHospital);
        bBike = findViewById(R.id.bttBike);
        bCar = findViewById(R.id.bttMechanic);

        tRestaurant = findViewById(R.id.txtRestaurant);
        tHotel = findViewById(R.id.txtHotel);
        tAtm = findViewById(R.id.txtAtm);
        tAttraction = findViewById(R.id.txtMonument);
        tShopping = findViewById(R.id.txtShopping);
        tPetrol = findViewById(R.id.txtGas);
        tHospital = findViewById(R.id.txtHospital);
        tBike = findViewById(R.id.txtBike);
        tCar = findViewById(R.id.txtCar);

        bAlong = findViewById(R.id.btnAlong);
        bNearby = findViewById(R.id.btnNearby);

        spinnerRadius = findViewById(R.id.cmbRadius);   //Dropdown menu for the radius
        spinnerAccuracy = findViewById(R.id.cmbAccuracy);   //Dropdown menu for accuracy

        imgClearStart = findViewById(R.id.imgClearStart);
        imgClearDestination = findViewById(R.id.imgClearDestination);

        mPolylineData = new ArrayList<>();
        radiusItemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsRadius);
        accuracyItemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemAccuracy);

        spinnerRadius.setAdapter(radiusItemsAdapter);   //Apply the adapter to the spinner
        spinnerAccuracy.setAdapter(accuracyItemsAdapter);

        //Operations
        getLocationPermission();   //Ask to the user for localisation permissions
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;   //Reset

        if ((requestCode == LOCATION_PERMISSION_REQUEST_CODE) && (grantResults.length > 0))   //grantResult value: PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED
        {
            //Check if the permission has been denied
            for (int i = 0; i < grantResults.length; i ++)
            {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                {
                    mLocationPermissionGranted = false;
                    return;
                }
            }

            mLocationPermissionGranted = true;
            initMap();   //Initialize the map
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline)   //Event when click the polyline
    {
        calculatePolyline(polyline);   //Retrieve the routes available
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
    }

    //METHODS-----------------------------------------------------------------------------------

    //Initialising method
    private void init()
    {
        //Disable the two buttons at the bottom
        bAlong.setEnabled(false);
        bNearby.setEnabled(false);

        //When the user presses the GPS icon on the search bar, retrieve the current location
        mGetGps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDeviceLocation();   //Get current location
                //Insert current location coordinates to the textbox
                mSearchStart.setText("My Location (" + String.valueOf(currentLocation.getLatitude() + currentLocation.getLongitude()) + ")");
                latLngStart = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                geoLocateStart();   //geo location starting point
            }
        });

        //Research method by clicking the autosuggested location (Start)
        mSearchStart.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideSoftKeyBoard();
                geoLocateStart();
            }
        });

        //Research method by typing the actual location (Start)
        mSearchStart.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    geoLocateStart();   //Execute method for searching

                return false;
            }
        });

        //Delete starting point content
        imgClearStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSearchStart.setText("");
            }
        });

        //Research method by clicking the autosuggested location (Destination)
        mSearchDestination.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideSoftKeyBoard();
                geoLocateDestination();
            }
        });

        //Research method by typing the actual location (Destination)
        mSearchDestination.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                    //Execute method for searching
                    geoLocateDestination();

                return false;
            }
        });

        //Delete destination content
        imgClearDestination.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSearchDestination.setText("");
            }
        });


        //----------------------

        //When the user presses the GPS icon, go back to the device location
        mGps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d (TAG, "onClick: Clicked GPS icon");
                getDeviceLocation();
            }
        });

        //When you click the icon, show the route selected
        mItinerary.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isRouteCreated)
                    zoomRoute(polyline.getPoints());

                else if (!isRouteCreated)
                    Toast.makeText(MapActivity.this, "Calculate directions before seeing the full itinerary", Toast.LENGTH_SHORT).show();
            }
        });

        //When you click the icon, reset all the values on the map
        mRefreshMap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Reset all parameters and variables
                mMap.clear();
                getDeviceLocation();
                mSearchStart.setText("");
                mSearchDestination.setText("");
                Toast.makeText(MapActivity.this, "Restart the journey", Toast.LENGTH_SHORT).show();

                //Reset variables
                bAlong.setEnabled(false);
                bNearby.setEnabled(false);

                isInterStopSelected = false;
                isSearchRouteSelected = false;
                isRouteSelected = false;
                isRouteCreated = false;
                isMarkerSelected = false;
            }
        });

        //When you click the compass icon, find directions
        mCompass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //If the user select a marker, the compass activity can be opened
                if (isMarkerSelected)
                {
                    //Create an intent to pass to the compass activity
                    Intent intent = new Intent(MapActivity.this, MyCompassActivity.class);
                    Bundle bundle = new Bundle();
                    LatLng latLngCurrent = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    //Find distance between current location and marker selected
                    distanceCompass = currentLocation.distanceTo(locationMarker);

                    //Retrieve the information to the compass activity
                    bundle.putParcelable("location", latLngMarker);   //Pass marker coordinates to the compass activity
                    bundle.putParcelable("origin", latLngCurrent);   //Pass current location coordinates to the compass activity
                    bundle.putFloat("distance", distanceCompass);

                    intent.putExtras(bundle);
                    startActivity(intent);   //Start the compass activity
                }

                //If the users haven't select a marke, the compass cannot be opened
                else if (!isMarkerSelected)
                {
                    //Retrieve error message
                    Toast.makeText(MapActivity.this, "Select a marker before using the compass!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //When you click the icon calculate the directions between the two points
        mDirections.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isSearchRouteSelected = true;
                isRouteSelected = true;
                calculateDirections(latLngStart, latLngDestination);
            }
        });

        //----------------------

        //Show restaurants when pressed the button
        bRestaurants.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.BLACK);
                tRestaurant.setTypeface(null, Typeface.BOLD);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "restaurant";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show hotels when pressed the button
        bHotels.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.BLACK);
                tHotel.setTypeface(null, Typeface.BOLD);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "hotel";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show ATMs when pressed the button
        bAtms.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.BLACK);
                tAtm.setTypeface(null, Typeface.BOLD);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "atm";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show touristic attractions when pressed the button
        bAttractions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.BLACK);
                tAttraction.setTypeface(null, Typeface.BOLD);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "attraction";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show shopping malls when pressed the button
        bShopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.BLACK);
                tShopping.setTypeface(null, Typeface.BOLD);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "shopping";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show petrol stations when pressed the button
        bPetrol.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.BLACK);
                tPetrol.setTypeface(null, Typeface.BOLD);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "petrol";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show hospitals when pressed the button
        bHospitals.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.BLACK);
                tHospital.setTypeface(null, Typeface.BOLD);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "hospital";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show bicycle shops when pressed the button
        bBike.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.BLACK);
                tBike.setTypeface(null, Typeface.BOLD);
                tCar.setTextColor(Color.parseColor("#989898"));
                tCar.setTypeface(null, Typeface.NORMAL);

                isInterStopSelected = true;
                selected = "bike";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //Show mechanics when pressed the button
        bCar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Highlight the selected button
                tRestaurant.setTextColor(Color.parseColor("#989898"));
                tRestaurant.setTypeface(null, Typeface.NORMAL);
                tHotel.setTextColor(Color.parseColor("#989898"));
                tHotel.setTypeface(null, Typeface.NORMAL);
                tAtm.setTextColor(Color.parseColor("#989898"));
                tAtm.setTypeface(null, Typeface.NORMAL);
                tAttraction.setTextColor(Color.parseColor("#989898"));
                tAttraction.setTypeface(null, Typeface.NORMAL);
                tShopping.setTextColor(Color.parseColor("#989898"));
                tShopping.setTypeface(null, Typeface.NORMAL);
                tPetrol.setTextColor(Color.parseColor("#989898"));
                tPetrol.setTypeface(null, Typeface.NORMAL);
                tHospital.setTextColor(Color.parseColor("#989898"));
                tHospital.setTypeface(null, Typeface.NORMAL);
                tBike.setTextColor(Color.parseColor("#989898"));
                tBike.setTypeface(null, Typeface.NORMAL);
                tCar.setTextColor(Color.BLACK);
                tCar.setTypeface(null, Typeface.BOLD);

                isInterStopSelected = true;
                selected = "car";

                bNearby.setEnabled(true);

                if(isRouteSelected)
                    bAlong.setEnabled(true);
            }
        });

        //----------------------

        //Show along route nearby stops
        bAlong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (selected)   //Select intermediate stops along the route
                {
                    case "restaurant":
                        place = "restaurant";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "hotel":
                        place = "lodging";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "atm":
                        place = "atm";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "attraction":
                        place = "tourist_attraction";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "shopping":
                        place = "shopping_mall";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "petrol":
                        place = "gas_station";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "hospital":
                        place = "doctor";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "bike":
                        place = "bicycle_store";
                        retrieveAlongRoutePlaces(place);
                        break;

                    case "car":
                        place = "car_repair";
                        retrieveAlongRoutePlaces(place);
                        break;
                }
            }
        });

        //Show nearby intermediate stops
        bNearby.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (selected)   //Select the intermediate stop around you
                {
                    //Get place type from Google
                    case "restaurant":
                        place = "restaurant";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "hotel":
                        place = "lodging";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "atm":
                        place = "atm";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "attraction":
                        place = "tourist_attraction";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "shopping":
                        place = "shopping_mall";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "petrol":
                        place = "gas_station";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "hospital":
                        place = "doctor";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "bike":
                        place = "bicycle_store";
                        retrieveIntermediatePlaces(place);
                        break;

                    case "car":
                        place = "car_repair";
                        retrieveIntermediatePlaces(place);
                        break;
                }
            }
        });

        //----------------------

        //Show marker when click on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                markerOptionStart = new MarkerOptions().position(latLng).title("Intermediate Stop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));   //Create a marker relative to the location searched
                mMap.addMarker(markerOptionStart);   //Add the marker to them map
            }
        });

        //Show markers along the polyline clicked
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
        {
            @Override
            public void onPolylineClick(Polyline polyline)
            {
                isRouteSelected = true;

                //Check if all the necessary button are selected in order to activate the button to calculate the inter stops along the route
                if(isSearchRouteSelected && isInterStopSelected)
                    bAlong.setEnabled(true);

                List<LatLng> polylinePoints = polyline.getPoints();   //Get the polyline points
                polylineSelectedPoints = polylinePoints;

                calculatePolyline(polyline);   //Retrieve routes available
            }
        });

        //When a marker has been clicked...
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                isMarkerSelected = true;   //Marker selected
                latLngMarker = marker.getPosition();   //Get location of the marker selected

                locationMarker = new Location("");   //Initialising Location viariable
                locationMarker.setLatitude(latLngMarker.latitude);   //Set coordinates to Location
                locationMarker.setLongitude(latLngMarker.longitude); //

                return false;
            }
        });

        hideSoftKeyBoard();   //Hide search keyboard
    }

    //METHODS---------------------

    //Calculate the formation relative to the routes available
    private void calculatePolyline (Polyline polyline)
    {
        int index = 0;   //Counter of the routes available

        for(PolylineData polylineData: mPolylineData)   //Check which polyline has been clicked and change color
        {
            index ++;   //Increase the index

            //when selecting a polyline
            if(polyline.getId().equals(polylineData.getPolyline().getId()))
            {
                polylineData.getPolyline().setColor(Color.BLUE);   //Change color of polyline selected
                polylineData.getPolyline().setZIndex(1);   //In case multiple routes are overlapping, paint the common route between the routes

                //Get latitude and longitude of the polyline destination
                LatLng endLocation = new LatLng(polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);

                //Add marker to the map
                Marker marker  = mMap.addMarker(markerOptionDestination.title("Route " + index).snippet("Trip: " + polylineData.getLeg().duration + ",  " + polylineData.getLeg().distance));   //Show the duration of the trip by clicking the route
                marker.showInfoWindow();   //Show duration and distance on info window
            }
            //Set color grey for unselected polylines
            else
            {
                polylineData.getPolyline().setColor(Color.GRAY);
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    //Retrieve the intermediate stop location around you
    private void retrieveIntermediatePlaces(String place)
    {
        mMap.clear();
        String url = getUrl1(place);   //Get the URL string with the place type
        executeDataTransfer(url);
    }

    //Retrieve the intermediate stop location along the route
    private void retrieveAlongRoutePlaces(String place)
    {
        //calculate inter stops in relation to the accuracy decided by the user
        switch (spinnerAccuracy.getSelectedItemPosition())
        {
            case 0:   //Lowest accuracy
                accuracy = 5;
                calculateIntermediatePoints(place, accuracy);
                break;

            case 1:
                accuracy = 4;
                calculateIntermediatePoints(place, accuracy);
                break;

            case 2:
                accuracy = 2;
                calculateIntermediatePoints(place, accuracy);
                break;

            case 3:   //Highest accuracy
                accuracy = 1;
                calculateIntermediatePoints(place, accuracy);
                break;

            default:
                break;
        }

        //Set customized info adapter
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
    }

    //Calculate the number of intermediate points in relation to the accuracy established
    private void calculateIntermediatePoints(String place, int accuracy)
    {
        for (int c = 0; c < polylineSelectedPoints.size(); c ++)
        {
            //Decrease or increase the number of intermediate points checked, in order change the speed of the research
            if (c % accuracy == 0)
            {
                interPointLatLng = polylineSelectedPoints.get(c);

                String url = getUrl2(place);
                executeDataTransfer(url);
            }
        }
    }

    //Execute the data transfer
    private void executeDataTransfer (String url)
    {
        Object [] DataTransfer = new Object[2];
        DataTransfer[0] = mMap;   //Add map to data transfer
        DataTransfer[1] = url;   //Add url to data transfer
        GetPlacesData getPlacesData = new GetPlacesData(this);
        getPlacesData.execute(DataTransfer);   //Get places around you by passing the data transfer created
    }

    //Find a starting point by typing on the first search bar
    private void geoLocateStart()
    {
        countLoc = 1;

        //Variables
        String searchString = mSearchStart.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);   //Geocoder is responsible to translate the location name to latitude and longitude coordinates
        List<Address> list = new ArrayList<>();

        if (countLoc == 3)
            mMap.clear();   //Clear the map when starting a new itinerary

        latLngStart = geoLocate(searchString, geocoder, list);
    }

    //Find a destination by typing on the second search bar
    private void geoLocateDestination()
    {
        countLoc = 2;

        String searchString = mSearchDestination.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        latLngDestination = geoLocate(searchString, geocoder, list);
    }

    //Geolocate function (Get latitude and longitude of location)
    private LatLng geoLocate(String searchString, Geocoder geocoder, List<Address> list)
    {
        //Initialising variable
        LatLng latitudeLongitude = new LatLng(0,0);

        //Get a list of addresses
        try
        {
            list = geocoder.getFromLocationName (searchString, 1);   //Look for only 1 result of addresses
        }
        catch (IOException e)
        {
            Toast.makeText(MapActivity.this, "Exception: Extraction of coordinates failed", Toast.LENGTH_SHORT).show();
        }

        if (list.size() > 0)   //If there are some result within the addresses
        {
            Address address = list.get(0);
            latitudeLongitude = new LatLng(address.getLatitude(), address.getLongitude());
            moveCamera(latitudeLongitude, DEFAULT_ZOOM, address.getAddressLine(0), countLoc);   //Move the camera and zoom it to the location researched
        }

        return latitudeLongitude;
    }

    //Get device location
    private void getDeviceLocation()
    {
        //Get current location
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try
        {
            //Get location of the user permitted
            if(mLocationPermissionGranted)
            {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener()
                {
                    //When the location has been completed
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())   //If location found...
                        {
                            currentLocation = (Location) task.getResult();  //Get location result in order to move camera
                            //Move the camera to the current location
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                        else
                            //Error message
                            Toast.makeText(MapActivity.this, "Unable to get the current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        catch (SecurityException e)
        {
        }
    }

    //Move the camera to the current location
    private void moveCamera (LatLng latLng, float zoom, String title, int countL)
    {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));   //Create an animation effect when the camera change position
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));   //Apply the customized infowindow

        //If the latLng send to the method is not relative to the current location, drop a marker (Remove marker on the current GPS location)
        if(!title.equals("My Location"))
        {
            if (countL == 1)   //Place a blue marker for the starting point
            {
                //Create a marker relative to the origin searched
                markerOptionStart = new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(markerOptionStart);   //Add the marker to them map
            }

            else if (countL == 2)   //Place a green marker for the  destination point
            {
                //Create a marker relative to the destination searched
                markerOptionDestination = new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker(markerOptionDestination);   //Add the marker to the map

                countLoc = 3;
            }
        }

        hideSoftKeyBoard();   //Hide the search keyboard
    }

    //Initialise the map
    private void initMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);   //Creates a map fragment
        mapFragment.getMapAsync(MapActivity.this);

        if(mGeoApiContext == null)
            //Get map by using Google Maps API
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_API_key)).build();   //Set the API key to authorize the requests
    }

    //Calculate directions between two points of the map
    private void calculateDirections(LatLng ltlnStart, LatLng ltlnDestination)
    {
        isRouteSelected = true;   //Route has been selected

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(ltlnDestination.latitude, ltlnDestination.longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);   //Show all the possible routes
        directions.origin(new com.google.maps.model.LatLng(ltlnStart.latitude, ltlnStart.longitude));

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>()
        {
            @Override
            public void onResult(DirectionsResult result)
            {
                addPolylinesToMap(result);   //Pass the result of the route to the method for the design of the route
            }

            @Override
            public void onFailure(Throwable e)
            {
                Toast.makeText(MapActivity.this, "Failed to calculate routes between the two points!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Draw the route to the map
    private void addPolylinesToMap(final DirectionsResult result)
    {
        isRouteCreated = true;
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                double duration = 999999999;   //Default maximum value for the trip duration

                if(mPolylineData.size() > 0)   //If this method has been previously called, don't add duplicate routes
                {
                    for(PolylineData polylineData: mPolylineData)
                        polylineData.getPolyline().remove();   //Remove the extra polyline from the map


                    //Reset the polyline array list
                    mPolylineData.clear();
                    mPolylineData = new ArrayList<>();
                }

                //Generate progressively every point of the polyline
                for(DirectionsRoute route: result.routes)
                {
                    //Get list of points along the route
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();

                    //Check every point of the polyline selected
                    for(com.google.maps.model.LatLng latLng: decodedPath)
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));

                    polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(Color.GRAY);
                    polyline.setClickable(true);
                    mPolylineData.add(new PolylineData(polyline, route.legs[0]));

                    //Get the duration of the trip
                    double tempDuration = route.legs[0].duration.inSeconds;

                    //Automatically highlight the fastest route
                    if(tempDuration < duration)
                    {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());   //Zoom the map in order to show the complete route
                    }
                }
            }
        });
    }

    //Check the permission to use Device Location
    private void getLocationPermission()
    {
        String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};   //ACCESS_FINE_LOCATION: Accurate GPS location
                                                                                                                          //ACCESS_COARSE_LOCATION: Less accurate location using WiFi or Mobile Data.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)   //Check if the device has been granted a particular permission (In this case FINE_LOCATION)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)   //Check if the device has been granted a particular permission (In this case COARSE_LOCATION)
            {
                mLocationPermissionGranted = true;
                initMap();   //If permission has been granted, initialise the map
            }

            else   //Else request permission to the user
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

        else
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    //Zoom the camera in order to show the complete route from start to destination
    public void zoomRoute(List<LatLng> lstLatLngRoute)
    {
        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty())
            return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 250;   //Zoom
        LatLngBounds latLngBounds = boundsBuilder.build();

        //Move camera to the location with the zoom value predefined
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding), 600, null);
    }

    //getUrl function used to find nearby places AROUND your location
    private String getUrl1(String nearbyPlace)
    {
        //Assign a radius value in relation to the spinners item selected
        switch (spinnerRadius.getSelectedItemPosition())
        {
            case 0:
                proxRadius = 100;
                break;

            case 1:
                proxRadius = 200;
                break;

            case 2:
                proxRadius = 500;
                break;

            case 3:
                proxRadius = 1000;
                break;

            case 4:
                proxRadius = 3000;
                break;

            case 5:
                proxRadius = 5000;
                break;

            default:
                break;
        }

        //Generate the URL from where to extract the places information
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude());   //Location
        googlePlacesUrl.append("&radius=" + proxRadius);   //Radius
        googlePlacesUrl.append("&type=" + nearbyPlace);   //Nearby place (Restaurant, hotel, doctor...)
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBV1-aIaajlChV0XbgWJaPOQ3KIQfM5T-g");   //Append API key to the url

        return (googlePlacesUrl.toString());   //Return the url string
    }

    //getUrl function used to find nearby places ALONG  a route
    private String getUrl2 (String nearbyPlace)
    {
        //Assign a radius value in relation to the spinners item selected
        switch (spinnerRadius.getSelectedItemPosition())
        {
            case 0:
                proxRadius = 100;
                break;

            case 1:
                proxRadius = 200;
                break;

            case 2:
                proxRadius = 500;
                break;

            case 3:
                proxRadius = 1000;
                break;

            case 4:
                proxRadius = 3000;
                break;

            case 5:
                proxRadius = 5000;
                break;

            default:
                break;
        }

        //Generate the URL from where to extract the places information
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + interPointLatLng.latitude + "," + interPointLatLng.longitude);   //Append location to the url
        googlePlacesUrl.append("&radius=" + proxRadius);   //Append radius to the url
        googlePlacesUrl.append("&type=" + nearbyPlace);   //Append point of interest type
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBV1-aIaajlChV0XbgWJaPOQ3KIQfM5T-g");   //Append API key to the url

        return (googlePlacesUrl.toString());
    }

    //Hide the keyboard after searching a location
    private void hideSoftKeyBoard()
    {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = MapActivity.this.getCurrentFocus();
        MapActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
