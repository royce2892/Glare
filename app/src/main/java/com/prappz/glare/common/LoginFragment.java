package com.prappz.glare.common;

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
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.prappz.glare.admin.AdminGlareListFragment;
import com.prappz.glare.driver.DriverHomeFragment;
import com.prappz.glare.user.UserHomeFragment;
import com.prappz.glare.R;

/**
 * Created by root on 2/11/16.
 */

public class LoginFragment extends Fragment {

    private LinearLayout nameLayout;
    private Button next;
    private EditText name;
    private String phoneNumber;
    private ProgressBar progressBar;
    private String id, prefix;

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

        switch (PreferenceManager.getInstance(getContext()).getInt(AppConstants.TYPE)) {

            case AppConstants.MODE_ADMIN:
                prefix = "a";
                break;
            case AppConstants.MODE_DRIVER:
                prefix = "d";
                break;
            default:
                prefix = "u";
                break;
        }
        progressBar.setVisibility(View.VISIBLE);
        ParseUser user = new ParseUser();
        id = prefix + phoneNumber;
        user.setUsername(id);
        user.setPassword(id);
        user.put("phone", phoneNumber);
        user.put("name", name.getText().toString());
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    skipToHome();
                } else {
                    progressBar.setVisibility(View.GONE);
                  //  showToast(e.getLocalizedMessage());
                    if (e.getCode() == ParseException.USERNAME_TAKEN)
                        loginUser();
                }
            }
        });
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        ParseUser.logInInBackground(id, id, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null)
                    skipToHome();
                else {
                    progressBar.setVisibility(View.GONE);
                    showToast(e.getLocalizedMessage());

                }
            }
        });
    }

    private void skipToHome() {
        progressBar.setVisibility(View.GONE);
        PreferenceManager.getInstance(getContext()).put(AppConstants.USER_NAME, name.getText().toString());
        PreferenceManager.getInstance(getContext()).put(AppConstants.USER_ID, id);
        PreferenceManager.getInstance(getContext()).put(AppConstants.PARSE_LOGGED_IN, true);
        if (prefix.contentEquals("u"))
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new UserHomeFragment()).commit();
        else if (prefix.contentEquals("d"))
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new DriverHomeFragment()).commit();
        else
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame, new AdminGlareListFragment()).commit();


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
                try {

                    phoneNumber = account.getPhoneNumber().toString();
                    PreferenceManager.getInstance(getContext()).put(AppConstants.USER_PHONE, phoneNumber);
                    showViews();
                } catch (NullPointerException ex) {
                    Log.i("RESP",ex.getLocalizedMessage());
                }
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
