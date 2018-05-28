package net.w2s.driverapp.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.w2s.driverapp.Beans.MapBean;
import net.w2s.driverapp.Beans.ParentBean;
import net.w2s.driverapp.MainActivityNew;
import net.w2s.driverapp.MapUtility.AppConstants;
import net.w2s.driverapp.MapUtility.HttpConnection;
import net.w2s.driverapp.MapUtility.PathJSONParser;
import net.w2s.driverapp.Utilities.NetworkHelperGet;
import net.w2s.driverapp.R;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.service.SendNotiToNextStudentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by LAKHAN on 6/12/2015.
 */
public class MapFragment extends Fragment implements LocationListener, OnMapReadyCallback {

    private final static String MODE_DRIVING = "driving";
    private GoogleMap mGoogleMap;
    private Marker marker1;
    private final Marker currentLocationMarker = null;
    private Marker movingMarker = null;
    private ArrayList<LatLng> INTERMIDIATE_STOP;
    private ArrayList<LatLng> MOVING_LOC;

    private LatLng LOWER_MANHATTAN;//= new LatLng(22.691887,75.86665440000002);
    private LatLng BROOKLYN_BRIDGE;// = new LatLng(22.7195687, 75.85772580000003);

    private Hashtable<String, MapBean> markers;

    private Map<String, MapBean> studentMap  ;
    private Map<String, Marker>   studentMarkers   ;


    private Hashtable<String, ArrayList<ParentBean>> parents;
    private boolean isFirstTime = true;
    private final ArrayList<Bitmap> imagesList = new ArrayList<>();
    private double current_lat = 0;
    private final ArrayList<LatLng> polyLoc = new ArrayList<>();

    private Context ct;
    private boolean isWrongRouteNotiSend = false;
    private boolean initiating = false ;

