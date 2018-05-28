package net.w2s.driverapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import net.w2s.driverapp.Utilities.Utility;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by RWS 6 on 11/25/2016.
 */
public class LoginWithQRCode extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    String nfc = null, nfc_code = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QrScanner();
    }

    @Override
    public void handleResult(Result rawResult) {
        mScannerView.stopCamera();
        Log.e("QrResult", rawResult.getText()); // Prints scan results
        if (!Utility.isStringNullOrBlank(rawResult.getText().toString())) {
            nfc = "1";
            nfc_code = rawResult.getText().toString();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("nfc", nfc);
            resultIntent.putExtra("nfc_code", nfc_code);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED);
            Toast.makeText(LoginWithQRCode.this, "" + getString(R.string.qr_empty), Toast.LENGTH_LONG).show();
        }
        finish();
        // show the scanner result into dialog box.
       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();*/
    }

    public void QrScanner() {


        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.

        mScannerView.startCamera();         // Start camera

    }
}
