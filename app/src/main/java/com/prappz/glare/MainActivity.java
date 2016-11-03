package com.prappz.glare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PreferenceManager.getInstance(this).getBoolean(AppConstants.PARSE_LOGGED_IN))
            getSupportFragmentManager().beginTransaction().addToBackStack("stack").add(R.id.frame, new HomeFragment()).commit();
        else
            getSupportFragmentManager().beginTransaction().add(R.id.frame, new LoginFragment()).commit();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (!checkForPermission())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 49);
        else
            mGoogleApiClient.connect();

    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    //Check for location permission
    private boolean checkForPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("CARD", "Permission is granted");
                return true;
            } else
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("CARD ", "Permission: " + permissions[0] + "was " + grantResults[0]);
            if (requestCode == 49)
                mGoogleApiClient.connect();
        } else {
            if (requestCode == 49)
                Toast.makeText(this, "Kindly give ACCESS_FINE_LOCATION permission to the app for selecting location", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i("RESP",String.valueOf(mLastLocation.getLatitude()));
            Log.i("RESP",String.valueOf(mLastLocation.getLongitude()));
            PreferenceManager.getInstance(this).put(AppConstants.USER_LAT,String.valueOf(mLastLocation.getLatitude()));
            PreferenceManager.getInstance(this).put(AppConstants.USER_LON,String.valueOf(mLastLocation.getLongitude()));
        } else
            Log.i("RESP","NULL");


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount()!=1)
            getSupportFragmentManager().popBackStack();
        else {
            this.finish();
        }
    }
}