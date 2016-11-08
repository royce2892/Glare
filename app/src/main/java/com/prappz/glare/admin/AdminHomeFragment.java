package com.prappz.glare.admin;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prappz.glare.R;

/**
 * Created by Royce RB on 4/11/16.
 */

public class AdminHomeFragment extends Fragment {

    private ViewPager pager;
    private TabLayout tabLayout;
    private GlarePagerAdapter glarePagerAdapter;

    public AdminHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_admin_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = (ViewPager) view.findViewById(R.id.pager);

        glarePagerAdapter = new GlarePagerAdapter(getActivity().getSupportFragmentManager());
        pager.setAdapter(glarePagerAdapter);
        tabLayout.setupWithViewPager(pager);
        try {
            pager.setOffscreenPageLimit(2);
        } catch (IllegalStateException ex) {
            Log.i("RESP", ex.getLocalizedMessage());
        }

    }

    public class GlarePagerAdapter extends FragmentStatePagerAdapter {
        public GlarePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt("status", position);
            AdminGlareListFragment fragment = new AdminGlareListFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "PENDING";
            else if (position == 1)
                return "ASSIGNED";
            else
                return "COMPLETED";
        }
    }
}
