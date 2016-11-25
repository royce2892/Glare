package com.prappz.glare.common;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.prappz.glare.admin.AdminHomeFragment;
import com.prappz.glare.driver.DriverHomeFragment;
import com.prappz.glare.user.UserHomeFragment;
import com.prappz.glare.R;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectLocation();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("FROMNOTIF", false)) {
                if (bundle.getInt("type") == 0) {
                    DriverHomeFragment driverHomeFragment = new DriverHomeFragment();
                    driverHomeFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().addToBackStack("stack").add(R.id.frame, driverHomeFragment).commit();

                }
                return;
            }
        }
        if (!PreferenceManager.getInstance(this).getBoolean(AppConstants.TYPE_CHOSEN))
            getSupportFragmentManager().beginTransaction().add(R.id.frame, new TypeChooseFragment()).commit();
        else if (!PreferenceManager.getInstance(this).getBoolean(AppConstants.PHONE_LOGGED_IN))
            getSupportFragmentManager().beginTransaction().add(R.id.frame, new LoginFragment()).commit();
        else {
            switch (PreferenceManager.getInstance(this).getInt(AppConstants.TYPE)) {
                case AppConstants.MODE_USER:
                    getSupportFragmentManager().beginTransaction().addToBackStack("stack").add(R.id.frame, new UserHomeFragment()).commit();
                    connectLocation();
                    break;

                case AppConstants.MODE_ADMIN:
                    getSupportFragmentManager().beginTransaction().addToBackStack("stack").add(R.id.frame, new AdminHomeFragment()).commit();
                    break;

                case AppConstants.MODE_DRIVER:
                    getSupportFragmentManager().beginTransaction().addToBackStack("stack").add(R.id.frame, new DriverHomeFragment()).commit();
                    break;
            }

        }

    }

    private void connectLocation() {
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
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        BusProvider.getInstance().register(this);
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        BusProvider.getInstance().unregister(this);
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
            else if (requestCode == 50)
                BusProvider.getInstance().post(new PermissionGrantedEvent());
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
            Log.i("RESP", String.valueOf(mLastLocation.getLatitude()));
            Log.i("RESP", String.valueOf(mLastLocation.getLongitude()));
            PreferenceManager.getInstance(this).put(AppConstants.USER_LAT, String.valueOf(mLastLocation.getLatitude()));
            PreferenceManager.getInstance(this).put(AppConstants.USER_LON, String.valueOf(mLastLocation.getLongitude()));
            ParseGeoPoint location = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            ParseUser.getCurrentUser().put("location", location);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        Log.i("RESP", "location of driver saved");
                    else
                        Log.i("RESP", e.getLocalizedMessage());
                }
            });

        } else {
            Log.i("RESP", "NULL LOCATION ON CONNCETED");
            displayLocationSettingsRequest();
        }

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
        if (getSupportFragmentManager().getBackStackEntryCount() != 1)
            getSupportFragmentManager().popBackStack();
        else {
            this.finish();
        }
    }

    private void displayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("RESP", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("RESP", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(MainActivity.this, 24);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("RESP", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("RESP", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        takeToSettings();
                        break;
                }
            }
        });
    }

    private void takeToSettings() {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(callGPSSettingIntent, 24);
    }
}
