package com.prappz.glare.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.prappz.glare.R;
import com.prappz.glare.common.AppConstants;

import java.util.List;
import java.util.Locale;

/**
 * Created by Royce RB on 3/11/16.
 */

public class AdminGlareListFragment extends Fragment {

    private RecyclerView mGlareList;
    private List<ParseObject> glares;
    private int status = 0;
    private ProgressBar progressBar;

    public AdminGlareListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_glare_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        status = getArguments().getInt("status");
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);
        mGlareList = (RecyclerView) view.findViewById(R.id.glare_list);
        mGlareList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (glares == null || glares.size() == 0)
            getData();
        else
            setAdapter();
    }

    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Glare");
        query.whereEqualTo("status", getArguments().getInt("status"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() != 0) {
                    if (glares != null && glares.size() != 0)
                        glares.clear();
                    glares = objects;
                    setAdapter();
                } else
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setAdapter() {
        progressBar.setVisibility(View.GONE);
        GlareAdapter glareAdapter = new GlareAdapter();
        mGlareList.setAdapter(glareAdapter);
    }

    private class GlareAdapter extends RecyclerView.Adapter<GlareAdapter.Holder> {

        private GlareAdapter() {
        }

        @Override
        public GlareAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_glare, parent, false));
        }

        @Override
        public void onBindViewHolder(GlareAdapter.Holder holder, int position) {
            holder.position = position;
            ParseObject glare = glares.get(position);
            holder.name.setText(glare.getString("name"));
            //   holder.phone.setText(glare.getString("phone"));
            if (glare.getInt("type") == AppConstants.GLARE_AMBULANCE)
                holder.type.setText("Ambulance");
            else if (glare.getInt("type") == AppConstants.GLARE_FIRE)
                holder.type.setText("Fire");
            else if (glare.getInt("type") == AppConstants.GLARE_POLICE)
                holder.type.setText("Police");
            if (glare.getParseFile("image") != null)
                Glide.with(getContext()).load(glare.getParseFile("image").getUrl()).thumbnail(0.1f).into(holder.image);
            if (status == 0)
                holder.driver.setText("View Nearby Drivers");
            else if (status == 1)
                holder.driver.setText("Mark completed");
            else if (status == 2)
                holder.driver.setText("Reopen Glare");

        }

        @Override
        public int getItemCount() {
            return glares.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            int position;
            ImageView image;
            TextView name, type;
            Button driver;

            private Holder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.glare_image);
                name = (TextView) itemView.findViewById(R.id.glare_name);
                type = (TextView) itemView.findViewById(R.id.glare_type);
                driver = (Button) itemView.findViewById(R.id.glare_nearby_drivers);
                // status = (TextView) itemView.findViewById(R.id.glare_status);

                itemView.findViewById(R.id.glare_location).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("RESP", "LOCATION " + glares.get(position).getParseGeoPoint("location").toString());
                        ParseGeoPoint geoPoint = glares.get(position).getParseGeoPoint("location");
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", geoPoint.getLatitude(), geoPoint.getLongitude());
                        Uri gmmIntentUri = Uri.parse(uri);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        startActivity(mapIntent);

                    }
                });

                if (status == 0)
                    driver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i("RESP", "NEARBY " + glares.get(position).getParseGeoPoint("location").toString());
                            skipToNearbyDriversFragment(glares.get(position));
                        }
                    });
                else if (status == 1)
                    driver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            changeStatus(glares.get(position), AppConstants.STATUS_COMPLETED);
                        }
                    });
                else if (status == 2)
                    driver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            changeStatus(glares.get(position), AppConstants.STATUS_PENDING);
                        }
                    });

            }
        }
    }

    private void changeStatus(ParseObject parseObject, int STATUS) {
        progressBar.setVisibility(View.VISIBLE);
        parseObject.put("status", STATUS);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("RESP", "JOb COmpelted");
                    getData();
                } else {
                    Log.i("RESP", "Job not marked completed due to " + e.getLocalizedMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void skipToNearbyDriversFragment(ParseObject glare) {

        NearbyDriversFragment fragment = new NearbyDriversFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", glare.getParseGeoPoint("location").getLatitude());
        bundle.putDouble("lon", glare.getParseGeoPoint("location").getLongitude());
        bundle.putString("id", glare.getObjectId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("stack").replace(R.id.frame, fragment).commit();
    }
}
