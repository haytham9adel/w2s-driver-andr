package net.w2s.driverapp.Fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.w2s.driverapp.R;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Android-2 on 3/1/2016.
 */
public class DialogChooseMessage extends DialogFragment implements View.OnClickListener {

    View viewChooseMessage;
    TextView message1, message2, message3, message4, message5, txtSend, txtCancel;
    View view_message1, view_message2, view_message3, view_message4, view_message5;
    private LinearLayout layout1, layout2, layout3, layout4, layout5;
    Context ctxChooseMesaage;
    String msg;
    String parents = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewChooseMessage = inflater.inflate(R.layout.dialog_choose_message, container);
        ctxChooseMesaage = getActivity();

        getDialog().setTitle(getString(R.string.choose_message));
        message1 = (TextView) viewChooseMessage.findViewById(R.id.message1);
        message2 = (TextView) viewChooseMessage.findViewById(R.id.message2);
        message3 = (TextView) viewChooseMessage.findViewById(R.id.message3);
        message4 = (TextView) viewChooseMessage.findViewById(R.id.message4);
        message5 = (TextView) viewChooseMessage.findViewById(R.id.message5);

        view_message1 = (View) viewChooseMessage.findViewById(R.id.view_message1);
        view_message2 = (View) viewChooseMessage.findViewById(R.id.view_message2);
        view_message3 = (View) viewChooseMessage.findViewById(R.id.view_message3);
        view_message4 = (View) viewChooseMessage.findViewById(R.id.view_message4);
        view_message5 = (View) viewChooseMessage.findViewById(R.id.view_message5);

        layout1 = (LinearLayout) viewChooseMessage.findViewById(R.id.layout_message1);
        layout2 = (LinearLayout) viewChooseMessage.findViewById(R.id.layout_message2);
        layout3 = (LinearLayout) viewChooseMessage.findViewById(R.id.layout_message3);
        layout4 = (LinearLayout) viewChooseMessage.findViewById(R.id.layout_message4);
        layout5 = (LinearLayout) viewChooseMessage.findViewById(R.id.layout_message5);

        txtSend = (TextView) viewChooseMessage.findViewById(R.id.txtSend);
        txtCancel = (TextView) viewChooseMessage.findViewById(R.id.txtCancel);

        message1.setOnClickListener(this);
        message2.setOnClickListener(this);
        message3.setOnClickListener(this);
        message4.setOnClickListener(this);
        message5.setOnClickListener(this);
        txtSend.setOnClickListener(this);
        txtCancel.setOnClickListener(this);

