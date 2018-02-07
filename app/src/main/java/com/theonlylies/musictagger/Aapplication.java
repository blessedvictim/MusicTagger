package com.theonlylies.musictagger;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by linuxoid on 20.12.17.
 */

public class Aapplication extends Application {
    private static Aapplication app;



    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        System.setProperty("android.util.Log", "DEBUG");
    }

    synchronized public Aapplication getInstance() {
        return app;
    }


    private Tracker mTracker;

    /**
     * Получает счетчик {@link Tracker}, используемый по умолчанию для этого приложения {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // Чтобы включить ведение журнала отладки, используйте adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
