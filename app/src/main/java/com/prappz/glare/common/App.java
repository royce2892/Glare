package com.prappz.glare.common;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.accountkit.AccountKit;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by root on 2/11/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "AJ9IJs5mRHclPcEghR0dFdcRtsuwenqHl6BhJmcz", "LxwYHjPMCqk6021l1Fc4tA4LnDbGkxRyqOQqIzpi");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();
        AccountKit.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }
}
