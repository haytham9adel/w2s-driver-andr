package net.w2s.driverapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
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
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import net.w2s.driverapp.Beans.ParentBean;
import net.w2s.driverapp.NavigationPack.NavigationDrawerAdapter;
import net.w2s.driverapp.SlidingTabs.ContentFragmentAdapter;
import net.w2s.driverapp.SlidingTabs.SlidingTabLayout;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.DialogListener;
import net.w2s.driverapp.Utilities.NdefMessageParser;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.NetworkHelperPost;
import net.w2s.driverapp.Utilities.ParsedNdefRecord;
import net.w2s.driverapp.Utilities.TextRecord;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.bluetooth.DeviceScanActivity;
import net.w2s.driverapp.other.MessageActivityNew;
import net.w2s.driverapp.service.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Android Developer-1 on 19-09-2016.
 */
public class MainActivityNew extends AppCompatActivity  {
    private Context appContext;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerPanel;
    private static String[] titles = null;
    private static int[] list_img = null;
    NavigationDrawerAdapter navigationDrawerAdapter;
    public static boolean isVisibleMainActivity = false;
    public static SlidingTabLayout slidingTabLayout;
    public static boolean needToRefreshMap = true;
    public static Map<Integer, ArrayList<ParentBean>> speedNotiBeanArrayList = new Hashtable<>();
    public static LatLng lastLocationStudent = null;

    String nfc = null, nfc_code = null;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private LinearLayout mTagContent;
    private String language = "";
    public static boolean tag_sendNotiToStudent = false;

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String language = Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.Setting_Language);
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

        setContentView(R.layout.activity_main_new);
        appContext = this;

        if (language.equals("1")) {
            ViewCompat.setLayoutDirection(findViewById(R.id.drawer_layout), ViewCompat.LAYOUT_DIRECTION_RTL);
        } else {
            ViewCompat.setLayoutDirection(findViewById(R.id.drawer_layout), ViewCompat.LAYOUT_DIRECTION_LTR);
        }


        getDataServer();

        mTagContent = (LinearLayout) findViewById(R.id.list);
        resolveIntent(getIntent());

        init();
        setTabs(2);




        Intent intent1 = new Intent(MainActivityNew.this, LocationService.class);
        startService(intent1);

        mAdapter = NfcAdapter.getDefaultAdapter(appContext);
        if (mAdapter == null) {
            Toast.makeText(appContext, getString(R.string.no_nfc), Toast.LENGTH_SHORT).show();
            // finish();
            //return;
        }
        mPendingIntent = PendingIntent.getActivity(appContext, 0,
                new Intent(appContext, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true)});

        Log.e("mNdefPushMessage", "" + mNdefPushMessage.toString());

    }

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

    private void resolveIntent(Intent intent) {
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
            ;

            String tagID = sb1.toString();
            Log.e("nfc ID", tagID);//46A0E000

          /*  String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            }*/

            //Test Code End


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
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
                Log.e("msgs NFC:", "" + msgs);
            }
            // Setup the views

            buildTagViews(msgs);
        }
    }

    private String dumpTagData(Parcelable p) {
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

    void buildTagViews(NdefMessage[] msgs) {
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
            timeView.setText(TIME_FORMAT.format(now));
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
            try {
                String base64 = "Mw==";
                // Receiving side
                byte[] data = Base64.decode(TextRecord.NFC_TEXT, Base64.DEFAULT);
                String text = new String(data, "UTF-8");

                Log.e("login id ", "" + text);

                nfc_code = TextRecord.NFC_TEXT;
                new StudentLoginNFC().execute(nfc_code);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public class StudentLoginNFC extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        ProgressDialog dialog = new ProgressDialog(MainActivityNew.this);

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setTitle("");
            dialog.setMessage(getString(R.string.wait));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "update_check_in_checkout?student_id=" + params[0] + "&s_address=" + TimeZone.getDefault().getID();
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
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        finish();
                        startActivity(new Intent(MainActivityNew.this, MainActivityNew.class));
                    } else {
                        Toast.makeText(appContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("Student Login Exception", "" + e);
                //Toast.makeText(appContext, "Please Check Your Internet Connection !", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }


    public class StudentLoginLogout extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        Context mContext;
        ProgressDialog dialog;

        public StudentLoginLogout(Context mContext) {
            this.mContext = mContext;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setTitle("");
            dialog.setMessage(getString(R.string.wait));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "update_check_in_checkout?student_id=" + params[0] + "&s_address=" + TimeZone.getDefault().getID();
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
            MainActivityNew.needToRefreshMap = true;
            Log.e("StudentNFC Response", "" + s);
            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(mContext, getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        if (!networkResponse.optString("responseMessage", "").equals("")) {
                            Toast.makeText(mContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                        }
                        finish();
                        startActivity(new Intent(MainActivityNew.this, MainActivityNew.class));
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

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void setUpNavigationDrawerHome() {
        titles = new String[]{getString(R.string.qr_login_txt), getString(R.string.set_notification), getString(R.string.profile), getString(R.string.chat_with_school_admin), getString(R.string.change_language), "Bluetooth", getString(R.string.logout)};
        list_img = new int[]{R.drawable.qr, R.drawable.notification, R.drawable.profile, R.drawable.message, R.drawable.lang, R.drawable.bluetooth, R.drawable.signout};
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }
        ((TextView) findViewById(R.id.title)).setText(Utility.getSharedPreferences(appContext, ConstantKeys.SCHOOL_NAME));
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(appContext, ConstantKeys.SCHOOL_LOGO))) {
            Picasso.with(appContext)
                    .load(ConstantKeys.SCHOOL_IMAGE_URL + Utility.getSharedPreferences(appContext, ConstantKeys.SCHOOL_LOGO))
                    .into(((ImageView) findViewById(R.id.imgViewSchoolLogo)));
        }
        ((ImageView) toolbar.findViewById(R.id.toggle_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(mDrawerPanel) == true) {
                    mDrawerLayout.closeDrawer(mDrawerPanel);
                } else {
                    mDrawerLayout.openDrawer(mDrawerPanel);
                }
            }
        });

        ((ImageView) toolbar.findViewById(R.id.imgViewSos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.SCHOOL_ADMIN_NUMBER).isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.SCHOOL_ADMIN_NUMBER_COUNTRY_CODE) + "" + Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.SCHOOL_ADMIN_NUMBER)));
                    startActivity(callIntent);
                }
            }
        });

        ListView mDrawerListView = (ListView) findViewById(R.id.navDrawerList);
        mDrawerPanel = (LinearLayout) findViewById(R.id.navDrawerPanel);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //String mActivityTitle = getTitle().toString();
        navigationDrawerAdapter = new NavigationDrawerAdapter(appContext, titles, list_img);
