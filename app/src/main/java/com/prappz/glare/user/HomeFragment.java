package com.prappz.glare.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prappz.glare.R;

/**
 * Created by Royce RB on 2/11/16.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_home,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.glare_amb).setOnClickListener(this);
        view.findViewById(R.id.glare_pol).setOnClickListener(this);
        view.findViewById(R.id.glare_fire).setOnClickListener(this);
        view.findViewById(R.id.volunteer).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.volunteer) {
            //TODO
        } else {
            Fragment fragment = new GlareFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("id",view.getId());
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("stack").replace(R.id.frame,fragment).commit();
        }
    }
}
