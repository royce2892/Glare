package com.prappz.glare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by root on 2/11/16.
 */

public class LoginFragment extends Fragment {

    private LinearLayout nameLayout;
    private Button next;
    private EditText name;
    private String phoneNumber;
    private ProgressBar progressBar;

    public LoginFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameLayout = (LinearLayout) view.findViewById(R.id.user_layout);
        next = (Button) view.findViewById(R.id.next);
        name = (EditText) view.findViewById(R.id.et_name);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_view);


        if (!PreferenceManager.getInstance(getContext()).getBoolean(AppConstants.PHONE_LOGGED_IN))
            onLoginPhone();
        else {
            showViews();

        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

    }

    private void createUser() {

        progressBar.setVisibility(View.VISIBLE);
        ParseUser user = new ParseUser();
        user.setUsername("u" + phoneNumber);
        user.setPassword("u" + phoneNumber);
        user.put("phone", phoneNumber);
        user.put("name",name.getText().toString());
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null) {
                    skipToHome();
                } else {
                    progressBar.setVisibility(View.GONE);
                    showToast(e.getLocalizedMessage());
                }
            }
        });
    }

    private void skipToHome() {
        PreferenceManager.getInstance(getContext()).put(AppConstants.USER_NAME,name.getText().toString());
        PreferenceManager.getInstance(getContext()).put(AppConstants.USER_ID,"u"+phoneNumber);
        PreferenceManager.getInstance(getContext()).put(AppConstants.PARSE_LOGGED_IN,true);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame,new HomeFragment()).commit();

    }

    public void onLoginPhone() {
        final Intent intent = new Intent(getContext(), AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, 23);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 23) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null) {
                showToast(loginResult.getError().getErrorType().getMessage());
            } else if (loginResult.wasCancelled()) {
                showToast("Login Cancelled");
            } else {
                if (loginResult.getAccessToken() != null) {
                    Log.i("RESP", loginResult.getAccessToken().getToken());
                    progressBar.setVisibility(View.VISIBLE);
                    getPhone();

                }
            }
        }
    }

    private void getPhone() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                // Get Account Kit ID
                // String accountKitId = account.getId();

                // Get phone number
                phoneNumber = account.getPhoneNumber().toString();
                PreferenceManager.getInstance(getContext()).put(AppConstants.USER_PHONE,phoneNumber);
                showViews();
            }

            @Override
            public void onError(final AccountKitError error) {
                // Handle Error
            }
        });
    }

    private void showViews() {
        progressBar.setVisibility(View.GONE);
        PreferenceManager.getInstance(getContext()).put(AppConstants.PHONE_LOGGED_IN, true);
        nameLayout.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
    }


    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
