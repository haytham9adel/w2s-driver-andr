package net.w2s.driverapp.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.w2s.driverapp.Adapters.MultichoiceAdapter;
import net.w2s.driverapp.Adapters.SingleChoiceAdapter;
import net.w2s.driverapp.Beans.ParentBean;
import net.w2s.driverapp.Beans.StudentBean;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.Utilities.QueryManager;
import net.w2s.driverapp.R;
import net.w2s.driverapp.SetMessageForStudent;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.DialogListener;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.service.LocationService;
import net.w2s.driverapp.service.StudentLoginLogout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by LAKHAN on 6/12/2015.
 */
public class StudentsReportFragment extends Fragment implements View.OnClickListener {

    private View v;
    private TextView absent_days_txt, present_days_txt, student_waiting;
    private RelativeLayout relLayTotal, relLayAbsent, relLayWaiting, relLayPresent;
    public static int listType = 0;
    private ArrayList<StudentBean> listStudent;
    public static String SELECTED_STUDENT_ID = "";

    static String TAG = "StudentsReportFragment" ;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.report_fragment, null);
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("refresh_list"));
        return v;
    }

    private void init() {
        ArrayList<StudentBean> list = new ArrayList<>(); // all in db
        List<String> students = new ArrayList<>();  // to show totla counter
        List<String> studentsFilter = new ArrayList<>(); // to show filtered list
        absent_days_txt = (TextView) v.findViewById(R.id.absent_days_txt);
        present_days_txt = (TextView) v.findViewById(R.id.present_days_txt);
        student_waiting = (TextView) v.findViewById(R.id.student_waiting);
        relLayTotal = (RelativeLayout) v.findViewById(R.id.relLayTotal);
        relLayAbsent = (RelativeLayout) v.findViewById(R.id.relLayAbsent);
        relLayWaiting = (RelativeLayout) v.findViewById(R.id.relLayWaiting);
        relLayPresent = (RelativeLayout) v.findViewById(R.id.relLayPresent);
        relLayTotal.setOnClickListener(this);
        relLayAbsent.setOnClickListener(this);
        relLayWaiting.setOnClickListener(this);
        relLayPresent.setOnClickListener(this);
        listStudent = new ArrayList<>();

        int total_absent = 0, total_present = 0, total_waiting = 0;
        JSONArray j = null;
        try {
            j = new JSONArray(Utility.getSharedPreferences(getActivity(), "STUDENT"));
            Log.e(TAG, "get student pref:" + j.toString());
        } catch (Exception e) {

        }
        if (j != null) {
            for (int i = 0; i < j.length(); i++) {
                String name = "";
                if (j.length() > 0) {
                    try {
                        StudentBean studentBean = new StudentBean();
                        name = j.getJSONObject(i).getString("s_fname") + " " + j.getJSONObject(i).optString("family_name", "");
                        students.add(name);

                        studentBean.setFirstName(j.getJSONObject(i).getString("s_fname"));
                        studentBean.setFamilyName(j.getJSONObject(i).optString("family_name", ""));
                        studentBean.setStatus(j.getJSONObject(i).getString("status"));
                        studentBean.setStatus_absent(j.getJSONObject(i).getString("absent_status"));
                        studentBean.setStudentContact(j.getJSONObject(i).getString("s_contact"));
                        studentBean.setStudentId(j.getJSONObject(i).getString("student_id"));

                        ArrayList<ParentBean> parentList = new ArrayList<>();
                        for (int k = 0; k < j.getJSONObject(i).getJSONArray("parent").length(); k++) {
                            JSONObject parentObj = j.getJSONObject(i).getJSONArray("parent").getJSONObject(k);
                            ParentBean parentBean = new ParentBean();
                            parentBean.setParent_fname(parentObj.getString("parent_fname"));
                            parentBean.setParent_family_name(parentObj.getString("parent_family_name"));
                            parentBean.setParent_number(parentObj.getString("parent_number"));
                            parentBean.setParent_id(parentObj.getString("parent_id"));
                            parentBean.setCountry_code(parentObj.getString("country_code"));
                            parentBean.setRelationship(parentObj.getString("relationship"));
                            parentList.add(parentBean);
                        }
                        studentBean.setParentList(parentList);
                        list.add(studentBean);

                        if (j.getJSONObject(i).getString("absent_status").equals("0")) {
                            total_absent = total_absent + 1;
                        }
                        if (j.getJSONObject(i).getString("absent_status").equals("1") && j.getJSONObject(i).getString("status").equals("1")) {
                            total_present = total_present + 1;
                        }

                        if (j.getJSONObject(i).getString("absent_status").equals("1") && (j.getJSONObject(i).getString("status").equals("2") || j.getJSONObject(i).getString("status").equals("0"))) {
                            total_waiting = total_waiting + 1;
                        }

                        if (listType == 0) {
                            //All
                            studentsFilter = students;
                            listStudent = list;
                        } else if (listType == 1) {
                            //Present
                            if (j.getJSONObject(i).getString("absent_status").equals("1")
                             && j.getJSONObject(i).getString("status").equals("1")) {
                                studentsFilter.add(name);
                                listStudent.add(studentBean);
                            }
                        } else if (listType == 2) {
                            //waiting
                            if (j.getJSONObject(i).getString("absent_status").equals("1")
                            && (  j.getJSONObject(i).getString("status").equals("2")
                               || j.getJSONObject(i).getString("status").equals("0"))) {
                                studentsFilter.add(name);
                                listStudent.add(studentBean);
                            }
                        } else if (listType == 3) {
                            //Absent
                            if (j.getJSONObject(i).getString("absent_status").equals("0")) {
                                studentsFilter.add(name);
                                listStudent.add(studentBean);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            ((ListView) v.findViewById(R.id.student_list)).setAdapter(new StudentListAdapter2(getActivity(), listStudent));

        }
        ((TextView) v.findViewById(R.id.student_txt)).setText("" + students.size());
        absent_days_txt.setText("" + total_absent);
        present_days_txt.setText("" + total_present);
        student_waiting.setText("" + total_waiting);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.relLayTotal:
                relLayTotal.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                relLayAbsent.setBackgroundColor(getResources().getColor(R.color.white));
                relLayWaiting.setBackgroundColor(getResources().getColor(R.color.white));
                relLayPresent.setBackgroundColor(getResources().getColor(R.color.white));
                listType = 0;
                init();
                break;
            case R.id.relLayAbsent:
                relLayTotal.setBackgroundColor(getResources().getColor(R.color.white));
                relLayAbsent.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                relLayWaiting.setBackgroundColor(getResources().getColor(R.color.white));
                relLayPresent.setBackgroundColor(getResources().getColor(R.color.white));
                listType = 3;
                init();
                break;
            case R.id.relLayWaiting:
                relLayTotal.setBackgroundColor(getResources().getColor(R.color.white));
                relLayAbsent.setBackgroundColor(getResources().getColor(R.color.white));
                relLayWaiting.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                relLayPresent.setBackgroundColor(getResources().getColor(R.color.white));
                listType = 2;
                init();
                break;
            case R.id.relLayPresent:
                relLayTotal.setBackgroundColor(getResources().getColor(R.color.white));
                relLayAbsent.setBackgroundColor(getResources().getColor(R.color.white));
                relLayWaiting.setBackgroundColor(getResources().getColor(R.color.white));
                relLayPresent.setBackgroundColor(getResources().getColor(R.color.selected_grey));
                listType = 1;
                init();
                break;
        }
    }

    /*   ------------------------------>CODE FOR GET STUDENT<---------------------------------   */
    public class GetStudentUpdateList extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        ProgressDialog dialog = new ProgressDialog(getActivity());

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
            String URL = ConstantKeys.SERVER_URL + "driver_route?" + ConstantKeys.ROUTE_ID + "=" + params[0];
            Log.e(TAG , "query " + URL ) ;
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

            try {
                networkResponse = new JSONObject(s);
                Log.e(TAG , "driver_route response : " + s);

                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        if (networkResponse.getJSONArray("student").length() > 0) {
                            setDB( networkResponse.getJSONArray("student") , networkResponse.getJSONArray("lat"), networkResponse.getJSONArray("lng"));
                            init();
                        }
                    } else {
                        Toast.makeText(getActivity(), "" + networkResponse.getString("error"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }

    private void setDB(JSONArray child, JSONArray lat, JSONArray lng) {
        Utility.setSharedPreference(getActivity(), "STUDENT", "" + child);
        Utility.setSharedPreference(getActivity(), "LAT", "" + lat);
        Utility.setSharedPreference(getActivity(), "LNG", "" + lng);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.e(TAG , "isVisibleToUser? " + isVisibleToUser ) ;

        if (isVisibleToUser) {
            new GetStudentUpdateList().execute(Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID));
        }
    }

    private void blink(final View v) {
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
        v.startAnimation(myFadeInAnimation);
    }

    public class StudentListAdapter2 extends BaseAdapter {
        ArrayList<StudentBean> search_item;
        private Activity mContext;

        public StudentListAdapter2(Activity context, ArrayList<StudentBean> list) {
            this.mContext = context;
            search_item = list;
        }

        @Override
        public int getCount() {
            return search_item.size();
        }

        @Override
        public Object getItem(int pos) {
            return search_item.get(pos);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int posstud, View convertView, final ViewGroup parent) {
            View v = convertView;
            StudentListAdapterViewHolder viewHolder;
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.student_single, null);
            viewHolder = new StudentListAdapterViewHolder(v);
            v.setTag(viewHolder);

            final StudentBean item = (StudentBean) getItem(posstud);

            viewHolder.mTVItem.setText(item.getFirstName() + " " + item.getFamilyName());
            viewHolder.send_notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String studid = item.getStudentId();
                        Utility.setSharedPreference(mContext, ConstantKeys.STUDENTID_MSG, studid);
                        Log.i(TAG , "try notifiy parent ")  ;
                        if (!Utility.getSharedPreferences(getActivity(), ConstantKeys.MESSAGE1).isEmpty() || !Utility.getSharedPreferences(getActivity(), ConstantKeys.MESSAGE2).isEmpty() || !Utility.getSharedPreferences(getActivity(), ConstantKeys.MESSAGE3).isEmpty() || !Utility.getSharedPreferences(getActivity(), ConstantKeys.MESSAGE4).isEmpty() || !Utility.getSharedPreferences(getActivity(), ConstantKeys.MESSAGE5).isEmpty()) {
                            //   OpenMessageDialog();
                            Log.i(TAG , "try find pareent    ")  ;
                            if (item.getParentList() != null && item.getParentList().size() != 0) {
                                Log.i(TAG , "show dialoug   ")  ;
                                String parents[] = new String[item.getParentList().size() + 1];
                                parents[0] = getString(R.string.all_parent);
                                for (int k = 0; k < item.getParentList().size(); k++) {
                                    parents[k + 1] = item.getParentList().get(k).getParent_fname() + " " + item.getParentList().get(k).getParent_family_name();
                                }
                                setDialog(Arrays.asList(parents), item.getParentList());
                                //      Utility.showMultichoiceAlert(getActivity(), parents, item.getParentList());
                            }
                        } else {
                            getActivity().startActivity(new Intent(getActivity(), SetMessageForStudent.class));
                            Toast.makeText(getActivity(), getString(R.string.add_message), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            viewHolder.call_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (item.getParentList() != null && item.getParentList().size() != 0) {
                            ArrayList<String> list = new ArrayList<>();
                            for (int k = 0; k < item.getParentList().size(); k++) {
                                list.add(item.getParentList().get(k).getParent_fname() + " " + item.getParentList().get(k).getParent_family_name());
                            }
                            ArrayList<ParentBean> parentBeanArrayList = new ArrayList<>(item.getParentList());
                            setSingleDialog(parentBeanArrayList, item.getParentList(), item.getStudentContact(), item.getFirstName(), item.getFamilyName());
                            //     Utility.showSingleChoiceAlert(getActivity(), arrayAdapter, item.getParentList());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            viewHolder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!item.getStatus_absent().equals("0")) {
                            String msg = "";
                            if (item.getStatus().equals("1")) {
                                msg = getString(R.string.check_out_confirmation_msg);
                            } else if (item.getStatus().equals("2")) {
                                msg = getString(R.string.check_in_confirmation_msg);
                            } else {
                                msg = getString(R.string.check_in_confirmation_msg);
                            }

                            Utility.setDialog(getActivity(), getString(R.string.confirm), msg, getString(R.string.cancel), getString(R.string.yes), new DialogListener() {
                                @Override
                                public void onNegative(DialogInterface dialog) {

                                }

                                @Override
                                public void onPositive(DialogInterface dialog) {
                                    try {
                                        String studid = item.getStudentId();
                                        new StudentLoginLogout(mContext).execute(studid);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.student_absent), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            int intsToParse = posstud + 1;
            viewHolder.no.setText("" + intsToParse);
            try {
                if (item.getStatus_absent().equals("0")) {
                    //absent
                    viewHolder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
                    viewHolder.status.setBackgroundResource(R.drawable.red_border);
                    viewHolder.status.setText(getString(R.string.absent));

                    v.setBackgroundColor(Color.parseColor("#a7ff3d36"));
                } else if (item.getStatus().equals("1")) {
                    //CheckedIn
                    viewHolder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_dot, 0, 0, 0);
                    viewHolder.status.setBackgroundResource(R.drawable.border_green);
                    viewHolder.status.setText(getString(R.string.CheckOut));
                }  else {
                    //CheckedOut
                    viewHolder.status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue_dot, 0, 0, 0);
                    viewHolder.status.setBackgroundResource(R.drawable.blue_border);
                    viewHolder.status.setText(getString(R.string.CheckIn));
                }
            } catch (Exception e) {
                Log.e("Exception", "" + e);
            }

            viewHolder.setAbsent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        if (!item.getStatus().equals("1") && item.getStatus_absent().equals("1")) {

                            Utility.setDialog(getActivity(), getString(R.string.confirm), getString(R.string.absent_confirmation_msg), getString(R.string.cancel), getString(R.string.yes), new DialogListener() {
                                @Override
                                public void onNegative(DialogInterface dialog) {

                                }

                                @Override
                                public void onPositive(DialogInterface dialog) {
                                    try {
                                        String studid = item.getStudentId();
                                        Calendar c = Calendar.getInstance();
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        String formattedDate = df.format(c.getTime());

                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("student_id", studid);
                                        jsonObject.put("absent_date", formattedDate);
                                        //new SetAbsent().execute(jsonObject.toString());
                                        setAbsent(formattedDate, studid);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if (item.getStatus().equals("1")) {
                            Toast.makeText(getActivity(), getString(R.string.student_CheckIn), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.student_absent), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            if ((posstud % 2) == 0) {
                v.setBackgroundColor(Color.parseColor("#f5f5f5"));
            } else {
                v.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            if (item.getStatus_absent().equals("0")) {
                v.setBackgroundColor(Color.parseColor("#a7ff3d36"));
            }

            if (item.getStudentId().equals(SELECTED_STUDENT_ID)) {
                SELECTED_STUDENT_ID = "";
                blink(v);
            }

            return v;
        }

        private void setAbsent(String absent_date, String student_id) {
            final ProgressDialog progressDialog =
                    ProgressDialog.show(getActivity(), getString(R.string.Updating), getString(R.string.wait), false, false);
            try {
                JSONStringer jsonStringer = new JSONStringer().object()
                        .key("student_id").value(student_id)
                        .key("absent_date").value(absent_date)
                        .endObject();
                QueryManager.getInstance().postRequest(ConstantKeys.SERVER_URL + "add_student_absent_by_driver", jsonStringer, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(mContext, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String result = response.body().string();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                try {
                                    JSONObject networkResponse = new JSONObject(result);
                                    if (networkResponse.equals(null) || networkResponse.equals("")) {
                                        Toast.makeText(getActivity(), getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                                    } else {
                                        if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                                            Toast.makeText(getActivity(), "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                                            UpdateStudentData();
                                        } else {
                                            Toast.makeText(getActivity(), "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                                    Log.e("ForgotPasswordTak Exc", "" + e);
                                }
                            }
                        });
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(mContext, getString(R.string.servernotresponding), Toast.LENGTH_SHORT).show();
            }
        }
    }





    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("set student list : " ,"Receive brodcast" ) ;
            UpdateStudentData();

            try {
                if (LocationService.lastLocation != null) {
                    new SendLocation().execute(Utility.getSharedPreferences(getActivity(), ConstantKeys.USER_ID), String.valueOf(LocationService.lastLocation.getLatitude()), String.valueOf(LocationService.lastLocation.getLongitude()), "" + (int) Math.round(LocationService.lastLocation.getSpeed()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    /*   ------------------------------>CODE FOR UPDATE LOCATION OF DRIVER AT SERVER<---------------------------------   */
    public class SendLocation extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = ConstantKeys.SERVER_URL + "driver_lat_lng?" + ConstantKeys.DRIVER_ID + "=" + params[0] +
                    "&" + ConstantKeys.LAT + "=" + params[1] + "&" + ConstantKeys.LNG + "=" + params[2] +
                    "&route_id=" + Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID) + "&speed=" + params[3];
            // Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID)
            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
            try {
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
        }
    }


    class StudentListAdapterViewHolder {
        public TextView mTVItem, no, status;
        public ImageView send_notification, call_parent, setAbsent, setPresent;

        public StudentListAdapterViewHolder(View base) {
            mTVItem = (TextView) base.findViewById(R.id.student_name);
            no = (TextView) base.findViewById(R.id.no_txt);
            status = (TextView) base.findViewById(R.id.status_student);
            send_notification = (ImageView) base.findViewById(R.id.send_notification);
            call_parent = (ImageView) base.findViewById(R.id.call_parent);
            setAbsent = (ImageView) base.findViewById(R.id.setAbsent);
            setPresent = (ImageView) base.findViewById(R.id.setPresent);
        }
    }

    private void UpdateStudentData() {
        new GetStudentUpdateList().execute(Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID));
        init();
    }

    private void setDialog(final List<String> itemList, final ArrayList<ParentBean> parentList) {
        final ArrayList<Boolean> checkList = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            checkList.add(false);
        }

        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.multichoice_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(getActivity().getResources().getString(R.string.title_parent_chooser_multi));
        ListView listView = (ListView) dialog.findViewById(R.id.multichoice_list);

        final ArrayList<ParentBean> newList = new ArrayList<>(parentList);

        newList.add(0, null);
        listView.setAdapter(new MultichoiceAdapter(getActivity(), newList, checkList));
        Utility.setListViewHeightBasedOnChildren(listView);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_action:
                        dialog.dismiss();

                        break;
                    case R.id.send_action:

                        ArrayList<String> parentsList = new ArrayList<String>();
                        for (int k = 1; k < itemList.size(); k++) {
                            if (k != 0 && checkList.get(k)) {
                                parentsList.add(newList.get(k).getParent_id());
                            }
                        }
                        String p = org.apache.commons.lang3.StringUtils.join(parentsList, ",");

                        DialogChooseMessage yesnoDialog = new DialogChooseMessage();
                        yesnoDialog.setCancelable(false);
                        yesnoDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                        Bundle bundle = new Bundle();
                        bundle.putString("parents", p);
                        yesnoDialog.setArguments(bundle);
                        yesnoDialog.show(getActivity().getFragmentManager(), "dialogsetmessage");
                        dialog.dismiss();

                        break;
                }
            }
        };

        dialog.findViewById(R.id.cancel_action).setOnClickListener(onClickListener);
        dialog.findViewById(R.id.send_action).setOnClickListener(onClickListener);
        dialog.show();
    }

    private void setSingleDialog(final ArrayList<ParentBean> parentBeanArrayList, final ArrayList<ParentBean> parentList, String studentContact, String studentName, String familyName) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.multichoice_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(getActivity().getResources().getString(R.string.title_parent_chooser));
        ListView listView = (ListView) dialog.findViewById(R.id.multichoice_list);

        ParentBean parentBean = new ParentBean();
        parentBean.setCountry_code(parentList.get(0).getCountry_code());
        parentBean.setRelationship(getString(R.string.student));
        parentBean.setParent_number(studentContact);
        parentBean.setParent_fname(studentName);
        parentBean.setParent_family_name(familyName);
        parentBeanArrayList.add(0, parentBean);
        listView.setAdapter(new SingleChoiceAdapter(getActivity(), parentBeanArrayList));
        Utility.setListViewHeightBasedOnChildren(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                String strNumber = parentBeanArrayList.get(position).getCountry_code() + "" + parentBeanArrayList.get(position).getParent_number();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strNumber));
                getActivity().startActivity(intent);
            }
        });

        dialog.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.send_action).setVisibility(View.GONE);
        dialog.show();
    }


}