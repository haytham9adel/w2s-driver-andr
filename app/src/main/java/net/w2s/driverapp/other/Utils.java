package net.w2s.driverapp.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    public static final String URL_WEBSOCKET = "ws://m3aak.net/chat?name=";

    private Context context;
    private SharedPreferences sharedPref;

    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_SESSION_ID = "sessionId",
            FLAG_MESSAGE = "message";
    public static String TYPE = "type"; // 1
    public static String MESSAGE = "message"; //
    public static String READ = "read";

    public Utils(Context context) {
        this.context = context;
        sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF,
                KEY_MODE_PRIVATE);
    }

    public void storeSessionId(String sessionId) {
        Editor editor = sharedPref.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    public String getSessionId() {
        return sharedPref.getString(KEY_SESSION_ID, null);
    }

    public String getSendMessageJSON(String message) {
        String json = null;

        try {
            /*JSONObject jObj = new JSONObject();
            jObj.put("flag", FLAG_MESSAGE);
			jObj.put("sessionId", getSessionId());
			jObj.put("message", message);*/


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", getSessionId());
            jsonObject.put("message", message);
            jsonObject.put("flag", "message");
            jsonObject.put("chat_type", "2");
            jsonObject.put("user_id", Utility.getSharedPreferences(context, ConstantKeys.Reciever_ID));
            jsonObject.put("sender_name", Utility.getSharedPreferences(context, ConstantKeys.FIRST_NAME));
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


    public String getLocationJSON(String message) {
        String json = null;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sessionId", getSessionId());
            jsonObject.put("message", "");
            jsonObject.put("flag", "");
            jsonObject.put("chat_type", "3");
            jsonObject.put("user_id", "");
            jsonObject.put("sender_name", "");
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
