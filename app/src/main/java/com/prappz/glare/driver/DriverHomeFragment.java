package com.prappz.glare.driver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.prappz.glare.common.PreferenceManager;

/**
 * Created by Royce RB on 3/11/16.
 */

public class DriverHomeFragment extends Fragment implements View.OnClickListener {

    String id;
    TextView callAdmin, callUser, name, desc;
    Button accept, reject;
    RelativeLayout driverLayout;
    ImageView image;
    ParseObject glareObj, ambRequest;

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
        callAdmin = (TextView) view.findViewById(R.id.call_admin);
        callUser = (TextView) view.findViewById(R.id.call_user);
        name = (TextView) view.findViewById(R.id.glare_name);
        desc = (TextView) view.findViewById(R.id.glare_text);
        accept = (Button) view.findViewById(R.id.accept);
        reject = (Button) view.findViewById(R.id.reject);
        image = (ImageView) view.findViewById(R.id.glare_image);

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
        }

        accept.setOnClickListener(this);
        reject.setOnClickListener(this);
    }

    private void getRequestData() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("AmbulanceRequest");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
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
                        name.setText(object.getString("name"));
                    if (object.getString("info") != null)
                        desc.setText(object.getString("info"));
                    if (object.getParseFile("image") != null)
                        Glide.with(getContext()).load(object.getParseFile("image").getUrl()).thumbnail(0.1f).into(image);
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

    }

    private void saveAmbRequestAccept(final boolean accept) {
        if (ambRequest != null) {
            ambRequest.put("hasAccepted", accept);
            ambRequest.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        updateGlareObject(accept);
                        Log.i("RESP", "ambulance updated");
                    } else
                        Log.i("RESP", "save amb request " + e.getLocalizedMessage());
                }
            });
        }
    }

    private void updateGlareObject(boolean accept) {
        if (accept)
            glareObj.put("status", AppConstants.STATUS_COMPLETED);
        else
            glareObj.put("status", AppConstants.STATUS_PENDING);
        glareObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    Log.i("RESP", "glare updated");
                else
                    Log.i("RESP", "glare updated error " + e.getLocalizedMessage());

            }
        });
    }
}
