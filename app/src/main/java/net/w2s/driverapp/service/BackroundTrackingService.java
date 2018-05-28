package net.w2s.driverapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class BackroundTrackingService extends Service implements LocationListener {

    public BackroundTrackingService() {
    }

    Location lastLocation; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 5000 /** 60 * 1*/
            ; // 1 minute

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //      startTimer();

        LocationManager locationManager = (LocationManager) BackroundTrackingService.this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //  connectSocket();
        return START_STICKY;
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        try {
            if (Utility.getSharedPreferences(BackroundTrackingService.this, ConstantKeys.ALREADY_LOGIN).equals("Yes")) {
                //initialize the TimerTask's job
                initializeTimerTask();
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "service stop self",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "service exception ",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        //schedule the timer, to wake up every 5 second
        timer.schedule(timerTask, 500, MIN_TIME_BW_UPDATES);
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        try {
            timerTask = new TimerTask() {
                public void run() {
                    final Location location = getLocation();
                    if (location != null) {
                        if (lastLocation == null) {
                            lastLocation = location;
                            new SendLocation().execute(Utility.getSharedPreferences(BackroundTrackingService.this, ConstantKeys.USER_ID), String.valueOf(latitude), String.valueOf(longitude), "" + location.getSpeed());
                        } else {
                            double distance = distance(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude());
                            if (distance > 20) {
                                //send location to server
                                new SendLocation().execute(Utility.getSharedPreferences(BackroundTrackingService.this, ConstantKeys.USER_ID), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "" + location.getSpeed());
                                lastLocation = location;
                            }
                        }
                    } else {
                        Handler handler1 = new Handler(Looper.getMainLooper());
                        handler1.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Location null",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*   ------------------------------>CODE FOR UPDATE LOCATION OF DRIVER AT SERVER<---------------------------------   */
    public class SendLocation extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "driver_lat_lng?" + ConstantKeys.DRIVER_ID + "=" + params[0] +
                    "&" + ConstantKeys.LAT + "=" + params[1] + "&" + ConstantKeys.LNG + "=" + params[2] +
                    "&route_id=" + Utility.getSharedPreferences(BackroundTrackingService.this, ConstantKeys.ROUTE_ID) + "&speed=" + params[3];
            // Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID)
            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
            try {
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            // System.out.println(s);
            Log.e("SendLocation Response", "" + s);
            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "error in sending location" + s, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Location updated successfully" + s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                // Toast.makeText(getActivity(), "Try again !", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                // Log.e("SendLocation Exce",""+e);
            }
            super.onPostExecute(s);
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    public Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) BackroundTrackingService.this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
               /* // First get location from Network Provider
                if (isNetworkEnabled) {

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                } else*/
                if (isGPSEnabled) {
                    if (location == null) {
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        String lat = "" + location.getLatitude();
        Log.e("locatoin", "" + lat);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("destroy", "destroy");

        try {
            timer.cancel();
            timer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
