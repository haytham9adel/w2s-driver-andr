package net.w2s.driverapp.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.w2s.driverapp.R;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.NetworkHelperGet;

import org.json.JSONObject;

import java.util.TimeZone;

/**
 * Created by Elnemr on 05/01/2018.
 */

public class StudentLoginLogout extends AsyncTask<String, String, String> {
    JSONObject networkResponse = null;
    Context mContext;
    ProgressDialog dialog;
    String studentId = "";

    public StudentLoginLogout(Context mContext) {
        this.mContext = mContext;
        dialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        dialog.setCancelable(false);
        dialog.setTitle("");
        dialog.setMessage( mContext.getString(R.string.wait) );
        dialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        studentId = params[0] ;
        String URL = ConstantKeys.SERVER_URL + "update_check_in_checkout?student_id=" + params[0]
                                             + "&s_address=" + TimeZone.getDefault().getID();
        //String responce=Utility.findJSONFromUrl(URL);
        NetworkHelperGet putRequest = new NetworkHelperGet(URL);
        try {
            return putRequest.sendGet();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        dialog.dismiss();
        Log.e("StudentNFC Response", "" + s);
        try {
            networkResponse = new JSONObject(s);
            if (networkResponse.equals(null) || networkResponse.equals("")) {
                Toast.makeText(mContext, mContext.getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
            } else {
                if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                    if (!networkResponse.optString("responseMessage", "").equals("")) {
                        Toast.makeText(mContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    } else {

                        Intent i = new Intent();
                        i.setAction("refresh_map");
                        mContext.sendBroadcast(i);

                        Intent i2 = new Intent();
                        i2.setAction("refresh_list");
                        mContext.sendBroadcast(i2);
                    }
                } else {
                    Toast.makeText(mContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("Student Login Exception", "" + e);
            //Toast.makeText(appContext, "Please Check Your Internet Connection !", Toast.LENGTH_LONG).show();
        }
        super.onPostExecute(s);


    }
}

