package com.prappz.glare.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prappz.glare.R;

/**
 * Created by Royce RB on 3/11/16.
 */

public class TypeChooseFragment extends Fragment implements View.OnClickListener {

    public TypeChooseFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_type, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.type_admin).setOnClickListener(this);
        view.findViewById(R.id.type_user).setOnClickListener(this);
        view.findViewById(R.id.type_driver).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        PreferenceManager.getInstance(getContext()).put(AppConstants.TYPE_CHOSEN, true);
        switch (view.getId()) {
            case R.id.type_admin:
                PreferenceManager.getInstance(getContext()).put(AppConstants.TYPE, AppConstants.MODE_ADMIN);
                break;
            case R.id.type_driver:
                PreferenceManager.getInstance(getContext()).put(AppConstants.TYPE, AppConstants.MODE_DRIVER);
                break;
            case R.id.type_user:
                PreferenceManager.getInstance(getContext()).put(AppConstants.TYPE, AppConstants.MODE_USER);
                break;

        }
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new LoginFragment()).commit();
    }
}
