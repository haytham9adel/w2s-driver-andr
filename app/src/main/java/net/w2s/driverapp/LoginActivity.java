package net.w2s.driverapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.Result;

import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.NdefMessageParser;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.NetworkHelperPost;
import net.w2s.driverapp.Utilities.ParsedNdefRecord;
import net.w2s.driverapp.Utilities.TextRecord;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.service.LogoutReceiver;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * Created by BD-2 on 8/11/2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ZXingScannerView.ResultHandler {
    private Context appContext;

    private NfcAdapter mAdapter1;
    private PendingIntent mPendingIntent1;
    private NdefMessage mNdefPushMessage1;
    // private AlertDialog mDialog;
    private static final DateFormat TIME_FORMAT1 = SimpleDateFormat.getDateTimeInstance();
    private LinearLayout mTagContent;
    String nfc = null, nfc_code = null;
    private ZXingScannerView mScannerView;
    private LinearLayout containerCardLayout, containerLogoutBtn;
    private CheckBox mRememberChk;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            if (b != null) {
                nfc = b.getString("nfc");
                nfc_code = b.getString("nfc_code");
                new Login().execute("", "");
            }
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            try {
                // navigation bar height
                int navigationBarHeight = 0;
                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                // status bar height
                int statusBarHeight = 0;
                resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                // display window size for the app layout
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                // screen height - (user app height + status + nav) ..... if non-zero, then there is a soft keyboard
                int keyboardHeight = findViewById(R.id.root_view).getRootView().getHeight() - (statusBarHeight + navigationBarHeight + rect.height());

                if (keyboardHeight <= 0) {
                    isOpen(false);
                } else {
                    isOpen(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void isOpen(boolean hasOpen) {
        if (hasOpen) {
            findViewById(R.id.main1).setVisibility(View.GONE);
            findViewById(R.id.main2).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.main1).setVisibility(View.VISIBLE);
            findViewById(R.id.main2).setVisibility(View.GONE);

            String email = ((EditText) findViewById(R.id.u_email1)).getText().toString();
            String pass = ((EditText) findViewById(R.id.u_pass1)).getText().toString();
            ((EditText) findViewById(R.id.u_email)).setText(email);
            ((EditText) findViewById(R.id.u_pass)).setText(pass);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String language = Utility.getSharedPreferences(LoginActivity.this, ConstantKeys.Setting_Language);
        if (language.equals("1")) {
            Locale locale = new Locale("ar");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        setContentView(R.layout.activity_login_new);
        appContext = this;

        if (language.equals("1")) {
            ViewCompat.setLayoutDirection(findViewById(R.id.root_view), ViewCompat.LAYOUT_DIRECTION_RTL);
        } else {
            ViewCompat.setLayoutDirection(findViewById(R.id.root_view), ViewCompat.LAYOUT_DIRECTION_LTR);
        }

        findViewById(R.id.root_view).getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        // Registered device for GCM
        if (Utility.isConnectingToInternet(appContext) == true) {

        } else {
            Toast.makeText(appContext,
                    getString(R.string.no_internet), Toast.LENGTH_LONG)
                    .show();
        }
       /* if(!Utility.getSharedPreferences(appContext, ConstantKeys.ROUTE_ID).equals("")){
         switchActivity();
        }else {*/
        init();
        //}
    }

    private class SetLanguageTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                /*http://localhost:8080/Tracking_bus/webservices/save_driver_setting?driver_id=3&lang=0*/
                NetworkHelperGet networkHelperGet = new NetworkHelperGet(ConstantKeys.SERVER_URL + "save_driver_setting?driver_id=" + Utility.getSharedPreferences(LoginActivity.this, ConstantKeys.USER_ID) + "&lang=" + params[0]);
                return networkHelperGet.sendGet();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private void init() {
        ((TextView) findViewById(R.id.login_txt)).setOnClickListener(this);
        ((TextView) findViewById(R.id.login_txt1)).setOnClickListener(this);
        ((TextView) findViewById(R.id.txtViewHelp)).setOnClickListener(this);
        ((TextView) findViewById(R.id.txtViewHelp1)).setOnClickListener(this);
        ((TextView) findViewById(R.id.loginWithQrCode)).setOnClickListener(this);

        mTagContent = (LinearLayout) findViewById(R.id.list);
        resolveIntent1(getIntent());

        EditText uEmail = (EditText) findViewById(R.id.u_email);
        EditText uEmail1 = (EditText) findViewById(R.id.u_email1);
        EditText uPass = (EditText) findViewById(R.id.u_pass);
        EditText uPass1 = (EditText) findViewById(R.id.u_pass1);

        if (Utility.getSharedPreferences(appContext, ConstantKeys.IS_REMEMBER).equals("Yes")) {
            ((CheckBox) findViewById(R.id.rememberChk)).setChecked(true);
            ((CheckBox) findViewById(R.id.rememberChk1)).setChecked(true);
            uEmail.setText(Utility.getSharedPreferences(appContext, ConstantKeys.USER_NAME));
            uEmail1.setText(Utility.getSharedPreferences(appContext, ConstantKeys.USER_NAME));
            uPass.setText(Utility.getSharedPreferences(appContext, ConstantKeys.USER_PASS));
            uPass1.setText(Utility.getSharedPreferences(appContext, ConstantKeys.USER_PASS));
        }

        ((EditText) findViewById(R.id.u_pass1)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                }
                return false;
            }
        });

        final CheckBox chk = (CheckBox) findViewById(R.id.rememberChk);
        final CheckBox chk1 = (CheckBox) findViewById(R.id.rememberChk1);
        chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && chk1.isChecked()) {

                } else {
                    chk1.setChecked(isChecked);
                }
            }
        });

        chk1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && chk.isChecked()) {

                } else {
                    chk.setChecked(isChecked);
                }
            }
        });

        containerCardLayout = (LinearLayout) findViewById(R.id.container_card);
        containerLogoutBtn = (LinearLayout) findViewById(R.id.container_logout_btn);

        // mDialog = new AlertDialog.Builder(appContext).setNeutralButton("Ok", null).create();

        mAdapter1 = NfcAdapter.getDefaultAdapter(appContext);
        if (mAdapter1 == null) {
            //showMessage("Error in NFC", "No NFC found on this device");
            Toast.makeText(appContext, getString(R.string.no_nfc), Toast.LENGTH_SHORT).show();
            //finish();
            // return;
        }

        mPendingIntent1 = PendingIntent.getActivity(appContext, 0,
                new Intent(appContext, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage1 = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true)});

        Log.e("mNdefPushMessage", "" + mNdefPushMessage1.toString());

    }

    private void submit() {
        nfc = "0";
        String res = CheckValidation(((EditText) findViewById(R.id.u_email1)).getText().toString(), ((EditText) findViewById(R.id.u_pass1)).getText().toString());
        if (res.equals("suc")) {
            if (Utility.isConnectingToInternet(appContext) == true) {
                new Login().execute(((EditText) findViewById(R.id.u_email1)).getText().toString(), ((EditText) findViewById(R.id.u_pass1)).getText().toString());
            } else {
                Toast.makeText(appContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(appContext, res, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_txt1:
            case R.id.login_txt:
                submit();
                break;
            case R.id.forgot_pass_txt:
                break;

            case R.id.txtViewHelp1:
            case R.id.txtViewHelp:
                Uri uri = Uri.parse("http://m3aak.net/help.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.loginWithQrCode:
                QrScanner();
                break;
        }
    }

    public String CheckValidation(String email, String pass) {
        if (email.equals("") || pass.equals("")) {
            return getString(R.string.not_empty);
        } else {
           /* if(Utility.isEmailAddressValid(email)!=true){
                return "Invalid Email ID !";
            }else {*/
            return "suc";
            // }
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        mScannerView.stopCamera();
        setContentView(R.layout.activity_login);
        appContext = this;
        init();
        Log.e("QrResult", rawResult.getText()); // Prints scan results
        if (!Utility.isStringNullOrBlank(rawResult.getText().toString())) {
            nfc = "1";
            nfc_code = rawResult.getText().toString();
            new Login().execute("", "");
        } else {
            Toast.makeText(appContext, "" + getString(R.string.qr_empty), Toast.LENGTH_LONG).show();
        }
        // show the scanner result into dialog box.
       /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();*/
    }

    public void QrScanner() {
        Intent i = new Intent(LoginActivity.this, LoginWithQRCode.class);
        startActivityForResult(i, 1);

      /*  mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera*/
    }
/*   ------------------------------>CODE FOR LOGIN<---------------------------------   */

    public class Login extends AsyncTask<String, String, String> {
        NetworkHelperPost putRequest = new NetworkHelperPost(ConstantKeys.SERVER_URL + "driverLogin");
        JSONObject networkResponse = null;
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setTitle(getString(R.string.login));
            dialog.setMessage(getString(R.string.wait));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
/*           {"nfc":"0","nfc_code":"","user_email":"m.mk","user_pass":"372","device_token":"sadasdas@@$dasfsar4"}*/
            try {
                String device_registered_id = FirebaseInstanceId.getInstance().getToken();
                Log.i("device_registered_id" , device_registered_id +"") ;
                String deviceId = Settings.Secure.getString(LoginActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.i("deviceId" , deviceId +"") ;

                JSONStringer putParameters = new JSONStringer()
                        .object()
                        .key(ConstantKeys.USER_EMAIL).value(params[0])
                        .key(ConstantKeys.USER_PASS).value(params[1])
                        .key("nfc").value(nfc)
                        .key("nfc_code").value(nfc_code)
                        .key("device_token").value(device_registered_id)
                        .key("device_id").value(deviceId)
                        .key("time_zone").value(TimeZone.getDefault().getID())
                        .endObject();
                return putRequest.executePostRequest(putParameters);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            Log.e("", "Login Response is " + s);
/*Login Response is
{
  "user_email": "new11@mailinator.com",
  "school_admin": "9",
  "role": "driver",
  "route_id": 2,
  "route_name": "Route-4",
  "user_name": "\u0000m\u0000.\u0000m\u0000k",
  "last_name": "Mk",
  "school_logo": "Choithram School1096899965.png",
  "school_name": "Choithram School",
  "contact_number": "9981472471",
  "result": "success",
  "school_id": 3,
  "user_id": 3,
  "image_path": "null907009221.png",
  "dob": "AB+",
  "nationlity": "Arabian",
  "first_name": "Mk"
  "blood_group": "Mk"
}
*/
            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(appContext, "" + getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        if (((CheckBox) findViewById(R.id.rememberChk)).isChecked()) {
                            Utility.setSharedPreference(appContext, ConstantKeys.IS_REMEMBER, "Yes");
                            Utility.setSharedPreference(appContext, ConstantKeys.USER_PASS, ((EditText) findViewById(R.id.u_pass1)).getText().toString().trim());
                            Utility.setSharedPreference(appContext, ConstantKeys.USER_NAME, ((EditText) findViewById(R.id.u_email1)).getText().toString().trim());
                        } else {
                            Utility.setSharedPreference(appContext, ConstantKeys.IS_REMEMBER, "No");
                        }

                        Utility.setSharedPreference(appContext, ConstantKeys.USER_EMAIL, networkResponse.getString(ConstantKeys.USER_EMAIL));
                        Utility.setSharedPreference(appContext, ConstantKeys.CONTACT_NO, networkResponse.getString(ConstantKeys.CONTACT_NO));
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_ID, networkResponse.getString(ConstantKeys.SCHOOL_ID));
                        Utility.setSharedPreference(appContext, ConstantKeys.ROLE, networkResponse.getString(ConstantKeys.ROLE));
                        Utility.setSharedPreference(appContext, ConstantKeys.USER_ID, networkResponse.getString(ConstantKeys.USER_ID));
                        Utility.setSharedPreference(appContext, ConstantKeys.FIRST_NAME, networkResponse.getString(ConstantKeys.FIRST_NAME));
                        Utility.setSharedPreference(appContext, ConstantKeys.ROUTE_ID, networkResponse.getString(ConstantKeys.ROUTE_ID));
                        Utility.setSharedPreference(appContext, ConstantKeys.LAST_NAME, networkResponse.getString(ConstantKeys.LAST_NAME));
                        Utility.setSharedPreference(appContext, ConstantKeys.Reciever_ID, networkResponse.getString("school_admin"));
                        Utility.setSharedPreference(appContext, ConstantKeys.ROUTE_NAME, networkResponse.getString(ConstantKeys.ROUTE_NAME));
                        Utility.setSharedPreference(appContext, ConstantKeys.DRIVER_IMAGEPATH, networkResponse.getString("image_path"));
                        // Utility.setSharedPreference(appContext, ConstantKeys.USER_NAME, networkResponse.getString(ConstantKeys.USER_NAME));
                        String school_log = networkResponse.getString(ConstantKeys.SCHOOL_LOGO);
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_LOGO, school_log.replaceAll(" ", "%20"));
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_NAME, networkResponse.getString(ConstantKeys.SCHOOL_NAME));
                        Utility.setSharedPreference(appContext, ConstantKeys.USER_DOB, networkResponse.getString(ConstantKeys.USER_DOB));
                        Utility.setSharedPreference(appContext, ConstantKeys.USER_NATIONALITY, networkResponse.getString(ConstantKeys.USER_NATIONALITY));
                        Utility.setSharedPreference(appContext, ConstantKeys.USER_BLOODGROUP, networkResponse.getString(ConstantKeys.USER_BLOODGROUP));
                        Utility.setSharedPreference(appContext, ConstantKeys.USER_QRCODE, networkResponse.getString(ConstantKeys.USER_QRCODE));
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_ADMIN_NAME, networkResponse.getString("school_admin_name"));

                        if (Utility.getSharedPreferencesBoolean(appContext, ConstantKeys.IS_FIRST_TIME)) {
                            Utility.setSharedPreferenceBoolean(appContext, ConstantKeys.IS_FIRST_TIME, false);
                            new SetLanguageTask().execute(Utility.getSharedPreferences(appContext, ConstantKeys.Setting_Language));
                        } else {
                            Utility.setSharedPreference(appContext, ConstantKeys.Setting_Language, networkResponse.optString("lang"));
                        }
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_ADMIN_NUMBER, networkResponse.optString(ConstantKeys.SCHOOL_ADMIN_NUMBER, ""));
                        Utility.setSharedPreference(appContext, ConstantKeys.SCHOOL_ADMIN_NUMBER_COUNTRY_CODE, networkResponse.optString(ConstantKeys.SCHOOL_ADMIN_NUMBER_COUNTRY_CODE, ""));
                        switchActivity();
                    } else {
                        Toast.makeText(appContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("Exception********", "" + e);
                Toast.makeText(appContext, "" + getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }

    private void switchActivity() {
        setAllReminder(LoginActivity.this);
        Utility.setSharedPreference(LoginActivity.this, ConstantKeys.ALREADY_LOGIN, "Yes");
        startActivity(new Intent(appContext, MainActivityNew.class));
        finish();
    }

    //Code for nfc card reader
   /* private void showMessage(String  title, String message) {
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.show();
    }*/

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter1 != null) {
            if (!mAdapter1.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mAdapter1.enableForegroundDispatch(this, mPendingIntent1, null, null);
            mAdapter1.enableForegroundNdefPush(this, mNdefPushMessage1);
        }

        if (mScannerView != null) {
            mScannerView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter1 != null) {
            mAdapter1.disableForegroundDispatch(this);
            mAdapter1.disableForegroundNdefPush(this);
        }
        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.nfc_disabled));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
        return;
    }

    private void resolveIntent1(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            //Test Code
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] extraID = tagFromIntent.getId();

            StringBuilder sb1 = new StringBuilder();
            for (byte b : extraID) {
                sb1.append(String.format("%02X", b));
            }

            String tagID = sb1.toString();
            Log.e("nfc ID", tagID);//46A0E000

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            Log.e("rawMsgs", "" + rawMsgs.toString());
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                Log.e("msgs[i] NFC", "" + msgs);
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData1(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
                Log.e("msgs NFC:", "" + msgs);
            }
            // Setup the views


            buildTagViews1(msgs);
        }
    }

    private String dumpTagData1(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        Log.e("sb.append(\"Tag ID )", "" + sb.append("Tag ID (hex): ").append(getHex(id)).append("\n").toString());
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");
        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    void buildTagViews1(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTagContent;

        // Parse the first message in the list
        // Build views for all of the sub records
        Date now = new Date();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        Log.e("records ", "" + records.toString());
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            TextView timeView = new TextView(this);
            timeView.setText(TIME_FORMAT1.format(now));
            content.addView(timeView, 0);
            ParsedNdefRecord record = records.get(i);
            content.addView(record.getView(this, inflater, content, i), 1 + i);
            content.addView(inflater.inflate(R.layout.tag_divider, content, false), 2 + i);
        }

        Log.e("NFC_TEXT ", "" + TextRecord.NFC_TEXT);
        if (TextRecord.NFC_TEXT != null) {
            /*if (TextRecord.NFC_TEXT== Utility.getSharedPreferences())
            {

            }*/
            nfc = "1";

            try {
                String base64 = "Mw==";
                // Receiving side
                byte[] data = Base64.decode(TextRecord.NFC_TEXT, Base64.DEFAULT);
                String text = new String(data, "UTF-8");

                Log.e("login id ", "" + text);

                nfc_code = TextRecord.NFC_TEXT;
                //  Toast.makeText(appContext, getString(R.string.NFC_RECORD) + TextRecord.NFC_TEXT, Toast.LENGTH_LONG).show();
                new Login().execute("", "");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent1(intent);
    }

    private void setAllReminder(Context context) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 30);
            long mills = cal.getTimeInMillis();
           /* AlarmManager am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
            Intent intent = new Intent(context, LogoutReceiver.class);
            intent.putExtra("data", mills);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, mills, AlarmManager.INTERVAL_DAY, pi);*/
            AlarmManager am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
            Intent intent = new Intent(context, LogoutReceiver.class);
            intent.putExtra("data", mills);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, mills, pi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
