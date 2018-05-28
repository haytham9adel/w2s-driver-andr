package net.w2s.driverapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.service.StudentLoginLogout;

import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Android-2 on 9/7/2015.
 */
public class StudentLogin extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private Context appContext;

    String nfc = null, nfc_code = null;
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_login);
        appContext = this;

        QrScanner();

    }

    public void QrScanner() {
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera
    }

    @Override
    public void handleResult(Result rawResult) {
        mScannerView.stopCamera();

        appContext = this;
        Log.e("QrResult", rawResult.getText()); // Prints scan results
        if (!Utility.isStringNullOrBlank(rawResult.getText().toString())) {
          //  nfc = "1";
          //  nfc_code = rawResult.getText().toString();
          //  new StudentLoginNFC().execute("" + nfc_code.charAt(16));
            new StudentLoginLogout(StudentLogin.this).execute(rawResult.getText());
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    StudentLogin.this.finish();
                }
            }, 2000);

        } else {
            Toast.makeText(appContext, "" + getString(R.string.qr_empty), Toast.LENGTH_LONG).show();
            finish();
        }
        // show the scanner result into dialog box.
       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();*/
    }


    public class StudentLoginNFC extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        ProgressDialog dialog = new ProgressDialog(StudentLogin.this);

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setTitle("");
            dialog.setMessage("Please Wait.......");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "update_check_in_checkout?student_id=" + params[0];
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
                    Toast.makeText(appContext, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.optString(ConstantKeys.RESULT, "success").equals("success")) {

                        //     Toast.makeText(appContext, "success", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(appContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("Student Login Exception", "" + e);
                Toast.makeText(appContext, getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);

            Intent i = new Intent();
            i.setAction("refresh_map");
            sendBroadcast(i);

            finish();
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }
}
