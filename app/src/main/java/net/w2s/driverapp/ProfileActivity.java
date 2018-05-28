package net.w2s.driverapp;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.w2s.driverapp.Beans.DocumentBean;
import net.w2s.driverapp.MyWidgets.CircleImageView;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.NetworkHelperPost;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private Context ctxProfile;
    private LinearLayout mDocumentLayout;
    private ArrayList<TextView> docAttachmentList = new ArrayList<>();
    private ArrayList<DocumentBean> documentBeanArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ctxProfile = this;
        ((TextView) findViewById(R.id.user_name)).setText(Utility.getSharedPreferences(ctxProfile, ConstantKeys.FIRST_NAME) +
                "  " + Utility.getSharedPreferences(ctxProfile, ConstantKeys.LAST_NAME));

        ((TextView) findViewById(R.id.txtNationality)).setText("" + Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_NATIONALITY));
        ((TextView) findViewById(R.id.txtBloodGroup)).setText("" + Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_BLOODGROUP));
        ((TextView) findViewById(R.id.txtDob)).setText("" + Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_DOB));
        ((TextView) findViewById(R.id.txtContact)).setText("" + Utility.getSharedPreferences(ctxProfile, ConstantKeys.CONTACT_NO));
        ((TextView) findViewById(R.id.txtEmail)).setText("" + Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_EMAIL));

        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxProfile, ConstantKeys.DRIVER_IMAGEPATH))) {
            Picasso.with(ctxProfile)
                    .load(ConstantKeys.DRIVER_IMAGE_URL + Utility.getSharedPreferences(ctxProfile, ConstantKeys.DRIVER_IMAGEPATH))
                    .into((ImageView) findViewById(R.id.user_pro_pic));
        }
        String path = ConstantKeys.DRIVER_IMAGE_URL + Utility.getSharedPreferences(ctxProfile, ConstantKeys.DRIVER_IMAGEPATH);
        Log.e("image", "" + path);
        init();
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_QRCODE))) {
            Picasso.with(ctxProfile)
                    .load(ConstantKeys.QRCODE_IMAGE_URL + Utility.getSharedPreferences(ctxProfile, ConstantKeys.USER_QRCODE))
                    .fit().into((ImageView) findViewById(R.id.imgQrCode));
        }
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }
        ((TextView) findViewById(R.id.title)).setText(getString(R.string.profile));
        ((CircleImageView) findViewById(R.id.toggle_btn)).setVisibility(View.GONE);
        mDocumentLayout = (LinearLayout) findViewById(R.id.document_container);

        new GetProfileTask().execute(Utility.getSharedPreferences(ProfileActivity.this, ConstantKeys.USER_ID));
    }

    private void addDoucumentView(DocumentBean documentItem) {
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        View docView = inflater.inflate(R.layout.driver_document_row, null);
        TextView attachmentTxt = (TextView) docView.findViewById(R.id.card_copy);
        TextView documentNameTxt = (TextView) docView.findViewById(R.id.document_name);
        TextView expieryDateTxt = (TextView) docView.findViewById(R.id.expiery_date);
        TextView remindBeforeDateTxt = (TextView) docView.findViewById(R.id.remind_before_day);

        attachmentTxt.setTextColor(Color.BLUE);
        attachmentTxt.setPaintFlags(attachmentTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        attachmentTxt.setText(getString(R.string.download));
        documentNameTxt.setText(documentItem.getInsurance_document_name());
        expieryDateTxt.setText(documentItem.getInsurance_document_expiry());
        remindBeforeDateTxt.setText(documentItem.getRemind_day());

        docAttachmentList.add(attachmentTxt);
        mDocumentLayout.addView(docView);

        for (int i = 0; i < docAttachmentList.size(); i++) {
            addClickForDownloadCard(i);
        }
        registerReceiver(broadcastReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void addClickForDownloadCard(final int i) {
        docAttachmentList.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String copyUrl = documentBeanArrayList.get(i).getInsurance_card_copy();
                if (copyUrl != null && !copyUrl.isEmpty()) {
                    downloadFile(copyUrl);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String url) {

        Toast.makeText(ProfileActivity.this, getString(R.string.download_start), Toast.LENGTH_SHORT).show();
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));
        dm.enqueue(request);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Toast.makeText(context, getString(R.string.download_done), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, getString(R.string.unable_download), Toast.LENGTH_SHORT).show();
            }
        }
    };


    class GetProfileTask extends AsyncTask<String, String, String> {

        NetworkHelperPost putRequest = new NetworkHelperPost(ConstantKeys.SERVER_URL + "driverDetails");
        ProgressDialog progressDialog = ProgressDialog.show(ProfileActivity.this, "", getString(R.string.loading), false, false);

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
            Log.e("responce ", "driver profile " + s);
            progressDialog.dismiss();

            if (s != null && !s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray documentArray = jsonObject.getJSONArray("documents");
                    for (int i = 0; i < documentArray.length(); i++) {
                        /*"v_doc_id": 12,
            "v_id": 3,
            "school_id": 3,
            "remind_day": "13",
            "insurance_document_expiry": "2016-10-20",
            "insurance_card_copy": "http://localhost:8080/Tracking_bus/resources/dashboard/uploads/insurance_card/504828772016-07-06 15-35-15.png",
            "status": 1,
            "insurance_document_name": "asdsad"*/
                        JSONObject itemObj = documentArray.getJSONObject(i);
                        DocumentBean item = new DocumentBean();
                        item.setInsurance_card_copy(itemObj.optString("insurance_card_copy", "N/A"));
                        item.setInsurance_document_expiry(itemObj.optString("insurance_document_expiry", "N/A"));
                        item.setInsurance_document_name(itemObj.optString("insurance_document_name", "N/A"));
                        item.setRemind_day(itemObj.optString("remind_day", "N/A"));
                        item.setSchool_id(itemObj.optString("school_id", "N/A"));
                        item.setStatus(itemObj.optString("status", "N/A"));
                        item.setV_doc_id(itemObj.optString("v_doc_id", "N/A"));
                        item.setV_id(itemObj.optString("v_id", "N/A"));
                        documentBeanArrayList.add(item);
                        addDoucumentView(item);
                    }
                    ((TextView) findViewById(R.id.licency_expirey_date)).setText("" + jsonObject.optString("licence_expiry_date", "N/A"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}