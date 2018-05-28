package net.w2s.driverapp.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.w2s.driverapp.LoginActivity;
import net.w2s.driverapp.MainActivityNew;
import net.w2s.driverapp.other.MessageActivityNew;
import net.w2s.driverapp.R;
import net.w2s.driverapp.other.UpdateChatListService;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.NotificationID;
import net.w2s.driverapp.Utilities.Utility;
import net.w2s.driverapp.service.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by RWS 6 on 6/14/2017.
 */
public class MessagingService extends FirebaseMessagingService {

    static Bitmap bitmap;

    static SharedPreferences sh_Pref;
    static String uid;

    static String NITIFICATION_ID, NOTIFICATION_TYPE, new_message;
    static boolean isChatNotification;
    //static Bundle responseBundle = null;
    static int noti_count = 0;
    static JSONObject jObj = new JSONObject();
    private static final String TAG = "PUSH_NOTIFICATION";

    static boolean isNotification = false;
    boolean isBlinkNoti = false;
    Context context;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        context = MessagingService.this;
        Log.e(TAG, "Received message");
        uid = Utility.getSharedPreferences(context, ConstantKeys.USER_ID);
        //Log.e("uid",""+uid);

      //  final String message = intent.getExtras().getString("msg");
        //{msg=Check irt, date=2016-08-11 05:05:40, noti_type=chat, noti_id=84}
        // notifies user
        noti_count = noti_count + 1;
        //Toast.makeText(getApplicationContext(),noti_count+" notification",Toast.LENGTH_SHORT).show();
        // PARSE MESSAGE HERE

        isNotification = true;
// Wrong route, speed response Bundle[{msg={"msg":"Bus of Route-1 is moving on wrong path","date":"2016-06-08 03:28:47","noti_id":"1"}, from=744231459759, collapse_key=do_not_collapse}]
        String msg = "";
        try {
            Map<String, String> params = remoteMessage.getData();
            remoteMessage.getNotification();
            jObj = new JSONObject(params.get("msg"));
            if (jObj.has("noti_type")) {
                NOTIFICATION_TYPE = jObj.getString("noti_type");
                if (NOTIFICATION_TYPE.equals("chat")) {
                    NITIFICATION_ID = jObj.getString("noti_id");
                    msg = jObj.getString("msg");
                    isChatNotification = true;
                    generateNotification(context, msg);
/*Chat Response  Bundle[{google.sent_time=1470912429217, msg={msg=hi, date=2016-08-11 04:17:12, noti_type=chat, noti_id=79},
 from=744231459759, google.message_id=0:1470912429222100%e409a6d9f9fd7ecd, collapse_key=do_not_collapse}]*/
                } else if (NOTIFICATION_TYPE.equals("blink")) {
                    if (MainActivityNew.isVisibleMainActivity == true) {
                        isBlinkNoti = true;
                        // context.startService(new Intent(context, UpdateBlinkService.class));
                    }
                } //To be opened for update blink status of student
                else if (NOTIFICATION_TYPE.equals("wrong_route") || NOTIFICATION_TYPE.equals("over_speed") || NOTIFICATION_TYPE.equals("within")) {
                /*	msg={"msg":"Bus of Route-4 is moving on wrong path","date":"2016-09-05 07:07:44","noti_id":"60"}*/
                    NITIFICATION_ID = jObj.getString("noti_id");
                    msg = jObj.getString("msg");
                    generateNotification(context, msg);
                               /* noti_type=wrong_route for WRONG ROUTE
                     =over_speed for High Speed
                     = within for 15 mnt to reach*/
                } else if (NOTIFICATION_TYPE.equals("stop_service")) {
                    context.stopService(new Intent(context, LocationService.class));
                    /*new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                       /     Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });*/
                } else {
                    isChatNotification = false;
                    NITIFICATION_ID = jObj.getString("noti_id");
                    msg = jObj.getString("msg");
                    generateNotification(context, msg);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
    private void generateNotification(Context context, String message) {

        try {

            int currentapiVersion = android.os.Build.VERSION.SDK_INT;

            int icon = R.drawable.bus_ic;
            long when = System.currentTimeMillis();

            AudioManager audio = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = new Notification(icon,
                    jObj.getString("msg"), when);
            //Bundle[{msg=Student vinayak kumar successfully checked in at 2015-12-17 07:12:03,
            // date=2015-12-17 07:12:03, from=744231459759, collapse_key=do_not_collapse, noti_id=1}]
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;

            notification.defaults |= Notification.DEFAULT_SOUND;

            String title = context.getString(R.string.app_name);

            Intent notificationIntent = null;

            if (Utility.isStringNullOrBlank(uid) == false) {
                Log.e("uid", "" + uid);
                if (isChatNotification == true) {
                    if (Utility.getSharedPreferences(context, ConstantKeys.ISCHATVISIBLE).equals("0")) {
                        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(context, ConstantKeys.COUNT_CHAT_NOTI))) {
                            int chatcount = Integer.parseInt(Utility.getSharedPreferences(context, ConstantKeys.COUNT_CHAT_NOTI));
                            Utility.setSharedPreference(context, ConstantKeys.COUNT_CHAT_NOTI, "" + (chatcount + 1));
                        } else {
                            Utility.setSharedPreference(context, ConstantKeys.COUNT_CHAT_NOTI, "1");
                        }
                        notificationIntent = new Intent(context, MessageActivityNew.class);
                        // set intent so it does not start a new activity

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent intent = null;
                        if (notificationIntent != null)
                            intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentTitle(title);
                        builder.setContentText(jObj.getString("msg"));
                        builder.setContentIntent(intent);
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(NotificationID.getID(), builder.build());
                        sendNotification(context, jObj.getString("msg"), title, notificationIntent);
                    } else {
                        Log.e("ChatVisible", "" + Utility.getSharedPreferences(context, ConstantKeys.ISCHATVISIBLE));
                        new_message = jObj.getString("msg");
                        context.startService(new Intent(context, UpdateChatListService.class));
                    }
                }
            } else {

                notificationIntent = new Intent(context,
                        LoginActivity.class);
// set intent so it does not start a new activity
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                );
                PendingIntent intent = null;
                if (notificationIntent != null)
                    intent = PendingIntent.getActivity(context, 0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                //PendingIntent.getActivity(context, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(title);
                builder.setContentText(jObj.getString("msg"));
                builder.setContentIntent(intent);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                //notification.number=noti_count;
                notificationManager.notify(NotificationID.getID(), builder.build());
                sendNotification(context, jObj.getString("msg"), title, notificationIntent);
            }
        } catch (Throwable t) {
            Log.e("Notification Issue ", " = " + t);
        }
    }


    public void sendNotification(Context context, String msg, String title, Intent intent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

//Create the intent that’ll fire when the user taps the notification//

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setSmallIcon(R.drawable.driver_app_icon);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(MessagingService.this.getResources(), R.drawable.driver_app_icon));
        mBuilder.setContentTitle("" + title);
        mBuilder.setContentText("" + msg);

        NotificationManager mNotificationManager =

                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

	/* ====== Class That Handles Push Notification Working ==== */

    /*
     * Handler : For call method for registering device on MyPref server
     */
    static Handler registerHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            startDeviceRegisterProcess();
        }
    };

    /******
     * Function to start server request for gallery images
     ******/
    private static void startDeviceRegisterProcess() {

    }
}
