package net.w2s.driverapp.Utilities;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by RWS 6 on 11/18/2016.
 */
public class MultidexApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        MultiDex.install(MultidexApp.this);
        super.onCreate();
    }


}