package com.prappz.glare.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
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

/**
 * Created by Royce RB on 4/11/16.
 */

public class NearbyDriversFragment extends Fragment {

    private RecyclerView list;
    private List<ParseUser> users;
    private ParseGeoPoint victim;
    private String phone;
    private ProgressBar progressBar;

    public NearbyDriversFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_nearby_drivers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = (RecyclerView) view.findViewById(R.id.list);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);
        progressBar.setVisibility(View.VISIBLE);
        list.setLayoutManager(new LinearLayoutManager(getContext()));

        victim = new ParseGeoPoint(getArguments().getDouble("lat"), getArguments().getDouble("lon"));
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
        public void onBindViewHolder(NearbyDriversFragment.DriversAdapter.Holder holder, int position) {
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
