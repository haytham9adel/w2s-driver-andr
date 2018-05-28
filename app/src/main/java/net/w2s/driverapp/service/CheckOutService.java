package net.w2s.driverapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.NetworkHelperPost;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Created by RWS 6 on 11/28/2016.
 */
public class CheckOutService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Utility.isConnectingToInternet(CheckOutService.this)) {
                 //   new Logout().execute(Utility.getSharedPreferences(CheckOutService.this, ConstantKeys.USER_ID));
                    JSONArray j;
                    try {
                        j = new JSONArray(Utility.getSharedPreferences(CheckOutService.this, "STUDENT"));
                        Log.e("STUDENT ARRAYDB", "" + j.toString());
                        if (j != null) {
                            for (int i = 0; i < j.length(); i++) {
                                if (j.getJSONObject(i).getString("absent_status").equals("1") && (j.getJSONObject(i).getString("status").equals("1"))) {
                                    //checkedIn student // need to check our when drice exit app
                                    String id = j.getJSONObject(i).getString("student_id");
                                    new StudentLoginLogout().execute(id);
                                }
                            }
                            stopSelf();
                        }
                    } catch (Exception e) {
                        stopSelf();
                    }
                } else {
                    stopSelf();
                }
            }
        }).start();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class Logout extends AsyncTask<String, String, String> {

        NetworkHelperPost putRequest = new NetworkHelperPost(ConstantKeys.SERVER_URL + "driverLogout");

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                /*{"driver_id":"3"}*/
                JSONStringer putParameters = new JSONStringer()
                        .object()
                        .key("driver_id").value(params[0])
                        .endObject();
                return putRequest.executePostRequest(putParameters);
            } catch (JSONException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            Utility.setSharedPreference(CheckOutService.this, ConstantKeys.ALREADY_LOGIN, "No");
        }
    }

    public class StudentLoginLogout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String URL = ConstantKeys.SERVER_URL + "update_check_in_checkout?student_id=" + params[0];
                //String responce=Utility.findJSONFromUrl(URL);
                NetworkHelperGet putRequest = new NetworkHelperGet(URL);
                try {
                    return putRequest.sendGet();
                } catch (Exception e) {
                    return "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            Log.e("check out ", "auto checkout " + s);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            s,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
