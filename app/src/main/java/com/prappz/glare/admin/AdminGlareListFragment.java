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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.prappz.glare.R;
import com.prappz.glare.common.AppConstants;

import java.util.List;

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
            String gender, type;
            if(glare.getInt("gender") == 0){
                gender = "Male";
            }else if(glare.getInt("gender") == 1){
                gender = "Female";
            }else if(glare.getInt("gender") == 2){
                gender = "Transgender";
            }else{
                gender = "NA";
            }

            if (glare.getInt("type") == AppConstants.GLARE_AMBULANCE)
                type = "Ambulance Service";
            else if (glare.getInt("type") == AppConstants.GLARE_FIRE)
                type = "Firefighting Service";
            else if (glare.getInt("type") == AppConstants.GLARE_POLICE)
                type = "Police Assistance";
            else
                type = "Service";


            holder.name.setText(glare.getString("name")+" requested "+type);
            holder.type.setText("Details: "+glare.getString("info")+"\nAge: "+glare.getString("age")+" Gender: "+gender);
            //   holder.phone.setText(glare.getString("phone"));

            if (glare.getParseFile("image") != null)
                Glide.with(getContext()).load(glare.getParseFile("image").getUrl()).thumbnail(0.1f).into(holder.image);
        }

        @Override
        public int getItemCount() {
            return glares.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            int position;
              ImageView image; //driver;
            TextView name, type;
            Button btnChangeStatus;

            private Holder(View itemView) {
                super(itemView);
               image = (ImageView) itemView.findViewById(R.id.glare_image);
                name = (TextView) itemView.findViewById(R.id.glare_name);
                type = (TextView) itemView.findViewById(R.id.glare_type);


                // status = (TextView) itemView.findViewById(R.id.glare_status);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        skipToNearbyDriversFragment(glares.get(position));
                    }
                });

            }
        }
    }

    private void skipToNearbyDriversFragment(ParseObject glare) {

        GlareReportDetailsFragment fragment = new GlareReportDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", glare.getParseGeoPoint("location").getLatitude());
        bundle.putDouble("lon", glare.getParseGeoPoint("location").getLongitude());
        bundle.putString("id", glare.getObjectId());
        bundle.putInt("status",status);
        bundle.putString("image",glare.getParseFile("image").getUrl());
        bundle.putInt("type",glare.getInt("type"));
        bundle.putString("info",glare.getString("info"));
        bundle.putInt("gender",glare.getInt("gender"));
        bundle.putString("name",glare.getString("name"));
        bundle.putString("age",glare.getString("age"));

        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("stack").replace(R.id.frame, fragment).commit();
    }
}
