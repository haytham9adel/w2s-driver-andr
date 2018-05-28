package net.w2s.driverapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    private Context appContext;
    private String language = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        String language = Utility.getSharedPreferences(SplashActivity.this, ConstantKeys.Setting_Language);
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


        appContext = this;
        setContentView(R.layout.activity_splash);
        Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 2 seconds
                    sleep(2 * 1000);
                    // After 2 seconds redirect to another intent
                  /*  runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSingleChoiceAlert(SplashActivity.this);
                        }
                    });*/

                    if (Utility.getSharedPreferencesBoolean(SplashActivity.this, ConstantKeys.IS_FIRST_TIME)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSingleChoiceAlert(SplashActivity.this);
                            }
                        });
                    } else {
                        if (!(Utility.getSharedPreferences(appContext, ConstantKeys.ALREADY_LOGIN)).equals("Yes")) {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivityNew.class));
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // start thread
        background.start();
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
        int height = size.y;

        lp.width = width - 50;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        final TextView englishTxt = (TextView) dialog.findViewById(R.id.english_txt);
        final TextView arabicTxt = (TextView) dialog.findViewById(R.id.arabic_txt);
        final LinearLayout arabicTxtLYT = (LinearLayout) dialog.findViewById(R.id.arabic_txt_layout);
        final LinearLayout englishTxtLYT = (LinearLayout) dialog.findViewById(R.id.english_txt_layout);
        language = "0";
        arabicTxtLYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                language = "1";
                englishTxtLYT.setBackgroundColor(Color.WHITE);
                arabicTxtLYT.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_corner_fill , null));
                englishTxt.setTextColor(Color.BLACK);
                arabicTxt.setTextColor(Color.WHITE);
            }
        });

        englishTxtLYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                language = "0";
                englishTxtLYT.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.round_corner_fill , null));
                arabicTxtLYT.setBackgroundColor(Color.WHITE);
                englishTxt.setTextColor(Color.WHITE);
                arabicTxt.setTextColor(Color.BLACK);
            }
        });

        Button saveBtn = (Button) dialog.findViewById(R.id.action_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage(Integer.parseInt(language));
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setLanguage(Integer.parseInt(language));
            }
        });
        dialog.show();
    }

    private void setLanguage(int tag) {
       try {
           Utility.setSharedPreference(SplashActivity.this, ConstantKeys.Setting_Language, "" + tag);
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
       }catch (Exception e) {e.printStackTrace();}
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }
}

