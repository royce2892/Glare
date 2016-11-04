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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.prappz.glare.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Royce RB on 3/11/16.
 */

public class AdminGlareListFragment extends Fragment {

    private RecyclerView mGlareList;
    private List<ParseObject> glares;
    private int status = 0;

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
        mGlareList = (RecyclerView) view.findViewById(R.id.glare_list);
        mGlareList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (glares == null || glares.size() == 0) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Glare");
            query.whereEqualTo("status", getArguments().getInt("status"));
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects.size() != 0) {
                        glares = objects;
                        setAdapter();
                    }
                }
            });
        } else
            setAdapter();
    }

    private void setAdapter() {
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
            holder.phone.setText(glare.getString("phone"));
            if (glare.getString("info") != null)
                holder.info.setText(glare.getString("info"));
            if (glare.getParseFile("image") != null)
                Glide.with(getContext()).load(glare.getParseFile("image").getUrl()).thumbnail(0.1f).into(holder.image);

        }

        @Override
        public int getItemCount() {
            return glares.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            int position;
            ImageView image;
            TextView name, phone, info, status;

            private Holder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.glare_image);
                name = (TextView) itemView.findViewById(R.id.glare_name);
                phone = (TextView) itemView.findViewById(R.id.glare_number);
                info = (TextView) itemView.findViewById(R.id.glare_info);
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

                itemView.findViewById(R.id.glare_nearby_drivers).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("RESP", "NEARBY " + glares.get(position).getParseGeoPoint("location").toString());
                        skipToNearbyDriversFragment(glares.get(position));
                    }
                });


            }
        }
    }

    private void skipToNearbyDriversFragment(ParseObject glare) {

        NearbyDriversFragment fragment = new NearbyDriversFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", glare.getParseGeoPoint("location").getLatitude());
        bundle.putDouble("lon", glare.getParseGeoPoint("location").getLongitude());
        bundle.putString("id",glare.getObjectId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("stack").replace(R.id.frame, fragment).commit();
    }
}
