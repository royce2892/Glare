package com.prappz.glare.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.prappz.glare.R;
import com.prappz.glare.common.AppConstants;
import com.prappz.glare.common.BusProvider;
import com.prappz.glare.common.PermissionGrantedEvent;
import com.prappz.glare.common.PreferenceManager;
import com.squareup.otto.Subscribe;

/**
 * Created by Royce RB on 3/11/16.
 */

public class DriverHomeFragment extends Fragment implements View.OnClickListener {

    private String id;
    private TextView name, desc;
    private RelativeLayout driverLayout;
    private ImageView image;
    private ParseObject glareObj, ambRequest;
    private ProgressBar progressBar;

    public DriverHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_driver_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverLayout = (RelativeLayout) view.findViewById(R.id.driver_home);
        name = (TextView) view.findViewById(R.id.glare_name);
        desc = (TextView) view.findViewById(R.id.glare_text);
        image = (ImageView) view.findViewById(R.id.glare_img);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);

        if (getArguments() != null) {
            if (getArguments().getBoolean("FROMNOTIF", false)) {
                id = getArguments().getString("id");
                if (id != null) {
                    getRequestData();
                    PreferenceManager.getInstance(getContext()).put(AppConstants.AMB_REQUEST_ID, id);
                } else {
                    if (!PreferenceManager.getInstance(getContext()).getString(AppConstants.AMB_REQUEST_ID).contentEquals("")) {
                        id = PreferenceManager.getInstance(getContext()).getString(AppConstants.AMB_REQUEST_ID);
                        getRequestData();
                    }
                }

            }
        } else {
            id = "BABo73Ann1";
            getRequestData();
        }

        view.findViewById(R.id.accept).setOnClickListener(this);
        view.findViewById(R.id.reject).setOnClickListener(this);
        view.findViewById(R.id.call_user).setOnClickListener(this);

    }

    private void getRequestData() {

        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AmbulanceRequest");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null && object != null)
                    setData(object);
            }
        });
    }

    private void setData(ParseObject amb) {
        ambRequest = amb;
        Toast.makeText(getContext(), amb.getObjectId(), Toast.LENGTH_SHORT).show();
        driverLayout.setVisibility(View.VISIBLE);

        final ParseQuery<ParseObject> glare = ParseQuery.getQuery("Glare");
        glare.getInBackground(amb.getString("glareId"), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    glareObj = object;
                    if (object.getString("name") != null)
                        name.setText("Reported by ".concat(object.getString("name")).toUpperCase());
                    if (object.getString("info") != null)
                        desc.setText(object.getString("info"));
                    if (object.getParseFile("image") != null)
                        Glide.with(getContext()).load(object.getParseFile("image").getUrl()).thumbnail(0.1f).into(image);
                    else
                        image.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.accept)
            saveAmbRequestAccept(true);
        else if (view.getId() == R.id.reject)
            saveAmbRequestAccept(false);
        else if (view.getId() == R.id.call_user)
            callUser();
    }

    private void callUser() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            askCallsPermission();
            return;
        }
        Uri number = Uri.parse("tel:" + glareObj.getString("phone"));

        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    private void askCallsPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 50);

    }

    private void saveAmbRequestAccept(final boolean accept) {
        progressBar.setVisibility(View.VISIBLE);
        if (ambRequest != null) {
            ambRequest.put("hasAccepted", accept);
            ambRequest.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    progressBar.setVisibility(View.GONE);
                    if (e == null) {
                        updateGlareObject(accept);
                        Log.i("RESP", "ambulance updated");
                    } else
                        Log.i("RESP", "save amb request " + e.getLocalizedMessage());
                }
            });
        }
    }

    private void updateGlareObject(final boolean accept) {
        progressBar.setVisibility(View.VISIBLE);
        if (accept)
            glareObj.put("status", AppConstants.STATUS_COMPLETED);
        else
            glareObj.put("status", AppConstants.STATUS_PENDING);
        glareObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null) {
                    Log.i("RESP", "glare updated");
                    if (accept)
                        startMaps();
                } else
                    Log.i("RESP", "glare updated error " + e.getLocalizedMessage());

            }
        });
    }

    private void startMaps() {
        /*startActivity(new Intent(getContext(), MapsActivity.class).
                putExtra("lat", glareObj.getParseGeoPoint("location").getLatitude()).
                putExtra("lon", glareObj.getParseGeoPoint("location").getLongitude()));*/
        final Intent intent = new
                Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                "saddr=" + Double.valueOf(PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_LAT)) + "," +
                Double.valueOf(PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_LON)) +
                "&daddr=" + glareObj.getParseGeoPoint("location").getLatitude() + "," +
                glareObj.getParseGeoPoint("location").getLongitude()));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }


    public void onStart() {
        BusProvider.getInstance().register(this);
        super.onStart();
    }

    public void onStop() {
        BusProvider.getInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void getPerm(PermissionGrantedEvent permissionGrantedEvent) {
        callUser();
    }

}
