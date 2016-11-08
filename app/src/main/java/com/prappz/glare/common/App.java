package com.prappz.glare.common;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.facebook.accountkit.AccountKit;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by root on 2/11/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "AJ9IJs5mRHclPcEghR0dFdcRtsuwenqHl6BhJmcz", "LxwYHjPMCqk6021l1Fc4tA4LnDbGkxRyqOQqIzpi");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        //  ParseUser.enableAutomaticUser();
        AccountKit.initialize(this);
        if (ParseUser.getCurrentUser() != null)
            if (ParseUser.getCurrentUser().getUsername() != null)
                ParsePush.subscribeInBackground(ParseUser.getCurrentUser().getUsername().replace("+",""), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null)
                            Log.i("RESP", "subscribed");
                        else
                            Log.i("RESP", e.getLocalizedMessage());
                    }
                });

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }
}
