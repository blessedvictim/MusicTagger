package com.theonlylies.musictagger;

import android.app.Application;

/**
 * Created by linuxoid on 20.12.17.
 */

public class Aapplication extends Application {
    private static Aapplication app;



    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
    }

    public Aapplication getInstance(){
        return app;
    }
}
