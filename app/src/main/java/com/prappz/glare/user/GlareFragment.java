package com.prappz.glare.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.prappz.glare.R;
import com.prappz.glare.common.AppConstants;
import com.prappz.glare.common.PreferenceManager;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by root on 2/11/16.
 */

public class GlareFragment extends Fragment {

    private ImageView mImage;
    private EditText mInfo, mAge;
    private Bitmap bitmap;
    private Spinner mGender;
    private ProgressBar progressBar;

    public GlareFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_glare, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImage = (ImageView) view.findViewById(R.id.glare_pic);
        mInfo = (EditText) view.findViewById(R.id.glare_desc);
        mAge = (EditText) view.findViewById(R.id.glare_age);
        mGender = (Spinner) view.findViewById(R.id.glare_gender_spinner);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);

        setSpinner();
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        view.findViewById(R.id.glare_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private void setSpinner() {
        String[] gender = {"Male", "Female", "Transgender"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, gender);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGender.setAdapter(adapter);
    }

    private void uploadImage() {

        progressBar.setVisibility(View.VISIBLE);
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
            final ParseFile file = new ParseFile("glare.png", image);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        saveGlare(file);
                    else {
                        progressBar.setVisibility(View.GONE);
                        showToast(e.getLocalizedMessage());
                    }
                }
            });
        } else
            saveGlare(null);

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void saveGlare(ParseFile file) {
        ParseObject glare = ParseObject.create("Glare");
        glare.put("info", mInfo.getText().toString());
        glare.put("age", mAge.getText().toString());
        glare.put("gender", mGender.getSelectedItemPosition());
        Double lat, lon;
        if (file != null)
            glare.put("image", file);
        glare.put("phone", PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_PHONE));
        glare.put("name", PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_NAME));
        glare.put("lat", PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_LAT));
        glare.put("lon", PreferenceManager.getInstance(getContext()).getString(AppConstants.USER_LON));

        lat = Double.parseDouble(glare.getString("lat"));
        lon = Double.parseDouble(glare.getString("lon"));

        if (getArguments().getInt("id") == R.id.glare_amb)
            glare.put("type", AppConstants.GLARE_AMBULANCE);
        else if (getArguments().getInt("id") == R.id.glare_fire)
            glare.put("type", AppConstants.GLARE_FIRE);
        else
            glare.put("type", AppConstants.GLARE_POLICE);

        glare.put("location", new ParseGeoPoint(lat, lon));
        glare.put("status", AppConstants.STATUS_PENDING);
        glare.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null) {
                    showToast("Glare sent");
                    getActivity().onBackPressed();
                } else
                    showToast(e.getLocalizedMessage());
            }
        });
    }

    private void takePicture() {
        Intent mRequestFileIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (mRequestFileIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(mRequestFileIntent, 23);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == 23) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                mImage.setImageBitmap(bitmap);
            }
        }
    }

}