//      ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.menulist));
        mDrawerListView.setAdapter(navigationDrawerAdapter);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(mDrawerPanel);
                openActivites(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle(getString(R.string.drawer_opened));
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void openActivites(int pos) {
        switch (pos) {
            case 0:
                startActivity(new Intent(appContext, StudentLogin.class));
                break;
            case 1:
                startActivity(new Intent(appContext, SetMessageForStudent.class));
                break;
            case 2:
                startActivity(new Intent(appContext, ProfileActivity.class));
                break;
            case 3:
                startActivity(new Intent(appContext, MessageActivityNew.class));
                break;
            case 4:
                showSingleChoiceAlert(MainActivityNew.this);
                break;
            case 5:
                startActivity(new Intent(MainActivityNew.this, DeviceScanActivity.class));
                break;
            case 6:
                Logout();
                break;
        }
    }

    public void showSingleChoiceAlert(final Context context) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialog_anim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.language_picker_popup);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        lp.width = width - 50;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final TextView englishTxt = (TextView) dialog.findViewById(R.id.english_txt);
        final TextView arabicTxt = (TextView) dialog.findViewById(R.id.arabic_txt);
        final LinearLayout arabicTxtLYT = (LinearLayout) dialog.findViewById(R.id.arabic_txt_layout);
        final LinearLayout englishTxtLYT = (LinearLayout) dialog.findViewById(R.id.english_txt_layout);

        if (Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.Setting_Language).equals("1")) {
            language = "1";
            englishTxtLYT.setBackgroundColor(Color.WHITE);
            arabicTxtLYT.setBackground(getResources().getDrawable(R.drawable.round_corner_fill));
            englishTxt.setTextColor(Color.BLACK);
            arabicTxt.setTextColor(Color.WHITE);
        } else {
            language = "0";
            englishTxtLYT.setBackground(getResources().getDrawable(R.drawable.round_corner_fill));
            arabicTxtLYT.setBackgroundColor(Color.WHITE);
            englishTxt.setTextColor(Color.WHITE);
            arabicTxt.setTextColor(Color.BLACK);
        }

        arabicTxtLYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                language = "1";
                englishTxtLYT.setBackgroundColor(Color.WHITE);
                arabicTxtLYT.setBackground(getResources().getDrawable(R.drawable.round_corner_fill));
                englishTxt.setTextColor(Color.BLACK);
                arabicTxt.setTextColor(Color.WHITE);
            }
        });

        englishTxtLYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                language = "0";
                englishTxtLYT.setBackground(getResources().getDrawable(R.drawable.round_corner_fill));
                arabicTxtLYT.setBackgroundColor(Color.WHITE);
                englishTxt.setTextColor(Color.WHITE);
                arabicTxt.setTextColor(Color.BLACK);
            }
        });

        Button saveBtn = (Button) dialog.findViewById(R.id.action_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new SetLanguageTask().execute(language);
            }
        });

        dialog.show();
    }

    private void setLanguage(int tag) {
        Utility.setSharedPreference(MainActivityNew.this, ConstantKeys.Setting_Language, "" + tag);
        if (tag == 1) {
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
        finish();
        startActivity(new Intent(MainActivityNew.this, MainActivityNew.class));
        Utility.setSharedPreferenceBoolean(MainActivityNew.this, ConstantKeys.IS_FIRST_TIME, false);
    }

    private class SetLanguageTask extends AsyncTask<String, String, String> {
        private int lang;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivityNew.this, "", getString(R.string.loading), false, false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && !s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("result").equals("success")) {
                        setLanguage(lang);
                    } else {
                        Toast.makeText(MainActivityNew.this, jsonObject.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivityNew.this, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivityNew.this, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                lang = Integer.parseInt(params[0]);
                /*http://localhost:8080/Tracking_bus/webservices/save_driver_setting?driver_id=3&lang=0*/
                NetworkHelperGet networkHelperGet = new NetworkHelperGet(ConstantKeys.SERVER_URL + "save_driver_setting?driver_id=" + Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.USER_ID) + "&lang=" + params[0]);
                return networkHelperGet.sendGet();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerPanel)) {
            mDrawerLayout.closeDrawer(mDrawerPanel);
        } else {
            super.onBackPressed();
        }
    }

    private void init() {
        setUpNavigationDrawerHome();
    }

    public void getDataServer() {
//        if (Utility.getSharedPreferences(appContext, "STUDENT").equals("")) {
        new GetAllStudentAndRoutes().execute(Utility.getSharedPreferences(appContext, ConstantKeys.ROUTE_ID));
        Log.e("route id", "rout_id" + Utility.getSharedPreferences(appContext, ConstantKeys.ROUTE_ID));
//        }
//        else{
////            new GetRoutes().execute();
//        }
    }

    private void setTabs(int count) {
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        ContentFragmentAdapter adapterViewPager = new ContentFragmentAdapter(getSupportFragmentManager(), this, count);
        vpPager.setAdapter(adapterViewPager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setTextColor(getResources().getColor(R.color.tab_text_color));
        slidingTabLayout.setTextColorSelected(getResources().getColor(R.color.tab_text_color_selected));
        slidingTabLayout.setDistributeEvenly();
        slidingTabLayout.setViewPager(vpPager);
        slidingTabLayout.setTabSelected(0);
        // Change indicator color
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.parseColor("#008A97");
            }
        });
    }

    /*   ------------------------------>CODE FOR GET STUDENT<---------------------------------   */
    public class GetAllStudentAndRoutes extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        ProgressDialog dialog = new ProgressDialog(MainActivityNew.this);

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setTitle(getString(R.string.loading));
            dialog.setMessage(getString(R.string.wait));
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
/*            String URL = ConstantKeys.SERVER_URL + "driver_route.php?" + ConstantKeys.ROUTE_ID + "=" + params[0];
            String responce = Utility.findJSONFromUrl(URL);
            Log.e("All Student ",""+responce);
            return responce;*/
            NetworkHelperGet putRequest = new NetworkHelperGet(ConstantKeys.SERVER_URL + "driver_route?route_id=" + params[0]);
            try {
               /* JSONStringer putParameters = new JSONStringer()
                        .object()
                        .endObject();*/
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                networkResponse = new JSONObject(s);
                Log.i("onPost get data "  ,s ) ;

                Log.e("All Student ", "" + s);
             /*{"result":"success","student":[{"p_status_id":"8827","s_email":"heo@mailinator.com",
             "s_contact":"123456","s_school_id":"3","s_lname":"Heo","s_image_path":"Mack1604546856.png",
             "status":"1","s_parent_id":"92","s_route_id":"1","student_id":"1","s_fname":"Mack"}],"lng":["75.90471267700195"]
             ,"source_lat":"22.6918606","destination_lng":"75.89570239999999","source_lng":"75.86683029999999",
             "destination_lat":"22.7684301","lat":["22.748000205125283"]}*/
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(appContext, "" + getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        Utility.setSharedPreference(appContext, "S_LAT", networkResponse.getString("source_lat"));
                        Log.e("source_lat", "" + Utility.getSharedPreferences(appContext, "S_LAT"));
                        System.out.println(networkResponse.getString("source_lat"));
                        Utility.setSharedPreference(appContext, "S_LNG", networkResponse.getString("source_lng"));
                        Utility.setSharedPreference(appContext, "D_LAT", networkResponse.getString("destination_lat"));
                        Utility.setSharedPreference(appContext, "D_LNG", networkResponse.getString("destination_lng"));
                        if (networkResponse.getJSONArray("student").length() > 0) {
                            //setDB(null,null,null);
                            setDB(networkResponse.getJSONArray("student"), networkResponse.getJSONArray("lat"), networkResponse.getJSONArray("lng"));
                        } else {
                            setDB(new JSONArray(), new JSONArray(), new JSONArray());
                        }
                        String radius = networkResponse.getString("radius");
                        if(!radius.isEmpty()) {
                            String[] radiusArr = radius.split(":");
                            JSONArray radi = new JSONArray();
                            for (String latlngArr : radiusArr) {
                                JSONObject obj = new JSONObject();
                                String[] radiusArrComm = latlngArr.split(",");
                                obj.put("lat", radiusArrComm[0]);
                                obj.put("lng", radiusArrComm[1]);
                                radi.put(obj);
                            }
                            JSONObject radiJson = new JSONObject();
                            radiJson.put("radius", radi);
                            //System.out.print("AJAY="+radiJson.toString());
                            Utility.setSharedPreference(appContext, ConstantKeys.GEOFENCEARRAY, radiJson.toString());
                        }
                    } else {
                        setDB(new JSONArray(), new JSONArray(), new JSONArray());
                        Toast.makeText(appContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDB(new JSONArray(), new JSONArray(), new JSONArray());
                Log.e("GetAllRoutes Exc", "" + e);
            }
            super.onPostExecute(s);
        }
    }

    private void setDB(JSONArray child, JSONArray lat, JSONArray lng) {

        Log.i("set student data : " ,lat.toString() ) ;

        Utility.setSharedPreference(appContext, "STUDENT", "" + child);
        Utility.setSharedPreference(appContext, "LAT", "" + lat);
        Utility.setSharedPreference(appContext, "LNG", "" + lng);

        Intent i = new Intent();
        i.setAction("refresh_map");
        sendBroadcast(i);

        new SetParentBean().execute();

    }



    private class SetParentBean extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                JSONArray j = new JSONArray(Utility.getSharedPreferences(MainActivityNew.this, "STUDENT"));
                for (int k = 0; k < j.length(); k++) {

                    ArrayList<ParentBean> parentList1 = new ArrayList<>();
                    for (int a = 0; a < j.getJSONObject(k).getJSONArray("parent").length(); a++) {
                        JSONObject parentObj = j.getJSONObject(a).getJSONArray("parent").getJSONObject(a);
                        ParentBean parentBean = new ParentBean();
                        parentBean.setParent_fname(parentObj.getString("parent_fname"));
                        parentBean.setParent_family_name(parentObj.getString("parent_family_name"));
                        parentBean.setParent_number(parentObj.getString("parent_number"));
                        parentBean.setParent_id(parentObj.getString("parent_id"));
                        parentBean.setIsNotiSend(false);
                        parentBean.setSpeed(parentObj.getString("speed"));
                        parentList1.add(parentBean);
                    }
                    MainActivityNew.speedNotiBeanArrayList.put(k, parentList1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }



    public void Logout() {

        Utility.showAlert(MainActivityNew.this, getString(R.string.confirm), getString(R.string.want_logout), getString(R.string.cancel), getString(R.string.yes), new DialogListener() {
            @Override
            public void onNegative(DialogInterface dialog) {
                dialog.dismiss();
            }

            @Override
            public void onPositive(DialogInterface dialog) {
                dialog.dismiss();
                SharedPreferences settings = appContext.getSharedPreferences("TrackingBusDriver", 0);
                SharedPreferences.Editor editor = settings.edit();
                //   editor.clear();
                //     editor.commit();
                File database = getApplicationContext().getDatabasePath("StudentManager");

                if (!database.exists()) {
                    // Database does not exist so copy it from assets here
                    Log.e("Database", "Not Found");
                } else {
                    Log.e("Database", "Found");
                    database.delete();
                }

                new Logout().execute(Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.USER_ID));
            }
        });
    }

    private class Logout extends AsyncTask<String, String, String> {

        NetworkHelperPost putRequest = new NetworkHelperPost(ConstantKeys.SERVER_URL + "driverLogout");
        JSONObject networkResponse = null;
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivityNew.this, "", getString(R.string.wait), false, false);
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
            progressDialog.dismiss();

            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(appContext, "" + getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        Utility.setSharedPreference(MainActivityNew.this, ConstantKeys.ALREADY_LOGIN, "No");
                        startActivity(new Intent(appContext, LoginActivity.class));
                        finish();
                        // stopService(new Intent(MainActivityNew.this, LocationService.class));
                    } else {
                        Toast.makeText(appContext, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(appContext, "" + getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.getSharedPreferences(MainActivityNew.this, ConstantKeys.ALREADY_LOGIN).equals("No")) {
            startActivity(new Intent(appContext, LoginActivity.class));
            finish();
        } else {
            isVisibleMainActivity = true;
            if (mAdapter != null) {
                if (!mAdapter.isEnabled()) {
                    showWirelessSettingsDialog();
                }
                mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
                mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
            }
        }

        ///
        registerReceiver(cardDataReceiver, new IntentFilter("com.card_data_receiver"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisibleMainActivity = false;


        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }

        if (cardDataReceiver != null) {
            unregisterReceiver(cardDataReceiver);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public WakefulBroadcastReceiver cardDataReceiver = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String studentId = intent.getExtras().getString("data");
            if (studentId != null && !studentId.isEmpty()) {
                new StudentLoginLogout(MainActivityNew.this).execute(intent.getExtras().getString("data"));
                // Toast.makeText(context, "data received" + studentId, Toast.LENGTH_SHORT).show();
                /*if (listStudent != null) {
                    for (StudentBean studentBean : listStudent) {
                        if (studentBean.getStudentId() != null && !studentBean.getStudentId().isEmpty()) {
                            if (studentBean.getStudentId().trim().equals(studentId.trim())) {

                                break;
                            }
                        }
                    }
                } else {
                    new StudentLoginLogout(getActivity()).execute(intent.getExtras().getString("data"));
                }*/
            } else {
                Toast.makeText(context, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
            }
        }
    };

}