    private void refresh() {
        if(!initiating) {
            Log.i("set student data : " ,"  refresh" ) ;
            initiating=true ;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 1000);
        }
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("set student data : " ,"Receive brodcast" ) ;
            refresh() ;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        init();
    }

    private void initializeMap() {
        if (mGoogleMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver) ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment, null);
        ct = getActivity();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LocationManager manager = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("refresh_map"));

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Call your Alert message
            AlertDialog.Builder dialog = new AlertDialog.Builder(ct);
            dialog.setTitle(getString(R.string.alert));
            dialog.setMessage(getString(R.string.enable_gps));
            dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }

        refresh();
        return v;
    }

    private void init() {
        if (mGoogleMap != null) {
            Log.e("map frag " , "will refresh the map") ;
            mGoogleMap.clear();
            MOVING_LOC = new ArrayList<>();
            this.mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
            markers = new Hashtable<>();
            parents = new Hashtable<>();
            INTERMIDIATE_STOP = new ArrayList<>();
            Location location = mGoogleMap.getMyLocation();
            if (location != null) {
                movingMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_ic))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }
            mGoogleMap.setOnMyLocationChangeListener(myLocationChangeListener);
            //Log.e("S_LAT",Utility.getSharedPreferences(ct,"S_LAT"));

            if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ct, "S_LAT")) &&
                    Utility.isStringNullOrBlank(Utility.getSharedPreferences(ct, "S_LNG")) == false) {
                double s_lat = Double.valueOf(Utility.getSharedPreferences(ct, "S_LAT"));
                double s_long = Double.valueOf(Utility.getSharedPreferences(ct, "S_LNG"));
                double d_lat = Double.valueOf(Utility.getSharedPreferences(ct, "D_LAT"));
                double d_long = Double.valueOf(Utility.getSharedPreferences(ct, "D_LNG"));
                LOWER_MANHATTAN = new LatLng(s_lat, s_long);
                BROOKLYN_BRIDGE = new LatLng(d_lat, d_long);
            }

           try {

                JSONArray networkResponse = new JSONArray(Utility.getSharedPreferences(getActivity(), "LAT"));
                JSONArray networkResponse1 = new JSONArray(Utility.getSharedPreferences(getActivity(), "LNG"));
                Log.i("map draweer stops "  ,networkResponse.length()+"" ) ;

               if (networkResponse.length() > 0) {
                    for (int i = 0; i < networkResponse.length(); i++) {
                        LatLng latLng1 = new LatLng(Double.parseDouble(networkResponse.getString(i).trim())
                                , Double.parseDouble(networkResponse1.getString(i).trim()));
                        INTERMIDIATE_STOP.add(latLng1);
                    }
                    setMap();
                }
            } catch (Exception e) {

            }
            initiating = false ;
        } else {
            initializeMap();
        }
    }

    private void setMap() {
        String url = getMapsApiDirectionsUrl();
        new ReadTask().execute(url);
        new GetAllImagesToBitmap().execute();
    }

    private String getMapsApiDirectionsUrl() {
        String waypoints = AppConstants.GOOGLE_MAPS_DIRECTION_API_URL
                + "origin=" + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
                + "&destination=" + BROOKLYN_BRIDGE.latitude + "," + BROOKLYN_BRIDGE.longitude
                + "&sensor=false&units=metric&mode=" + MODE_DRIVING + "&alternatives=true&key=" + AppConstants.APP_GOOGLE_SERVER_API_KEY + "&waypoints=optimize:true|";
        String way = "";
        for (int i = 0; i < INTERMIDIATE_STOP.size(); i++) {
            LatLng points = INTERMIDIATE_STOP.get(i);
            way = way + points.latitude + "," + points.longitude + "|";
        }
        waypoints = waypoints + way;
        return waypoints;
    }

    private class GetAllImagesToBitmap extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.loading_map), getString(R.string.loading), false, false);
        }

        @Override
        protected Void doInBackground(Void... params) {

            JSONArray j;
            try {
                j = new JSONArray(Utility.getSharedPreferences(getActivity(), "STUDENT"));

                for (int k = 0; k < INTERMIDIATE_STOP.size(); k++) {
                    try {
                        String imageurl = j.getJSONObject(k).getString("s_image_path");
                        imagesList.add(getBitmapFromURL(ConstantKeys.STUDENT_IMAGE_URL + imageurl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            addMarkers();
        }
    }

    private void addMarkers() {
        if (mGoogleMap != null) {
            markers = new Hashtable<>();
            parents = new Hashtable<>();
            studentMap = new HashMap<>() ;
            studentMarkers  = new HashMap<>() ;


            JSONArray j = null;
            try {
                j = new JSONArray(Utility.getSharedPreferences(getActivity(), "STUDENT"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < INTERMIDIATE_STOP.size(); i++) {
                String name = "";
                String add = "";
                String imageurl = "";
                String student_id = "";
                if ((j != null ? j.length() : 0) > 0) {
                    try {
                        name = j.getJSONObject(i).getString("s_fname") + " " + j.getJSONObject(i).optString("family_name", "");
                        add = j.getJSONObject(i).getString("s_contact");
                        imageurl = j.getJSONObject(i).getString("s_image_path");
                        student_id = j.getJSONObject(i).getString("student_id");
                    } catch (Exception e) {
                        Log.e("Exception ", "" + e);
                    }
                }

                View viewMarker = ((LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.marker_student, null);
                ImageView myImage = (ImageView) viewMarker.findViewById(R.id.img_id);
                ImageView bgImg = (ImageView) viewMarker.findViewById(R.id.bg_img);

                // 14-11-2014 deepak code
                try {
                    if (j.getJSONObject(i).getString("absent_status").equals("0")) {
                        //Absent
                        //     myImage.setBorderColor(getActivity().getResources().getColor(R.color.red_marker));
                        bgImg.setImageResource(R.drawable.down_arrow_red);
                    } else if (j.getJSONObject(i).getString("status").equals("1")) {
                        //checkedin
                        //      myImage.setBorderColor(getActivity().getResources().getColor(R.color.green_marker));
                        bgImg.setImageResource(R.drawable.down_arrow_green);

                    } else if (j.getJSONObject(i).getString("status").equals("2")) {
                        //checkedout
                        //     myImage.setBorderColor(getActivity().getResources().getColor(R.color.yellow_marker));
                        bgImg.setImageResource(R.drawable.down_arrow_yellow);
                    } else {
                        //None//yellow
                        bgImg.setImageResource(R.drawable.down_arrow_yellow);
                        //      myImage.setBorderColor(getActivity().getResources().getColor(R.color.yellow_marker));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //None
                    //    myImage.setBorderColor(getActivity().getResources().getColor(R.color.yellow_marker));
                    bgImg.setImageResource(R.drawable.down_arrow_yellow);
                }
                // 14-11-2014 deepak code end

                if (!Utility.isStringNullOrBlank(imageurl)) {
                    try {
                        myImage.setImageBitmap(imagesList.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                        myImage.setImageResource(R.drawable.profile_big);
                    }
                }

                Bitmap bmp = createDrawableFromView(ct, viewMarker);
                Marker m8 = mGoogleMap.addMarker(new MarkerOptions().position(INTERMIDIATE_STOP.get(i))
                        .title(name).snippet(name));
                m8.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));

                MapBean mapBean = new MapBean();
                mapBean.setImage(imagesList.get(i));
                mapBean.setContact(add);
                mapBean.setStudent_id(student_id);

                markers.put(m8.getId() , mapBean);

                studentMap.put(student_id , mapBean ) ;
                studentMarkers.put(student_id , m8  ) ;

                try {
                    ArrayList<ParentBean> parentList1 = new ArrayList<>();
                    for (int k = 0; k < j.getJSONObject(i).getJSONArray("parent").length(); k++) {
                        JSONObject parentObj = j.getJSONObject(i).getJSONArray("parent").getJSONObject(k);
                        ParentBean parentBean = new ParentBean();
                        parentBean.setParent_fname(parentObj.getString("parent_fname"));
                        parentBean.setParent_family_name(parentObj.getString("parent_family_name"));
                        parentBean.setParent_number(parentObj.getString("parent_number"));
                        parentBean.setIsNotiSend(false);
                        parentBean.setSpeed(parentObj.getString("speed"));
                        parentList1.add(parentBean);
                    }
                    parents.put(m8.getId(), parentList1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Marker marker21 = mGoogleMap.addMarker(new MarkerOptions().position(BROOKLYN_BRIDGE)
                    .title("Destination").icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.marker_school)));
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.profile_big);
            MapBean mapBean = new MapBean();
            mapBean.setImage(icon);
            mapBean.setContact("");
            mapBean.setStudent_id("");
            markers.put(marker21.getId(), mapBean);
            Marker marker22 = mGoogleMap.addMarker(new MarkerOptions().position(LOWER_MANHATTAN)
                    .title("Source").icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.blue_marker)));
            markers.put(marker22.getId(), mapBean);
            mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();

        }
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        animateMarker(currentLocationMarker, loc );

        // Zoom in the Google Map
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        //Toast.makeText(getActivity(), "LongLatIs" + location.getLatitude() + "And" + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;

            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                Log.e("jObject", "" + jObject);
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
            //Log.e("routes",routes.toString());
            // if (routes.size()>0) {
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions().width(5).geodesic(true);
                List<HashMap<String, String>> path = routes.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                    polyLoc.add(position);
                }
                polyLineOptions.addAll(points);
                polyLineOptions.color(Color.RED);
            }
            // }
            Log.e("points from s to d :", "" + points);

            Utility.setSharedPreference(getActivity(), "POINTS", "" + points);
            mGoogleMap.addPolyline(polyLineOptions);
        }
    }

      private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view;

        public CustomInfoWindowAdapter() {
            view = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
       }

        @Override
        public View getInfoContents(Marker marker) {

            if (marker1 != null
                    && marker1.isInfoWindowShown()) {
                marker1.hideInfoWindow();
                marker1.showInfoWindow();
            }
            return null;
        }


        @Override
        public View getInfoWindow(final Marker marker) {
            marker1 = marker;
            MapBean url = null;
            if (marker.getId() != null && markers != null ) {
                if (markers.get(marker.getId()) != null ) {
                    url = markers.get(marker.getId());
                }
            }

            final ImageView image = ((ImageView) view.findViewById(R.id.badge));
            if (url != null) {
                image.setImageBitmap(url.getImage());
            } else {
                image.setImageResource(R.drawable.profile_big);
            }

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker1) {
                    Log.e("**********", "Clicked call on Map");
                    StudentsReportFragment.SELECTED_STUDENT_ID = markers.get(marker.getId()).getStudent_id();
                    MainActivityNew.slidingTabLayout.setCurrentTab(1);
                }
            });

            return view;
        }
    }



    /*-------------------------LOCATION LISTENER----------------------------*/
    private double lastlistenerlat = 0.0d;
    private double lastlistenerlong = 0.0d;
    private double listDistance = 0.0d;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {

            try {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (current_lat != latitude) {
                    if (Utility.isConnectingToInternet(getActivity())) {
                        //Code for check driver moving to right direction
                        LatLng currentpos = new LatLng(latitude, longitude);
                        // LatLng currentpos=new LatLng(23.179410,75.785203);

                        //New code for check point inside/outside to geofence
                        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(getActivity(), ConstantKeys.GEOFENCEARRAY))) {
                            String str = Utility.getSharedPreferences(getActivity(), ConstantKeys.GEOFENCEARRAY);
                            ArrayList<LatLng> arrayListGeofence = new ArrayList<>();
                            try {
                                JSONObject jObj = new JSONObject(str);
                                JSONArray jArray = jObj.getJSONArray("radius");
                                for (int i = 0; i < jArray.length(); i++) {
                                    JSONObject JObjLatLong = jArray.getJSONObject(i);
                                    LatLng lt = new LatLng(Double.parseDouble(JObjLatLong.getString("lat")), Double.parseDouble(JObjLatLong.getString("lng")));
                                    arrayListGeofence.add(lt);
                                }
                                if (arrayListGeofence.size() != 0) {
                                    // odd = inside, even = outside;
                                    boolean result = isPointInPolygon(currentpos, arrayListGeofence);
                                    Log.e("result", "" + result);
                                    if (!result) {
                                        if (!isWrongRouteNotiSend) {
                                            isWrongRouteNotiSend = true;
                                            new SendNotiRequest().execute("1", Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID), "1");
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!MainActivityNew.tag_sendNotiToStudent) {
                            MainActivityNew.tag_sendNotiToStudent = true;
                            current_lat = latitude;
                            double current_long = longitude;
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = new Date();

                            if (!Utility.getSharedPreferences(getActivity(), ConstantKeys.NOTI_DATE).equals(dateFormat.format(date))) {
                                Utility.setSharedPreference(getActivity(), ConstantKeys.NOTI_DATE, dateFormat.format(date));
                                Utility.setSharedPreference(getActivity(), ConstantKeys.EARLY_NOTI_SEND_IDS, "");
                                if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("0")) {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "1");
                                } else if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("1")) {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                } else {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                }
                            }
                            Location location1 = new Location("destination");
                            Location location3 = new Location("source");
                            Location location2 = new Location("my");
                            location3.setLatitude(LOWER_MANHATTAN.latitude);
                            location3.setLongitude(LOWER_MANHATTAN.longitude);

                            location1.setLatitude(BROOKLYN_BRIDGE.latitude);
                            location1.setLongitude(BROOKLYN_BRIDGE.longitude);

                            location2.setLatitude(latitude);
                            location2.setLongitude(longitude);

                            if (location1.distanceTo(location2) > 50) {
                                if (location3.distanceTo(location2) > 50) {
                                    SendNotiToNextStudentService.startActionFoo(getActivity(), current_lat + "-" + current_long);
                                } else {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.EARLY_NOTI_SEND_IDS, "");
                                    if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("0")) {
                                        Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "1");
                                    } else if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("1")) {
                                        Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                    } else {
                                        Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                    }
                                }
                            } else {
                                Utility.setSharedPreference(getActivity(), ConstantKeys.EARLY_NOTI_SEND_IDS, "");
                                if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("0")) {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "1");
                                } else if (Utility.getSharedPreferences(getActivity(), ConstantKeys.MORNING_EVENING).equals("1")) {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                } else {
                                    Utility.setSharedPreference(getActivity(), ConstantKeys.MORNING_EVENING, "0");
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
                Location locationA = new Location("point A");
                Location locationB = new Location("point B");
                if (lastlistenerlat != 0.0d && lastlistenerlong != 0.0d) {
                    locationA.setLatitude(lastlistenerlat);// lastlat / 1E6
                    locationA.setLongitude(lastlistenerlong);
                    locationB.setLatitude(latitude);
                    locationB.setLongitude(longitude);
                    listDistance = locationA.distanceTo(locationB);
                    String str = String.format("%.0f", listDistance);

                    //                Double distanceKm = Double.parseDouble(str) / 1000f;
                    // Toast.makeText(getActivity(), "Distance is :"+str, Toast.LENGTH_SHORT).show();
                    //if (Double.parseDouble(str) > 20) { //to be opened

                    double speedInKm2;
                    if (location.hasSpeed()) {
                        speedInKm2 = location.getSpeed() * 3.6;
                        int speedk2 = (int) speedInKm2;
                        Toast.makeText(getActivity(), getString(R.string.speed_is) + speedk2, Toast.LENGTH_LONG).show();
                        new SpeedNotiTask().execute("" + speedk2);

                    } else {
                        speedInKm2 = 0.0;
                        new SpeedNotiTask().execute("" + speedInKm2);
                    }
                    //Need to add speed parameter to SendLocation()
                    Log.e("LAT DIS=", "" + Integer.parseInt(str));
                    // if (Double.parseDouble(str) > 0) {
                   /* if (!Utility.isStringNullOrBlank(str)) {
                        if (Integer.parseInt(str) > 20) {
                            if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(getActivity(), ConstantKeys.USER_ID)))
                                new SendLocation().execute(Utility.getSharedPreferences(getActivity(), ConstantKeys.USER_ID), String.valueOf(latitude), String.valueOf(longitude), "" + speedInKm2);
                            // Toast.makeText(getActivity(), "Distance is :" + Double.parseDouble(str), Toast.LENGTH_SHORT).show();
                        }
                    }*/

                    LatLng locMoveMarker = new LatLng(location.getLatitude(), location.getLongitude());

                    if (movingMarker != null) {
                        if (!Utility.isStringNullOrBlank(str)) {
                            if (Integer.parseInt(str) > 5) {
                                MOVING_LOC.add(locMoveMarker);
                                PolylineOptions polyLineOptionMoving = new PolylineOptions().width(5).geodesic(true);
                                polyLineOptionMoving.addAll(MOVING_LOC);
                                polyLineOptionMoving.color(Color.GREEN);
                                mGoogleMap.addPolyline(polyLineOptionMoving);
                                ArrayList<LatLng> directioList = new ArrayList<>();
                                Log.e("marker", "marker not null");
                                   /* movingMarker = mGoogleMap
                                            .addMarker(new MarkerOptions().position(loc_real).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
                                    animateMarker(movingMarker, loc_real,
                                            false, 0); *///to be opened

                                LatLng markerLocation = movingMarker.getPosition();
                                Location prevLoc = new Location("");
                                Location currLoc = new Location("");
                                prevLoc.setLatitude(markerLocation.latitude);
                                prevLoc.setLongitude(markerLocation.longitude);
                                movingMarker.remove();
                                currLoc.setLatitude(latitude);
                                currLoc.setLongitude(longitude);
                                // float bearing = prevLoc.bearingTo(currLoc) ; //to be opened

                                double bearing = bearingBetweenLocations(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()), new LatLng(currLoc.getLatitude(), currLoc.getLongitude()));
                                //If you have a bearing, you can set the rotation of the marker using MarkerOptions.rotation():
                                Log.e("bearing Second", "" + bearing);
                                movingMarker = mGoogleMap.addMarker(new MarkerOptions()
                                        .position(locMoveMarker)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_ic))
                                        .anchor(0.5f, 0.5f)
                                        .rotation((float) bearing)
                                        .flat(true));
                                directioList.add(new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude()));
                                directioList.add(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()));
                                // animateMarker3(mGoogleMap, movingMarker,directioList,false );//to be opened
                                if (bearing > 0)
                                    rotateMarker(movingMarker, (float) bearing);
                            }
                        }
                    } else {
                        MOVING_LOC.add(locMoveMarker);
                        PolylineOptions polyLineOptionMoving = new PolylineOptions().width(5).geodesic(true);
                        polyLineOptionMoving.addAll(MOVING_LOC);
                        polyLineOptionMoving.color(Color.GREEN);
                        mGoogleMap.addPolyline(polyLineOptionMoving);
                        Log.e("marker", "marker null");
                        Location prevLoc = new Location("");
                        Location currLoc = new Location("");
                        if (LOWER_MANHATTAN != null) {
                            prevLoc.setLatitude(LOWER_MANHATTAN.latitude);
                            prevLoc.setLongitude(LOWER_MANHATTAN.longitude);
                        }
                        if (currLoc != null) {
                            currLoc.setLatitude(latitude);
                            currLoc.setLongitude(longitude);
                        }
                        //float bearing = prevLoc.bearingTo(currLoc) ;
                        double bearing = bearingBetweenLocations(new LatLng(LOWER_MANHATTAN.latitude, LOWER_MANHATTAN.longitude), new LatLng(currLoc.getLatitude(), currLoc.getLongitude()));
                        //If you have a bearing, you can set the rotation of the marker using MarkerOptions.rotation():
                        Log.e("bearing First", "" + bearing);
                        movingMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(locMoveMarker)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_ic))
                                .anchor(0.5f, 0.5f)
                                .rotation((float) bearing)
                                .flat(true));
                        if (bearing > 0)
                            rotateMarker(movingMarker, (float) bearing);
                    }
                }

                lastlistenerlat = latitude;
                lastlistenerlong = longitude;
                final LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                double speedInKm;
                if (location.hasSpeed()) {
                    speedInKm = location.getSpeed() * 3.6;
                    int speedk = (int) speedInKm;
                    if (speedk >= 30) {
                    }
                } else {
                    speedInKm = 0.0;
                }

                if (isFirstTime == true) {
                    isFirstTime = false;
                    if (LOWER_MANHATTAN != null && BROOKLYN_BRIDGE != null) {
                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(LOWER_MANHATTAN)
                                .include(BROOKLYN_BRIDGE).include(loc).build();
                        Point displaySize = new Point();
                        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 400, 15));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private class SpeedNotiTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < MainActivityNew.speedNotiBeanArrayList.size(); i++) {
                ArrayList<ParentBean> parentBeans = MainActivityNew.speedNotiBeanArrayList.get(i);
                for (int j = 0; j < parentBeans.size(); j++) {
                    if (Double.parseDouble(parentBeans.get(j).getSpeed()) < Double.parseDouble(params[0])) {
                        if (!parentBeans.get(j).isNotiSend()) {
//send notification to parent that bus is our speed

                            // new SendNotiRequest().execute("2", Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID), null);
                           /* String URL = ConstantKeys.SERVER_URL + "notification?method=sms&student_id=" + params[1] + "&msg=" + params[2] + "&parent_ids=" + params[3];
                            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
                            try {
                                return putRequest.sendGet();
                            } catch (Exception e) {
                                return "";
                            }*/
                            parentBeans.get(j).setIsNotiSend(true);
                            try {
                                /*{"parent_ids":"12","max_speed":"50","route_id":"2"}*/
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("parent_ids", "" + parentBeans.get(j).getParent_id());
                                jsonObject.put("max_speed", "" + parentBeans.get(j).getSpeed());
                                jsonObject.put("route_id", Utility.getSharedPreferences(getActivity(), ConstantKeys.ROUTE_ID));
                                NetworkHelperGet networkHelperGet = new NetworkHelperGet(ConstantKeys.SERVER_URL + "parentSpeedNotification");
                                String responce = networkHelperGet.performPostCall(jsonObject.toString());
                                Log.e("over speed responce", "" + responce);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Over speed notification sent.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {

                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    /*-----------------------------------CODE FOR SHOW MARKER---------------------------------*/
    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                }
            }
        });
    }

    /*-----------------------------------CODE FOR SHOW MARKER---------------------------------*/
    private void animateMarker(final Marker marker, final LatLng toPosition) {
        try {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            com.google.android.gms.maps.Projection proj = mGoogleMap.getProjection();
            Point startPoint = proj.toScreenLocation(marker.getPosition());
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final long duration = 500;
            final LinearInterpolator interpolator = new LinearInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * toPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));
                    float t1 = interpolator.getInterpolation((float) elapsed / duration);
                    float rot = t1 * (float) 0 + (1 - t1) * startRotation;
                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (false) {
                            marker.setVisible(false);
                        } else {
                            marker.setVisible(true);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*   ------------------------------>CODE FOR Request To Server for send notification<---------------------------------   */
    public class SendNotiRequest extends AsyncTask<String, String, String> {
        JSONObject networkResponse = null;
        String noti_type;

        @Override
        protected String doInBackground(String... params) {
            noti_type = params[0];
            String URL = ConstantKeys.SERVER_URL + "/notification?method=noti&noti_type=" + params[0] + "&route_id=" + params[1] + "&student_id=" + params[2];
            NetworkHelperGet putRequest = new NetworkHelperGet(URL);
            try {
                return putRequest.sendGet();
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("SendNotiRequest Resp", "" + s);
            try {
                networkResponse = new JSONObject(s);
                if (networkResponse.equals(null) || networkResponse.equals("")) {
                    Toast.makeText(getActivity(), "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                } else {
                    if (networkResponse.getString(ConstantKeys.RESULT).equals("success")) {
                        if (noti_type.equals("1")) {
                            Toast.makeText(getActivity(), getString(R.string.wrong_route_noti), Toast.LENGTH_LONG).show();
                        } else if (noti_type.equals("2")) {
                            Toast.makeText(getActivity(), getString(R.string.over_speed_noti), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //Toast.makeText(getActivity(), "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(getActivity(), "" + networkResponse.getString("responseMessage"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                // Toast.makeText(getActivity(),getString(R.string.servernotresponding), Toast.LENGTH_LONG).show();
                Log.e("SendNotiRequest Exc", "" + e);
            }
            super.onPostExecute(s);
        }
    }

    private static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isPointInPolygon(LatLng tap, ArrayList<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return ((intersectCount % 2) == 1); // odd = inside, even = outside;
    }

    private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
    }


}
