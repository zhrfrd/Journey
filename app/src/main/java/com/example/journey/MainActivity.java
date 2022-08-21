package com.example.journey;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If the Google Play Services are ok you can go forward with the app
        if(isServicesOK())
            init();   //Go to the init() function in order to start the MapActivity
    }

    private void init()
    {
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener()
        {   //Set click listener to the button
            @Override
            public void onClick(View v)
            {
                //Create an intent between the actual activity and the MapActivity class
                Intent intent = new Intent (MainActivity.this, MapActivity.class);
                startActivity(intent);   //Start the MapActivity
            }
        });
    }

    //Check if the right version of Google Play Services is installed in the users mobile phone
    public boolean isServicesOK()
    {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);   //Check Google Play Services correct version. (Returns: SUCCESS, SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID)

        if(available == ConnectionResult.SUCCESS)
            //If the Google Play version is correct return true
            return true;

        else
            Toast.makeText(this, "Check Google Play Services version!", Toast.LENGTH_SHORT).show();

        return false;
    }
}