        Log.e("Message 1", "" + Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE1));
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE1))) {
            message1.setText(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE1));
            message1.setVisibility(View.VISIBLE);
            layout1.setVisibility(View.VISIBLE);
            view_message1.setVisibility(View.VISIBLE);
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE2))) {
            message2.setText(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE2));
            message2.setVisibility(View.VISIBLE);
            view_message2.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.VISIBLE);
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE3))) {
            message3.setText(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE3));
            message3.setVisibility(View.VISIBLE);
            view_message3.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.VISIBLE);
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE4))) {
            message4.setText(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE4));
            message4.setVisibility(View.VISIBLE);
            view_message4.setVisibility(View.VISIBLE);
            layout4.setVisibility(View.VISIBLE);
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE5))) {
            message5.setText(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.MESSAGE5));
            message5.setVisibility(View.VISIBLE);
            view_message5.setVisibility(View.VISIBLE);
            layout5.setVisibility(View.VISIBLE);
        }

        if (getArguments() != null) {
            parents = getArguments().getString("parents");
        }

        return viewChooseMessage;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message1:
                message1.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                message2.setBackgroundColor(getResources().getColor(R.color.white));
                message3.setBackgroundColor(getResources().getColor(R.color.white));
                message4.setBackgroundColor(getResources().getColor(R.color.white));
                message5.setBackgroundColor(getResources().getColor(R.color.white));
                msg = message1.getText().toString();

                layout1.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                layout2.setBackgroundColor(getResources().getColor(R.color.white));
                layout3.setBackgroundColor(getResources().getColor(R.color.white));
                layout4.setBackgroundColor(getResources().getColor(R.color.white));
                layout5.setBackgroundColor(getResources().getColor(R.color.white));

                break;
            case R.id.message2:
                message1.setBackgroundColor(getResources().getColor(R.color.white));
                message2.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                message3.setBackgroundColor(getResources().getColor(R.color.white));
                message4.setBackgroundColor(getResources().getColor(R.color.white));
                message5.setBackgroundColor(getResources().getColor(R.color.white));
                msg = message2.getText().toString();

                layout1.setBackgroundColor(getResources().getColor(R.color.white));
                layout2.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                layout3.setBackgroundColor(getResources().getColor(R.color.white));
                layout4.setBackgroundColor(getResources().getColor(R.color.white));
                layout5.setBackgroundColor(getResources().getColor(R.color.white));

                break;
            case R.id.message3:
                message1.setBackgroundColor(getResources().getColor(R.color.white));
                message2.setBackgroundColor(getResources().getColor(R.color.white));
                message3.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                message4.setBackgroundColor(getResources().getColor(R.color.white));
                message5.setBackgroundColor(getResources().getColor(R.color.white));
                msg = message3.getText().toString();

                layout1.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                layout2.setBackgroundColor(getResources().getColor(R.color.white));
                layout3.setBackgroundColor(getResources().getColor(R.color.white));
                layout4.setBackgroundColor(getResources().getColor(R.color.white));
                layout5.setBackgroundColor(getResources().getColor(R.color.white));

                layout1.setBackgroundColor(getResources().getColor(R.color.white));
                layout2.setBackgroundColor(getResources().getColor(R.color.white));
                layout3.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                layout4.setBackgroundColor(getResources().getColor(R.color.white));
                layout5.setBackgroundColor(getResources().getColor(R.color.white));

                break;
            case R.id.message4:
                message1.setBackgroundColor(getResources().getColor(R.color.white));
                message2.setBackgroundColor(getResources().getColor(R.color.white));
                message3.setBackgroundColor(getResources().getColor(R.color.white));
                message4.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                message5.setBackgroundColor(getResources().getColor(R.color.white));
                msg = message4.getText().toString();

                layout1.setBackgroundColor(getResources().getColor(R.color.white));
                layout2.setBackgroundColor(getResources().getColor(R.color.white));
                layout3.setBackgroundColor(getResources().getColor(R.color.white));
                layout4.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                layout5.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            case R.id.message5:
                message1.setBackgroundColor(getResources().getColor(R.color.white));
                message2.setBackgroundColor(getResources().getColor(R.color.white));
                message3.setBackgroundColor(getResources().getColor(R.color.white));
                message4.setBackgroundColor(getResources().getColor(R.color.white));
                message5.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                msg = message5.getText().toString();

                layout1.setBackgroundColor(getResources().getColor(R.color.white));
                layout2.setBackgroundColor(getResources().getColor(R.color.white));
                layout3.setBackgroundColor(getResources().getColor(R.color.white));
                layout4.setBackgroundColor(getResources().getColor(R.color.white));
                layout5.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                break;
            case R.id.txtSend:
                if (Utility.isConnectingToInternet(ctxChooseMesaage)) {
                    if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.STUDENTID_MSG))) {
                        if (!Utility.isStringNullOrBlank(msg)) {
                            dismiss();
                            new SendMessageToParent1().execute(Utility.getSharedPreferences(ctxChooseMesaage, ConstantKeys.STUDENTID_MSG), msg.replace(" ", "%20"), parents);
                        } else {
                            Toast.makeText(ctxChooseMesaage, getString(R.string.set_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ctxChooseMesaage, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.txtCancel:
                dismiss();
                break;
        }
    }

    //AsynchTask to send message to parent of selected child
    private class SendMessageToParent1 extends AsyncTask<String, String, String> {
        Context cntx;
        JSONObject networkResponse = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {



            try {


                OkHttpClient client = new OkHttpClient();

                HttpUrl url1 = new HttpUrl.Builder()
                        .scheme("http")
                        .host("m3aak.net")
                        .addPathSegment("webservices")
                        .addPathSegment("notification")
                        .addQueryParameter("msg", params[1].replace("%20" , " ") )
                        .addQueryParameter("method","sms" )
                        .addQueryParameter("student_id", params[0] )
                        .addQueryParameter("parent_ids", params[2] )
                        .addQueryParameter("ins_status", "1" )
                        .build();



                Request request = new Request.Builder()
                        .url(url1)
                        .get()
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build();

                Log.i(" url :" ,request.url().toString() ) ;

                Response response = client.newCall(request).execute();
                Log.i("response : " , response.body().string()) ;

            //    String s = Uri.encode(urlS);
            //    NetworkHelperGet putRequest = new NetworkHelperGet(ConstantKeys.SERVER_URL + s);
             //   return putRequest.sendGet();
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("send noti responce", "" + s);
            try {
                networkResponse = new JSONObject(s);
                Log.e("Send Message", "" + networkResponse);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(cntx, cntx.getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        Toast.makeText(ctxChooseMesaage, getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(cntx, "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("SendMessage Exception", "" + e);
                //Toast.makeText(appContext, "Please Check Your Internet Connection !", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }
}