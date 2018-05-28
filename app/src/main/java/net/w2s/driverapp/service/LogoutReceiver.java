package net.w2s.driverapp.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import net.w2s.driverapp.Utilities.NetworkHelperPost;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by RWS 6 on 3/23/2017.
 */

public class LogoutReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
       Toast.makeText(context, "logout", Toast.LENGTH_SHORT).show();
        new Logout(context).execute(Utility.getSharedPreferences(context, ConstantKeys.USER_ID));
    }

    private class Logout extends AsyncTask<String, String, String> {

        NetworkHelperPost putRequest = new NetworkHelperPost(ConstantKeys.SERVER_URL + "driverLogout");
        JSONObject networkResponse = null;
        Context context;

        public Logout(Context context) {
            this.context = context;
        }

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {

                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        Utility.setSharedPreference(context, ConstantKeys.ALREADY_LOGIN, "No");
                        //  context.stopService(new Intent(context, LocationService.class));
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}
