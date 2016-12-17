package com.prappz.glare.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.prappz.glare.R;
import com.prappz.glare.common.AppConstants;
import com.prappz.glare.common.BusProvider;
import com.prappz.glare.common.PermissionGrantedEvent;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * Created by Royce RB on 4/11/16.
 */

public class GlareReportDetailsFragment extends Fragment {

    private RecyclerView list;
    private List<ParseUser> users;
    private ParseGeoPoint victim;
    private String phone,id;
    private ProgressBar progressBar;
    private int status = 0;

    private LinearLayout btnChangeStatus, btnLocation, btnCall;
    private TextView glareReportTitle, glareReportDetails, reportStatus, glareChangeStatustText;
    private ImageView reportImage, glareChangeStatusImage;

    public GlareReportDetailsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_glare_reports_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = (RecyclerView) view.findViewById(R.id.list);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);
        progressBar.setVisibility(View.VISIBLE);
        btnChangeStatus = (LinearLayout) view.findViewById(R.id.g_change_status);
        btnCall = (LinearLayout) view.findViewById(R.id.g_call_reporter);
        btnLocation = (LinearLayout) view.findViewById(R.id.g_locate_victim);

        glareReportDetails = (TextView) view.findViewById(R.id.glare_report_detail);
        glareReportTitle = (TextView) view.findViewById(R.id.glare_report_title);
        reportStatus = (TextView) view.findViewById(R.id.report_status);
        glareChangeStatustText = (TextView) view.findViewById(R.id.glare_change_status_text);

        reportImage = (ImageView) view.findViewById(R.id.glare_image);
        glareChangeStatusImage = (ImageView) view.findViewById(R.id.glare_change_status_image);

        list.setLayoutManager(new LinearLayoutManager(getContext()));

        victim = new ParseGeoPoint(getArguments().getDouble("lat"), getArguments().getDouble("lon"));
        status = getArguments().getInt("status");
        id = getArguments().getString("status");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNear("location", victim);
        query.whereStartsWith("username", "d");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null && objects.size() != 0)
                    setAdapter(objects);
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        if (status == 0) {
            glareChangeStatustText.setText("Close");
            glareChangeStatusImage.setImageResource(R.drawable.ic_close_24dp);
            reportStatus.setText("Status : Waiting to Assign Driver");
            reportStatus.setTextColor(Color.RED);
        }
        else if (status == 1) {
            glareChangeStatustText.setText("Mark Done");
            reportStatus.setText("Status : Driver Assigned");
            glareChangeStatusImage.setImageResource(R.drawable.ic_check_24dp);
            reportStatus.setTextColor(Color.GREEN);
        }
        else if (status == 2) {
            glareChangeStatustText.setText("Reopen");
            reportStatus.setText("Status : Completed");
            reportStatus.setTextColor(Color.CYAN);
            glareChangeStatusImage.setImageResource(R.drawable.ic_reopen_24dp);
        }

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == 0){
                    getobject(2);
                }else if(status ==1){
                    getobject(2);
                }else if(status ==2){
                    getobject(0);
                }
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        String gender, type;
        if(getArguments().getInt("gender") == 0){
            gender = "Male";
        }else if(getArguments().getInt("gender") == 1){
            gender = "Female";
        }else if(getArguments().getInt("gender") == 2){
            gender = "Transgender";
        }else{
            gender = "NA";
        }

        if (getArguments().getInt("type") == AppConstants.GLARE_AMBULANCE)
            type = "Ambulance Service";
        else if (getArguments().getInt("type") == AppConstants.GLARE_FIRE)
            type = "Firefighting Service";
        else if (getArguments().getInt("type") == AppConstants.GLARE_POLICE)
            type = "Police Assistance";
        else
            type = "Service";


        glareReportTitle.setText(getArguments().getString("name")+" requested "+type);
        glareReportDetails.setText("Details: "+getArguments().getString("info")+"\nAge: "+getArguments().getString("age")+" Gender: "+gender);
        //   holder.phone.setText(glare.getString("phone"));

        if (getArguments().getString("image") != null)
            Glide.with(getContext()).load(getArguments().getString("image")).thumbnail(0.1f).into(reportImage);


        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });
    }

    private void openMap() {
        final ParseGeoPoint[] geoPoint = new ParseGeoPoint[1];
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Glare");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e==null&&object!=null)
                    geoPoint[0] = object.getParseGeoPoint("location");
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", geoPoint[0].getLatitude(), geoPoint[0].getLongitude());
                    Uri gmmIntentUri = Uri.parse(uri);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(mapIntent);
            }
        });
    }
    private void getobject(final int status) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Glare");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e==null&&object!=null)
                    changeStatus(object,status);
            }
        });
    }

    private void changeStatus(ParseObject parseObject, int STATUS) {
        progressBar.setVisibility(View.VISIBLE);
        parseObject.put("status", STATUS);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("RESP", "JOb COmpelted");
                } else {
                    Log.i("RESP", "Job not marked completed due to " + e.getLocalizedMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void callUser(String phone) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            askCallsPermission();
            return;
        }
        Uri number;
        if (phone != null) {
            number = Uri.parse("tel:" + phone);
            this.phone = phone;
        } else
            number = Uri.parse("tel:" + this.phone);

        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    private void askCallsPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 50);

    }

    private void setAdapter(List<ParseUser> users) {
        this.users = users;
        list.setAdapter(new DriversAdapter());
    }

    private class DriversAdapter extends RecyclerView.Adapter<DriversAdapter.Holder> {

        private DriversAdapter() {
        }

        @Override
        public DriversAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DriversAdapter.Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_nearby_driver, parent, false));
        }

        @Override
        public void onBindViewHolder(GlareReportDetailsFragment.DriversAdapter.Holder holder, int position) {
            holder.position = position;
            ParseUser user = users.get(position);
            holder.name.setText(user.getString("name"));
            String dist = String.valueOf(user.getParseGeoPoint("location").distanceInKilometersTo(victim));
            holder.distance.setText(dist.substring(0, dist.indexOf(".")) + " kms away");

        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            int position;
            TextView name, distance;
            ImageView phone;

            private Holder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.driver_name);
                phone = (ImageView) itemView.findViewById(R.id.driver_number);
                distance = (TextView) itemView.findViewById(R.id.driver_distance);

                itemView.findViewById(R.id.driver_assign).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("RESP", "LOCATION " + users.get(position).getParseGeoPoint("location").toString());
                        progressBar.setVisibility(View.VISIBLE);
                        createAmbulanceRequest(users.get(position));
                    }
                });

                itemView.findViewById(R.id.driver_number).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("RESP", "CALL " + users.get(position).getString("phone"));
                        callUser(users.get(position).getString("phone"));
                    }
                });

            }
        }
    }

    private void createAmbulanceRequest(final ParseUser parseUser) {
        final ParseObject ambrequest = ParseObject.create("AmbulanceRequest");
        ambrequest.put("location", victim);
        ambrequest.put("glareId", getArguments().getString("id"));
        ambrequest.put("hasAccepted", false);
        ambrequest.put("driver", parseUser);
        ambrequest.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    sendDriverPush(parseUser, ambrequest.getObjectId());
                    updateGlareObject(getArguments().getString("id"));
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Request Failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateGlareObject(String id) {
        ParseQuery<ParseObject> glareId = ParseQuery.getQuery("Glare");
        glareId.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    object.put("status", AppConstants.STATUS_ASSIGNED);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                                Log.i("RESP", "Glare status changed");
                            else
                                Log.i("RESP", "Glare status changed failed " + e.getLocalizedMessage());

                        }
                    });
                }
            }
        });
    }

    private void sendDriverPush(ParseUser user, String ambRequestId) {
        ParsePush parsePush = new ParsePush();
        parsePush.setChannel(user.getUsername().replace("+", ""));
        JSONObject json = new JSONObject();
        try {
            //o for ambulance request
            json.put("type", 0);
            json.put("id", ambRequestId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        parsePush.setData(json);
        parsePush.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null)
                    Toast.makeText(getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "Request Failed due to " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        callUser(null);
    }

}
