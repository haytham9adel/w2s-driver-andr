package net.w2s.driverapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.w2s.driverapp.Beans.NotiBean;
import net.w2s.driverapp.MainActivityNew;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.R;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SendNotiToNextStudentService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.rudiment.trackingbus.driverapp.service.action.handleActionDistance";
    public static final String EXTRA_PARAM1 = "com.rudiment.trackingbus.driverapp.service.extra.PARAM1";

    double lastdis = 0;

    ArrayList<NotiBean> studentsList = new ArrayList<>();

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFoo(Context context, String param1) {
        Intent intent = new Intent(context, SendNotiToNextStudentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public SendNotiToNextStudentService() {
        super("SendNotiToNextStudentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                String a[] = param1.split("-");
                handleActionDistance(new LatLng(Double.parseDouble(a[0]), Double.parseDouble(a[1])));
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDistance(LatLng param1) {

        try {
            JSONArray networkResponse = new JSONArray(Utility.getSharedPreferences(SendNotiToNextStudentService.this, "LAT"));
            JSONArray networkResponse1 = new JSONArray(Utility.getSharedPreferences(SendNotiToNextStudentService.this, "LNG"));
            JSONArray j = new JSONArray(Utility.getSharedPreferences(SendNotiToNextStudentService.this, "STUDENT"));
            ArrayList<LatLng> lastLatLng = new ArrayList<>();
            if (networkResponse.length() > 0) {
                ArrayList<String> parentList = null;
                for (int i = 0; i < networkResponse.length(); i++) {
                    LatLng latLng1 = new LatLng(Double.parseDouble(networkResponse.getString(i).trim())
                            , Double.parseDouble(networkResponse1.getString(i).trim()));
                    parentList = new ArrayList<>();
                    //double a = distance(param1.latitude, param1.longitude, latLng1.latitude, latLng1.longitude);
                    int a = getDistanceFromUrl(createUrl(new LatLng(param1.latitude, param1.longitude), new LatLng(latLng1.latitude, latLng1.longitude)));
                   /* if (lastdis == 0) {
                        lastdis = a;
                        for (int k = 0; k < j.getJSONObject(i).getJSONArray("parent").length(); k++) {
                            JSONObject parentObj = j.getJSONObject(i).getJSONArray("parent").getJSONObject(k);
                            parentList.add(parentObj.getString("parent_id"));
                        }
                        NotiBean notiBean = new NotiBean();
                        notiBean.setDistance("" + a);
                        notiBean.setStudentId(j.getJSONObject(i).getString("student_id"));
                        notiBean.setParentList(parentList);
                        studentsList.add(notiBean);
                    } else */
                    if (a < 15) {
                        for (int k = 0; k < j.getJSONObject(i).getJSONArray("parent").length(); k++) {
                            JSONObject parentObj = j.getJSONObject(i).getJSONArray("parent").getJSONObject(k);
                            parentList.add(parentObj.getString("parent_id"));
                        }
                        NotiBean notiBean = new NotiBean();
                        notiBean.setDistance("" + a);
                        notiBean.setStudentId(j.getJSONObject(i).getString("student_id"));
                        notiBean.setParentList(parentList);
                        studentsList.add(notiBean);
                    }
                }

                String id = Utility.getSharedPreferences(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS);
                if (id.isEmpty()) {
                    for (int i = 0; i < studentsList.size(); i++) {
                        if (i == 0) {
                            Utility.setSharedPreference(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS, studentsList.get(i).getStudentId());
                        } else {
                            Utility.setSharedPreference(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS, studentsList.get(i).getStudentId() + "," + Utility.getSharedPreferences(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS));
                        }
                        try {
                            attempSend(studentsList.get(i));
                            sendBeforeTracking(studentsList.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    MainActivityNew.tag_sendNotiToStudent = false;
                } else {
                    ArrayList<String> ids = new ArrayList<>(Arrays.asList(id.split(",")));
                    for (int k = 0; k < studentsList.size(); k++) {
                        if (!ids.contains(studentsList.get(k).getStudentId())) {
                            Utility.setSharedPreference(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS, studentsList.get(k).getStudentId() + "," + Utility.getSharedPreferences(SendNotiToNextStudentService.this, ConstantKeys.EARLY_NOTI_SEND_IDS));
                            try {
                                attempSend(studentsList.get(k));
                                sendBeforeTracking(studentsList.get(k));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    MainActivityNew.tag_sendNotiToStudent = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDistanceFromUrl(String url) {
        try {
            NetworkHelperGet networkHelperGet = new NetworkHelperGet(url);
            String responce = networkHelperGet.sendGet();
            Log.e("responce for duration", "responce for duration" + responce);
            //now if duration is less then or equl to 10 minute then send notification and
            //when bus is in school then clean preference of ids
            //and clean preference if date changed.

            JSONObject jsonObject = new JSONObject(responce);
            JSONArray routesArray = jsonObject.getJSONArray("routes");
            JSONObject jsonObject1 = routesArray.getJSONObject(0);
            JSONArray legs = jsonObject1.getJSONArray("legs");
            JSONObject jsonObject2 = legs.getJSONObject(0);
            JSONObject duration = jsonObject2.getJSONObject("duration");
            String durationString = duration.getString("text");
            String duratinArray[] = durationString.split(" ");
            return Integer.parseInt(duratinArray[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void attempSend(NotiBean item) throws Exception {
        try {
            String parentids = StringUtils.join(item.getParentList(), ",");
            new SendMessageToParent1().execute(item.getStudentId(), getString(R.string.bus_arrive_str) + " " + item.getDistance() + " " + getString(R.string.mins), parentids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendBeforeTracking(NotiBean item) {
        try {
            String parentids = StringUtils.join(item.getParentList(), ",");
            new SendMessageToParentBefore().execute(parentids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //AsynchTask to send message to parent of child
    private class SendMessageToParent1 extends AsyncTask<String, String, String> {

        JSONObject networkResponse = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String ins_status = "";
            if (Utility.getSharedPreferences(SendNotiToNextStudentService.this, ConstantKeys.MORNING_EVENING).equals("0")) {
                ins_status = "2";
            } else {
                ins_status = "3";
            }
            String URL = ConstantKeys.SERVER_URL + "notification?method=sms&student_id=" + params[0] + "&msg=" + params[1] + "&parent_ids=" + params[2] + "&ins_status=" + ins_status;
            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
            try {
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                networkResponse = new JSONObject(s);
                Log.e("Send Message auto noti", "" + networkResponse);

            } catch (Exception e) {
                Log.e("SendMsg Exception auto", "" + e);
            }
            super.onPostExecute(s);
        }
    }


    //AsyncTask to send message to parent for track before 15 mnt
    private class SendMessageToParentBefore extends AsyncTask<String, String, String> {

        JSONObject networkResponse = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "/trackNotification?parent_ids=" + params[0];
            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
            try {
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                networkResponse = new JSONObject(s);
                Log.e("Send Message auto noti", "" + networkResponse);

            } catch (Exception e) {
                Log.e("SendMsg Exception auto", "" + e);
            }
            super.onPostExecute(s);
        }
    }


    private String createUrl(LatLng startLatLng, LatLng endLatLng) {

        // Origin of route
        String str_origin = "origin=" + startLatLng.latitude + "," + startLatLng.longitude;

        // Destination of route
        String str_dest = "destination=" + endLatLng.latitude + "," + endLatLng.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&mode=driving";


        return url;
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
}
