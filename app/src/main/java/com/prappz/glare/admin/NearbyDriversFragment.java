package com.prappz.glare.admin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.prappz.glare.R;

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
        list.setLayoutManager(new LinearLayoutManager(getContext()));

        victim = new ParseGeoPoint(getArguments().getDouble("lat"), getArguments().getDouble("lon"));
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNear("location", victim);
        query.whereStartsWith("username", "d");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() != 0)
                    setAdapter(objects);
            }
        });


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
            holder.phone.setText(user.getString("phone"));
            String dist = String.valueOf(user.getParseGeoPoint("location").distanceInKilometersTo(victim));
            holder.distance.setText(dist.substring(0, dist.indexOf(".")) + " kms away");

        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            int position;
            TextView name, phone, distance;

            private Holder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.driver_name);
                phone = (TextView) itemView.findViewById(R.id.driver_number);
                distance = (TextView) itemView.findViewById(R.id.driver_distance);

                itemView.findViewById(R.id.driver_assign).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("RESP", "LOCATION " + users.get(position).getParseGeoPoint("location").toString());

                        sendDriverPush(users.get(position));
                    }
                });

            }
        }
    }

    private void sendDriverPush(ParseUser user) {
        ParsePush parsePush = new ParsePush();
        parsePush.setChannel(user.getUsername().replace("+",""));
        JSONObject json = new JSONObject();
        try {
            json.put("from", "Royce Raju");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        parsePush.setData(json);
        parsePush.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    Log.i("RESP", "push");
                else
                    Log.i("RESP", e.getLocalizedMessage());
            }
        });
    }
}
